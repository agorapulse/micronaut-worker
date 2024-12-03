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
package com.agorapulse.worker.redis;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.event.JobExecutorEvent;
import com.agorapulse.worker.executor.DistributedJobExecutor;
import com.agorapulse.worker.executor.ExecutorId;
import com.agorapulse.worker.job.JobRunContext;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
@Requires(beans = {StatefulRedisConnection.class}, property = "redis.uri")
@Requires(property = "worker.executor.redis.enabled", value = "true", defaultValue = "true")
public class RedisJobExecutor implements DistributedJobExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisJobExecutor.class);

    private static final String EXECUTOR_TYPE = "redis";

    private static final String LIBRARY_PREFIX = "APMW::";
    private static final String PREFIX_LEADER = LIBRARY_PREFIX + "LEADER::";
    private static final String PREFIX_COUNT = LIBRARY_PREFIX + "COUNT::";
    private static final int LEADER_INACTIVITY_TIMEOUT = 500;
    private static final int LOCK_TIMEOUT = 60;
    private static final int COUNT_TIMEOUT = 600;

    private static final String LEADER_CHECK = String.join("\n",
        "redis.call('set', KEYS[1], KEYS[2], 'nx', 'ex', KEYS[3])",
        "local result = redis.call('get', KEYS[1])",
        "if result and result == KEYS[2]",
        "then redis.call('expire', KEYS[1], KEYS[3]) end",
        "return result"
    );

    private static final String INCREASE_JOB_COUNT = String.join("\n",
        "redis.call('set', KEYS[1], 0, 'nx', 'ex', KEYS[2])",
        "return redis.call('incr', KEYS[1])"
    );

    private static final String DECREASE_JOB_COUNT = "return redis.call('decr', KEYS[1])";

    private final StatefulRedisConnection<String, String> connection;
    private final ExecutorId executorId;
    private final BeanContext beanContext;
    private final JobManager jobManager;
    private final ApplicationEventPublisher<JobExecutorEvent> eventPublisher;

    public RedisJobExecutor(
        StatefulRedisConnection<String, String> connection,
        ExecutorId executorId,
        BeanContext beanContext,
        JobManager jobManager,
        ApplicationEventPublisher<JobExecutorEvent> eventPublisher
    ) {
        this.connection = connection;
        this.executorId = executorId;
        this.beanContext = beanContext;
        this.jobManager = jobManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(JobRunContext context, Callable<R> supplier) {
        RedisAsyncCommands<String, String> commands = connection.async();
        return readMasterHostname(context.getStatus().getName(), commands).flatMap(h -> {
            if (executorId.id().equals(h)) {
                context.executed();
                eventPublisher.publishEvent(JobExecutorEvent.leaderOnly(EXECUTOR_TYPE, JobExecutorEvent.Execution.EXECUTE, context.getStatus(), executorId.id()));
                return Mono.fromCallable(supplier).subscribeOn(Schedulers.fromExecutorService(getExecutorService(context.getStatus().getName())));
            }
            eventPublisher.publishEvent(JobExecutorEvent.leaderOnly(EXECUTOR_TYPE, JobExecutorEvent.Execution.SKIP, context.getStatus(), executorId.id()));
            context.skipped();
            return Mono.empty();
        }).flux();
    }

    @Override
    public <R> Publisher<R> executeConcurrently(JobRunContext context, int maxConcurrency, Callable<R> supplier) {
        RedisAsyncCommands<String, String> commands = connection.async();
        return readAndIncreaseCurrentCount(context.getStatus().getName(), commands, maxConcurrency <= 1 ? LOCK_TIMEOUT : COUNT_TIMEOUT)
            .flatMap(count -> {
                if (count > maxConcurrency) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Skipping execution of the job {} as the concurrency level {} is already reached", context.getStatus().getName(), maxConcurrency);
                    }
                    eventPublisher.publishEvent(JobExecutorEvent.concurrent(EXECUTOR_TYPE, JobExecutorEvent.Execution.SKIP, context.getStatus(), maxConcurrency, executorId.id()));
                    context.skipped();
                    return decreaseCurrentExecutionCount(context.getStatus().getName(), commands).flatMap(decreased -> Mono.empty());
                }

                context.executed();
                context.onFinished(s-> decreaseCurrentExecutionCount(s.getName(), commands).subscribe());
                eventPublisher.publishEvent(JobExecutorEvent.concurrent(EXECUTOR_TYPE, JobExecutorEvent.Execution.EXECUTE, context.getStatus(), maxConcurrency, executorId.id()));
                return Mono.fromCallable(supplier).subscribeOn(Schedulers.fromExecutorService(getExecutorService(context.getStatus().getName())));
            }).flux();
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(JobRunContext context, Callable<R> supplier) {
        RedisAsyncCommands<String, String> commands = connection.async();
        return readMasterHostname(context.getStatus().getName(), commands).flatMap(h -> {
            if (!"".equals(h) && h.equals(executorId.id())) {
                eventPublisher.publishEvent(JobExecutorEvent.followerOnly(EXECUTOR_TYPE, JobExecutorEvent.Execution.SKIP, context.getStatus(), executorId.id()));
                context.skipped();
                return Mono.empty();
            }
            context.executed();
            eventPublisher.publishEvent(JobExecutorEvent.followerOnly(EXECUTOR_TYPE, JobExecutorEvent.Execution.EXECUTE, context.getStatus(), executorId.id()));
            return Mono.fromCallable(supplier).subscribeOn(Schedulers.fromExecutorService(getExecutorService(context.getStatus().getName())));
        }).flux();
    }


    private static Mono<Long> readAndIncreaseCurrentCount(String jobName, RedisAsyncCommands<String, String> commands, int timeout) {
        return Mono.fromFuture(commands.eval(
            INCREASE_JOB_COUNT,
            ScriptOutputType.INTEGER,
            PREFIX_COUNT + jobName, String.valueOf(timeout)
        ).toCompletableFuture()).map(Long.class::cast);
    }

    private static Mono<Long> decreaseCurrentExecutionCount(String jobName, RedisAsyncCommands<String, String> commands) {
        return Mono.fromFuture(commands.eval(
            DECREASE_JOB_COUNT,
            ScriptOutputType.INTEGER,
            PREFIX_COUNT + jobName
        ).toCompletableFuture()).map(Long.class::cast);
    }

    private Mono<Object> readMasterHostname(String jobName, RedisAsyncCommands<String, String> commands) {
        int randomDelay = ThreadLocalRandom.current().nextInt(1, 500);
        return Mono.delay(Duration.ofMillis(randomDelay))
            .flatMap(ignored -> Mono.fromFuture(commands.eval(
                LEADER_CHECK,
                ScriptOutputType.VALUE,
                PREFIX_LEADER + jobName, executorId.id(), String.valueOf(LEADER_INACTIVITY_TIMEOUT)
            ).toCompletableFuture()))
            .defaultIfEmpty("");
    }

    private ExecutorService getExecutorService(String jobName) {
        return jobManager
            .getJob(jobName)
            .map(Job::getConfiguration)
            .map(JobConfiguration::getScheduler)
            .flatMap(name -> beanContext.findBean(ExecutorService.class, Qualifiers.byName(name)))
            .or(() -> beanContext.findBean(ExecutorService.class))
            .orElseThrow(() -> new IllegalArgumentException("No executor service found for job " + jobName));
    }
}
