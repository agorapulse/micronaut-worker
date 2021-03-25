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
package com.agorapulse.worker.manager;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.report.JobReport;
import io.micronaut.context.BeanContext;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class DefaultJobManager implements JobManager {

    private final ConcurrentMap<String, Job> tasks = new ConcurrentHashMap<>();

    private final BeanContext beanContext;

    public DefaultJobManager(List<Job> tasksFromContext, BeanContext beanContext) {
        this.beanContext = beanContext;
        tasksFromContext.forEach(this::registerInternal);
    }

    @Override
    public void register(Job job) {
        registerInternal(job);
    }

    @Override
    public Optional<Job> getJob(String jobName) {
        return Optional.ofNullable(tasks.get(jobName));
    }

    @Override
    public Set<String> getJobNames() {
        return new TreeSet<>(tasks.keySet());
    }

    // called from constructor, preventing issues if subclassed
    private void registerInternal(Job jobMethodTask) {
        tasks.put(jobMethodTask.getName(), jobMethodTask);
    }

    @Override
    public String toString() {
        return JobReport.report(this);
    }

    @Override
    public void enqueue(String jobName, Object payload) {
        getJob(jobName).ifPresent(job ->
                beanContext.getBean(JobQueues.class, job.getJobQueueQualifier()).sendMessage(job.getConfiguration().getConsumer().getQueueName(), payload)
        );
    }
}
