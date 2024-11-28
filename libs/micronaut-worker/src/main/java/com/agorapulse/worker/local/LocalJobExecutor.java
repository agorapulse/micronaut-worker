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

import com.agorapulse.worker.executor.DistributedJobExecutor;
import com.agorapulse.worker.job.JobRunContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
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

    private final ConcurrentMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public LocalJobExecutor(@Named("local-job-executor") ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(JobRunContext context, Callable<R> supplier) {
        return executeConcurrently(context, 1, supplier);
    }

    @Override
    public <R> Publisher<R> executeConcurrently(JobRunContext context, int concurrency, Callable<R> supplier) {
        return Mono.fromCallable(() -> {
            int increasedCount = counts.computeIfAbsent(context.getStatus().getName(), s -> new AtomicInteger(0)).incrementAndGet();
            LOGGER.trace("Increased count for job {} limited to {}: {}", context.getStatus().getName(), concurrency, increasedCount);

            if (increasedCount > concurrency) {
                counts.get(context.getStatus().getName()).decrementAndGet();
                return null;
            }

            context.onFinished(s -> {
                int decreasedCount = counts.get(context.getStatus().getName()).decrementAndGet();
                LOGGER.trace("Decreased count for job {} limited to {}: {}", context.getStatus().getName(), concurrency, decreasedCount);
            });

            return supplier.call();
        }).subscribeOn(Schedulers.fromExecutor(executorService)).flux();
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(JobRunContext context, Callable<R> supplier) {
        return Mono.fromCallable(supplier).subscribeOn(Schedulers.fromExecutor(executorService)).flux();
    }

}
