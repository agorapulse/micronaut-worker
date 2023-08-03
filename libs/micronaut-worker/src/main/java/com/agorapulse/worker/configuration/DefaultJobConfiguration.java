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
package com.agorapulse.worker.configuration;

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.WorkerConfiguration;
import com.agorapulse.worker.json.DurationSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.util.StringUtils;
import io.micronaut.scheduling.TaskExecutors;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Duration;
import java.util.function.Consumer;

@EachProperty("worker.jobs")
public class DefaultJobConfiguration implements MutableJobConfiguration {

    @JsonInclude
    public static class DefaultQueueConfiguration implements MutableQueueConfiguration {

        private String queueName;
        private String queueType;

        @Nullable
        @Override
        public String getQueueName() {
            return queueName;
        }

        @Override
        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        @Nullable
        @Override
        public String getQueueType() {
            return queueType;
        }

        @Override
        public void setQueueType(String queueType) {
            this.queueType = queueType;
        }

        @Override
        public void mergeWith(QueueConfiguration overrides) {
            if (StringUtils.isNotEmpty(overrides.getQueueName())) {
                this.queueName = overrides.getQueueName();
            }

            if (StringUtils.isNotEmpty(overrides.getQueueType())) {
                this.queueType = overrides.getQueueType();
            }
        }
    }

    @JsonInclude
    public static class DefaultConsumerQueueConfiguration extends DefaultQueueConfiguration implements MutableConsumerQueueConfiguration {

        private static final int DEFAULT_MAX_MESSAGES = 10;

        private int maxMessages = DEFAULT_MAX_MESSAGES;
        private Duration waitingTime = Duration.ofSeconds(20);

        @Min(1)
        @Override
        public int getMaxMessages() {
            return maxMessages;
        }

        @Override
        public void setMaxMessages(@Min(1) int maxMessages) {
            this.maxMessages = maxMessages;
        }

        @Nullable
        @Override
        @JsonSerialize(using = DurationSerializer.class)
        public Duration getWaitingTime() {
            return waitingTime;
        }

        @Override
        public void setWaitingTime(Duration waitingTime) {
            this.waitingTime = waitingTime;
        }

        @Override
        public void mergeWith(ConsumerQueueConfiguration overrides) {
            super.mergeWith(overrides);

            if (overrides.getMaxMessages() != DEFAULT_MAX_MESSAGES && overrides.getMaxMessages() != this.maxMessages) {
                this.maxMessages = overrides.getMaxMessages();
            }

            if (overrides.getWaitingTime() != null && !overrides.getWaitingTime().isZero() && overrides.getWaitingTime() != this.waitingTime) {
                this.waitingTime = overrides.getWaitingTime();
            }
        }
    }

    private final String name;

    boolean enabled;

    int concurrency;
    boolean leaderOnly;
    boolean followerOnly;

    @Nullable private String cron;
    @Nullable private Duration fixedDelay;
    @Nullable private Duration initialDelay;
    @Nullable private Duration fixedRate;
    @NotBlank private String scheduler = TaskExecutors.SCHEDULED;

    @Positive private int fork = 1;

    @ConfigurationBuilder(configurationPrefix = "consumer")
    private final DefaultConsumerQueueConfiguration consumer = new DefaultConsumerQueueConfiguration();

    @ConfigurationBuilder(configurationPrefix = "producer")
    private final DefaultQueueConfiguration producer = new DefaultQueueConfiguration();

    public DefaultJobConfiguration(@Parameter String name, WorkerConfiguration workerConfiguration) {
        this.enabled = workerConfiguration.isEnabled();
        this.consumer.setQueueType(workerConfiguration.getQueueType());
        this.producer.setQueueType(workerConfiguration.getQueueType());
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    @Override
    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    @Override
    public boolean isLeaderOnly() {
        return leaderOnly;
    }

    @Override
    public void setLeaderOnly(boolean leaderOnly) {
        this.leaderOnly = leaderOnly;
    }

    @Override
    public boolean isFollowerOnly() {
        return followerOnly;
    }

    @Override
    public void setFollowerOnly(boolean followerOnly) {
        this.followerOnly = followerOnly;
    }

    @Override
    @Nullable
    public String getCron() {
        return cron;
    }

    @Override
    public void setCron(@Nullable String cron) {
        this.cron = cron;
    }

    @Override
    @Nullable
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getFixedDelay() {
        return fixedDelay;
    }

    @Override
    public void setFixedDelay(@Nullable Duration fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    @Override
    @Nullable
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getInitialDelay() {
        return initialDelay;
    }

    @Override
    public void setInitialDelay(@Nullable Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    @Override
    @Nullable
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getFixedRate() {
        return fixedRate;
    }

    @Override
    public void setFixedRate(@Nullable Duration fixedRate) {
        this.fixedRate = fixedRate;
    }

    @Override
    @NotBlank
    public String getScheduler() {
        return scheduler;
    }

    @Override
    public void setScheduler(@NotBlank String scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public int getFork() {
        return fork;
    }

    public void setFork(int fork) {
        this.fork = fork;
    }

    @Override
    public DefaultConsumerQueueConfiguration getConsumer() {
        return consumer;
    }

    @Override
    public void withConsumer(Consumer<MutableQueueConfiguration> consumer) {
        consumer.accept(this.consumer);
    }

    @Override
    public DefaultQueueConfiguration getProducer() {
        return producer;
    }

    @Override
    public void withProducer(Consumer<MutableQueueConfiguration> producer) {
        producer.accept(this.producer);
    }

    @Override
    public JobConfiguration mergeWith(JobConfiguration overrides) {
        this.enabled = overrides.isEnabled();

        if (overrides.getConcurrency() > 0) {
            this.concurrency = overrides.getConcurrency();
        }

        if (overrides.isLeaderOnly()) {
            this.leaderOnly = overrides.isLeaderOnly();
            this.followerOnly = false;
        }

        if (overrides.isFollowerOnly()) {
            this.followerOnly = overrides.isFollowerOnly();
            this.leaderOnly = false;
        }

        if (StringUtils.isNotEmpty(overrides.getCron())) {
            this.cron = overrides.getCron();
            this.initialDelay = null;
            this.fixedDelay = null;
            this.fixedRate = null;
        }

        if (overrides.getFixedDelay() != null) {
            this.cron = null;
            this.fixedRate = null;
            this.fixedDelay = overrides.getFixedDelay();
        }

        if (overrides.getFixedRate() != null) {
            this.cron = null;
            this.fixedDelay = null;
            this.fixedRate = overrides.getFixedRate();
        }

        if (overrides.getInitialDelay() != null) {
            this.cron = null;
            this.initialDelay = overrides.getInitialDelay();
        }

        if (overrides.getScheduler() != null && !overrides.getScheduler().equals(TaskExecutors.SCHEDULED)) {
            this.scheduler = overrides.getScheduler();
        }

        if (overrides.getFork() != 1) {
            this.fork = overrides.getFork();
        }

        consumer.mergeWith(overrides.getConsumer());
        producer.mergeWith(overrides.getProducer());

        return this;
    }
}
