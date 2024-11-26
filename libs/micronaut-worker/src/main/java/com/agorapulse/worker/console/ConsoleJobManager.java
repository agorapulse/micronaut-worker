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
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConsoleJobManager implements JobManager {

    private final JobManager delegate;

    public ConsoleJobManager(JobManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void register(Job job) {
        delegate.register(job);
    }

    @Override
    public Optional<Job> getJob(String name) {
        return delegate.getJob(name);
    }

    @Override
    public Set<String> getJobNames() {
        return delegate.getJobNames();
    }

    @Override
    public void enqueue(String jobName, Object message) {
        delegate.enqueue(jobName, message);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @JsonValue
    List<JobAccessor> getJobs() {
        return getJobNames().stream().map(name -> new JobAccessor(name, this)).collect(Collectors.toList());
    }

    @Override
    public void reconfigure(String jobName, Consumer<MutableJobConfiguration> configuration) {
        delegate.reconfigure(jobName, configuration);
    }

}
