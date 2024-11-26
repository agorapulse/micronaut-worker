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
import jakarta.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JobRunContext {
    public static JobRunContext create(JobRunStatus status) {
        return new JobRunContext(status);
    }

    private final JobRunStatus status;

    private BiConsumer<JobRunStatus, Object> onMessage = (aStatus, m) -> { };
    private BiConsumer<JobRunStatus, Throwable> onError = (aStatus, e) -> { };
    private Consumer<JobRunStatus> onFinished = s -> { };
    private BiConsumer<JobRunStatus, Object> onResult = (aStatus, r) -> { };

    public JobRunContext(JobRunStatus status) {
        this.status = status;
    }

    public JobRunContext onMessage(BiConsumer<JobRunStatus, Object> onMessage) {
        this.onMessage = this.onMessage.andThen(onMessage);
        return this;
    }

    public JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError) {
        this.onError = this.onError.andThen(onError);
        return this;
    }

    public JobRunContext onFinished(Consumer<JobRunStatus> onFinished) {
        this.onFinished = this.onFinished.andThen(onFinished);
        return this;
    }

    public JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult) {
        this.onResult = this.onResult.andThen(onResult);
        return this;
    }

    public void message(@Nullable Object event) {
        onMessage.accept(status, event);
    }

    public void error(Throwable error) {
        onError.accept(status, error);
    }

    public void finished() {
        onFinished.accept(status);
    }

    public void result(@Nullable Object result) {
        onResult.accept(status, result);
    }

}
