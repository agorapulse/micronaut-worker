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
package com.agorapulse.worker;

import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Job configuration.
 */
public interface JobConfiguration {

    static JobConfiguration create(String name, Consumer<MutableJobConfiguration> configuration) {
        // using merge prevents misconfiguration
        DefaultJobConfiguration first = new DefaultJobConfiguration(name, WorkerConfiguration.ENABLED);
        DefaultJobConfiguration second = new DefaultJobConfiguration(name, WorkerConfiguration.ENABLED);
        configuration.accept(second);
        return first.mergeWith(second);
    }

    interface QueueConfiguration {

        /**
         * Returns the name of the queue (defaults to name of the job without job/producer/consumer suffix or
         * a value from @{@link com.agorapulse.worker.annotation.Consumes} or {@link com.agorapulse.worker.annotation.Produces}
         * annotations.
         *
         * @return the name of the queue override
         */
        @Nullable
        String getQueueName();

        /**
         * Returns the name of the preferred queue implementation or <code>null</code> if the default should be used.
         *
         * If there is no queue implementation of given name present the default one will be used.
         *
         * @return the name of the preferred queue implementation
         */
        @Nullable
        String getQueueType();

        /**
         * Merges the values from overrides into this configuration.
         *
         * @param overrides the configuration overrides
         */
        void mergeWith(QueueConfiguration overrides);
    }

    interface ConsumerQueueConfiguration extends QueueConfiguration {

        int DEFAULT_MAX_MESSAGES = 10;
        Duration DEFAULT_WAITING_TIME = Duration.ofSeconds(20);
        String DEFAULT_WAITING_TIME_STRING = "20s";

        /**
         * @return the number of messages which are fetched from the queue in a single poll, defaults to {@link #DEFAULT_MAX_MESSAGES} when set to <code>0</code>
         */
        @Min(1)
        int getMaxMessages();

        /**
         * @return waiting time for a single poll from the queue as a {@link java.time.Duration}
         */
        @Nullable
        Duration getWaitingTime();

        /**
         * Merges the values from overrides into this configuration.
         *
         * @param overrides the configuration overrides
         */
        void mergeWith(ConsumerQueueConfiguration overrides);

    }

    /**
     * @return the name of the job which should be always hyphenated
     */
    String getName();

    /**
     * @return whether the job is enabled
     */
    boolean isEnabled();

    /**
     * @return how many jobs can run at the same time, zero for no limits
     */
    int getConcurrency();

    /**
     * @return whether the job should be only executed on leader servers
     */
    boolean isLeaderOnly();

    /**
     * @return whether the job should be only executed on follower servers
     */
    boolean isFollowerOnly();

    /**
     * @return the CRON expression
     */
    @Nullable
    String getCron();

    /**
     * @return the {@link java.time.Duration} between the time of the last execution and the
     * beginning of the next. For example 10m == 10 minutes
     */
    @Nullable
    Duration getFixedDelay();

    /**
     * @return the {@link java.time.Duration} before starting executions. For example
     * 10m == 10 minutes
     */
    @Nullable
    Duration getInitialDelay();

    /**
     * @return the {@link java.time.Duration} between executions. For example 10m == 10 minutes
     */
    @Nullable
    Duration getFixedRate();

    /**
     * @return the name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    @NotBlank
    String getScheduler();

    /**
     * @return the consumer configuration
     */
    @NotNull
    ConsumerQueueConfiguration getConsumer();

    /**
     * @return the producer configuration
     */
    @NotNull
    QueueConfiguration getProducer();

    @Positive
    int getFork();

    /**
     * @param overrides the configuration which non-default values will override the values in this configuration
     * @return self with the values overridden from the other configuration object
     */
    JobConfiguration mergeWith(JobConfiguration overrides);

    /**
     * Creates new {@link Job} with current configuration and
     * @param task the task definition
     * @return new job with a copy of this configuration
     */
    default Job run(Runnable task) {
        return Job.create(new DefaultJobConfiguration(getName(), WorkerConfiguration.ENABLED).mergeWith(this), task);
    }

}
