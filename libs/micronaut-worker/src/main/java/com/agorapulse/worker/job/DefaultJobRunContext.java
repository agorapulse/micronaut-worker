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
import com.agorapulse.worker.queue.QueueMessage;
import jakarta.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultJobRunContext implements JobRunContext {

    private final JobRunStatus status;

    private BiConsumer<JobRunStatus, QueueMessage<?>> onMessage = (aStatus, m) -> { };
    private BiConsumer<JobRunStatus, Throwable> onError = (aStatus, e) -> { };
    private Consumer<JobRunStatus> onFinished = s -> { };
    private BiConsumer<JobRunStatus, Object> onResult = (aStatus, r) -> { };
    private Consumer<JobRunStatus> onExecuted = s -> { };
    private Consumer<JobRunStatus> onSkipped = s -> { };

    public DefaultJobRunContext(JobRunStatus status) {
        this.status = status;
    }

    private DefaultJobRunContext(JobRunStatus status, BiConsumer<JobRunStatus, QueueMessage<?>> onMessage, BiConsumer<JobRunStatus, Throwable> onError, Consumer<JobRunStatus> onFinished, BiConsumer<JobRunStatus, Object> onResult, Consumer<JobRunStatus> onExecuted, Consumer<JobRunStatus> onSkipped) {
        this.status = status;
        this.onMessage = onMessage;
        this.onError = onError;
        this.onFinished = onFinished;
        this.onResult = onResult;
        this.onExecuted = onExecuted;
        this.onSkipped = onSkipped;
    }

    @Override
    public JobRunContext onMessage(BiConsumer<JobRunStatus, QueueMessage<?>> onMessage) {
        this.onMessage = this.onMessage.andThen(onMessage);
        return this;
    }

    @Override
    public JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError) {
        this.onError = this.onError.andThen(onError);
        return this;
    }

    @Override
    public JobRunContext onFinished(Consumer<JobRunStatus> onFinished) {
        this.onFinished = this.onFinished.andThen(onFinished);
        return this;
    }

    @Override
    public JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult) {
        this.onResult = this.onResult.andThen(onResult);
        return this;
    }

    @Override
    public JobRunContext onExecuted(Consumer<JobRunStatus> onExecuted) {
        this.onExecuted = this.onExecuted.andThen(onExecuted);
        return this;
    }

    @Override
    public JobRunContext onSkipped(Consumer<JobRunStatus> onSkipped) {
        this.onSkipped = this.onSkipped.andThen(onSkipped);
        return this;
    }

    @Override
    public void message(@Nullable QueueMessage<?> event) {
        onMessage.accept(status, event);
    }

    @Override
    public void error(Throwable error) {
        onError.accept(status, error);
    }

    @Override
    public void finished() {
        onFinished.accept(status);
    }

    @Override
    public void result(@Nullable Object result) {
        onResult.accept(status, result);
    }

    @Override
    public void executed() {
        onExecuted.accept(status);
    }

    @Override
    public void skipped() {
        onSkipped.accept(status);
    }

    @Override
    public JobRunStatus getStatus() {
        return status;
    }

    @Override
    public JobRunContext copy() {
        return new DefaultJobRunContext(status.copy(), onMessage, onError, onFinished, onResult, onExecuted, onSkipped);
    }

}
