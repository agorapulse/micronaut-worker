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
package com.agorapulse.worker.job;

import com.agorapulse.worker.JobStatus;
import com.agorapulse.worker.json.DurationSerializer;
import com.agorapulse.worker.json.StacktraceSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@JsonInclude
public final class ConcurrentJobStatus implements JobStatus {

    private final AtomicInteger executionCount = new AtomicInteger(0);

    private final AtomicReference<Instant> lastTriggered = new AtomicReference<>();
    private final AtomicReference<Instant> lastFinished = new AtomicReference<>();
    private final AtomicReference<Duration> lastDuration = new AtomicReference<>();
    private final AtomicReference<Throwable> lastException = new AtomicReference<>();

    private final String name;

    public ConcurrentJobStatus(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getLastTriggered() {
        return lastTriggered.get();
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getLastFinished() {
        return lastFinished.get();
    }

    @Override
    @JsonSerialize(using = StacktraceSerializer.class)
    public Throwable getLastException() {
        return lastException.get();
    }

    @Override
    public int getExecutionCount() {
        return executionCount.get();
    }

    @Override
    @JsonSerialize(using = DurationSerializer.class)
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
