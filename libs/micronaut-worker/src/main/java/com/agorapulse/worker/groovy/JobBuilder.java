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
package com.agorapulse.worker.groovy;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import java.time.Duration;

public class JobBuilder {

    private final DefaultJobConfiguration configuration;
    private Runnable task;

    public JobBuilder(String name) {
        this.configuration = new DefaultJobConfiguration(name);
    }

    public void task(
        Runnable task
    ) {
        this.task = task;
    }

    public Job build() {
        if (task == null) {
            throw new IllegalStateException("Task cannot be null");
        }
        return Job.create(configuration, task);
    }


    public void enabled(boolean enabled) {
        configuration.setEnabled(enabled);
    }


    public void concurrency(int concurrency) {
        configuration.setConcurrency(concurrency);
    }


    public void leaderOnly(boolean leaderOnly) {
        configuration.setLeaderOnly(leaderOnly);
    }


    public void followerOnly(boolean followerOnly) {
        configuration.setFollowerOnly(followerOnly);
    }


    public void cron(@Nullable String cron) {
        configuration.setCron(cron);
    }


    public void fixedDelay(@Nullable Duration fixedDelay) {
        configuration.setFixedDelay(fixedDelay);
    }


    public void initialDelay(@Nullable Duration initialDelay) {
        configuration.setInitialDelay(initialDelay);
    }


    public void fixedRate(@Nullable Duration fixedRate) {
        configuration.setFixedRate(fixedRate);
    }


    public void scheduler(@NotBlank String scheduler) {
        configuration.setScheduler(scheduler);
    }

    public void consumer(
        @DelegatesTo(value = MutableJobConfiguration.MutableConsumerQueueConfiguration.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.configuration.MutableJobConfiguration.MutableConsumerQueueConfiguration")
            Closure<?> consumer
    ) {
        configuration.withConsumer(ConsumerWithDelegate.create(consumer));
    }
    public void producer(
        @DelegatesTo(value = MutableJobConfiguration.MutableQueueConfiguration.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.configuration.MutableJobConfiguration.MutableQueueConfiguration")
            Closure<?> producer
    ) {
        configuration.withProducer(ConsumerWithDelegate.create(producer));
    }
}
