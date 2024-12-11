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
package com.agorapulse.worker.configuration;

import com.agorapulse.worker.JobConfiguration;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.function.Consumer;

public interface MutableJobConfiguration extends JobConfiguration {

    interface MutableQueueConfiguration extends JobConfiguration.QueueConfiguration {
        void setQueueName(String queueName);

        void setQueueType(String queueType);
    }

    interface MutableConsumerQueueConfiguration extends MutableQueueConfiguration, JobConfiguration.ConsumerQueueConfiguration {
        void setMaxMessages(@Min(1) int maxMessages);

        void setWaitingTime(Duration waitingTime);
    }

    void setEnabled(boolean enabled);

    void setConcurrency(int concurrency);

    void setLeaderOnly(boolean leaderOnly);

    void setVirtualThreadsCompatible(boolean leaderOnly);

    void setFollowerOnly(boolean followerOnly);

    void setCron(@Nullable String cron);

    void setFixedDelay(@Nullable Duration fixedDelay);

    void setInitialDelay(@Nullable Duration initialDelay);

    void setFixedRate(@Nullable Duration fixedRate);

    void setScheduler(@NotBlank String scheduler);

    void setFork(@Positive int fork);

    void withConsumer(Consumer<MutableQueueConfiguration> consumer);

    void withProducer(Consumer<MutableQueueConfiguration> producer);
}
