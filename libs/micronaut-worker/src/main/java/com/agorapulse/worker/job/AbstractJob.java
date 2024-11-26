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

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobStatus;
import com.agorapulse.worker.configuration.MutableJobConfiguration;

import java.util.function.Consumer;

public abstract class AbstractJob implements MutableCancelableJob {

    private final JobConfiguration configuration;
    private final ConcurrentJobStatus status;
    private Runnable cancelAction = () -> {};

    protected AbstractJob(JobConfiguration configuration) {
        this.configuration = configuration;
        this.status = new ConcurrentJobStatus(configuration.getName());
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
            forceRun();
        }
    }

    @Override
    public final void forceRun() {
        status.run(this::doRun);
    }

    @Override
    public void configure(Consumer<MutableJobConfiguration> change) {
        if (configuration instanceof MutableJobConfiguration c) {
            change.accept(c);
        } else {
            throw new IllegalStateException("The configuration of the job is not mutable!");
        }
    }

    @Override
    public String getSource() {
        return "";
    }

    protected abstract void doRun(JobRunContext context);

    @Override
    public void cancelAction(Runnable runnable) {
        Runnable current = this.cancelAction;
        this.cancelAction = () -> {
            current.run();
            runnable.run();
        };
    }

    @Override
    public void cancel() {
        cancelAction.run();
    }

}
