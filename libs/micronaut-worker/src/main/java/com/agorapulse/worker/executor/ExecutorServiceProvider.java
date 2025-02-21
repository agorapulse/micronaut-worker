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
package com.agorapulse.worker.executor;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.WorkerConfiguration;
import io.micronaut.scheduling.TaskScheduler;

import java.util.concurrent.ExecutorService;

public interface ExecutorServiceProvider {

    static String getSchedulerName(JobConfiguration configuration) {
        return WorkerConfiguration.DEFAULT_SCHEDULER.equals(configuration.getScheduler()) ? configuration.getName() : configuration.getScheduler();
    }

    TaskScheduler getTaskScheduler(Job job);
    ExecutorService getExecutorService(Job job);

}
