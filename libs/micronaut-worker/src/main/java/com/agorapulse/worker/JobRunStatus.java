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
package com.agorapulse.worker;

import com.agorapulse.worker.job.DefaultJobRunStatus;
import com.agorapulse.worker.json.DurationSerializer;
import com.agorapulse.worker.report.JobReport;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;

public interface JobRunStatus {

    static JobRunStatus create(String name) {
        return DefaultJobRunStatus.create(name);
    }

    @Nonnull
    @JsonSerialize(using = DurationSerializer.class)
    default Duration getDuration() {
        Instant started = getStarted();
        Instant finished = getFinished();

        if (finished == null) {
            return Duration.ZERO;
        }

        return Duration.between(started, finished);
    }

    default String getHumanReadableDuration() {
        return JobReport.humanReadableFormat(getDuration());
    }

    default long getDurationMillis() {
        return getDuration().toMillis();
    }

    int getExecutionCount();

    @Nonnull String getName();
    @Nonnull String getId();

    @Nonnull Instant getStarted();
    @Nullable Instant getFinished();

    @Nullable Throwable getException();

}
