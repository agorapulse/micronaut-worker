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

import com.agorapulse.worker.job.JobRunContext;
import io.micronaut.validation.Validated;
import org.reactivestreams.Publisher;

import java.util.concurrent.Callable;

@Validated
public interface DistributedJobExecutor {

    /**
     * Executes the tasks only on the leader server.
     *
     * @param context       the execution context
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls to the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R> executeOnlyOnLeader(JobRunContext context, Callable<R> task);

    /**
     * Executes the tasks only if it's not already running.
     *
     * @param context       the execution context
     * @param concurrency   the maximal count of jobs running at the same time
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R> executeConcurrently(JobRunContext context, int concurrency, Callable<R> task);

    /**
     * Executes the tasks only on the follower server.
     *
     * @param context       the execution context
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R> executeOnlyOnFollower(JobRunContext context, Callable<R> task);

    /**
     * Always executes the task.
     *
     * @param context       the execution context
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls the original supplier
     */
    <R> Publisher<R> execute(JobRunContext context, Callable<R> task);

}
