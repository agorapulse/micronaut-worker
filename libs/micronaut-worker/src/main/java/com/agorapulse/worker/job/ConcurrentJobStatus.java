/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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

import com.agorapulse.worker.JobRunStatus;
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
    private final AtomicReference<String> lastId = new AtomicReference<>();

    private final String name;

    public ConcurrentJobStatus(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastId() {
        return lastId.get();
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

    public void run(Consumer<JobRunContext> doRun) {
        DefaultJobRunStatus status = DefaultJobRunStatus.create(getName());
        executionCount.incrementAndGet();
        lastTriggered.set(status.getStarted());

        JobRunContext context = JobRunContext.create(status)
            .onExecuted(s -> status.executed())
            .onFinished(s -> {
                status.finish();
                recordLastStatus(s);
            })
            .onError((s, ex) -> {
                status.fail(ex);
                lastException.set(ex);
                recordLastStatus(s);
            });

        doRun.accept(context);
    }

    private void recordLastStatus(JobRunStatus s) {
        lastFinished.set(s.getFinished());
        lastDuration.set(s.getDuration());
        lastId.set(s.getId());
        executionCount.decrementAndGet();
    }

}
