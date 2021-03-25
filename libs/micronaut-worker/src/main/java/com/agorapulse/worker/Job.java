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
package com.agorapulse.worker;

import com.agorapulse.worker.queue.JobQueues;
import io.micronaut.context.Qualifier;

import java.time.Duration;
import java.time.Instant;

/**
 * Job is a {@link Runnable} with a name.
 */
public interface Job extends Runnable {

    /**
     * @return the name of the job
     */
    default String getName() {
        return getConfiguration().getName();
    }

    /**
     * @return the source of the job, e.g. the class and method name
     */
    String getSource();

    /**
     * @return the qualifier to be used find the appropriate {@link com.agorapulse.worker.queue.JobQueues} bean.
     */
    Qualifier<JobQueues> getJobQueueQualifier();

    JobConfiguration getConfiguration();

    // TODO: move following properties into status object
    Instant getLastTriggered();
    Instant getLastFinished();
    Duration getLastDuration();
    Throwable getLastException();
    int getCurrentExecutionCount();
}
