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
package com.agorapulse.worker.console;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobInfo;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobStatus;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import com.agorapulse.worker.report.JobReport;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class JobAccessor implements JobInfo {

    private final String jobName;
    private final JobManager jobManager;

    public JobAccessor(String jobName, JobManager jobManager) {
        this.jobName = jobName;
        this.jobManager = jobManager;
    }

    public void run() {
        jobManager.run(jobName);
    }

    public void enqueue(Object o) {
        jobManager.enqueue(jobName, o);
    }

    public JobAccessor reconfigure(Consumer<MutableJobConfiguration> configurator) {
        jobManager.reconfigure(jobName, configurator);
        return this;
    }

    @Override
    public String toString() {
        return JobReport.report(jobManager, Collections.singleton(jobName));
    }

    @Override
    public String getName() {
        return jobName;
    }

    @Override
    public String getSource() {
        return findJob().map(Job::getSource).orElse(null);
    }

    @Override
    public JobStatus getStatus() {
        return findJob().map(Job::getStatus).orElse(null);
    }

    @Override
    public JobConfiguration getConfiguration() {
        return findJob().map(Job::getConfiguration).orElse(null);
    }

    private Optional<Job> findJob() {
        return jobManager.getJob(jobName);
    }

}
