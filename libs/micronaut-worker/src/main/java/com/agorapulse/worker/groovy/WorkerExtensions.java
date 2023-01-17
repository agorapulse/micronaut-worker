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
package com.agorapulse.worker.groovy;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import com.agorapulse.worker.console.JobAccessor;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;

public class WorkerExtensions {

    public static void enabled(MutableJobConfiguration self, boolean enabled) {
        self.setEnabled(enabled);
    }

    public static void concurrency(MutableJobConfiguration self, int concurrency) {
        self.setConcurrency(concurrency);
    }

    public static void leaderOnly(MutableJobConfiguration self, boolean leaderOnly) {
        self.setLeaderOnly(leaderOnly);
    }

    public static void followerOnly(MutableJobConfiguration self, boolean followerOnly) {
        self.setFollowerOnly(followerOnly);
    }

    public static void cron(MutableJobConfiguration self, @Nullable String cron) {
        self.setCron(cron);
    }

    public static void fixedDelay(MutableJobConfiguration self, @Nullable Duration fixedDelay) {
        self.setFixedDelay(fixedDelay);
    }

    public static void initialDelay(MutableJobConfiguration self, @Nullable Duration initialDelay) {
        self.setInitialDelay(initialDelay);
    }

    public static void fixedRate(MutableJobConfiguration self, @Nullable Duration fixedRate) {
        self.setFixedRate(fixedRate);
    }

    public static void scheduler(MutableJobConfiguration self, @NotBlank String scheduler) {
        self.setScheduler(scheduler);
    }

    public static void fork(MutableJobConfiguration self, @Positive int fork) {
        self.setFork(fork);
    }


    public static void consumer(
        MutableJobConfiguration self,
        @DelegatesTo(value = MutableJobConfiguration.MutableConsumerQueueConfiguration.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.configuration.MutableJobConfiguration.MutableConsumerQueueConfiguration")
            Closure<?> consumer
    ) {
        self.withConsumer(ConsumerWithDelegate.create(consumer));
    }

    public static void producer(
        MutableJobConfiguration self,
        @DelegatesTo(value = MutableJobConfiguration.MutableQueueConfiguration.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.configuration.MutableJobConfiguration.MutableQueueConfiguration")
            Closure<?> producer
    ) {
        self.withProducer(ConsumerWithDelegate.create(producer));
    }

    public static void queueName(MutableJobConfiguration.MutableQueueConfiguration self, String queueName) {
        self.setQueueName(queueName);
    }
    public static void queueType(MutableJobConfiguration.MutableQueueConfiguration self, String queueType) {
        self.setQueueType(queueType);
    }
    public static void maxMessages(MutableJobConfiguration.MutableConsumerQueueConfiguration self, @Min(1) int maxMessages) {
        self.setMaxMessages(maxMessages);
    }
    public static void waitingTime(MutableJobConfiguration.MutableConsumerQueueConfiguration self, Duration waitingTime) {
        self.setWaitingTime(waitingTime);
    }

    public static void call(JobAccessor accessor, Object payload) {
        accessor.enqueue(payload);
    }

    public static void call(JobAccessor accessor) {
        accessor.run();
    }

    public static Job register(
        JobManager self,
        String name,
        @DelegatesTo(value = JobBuilder.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.groovy.JobBuilder")
            Closure<?> builder
    ) {
        Job job = WorkerStaticExtensions.build((Job) null, name, builder);
        self.register(job);
        return job;
    }

}
