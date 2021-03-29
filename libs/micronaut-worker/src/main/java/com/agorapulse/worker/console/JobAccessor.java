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
package com.agorapulse.worker.console;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobStatus;
import com.agorapulse.worker.report.JobReport;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;

public class JobAccessor {

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

    // groovy DSL sugar
    public void call(Object object) {
        enqueue(object);
    }

    public String toString() {
        return JobReport.report(jobManager, Collections.singleton(jobName));
    }

    @JsonValue
    public JobStatus getStatus() {
        return jobManager.getJob(jobName).map(Job::getStatus).orElse(null);
    }

}
