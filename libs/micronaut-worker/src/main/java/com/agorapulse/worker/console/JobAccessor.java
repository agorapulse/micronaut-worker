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

import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.report.JobReport;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class JobAccessor implements Callable<Void>, Consumer<Object> {

    private final String jobName;
    private final JobManager jobManager;

    public JobAccessor(String jobName, JobManager jobManager) {
        this.jobName = jobName;
        this.jobManager = jobManager;
    }

    // groovy DSL sugar
    @Override
    public Void call() {
        jobManager.run(jobName);
        return null;
    }

    @Override
    public void accept(Object o) {
        jobManager.enqueue(jobName, o);
    }

    // groovy DSL sugar
    public void call(Object object) {
        accept(object);
    }

    public String toString() {
        return JobReport.report(jobManager, Collections.singleton(jobName));
    }

}
