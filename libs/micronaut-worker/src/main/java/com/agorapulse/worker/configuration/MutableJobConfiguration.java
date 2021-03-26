package com.agorapulse.worker.configuration;

import com.agorapulse.worker.JobConfiguration;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.function.Consumer;

public interface MutableJobConfiguration extends JobConfiguration {

    interface MutableQueueConfiguration extends JobConfiguration.QueueConfiguration {
        void setQueueName(String queueName);

        void setQueueQualifier(String queueQualifier);

        void setMaxMessages(@Min(1) int maxMessages);

        void setWaitingTime(Duration waitingTime);
    }

    void setEnabled(boolean enabled);

    void setConcurrency(int concurrency);

    void setLeaderOnly(boolean leaderOnly);

    void setFollowerOnly(boolean followerOnly);

    void setCron(@Nullable String cron);

    void setFixedDelay(@Nullable Duration fixedDelay);

    void setInitialDelay(@Nullable Duration initialDelay);

    void setFixedRate(@Nullable Duration fixedRate);

    void setScheduler(@NotBlank String scheduler);

    void withConsumer(Consumer<MutableQueueConfiguration> consumer);

    void withProducer(Consumer<MutableQueueConfiguration> consumer);
}
