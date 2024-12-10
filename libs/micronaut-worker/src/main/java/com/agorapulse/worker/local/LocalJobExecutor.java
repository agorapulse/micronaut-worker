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
package com.agorapulse.worker.local;

import com.agorapulse.worker.JobRunStatus;
import com.agorapulse.worker.event.JobExecutorEvent;
import com.agorapulse.worker.executor.DistributedJobExecutor;
import com.agorapulse.worker.executor.ExecutorId;
import com.agorapulse.worker.job.JobRunContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Secondary
@Singleton
@Requires(property = "worker.executor.local.enabled", value = "true", defaultValue = "true")
public class LocalJobExecutor implements DistributedJobExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalJobExecutor.class);
    private static final String EXECUTOR_TYPE = "local";

    private final ConcurrentMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher<JobExecutorEvent> eventPublisher;
    private final ExecutorId executorId;
    private final Scheduler scheduler;

    public LocalJobExecutor(@Named("local-job-executor") ExecutorService executorService, ApplicationEventPublisher<JobExecutorEvent> eventPublisher, ExecutorId executorId) {
        this.eventPublisher = eventPublisher;
        this.executorId = executorId;
        scheduler = Schedulers.fromExecutor(executorService);
    }

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(JobRunContext context, Callable<R> supplier) {
        return doExecuteConcurrently(JobExecutorEvent.Type.LEADER_ONLY, context, 1, supplier);
    }

    @Override
    public <R> Publisher<R> executeConcurrently(JobRunContext context, int concurrency, Callable<R> supplier) {
        return doExecuteConcurrently(JobExecutorEvent.Type.CONCURRENT, context, concurrency, supplier);
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(JobRunContext context, Callable<R> supplier) {

        return Mono.fromCallable(supplier).doFinally(ignored -> {
            context.executed();
            eventPublisher.publishEvent(
                JobExecutorEvent.followerOnly(EXECUTOR_TYPE, JobExecutorEvent.Execution.EXECUTE, context.getStatus(), executorId.id())
            );
        }).subscribeOn(scheduler).flux();
    }

    private  <R> Publisher<R> doExecuteConcurrently(JobExecutorEvent.Type type, JobRunContext context, int concurrency, Callable<R> supplier) {
        return Mono.fromCallable(() -> {
            JobRunStatus status = context.getStatus();
            int increasedCount = counts.computeIfAbsent(status.getName(), s -> new AtomicInteger(0)).incrementAndGet();
            LOGGER.trace("Increased count for job {} limited to {}: {}", status.getName(), concurrency, increasedCount);

            if (increasedCount > concurrency) {
                counts.get(status.getName()).decrementAndGet();
                eventPublisher.publishEvent(new JobExecutorEvent(EXECUTOR_TYPE, type, JobExecutorEvent.Execution.SKIP, status, concurrency, executorId.id()));
                return null;
            }

            context.executed();
            eventPublisher.publishEvent(new JobExecutorEvent(EXECUTOR_TYPE, type, JobExecutorEvent.Execution.EXECUTE, status, concurrency, executorId.id()));

            context.onFinished(s -> {
                int decreasedCount = counts.get(s.getName()).decrementAndGet();
                LOGGER.trace("Decreased count for job {} limited to {}: {}", s.getName(), concurrency, decreasedCount);
            });

            return supplier.call();
        }).subscribeOn(scheduler).flux();
    }

    @Override
    public <R> Publisher<R> execute(JobRunContext context, Callable<R> supplier) {
        return Mono.fromCallable(supplier).doFinally(ignored -> {
            context.executed();
            eventPublisher.publishEvent(
                new JobExecutorEvent(
                    EXECUTOR_TYPE,
                    JobExecutorEvent.Type.ALWAYS,
                    JobExecutorEvent.Execution.EXECUTE,
                    context.getStatus(),
                    0,
                    executorId.id()
                )
            );
        }).subscribeOn(scheduler).flux();
    }

}
