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

public interface JobRunContext {
    static JobRunContext create(JobRunStatus status) {
        return new DefaultJobRunContext(status);
    }

    JobRunContext onMessage(BiConsumer<JobRunStatus, Object> onMessage);

    JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError);

    JobRunContext onFinished(Consumer<JobRunStatus> onFinished);

    JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult);

    JobRunContext onExecuted(Consumer<JobRunStatus> onExecuted);

    void message(@Nullable Object event);

    void error(Throwable error);

    void finished();

    void result(@Nullable Object result);

    void executed();

    JobRunStatus getStatus();
}
