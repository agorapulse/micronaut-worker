/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2024 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.worker.processor;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.executor.DistributedJobExecutor;
import com.agorapulse.worker.executor.ExecutorServiceProvider;
import com.agorapulse.worker.job.JobRunContext;
import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.queue.QueueMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Singleton
@Refreshable
public class DefaultMethodJobInvoker implements MethodJobInvoker, ApplicationEventListener<RefreshEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final Map<String, Scheduler> schedulersCache = new ConcurrentHashMap<>();

    private final BeanContext context;
    private final ExecutorServiceProvider executorServiceProvider;
    private final DistributedJobExecutor distributedJobExecutor;

    public DefaultMethodJobInvoker(
        BeanContext context, ExecutorServiceProvider executorServiceProvider,
        DistributedJobExecutor distributedJobExecutor
    ) {
        this.context = context;
        this.executorServiceProvider = executorServiceProvider;
        this.distributedJobExecutor = distributedJobExecutor;
    }

    @Override
    public void onApplicationEvent(RefreshEvent event) {
        schedulersCache.clear();
    }

    public <B> void invoke(MethodJob<B, ?> job, B bean, JobRunContext context) {
        ExecutableMethod<B, ?> method = job.getMethod();
        boolean producer = !method.getReturnType().isVoid();
        JobConfiguration configuration = job.getConfiguration();

        if (method.getArguments().length == 0) {
            context.message(null);
            handleResult(configuration, context, executor(context, configuration).apply(() -> {
                if (configuration.getFork() > 1) {
                    ParallelFlux<Object> resultsOfParallelExecution = Flux.range(0, configuration.getFork())
                        .parallel(configuration.getFork())
                        .runOn(getScheduler(job))
                        .flatMap(i -> {
                            JobRunContext forkedContext = context.createChildContext(i.toString());
                            try {
                                Object result = method.invoke(bean);

                                if (result == null) {
                                    return Mono.empty();
                                }

                                if (result instanceof Publisher<?> p) {
                                    return Flux.from(p);
                                }

                                return Mono.just(result);
                            } catch (Exception e) {
                                forkedContext.error(e);
                                return Mono.empty();
                            }
                        });

                    if (producer) {
                        return resultsOfParallelExecution.sequential(configuration.getFork());
                    }

                    return resultsOfParallelExecution.then();
                }

                return method.invoke(bean);
            }));
        } else if (method.getArguments().length == 1) {
            handleResult(configuration, context, executor(context, configuration).apply(() -> {
                JobConfiguration.ConsumerQueueConfiguration queueConfiguration = configuration.getConsumer();
                Flux<? extends QueueMessage<?>> messages = Flux.from(
                    queues(queueConfiguration.getQueueType()).readMessages(
                        queueConfiguration.getQueueName(),
                        queueConfiguration.getMaxMessages() < 1 ? 1 : queueConfiguration.getMaxMessages(),
                        Optional.ofNullable(queueConfiguration.getWaitingTime()).orElse(Duration.ZERO),
                        method.getArguments()[0]
                    )
                );

                Function<QueueMessage<?>, Publisher<?>> messageProcessor = message -> {
                    JobRunContext messageContext = context.createChildContext(message.getId());
                    try {
                        messageContext.message(message);

                        Object result = method.invoke(bean, message.getMessage());

                        message.delete();

                        if (result == null) {
                            return Mono.empty();
                        }

                        if (result instanceof Publisher<?> p) {
                            return Flux.from(p);
                        }

                        return Mono.just(result);
                    } catch (Exception e) {
                        message.requeue();
                        messageContext.error(e);
                        return Mono.empty();
                    }

                };

                if (configuration.getFork() > 1) {
                    ParallelFlux<Object> parallelFlux = messages
                        .parallel(configuration.getFork())
                        .runOn(getScheduler(job))
                        .flatMap(messageProcessor);

                    if (producer) {
                        return parallelFlux.sequential(configuration.getFork());
                    }

                    return parallelFlux.then();
                }

                return messages.flatMap(messageProcessor);
            }));
        } else {
            LOGGER.error("Too many arguments for {}! The job method wasn't executed!", method);
        }
    }

    private <T> Function<Callable<T>, Publisher<T>> executor(JobRunContext context, JobConfiguration configuration) {
        if (configuration.isLeaderOnly()) {
            return s -> distributedJobExecutor.executeOnlyOnLeader(context, s);
        }
        if (configuration.isFollowerOnly()) {
            return s -> distributedJobExecutor.executeOnlyOnFollower(context, s);
        }
        if (configuration.getConcurrency() > 0) {
            return s -> distributedJobExecutor.executeConcurrently(context, configuration.getConcurrency(), s);
        }
        return s -> distributedJobExecutor.execute(context, s);
    }

    protected void handleResult(JobConfiguration configuration, JobRunContext callback, Publisher<Object> resultPublisher) {
        Object result = Flux.from(resultPublisher).blockFirst();

        if (result == null) {
            callback.finished();
            return;
        }

        String queueName = configuration.getProducer().getQueueName();

        JobQueues sender = queues(configuration.getProducer().getQueueType());

        if (result instanceof Publisher) {
            Flux<?> resultFLux = Flux.from((Publisher<?>) result)
                .doOnNext(callback::result)
                .doOnError(callback::error)
                .doFinally(signalType -> callback.finished());
            sender.sendMessages(queueName, resultFLux);
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending message {} to {} using {}", result, queueName, sender);

        }

        callback.result(result);
        sender.sendMessage(queueName, result);
        callback.finished();
    }

    private <B> Scheduler getScheduler(MethodJob<B, ?> job) {
        return schedulersCache.computeIfAbsent(ExecutorServiceProvider.getSchedulerName(job.getConfiguration()), s -> Schedulers.fromExecutor(executorServiceProvider.getExecutorService(job)));
    }

    private JobQueues queues(String qualifier) {
        return context.findBean(
                JobQueues.class,
                qualifier == null ? null : Qualifiers.byName(qualifier)
            )
            .orElseGet(() -> context.getBean(JobQueues.class));
    }

}
