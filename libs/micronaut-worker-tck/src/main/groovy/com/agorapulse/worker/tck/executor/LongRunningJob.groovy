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
package com.agorapulse.worker.tck.executor

import com.agorapulse.worker.annotation.Concurrency
import com.agorapulse.worker.annotation.Consecutive
import com.agorapulse.worker.annotation.Consumes
import com.agorapulse.worker.annotation.FollowerOnly
import com.agorapulse.worker.annotation.Fork
import com.agorapulse.worker.annotation.Job
import com.agorapulse.worker.annotation.LeaderOnly
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import io.netty.util.concurrent.FastThreadLocalThread
import org.reactivestreams.Publisher

import jakarta.inject.Singleton
import reactor.core.publisher.Flux

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

import static AbstractJobExecutorSpec.JOBS_INITIAL_DELAY
import static AbstractJobExecutorSpec.LONG_RUNNING_JOB_DURATION

@Singleton
@CompileStatic
@Requires(env = AbstractJobExecutorSpec.CONCURRENT_JOB_TEST_ENVIRONMENT)
class LongRunningJob {

    public static final String CONCURRENT_CONSUMER_QUEUE_NAME = 'concurrent-queue'
    public static final String REGULAR_CONSUMER_QUEUE_NAME = 'normal-queue'

    final AtomicInteger producer = new AtomicInteger()
    final AtomicInteger leader = new AtomicInteger()
    final AtomicInteger follower = new AtomicInteger()
    final AtomicInteger consecutive = new AtomicInteger()
    final AtomicInteger unlimited = new AtomicInteger()
    final AtomicInteger concurrent = new AtomicInteger()
    final Queue<String> consumedConcurrentMessages = new ConcurrentLinkedQueue()
    final Queue<String> consumedRegularMessages = new ConcurrentLinkedQueue()
    final AtomicInteger fork = new AtomicInteger()

    @Job(initialDelay = JOBS_INITIAL_DELAY)
    Publisher<String> executeProducer() {
        runLongTask()
        producer.incrementAndGet()
        return Flux.just('Hello')
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

    @Concurrency(2)
    @Job(initialDelay = JOBS_INITIAL_DELAY)
    @Consumes(value = CONCURRENT_CONSUMER_QUEUE_NAME, maxMessages = 3)
    void executeConcurrentConsumer(String message) {
        runLongTask()
        consumedConcurrentMessages.add(message)
    }

    @Job(initialDelay = JOBS_INITIAL_DELAY)
    @Consumes(value = REGULAR_CONSUMER_QUEUE_NAME, maxMessages = 3)
    void executeRegularConsumer(String message) {
        runLongTask()
        consumedRegularMessages.add(message)
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

    @SuppressWarnings('Instanceof')
    private static void runLongTask() {
        if (Thread.currentThread() instanceof FastThreadLocalThread) {
            throw new IllegalStateException('Running on FastThreadLocalThread will fail execution of HTTP client requests')
        }
        Thread.sleep(LONG_RUNNING_JOB_DURATION)
    }

}

