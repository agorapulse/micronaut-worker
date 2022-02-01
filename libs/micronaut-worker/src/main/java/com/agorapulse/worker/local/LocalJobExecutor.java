/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
import io.micronaut.context.annotation.Secondary;
import io.reactivex.Maybe;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Secondary
@Singleton
public class LocalJobExecutor implements DistributedJobExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalJobExecutor.class);

    private final ConcurrentMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public LocalJobExecutor(@Named("local-job-executor") ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(String jobName, Callable<R> supplier) {
        return executeConcurrently(jobName, 1, supplier);
    }

    @Override
    public <R> Publisher<R> executeConcurrently(String jobName, int concurrency, Callable<R> supplier) {
        return Maybe.fromFuture(executorService.submit(() -> {
            int increasedCount = counts.computeIfAbsent(jobName, s -> new AtomicInteger(0)).incrementAndGet();
            LOGGER.trace("Increased count for job {} limited to {}: {}", jobName, concurrency, increasedCount);
            if (increasedCount > concurrency) {
                counts.get(jobName).decrementAndGet();
                return null;
            }

            R result = supplier.call();
            int decreasedCount = counts.get(jobName).decrementAndGet();
            LOGGER.trace("Decreased count for job {} limited to {}: {}", jobName, concurrency, decreasedCount);
            return result;
        })).toFlowable();
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(String jobName, Callable<R> supplier) {
        return Maybe.fromFuture(executorService.submit(supplier)).toFlowable();
    }

}
