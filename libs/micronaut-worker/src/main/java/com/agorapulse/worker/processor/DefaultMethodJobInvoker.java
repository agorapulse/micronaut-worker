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
import com.agorapulse.worker.job.JobRunContext;
import com.agorapulse.worker.queue.JobQueues;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Singleton
public class DefaultMethodJobInvoker implements MethodJobInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final BeanContext context;
    private final DistributedJobExecutor distributedJobExecutor;

    public DefaultMethodJobInvoker(
        BeanContext context,
        DistributedJobExecutor distributedJobExecutor
    ) {
        this.context = context;
        this.distributedJobExecutor = distributedJobExecutor;
    }

    public <B> void invoke(MethodJob<B, ?> job, B bean, JobRunContext context) {
        ExecutableMethod<B, ?> method = job.getMethod();
        JobConfiguration configuration = job.getConfiguration();

        if (method.getArguments().length == 0) {
            context.message(null);
            handleResult(configuration, context, executor(context, configuration).apply(() -> method.invoke(bean)));
        } else if (method.getArguments().length == 1) {
            handleResult(configuration, context, executor(context, configuration).apply(() -> {
                JobConfiguration.ConsumerQueueConfiguration queueConfiguration = configuration.getConsumer();
                return Flux.from(
                        queues(queueConfiguration.getQueueType()).readMessages(
                            queueConfiguration.getQueueName(),
                            queueConfiguration.getMaxMessages() < 1 ? 1 : queueConfiguration.getMaxMessages(),
                            Optional.ofNullable(queueConfiguration.getWaitingTime()).orElse(Duration.ZERO),
                            method.getArguments()[0]
                        )
                    )
                    .flatMap(message -> {
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
                        } catch (Throwable e) {
                            message.requeue();
                            messageContext.error(e);
                            return Mono.empty();
                        }

                    });
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
        return s -> Mono.fromCallable(s).flux();
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

    private JobQueues queues(String qualifier) {
        return context.findBean(
                JobQueues.class,
                qualifier == null ? null : Qualifiers.byName(qualifier)
            )
            .orElseGet(() -> context.getBean(JobQueues.class));
    }

}
