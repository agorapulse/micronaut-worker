/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
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
package com.agorapulse.worker.job;

import com.agorapulse.worker.JobStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class DefaultJobStatus implements JobStatus {

    private final AtomicInteger executionCount = new AtomicInteger(0);

    private final AtomicReference<Instant> lastTriggered = new AtomicReference<>();
    private final AtomicReference<Instant> lastFinished = new AtomicReference<>();
    private final AtomicReference<Duration> lastDuration = new AtomicReference<>();
    private final AtomicReference<Throwable> lastException = new AtomicReference<>();

    @Override
    public Instant getLastTriggered() {
        return lastTriggered.get();
    }

    @Override
    public Instant getLastFinished() {
        return lastFinished.get();
    }

    @Override
    public Throwable getLastException() {
        return lastException.get();
    }

    @Override
    public int getCurrentExecutionCount() {
        return executionCount.get();
    }

    @Override
    public Duration getLastDuration() {
        return lastDuration.get();
    }

    public final void run(Consumer<Consumer<Throwable>> onError) {
        Instant start = Instant.now();
        executionCount.incrementAndGet();
        lastTriggered.set(start);

        try {
            onError.accept(lastException::set);
        } finally {
            executionCount.decrementAndGet();
            Instant finish = Instant.now();
            lastFinished.set(finish);
            lastDuration.set(Duration.between(start, finish));
        }
    }

}
