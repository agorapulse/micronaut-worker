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
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.function.Consumer;

public class JobBuilder implements MutableJobConfiguration {

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

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public boolean isEnabled() {
        return configuration.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        configuration.setEnabled(enabled);
    }

    @Override
    public int getConcurrency() {
        return configuration.getConcurrency();
    }

    @Override
    public void setConcurrency(int concurrency) {
        configuration.setConcurrency(concurrency);
    }

    @Override
    public boolean isLeaderOnly() {
        return configuration.isLeaderOnly();
    }

    @Override
    public void setLeaderOnly(boolean leaderOnly) {
        configuration.setLeaderOnly(leaderOnly);
    }

    @Override
    public boolean isFollowerOnly() {
        return configuration.isFollowerOnly();
    }

    @Override
    public void setFollowerOnly(boolean followerOnly) {
        configuration.setFollowerOnly(followerOnly);
    }

    @Override
    @Nullable
    public String getCron() {
        return configuration.getCron();
    }

    @Override
    public void setCron(@Nullable String cron) {
        configuration.setCron(cron);
    }

    @Override
    @Nullable
    public Duration getFixedDelay() {
        return configuration.getFixedDelay();
    }

    @Override
    public void setFixedDelay(@Nullable Duration fixedDelay) {
        configuration.setFixedDelay(fixedDelay);
    }

    @Override
    @Nullable
    public Duration getInitialDelay() {
        return configuration.getInitialDelay();
    }

    @Override
    public void setInitialDelay(@Nullable Duration initialDelay) {
        configuration.setInitialDelay(initialDelay);
    }

    @Override
    @Nullable
    public Duration getFixedRate() {
        return configuration.getFixedRate();
    }

    @Override
    public void setFixedRate(@Nullable Duration fixedRate) {
        configuration.setFixedRate(fixedRate);
    }

    @Override
    @NotBlank
    public String getScheduler() {
        return configuration.getScheduler();
    }

    @Override
    public void setScheduler(@NotBlank String scheduler) {
        configuration.setScheduler(scheduler);
    }

    @Override
    public DefaultJobConfiguration.ConsumerQueueConfiguration getConsumer() {
        return configuration.getConsumer();
    }

    @Override
    public void withConsumer(Consumer<MutableQueueConfiguration> consumer) {
        configuration.withConsumer(consumer);
    }

    @Override
    public DefaultJobConfiguration.ProducerQueueConfiguration getProducer() {
        return configuration.getProducer();
    }

    @Override
    public void withProducer(Consumer<MutableQueueConfiguration> producer) {
        configuration.withProducer(producer);
    }

    @Override
    public JobConfiguration mergeWith(JobConfiguration overrides) {
        return configuration.mergeWith(overrides);
    }
}
