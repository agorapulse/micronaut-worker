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
package com.agorapulse.worker.job;

import com.agorapulse.worker.JobRunStatus;
import com.agorapulse.worker.json.StacktraceSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nonnull;

import java.time.Instant;
import java.util.UUID;

public class DefaultJobRunStatus implements JobRunStatus {

    public static DefaultJobRunStatus create(String name) {
        return new DefaultJobRunStatus(UUID.randomUUID().toString(), name, Instant.now());
    }

    private final String id;
    private final String name;
    private final Instant started;
    private Instant finished;
    private Throwable exception;

    private DefaultJobRunStatus(String id, String name, Instant started) {
        this.id = id;
        this.name = name;
        this.started = started;
    }

    @Override @Nonnull
    public String getId() {
        return id;
    }

    @Override @Nonnull
    public String getName() {
        return name;
    }


    @Override @Nonnull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getStarted() {
        return started;
    }

    @Override
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant getFinished() {
        return finished;
    }

    @Override
    @JsonSerialize(using = StacktraceSerializer.class)
    public Throwable getException() {
        return exception;
    }

    public void finish() {
        finished = Instant.now();
    }

    public void fail(Throwable exception) {
        finished = Instant.now();
        this.exception = exception;
    }

}