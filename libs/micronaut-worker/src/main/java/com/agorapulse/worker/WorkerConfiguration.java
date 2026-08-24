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
package com.agorapulse.worker;

import io.micronaut.scheduling.TaskExecutors;

import java.util.Collections;
import java.util.List;

public interface WorkerConfiguration {

    String DEFAULT_SCHEDULER = TaskExecutors.SCHEDULED;
    boolean DEFAULT_VIRTUAL_THREAD_COMPATIBLE = false;

    /**
     * Property holding the allow-list of job names to schedule. Bound to {@link #getForcedJobNames()}.
     */
    String FORCED_JOB_NAMES_PROPERTY = "worker.forced-job-names";

    WorkerConfiguration ENABLED = new WorkerConfiguration() {
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getQueueType() {
            return null;
        }

        @Override
        public String getScheduler() {
            return DEFAULT_SCHEDULER;
        }

        @Override
        public boolean isVirtualThreadsCompatible() {
            return DEFAULT_VIRTUAL_THREAD_COMPATIBLE;
        }

    };

    boolean isEnabled();

    /**
     * @return the default queue type such as <code>local</code> or <code>sqs</code>
     */
    String getQueueType();

    String getScheduler();

    boolean isVirtualThreadsCompatible();

    /**
     * Allow-list of job names to schedule. When empty (the default) every enabled job is scheduled. When
     * non-empty only the listed jobs are scheduled; all other jobs are still registered with the
     * {@link JobManager} but never started. Used by the job runner to run a single job without letting the
     * other jobs' consumers poll their queues.
     *
     * @return the names of the jobs to schedule, or an empty list to schedule every enabled job
     */
    default List<String> getForcedJobNames() {
        return Collections.emptyList();
    }

}
