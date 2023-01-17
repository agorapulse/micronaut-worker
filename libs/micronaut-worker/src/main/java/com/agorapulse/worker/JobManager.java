/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Agorapulse.
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

import io.micronaut.core.naming.NameUtils;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface JobManager {

    static String getDefaultJobName(Class<?> jobClass) {
        return NameUtils.hyphenate(jobClass.getSimpleName());
    }

    static String getDefaultJobName(Class<?> jobClass, String methodName) {
        return NameUtils.hyphenate(jobClass.getSimpleName() + "-" + methodName);
    }

    /**
     * Registers a new job.
     *
     * @param job the job to be registered
     */
    void register(Job job);

    /**
     * @param name the name of the job
     * @return the job if present
     */
    Optional<Job> getJob(String name);

    /**
     * @return the set of all jobs' names
     */
    Set<String> getJobNames();

    /**
     * Execute the job synchronously.
     *
     * @param jobName the name of the job
     */
    default void run(String jobName) {
        getJob(jobName).ifPresent(Runnable::run);
    }

    /**
     * Force Execute the job synchronously, even the job is disabled.
     *
     * @param jobName the name of the job
     */
    default void forceRun(String jobName) {
        getJob(jobName).ifPresent(Job::forceRun);
    }

    /**
     * Sends a message to the job queue to be consumed during one of the next executions.
     *
     * @param jobName the name of the job
     * @param message the message for the job
     */
    void enqueue(String jobName, Object message);

    default void run(Class<?> jobClass) {
        run(getDefaultJobName(jobClass));
    }

    default void run(Class<?> jobClass, String methodName) {
        run(getDefaultJobName(jobClass, methodName));
    }

    default void forceRun(Class<?> jobClass) {
        forceRun(getDefaultJobName(jobClass));
    }

    default void forceRun(Class<?> jobClass, String methodName) {
        forceRun(getDefaultJobName(jobClass, methodName));
    }

    default <T> void enqueue(Class<? extends Consumer<? extends T>> jobClass, T message) {
        enqueue(getDefaultJobName(jobClass), message);
    }
}
