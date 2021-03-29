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
package com.agorapulse.worker.job;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobStatus;

import java.util.function.Consumer;

public abstract class AbstractJob implements Job {

    private final JobConfiguration configuration;
    private final DefaultJobStatus status;

    protected AbstractJob(JobConfiguration configuration) {
        this.configuration = configuration;
        this.status = new DefaultJobStatus(configuration.getName());
    }

    @Override
    public JobConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public JobStatus getStatus() {
        return status;
    }

    @Override
    public final void run() {
        if (configuration.isEnabled()) {
            status.run(this::doRun);
        }
    }

    protected abstract void doRun(Consumer<Throwable> onError);
}
