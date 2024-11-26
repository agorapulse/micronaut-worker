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

import com.agorapulse.worker.configuration.MutableJobConfiguration;
import com.agorapulse.worker.job.SimpleJob;

import java.util.function.Consumer;

/**
 * Job is a {@link Runnable} with a name.
 */
public interface Job extends Runnable, JobInfo {

    static Job create(JobConfiguration configuration, Runnable task) {
        return new SimpleJob(configuration, task);
    }

    void forceRun();

    /**
     * Allows changing the configuration of the job if possible only for the lifetime of the application.
     * @param configuration the configuration to be changed
     */
    void configure(Consumer<MutableJobConfiguration> configuration);

}
