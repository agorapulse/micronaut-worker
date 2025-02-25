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
package com.agorapulse.worker.event;

import com.agorapulse.worker.JobRunStatus;
import io.micronaut.core.annotation.Introspected;

/**
 * Event dispatched after successful job execution.
 */
@Introspected
public class JobExecutionFinishedEvent {

    private final String name;
    private final JobRunStatus status;

    public JobExecutionFinishedEvent(String name, JobRunStatus status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public JobRunStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "JobExecutionFinishedEvent{name='%s', status=%s}".formatted(name, status);
    }
}
