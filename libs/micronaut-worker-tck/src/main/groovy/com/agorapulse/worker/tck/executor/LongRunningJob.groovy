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
package com.agorapulse.worker.tck.executor

import com.agorapulse.worker.annotation.Concurrency
import com.agorapulse.worker.annotation.Consecutive
import com.agorapulse.worker.annotation.FollowerOnly
import com.agorapulse.worker.annotation.Fork
import com.agorapulse.worker.annotation.Job
import com.agorapulse.worker.annotation.LeaderOnly
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import io.reactivex.Flowable
import org.reactivestreams.Publisher

import jakarta.inject.Singleton
import java.util.concurrent.atomic.AtomicInteger

import static AbstractJobExecutorSpec.JOBS_INITIAL_DELAY
import static AbstractJobExecutorSpec.LONG_RUNNING_JOB_DURATION

@Singleton
@CompileStatic
@Requires(env = AbstractJobExecutorSpec.CONCURRENT_JOB_TEST_ENVIRONMENT)
class LongRunningJob {

    final AtomicInteger producer = new AtomicInteger()
    final AtomicInteger leader = new AtomicInteger()
    final AtomicInteger follower = new AtomicInteger()
    final AtomicInteger consecutive = new AtomicInteger()
    final AtomicInteger unlimited = new AtomicInteger()
    final AtomicInteger concurrent = new AtomicInteger()
    final AtomicInteger fork = new AtomicInteger()

    @Job(initialDelay = JOBS_INITIAL_DELAY)
    Publisher<String> executeProducer() {
        runLongTask()
        producer.incrementAndGet()
        return Flowable.just('Hello')
    }

    @LeaderOnly
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeOnLeader() {
        runLongTask()
        leader.incrementAndGet()
    }

    @FollowerOnly
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeOnFollower() {
        runLongTask()
        follower.incrementAndGet()
    }

    @Consecutive
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeConsecutive() {
        runLongTask()
        consecutive.incrementAndGet()
    }

    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeUnlimited() {
        runLongTask()
        unlimited.incrementAndGet()
    }

    @Concurrency(2)
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeConcurrent() {
        runLongTask()
        concurrent.incrementAndGet()
    }

    @Fork(2)
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    void executeFork() {
        runLongTask()
        fork.incrementAndGet()
    }

    @Override
    @SuppressWarnings('LineLength')
    String toString() {
        return "LongRunningJob{producer=$producer, leader=$leader, follower=$follower, consecutive=$consecutive, unlimited=$unlimited, concurrent=$concurrent, fork=$fork}"
    }

    private static void runLongTask() {
        Thread.sleep(LONG_RUNNING_JOB_DURATION)
    }

}

