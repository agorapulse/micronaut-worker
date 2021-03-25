package com.agorapulse.worker.configuration;

import com.agorapulse.worker.JobConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.TaskExecutors;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.Duration;

@EachProperty("jobs")
@Requires(property = "jobs.enabled", notEquals = "false")
public class DefaultJobConfiguration implements JobConfiguration {

    public static class DefaultQueueConfiguration implements QueueConfiguration {

        private String queueName;
        private int maxMessages = 1;
        private Duration waitingTime = Duration.ZERO;

        @Nullable
        @Override
        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        @Min(1)
        @Override
        public int getMaxMessages() {
            return maxMessages;
        }

        public void setMaxMessages(@Min(1)int maxMessages) {
            this.maxMessages = maxMessages;
        }

        @Nullable
        @Override
        public Duration getWaitingTime() {
            return waitingTime;
        }

        public void setWaitingTime(Duration waitingTime) {
            this.waitingTime = waitingTime;
        }

        @Override
        public void mergeWith(QueueConfiguration overrides) {
            if (overrides.getQueueName() != null) {
                this.queueName = overrides.getQueueName();
            }

            if (overrides.getMaxMessages() != this.maxMessages) {
                this.maxMessages = overrides.getMaxMessages();
            }

            if (overrides.getWaitingTime() != null) {
                this.waitingTime = overrides.getWaitingTime();
            }
        }
    }

    @ConfigurationProperties("consumer")
    public static class ConsumerQueueConfiguration extends DefaultQueueConfiguration { }

    @ConfigurationProperties("producer")
    public static class ProducerQueueConfiguration extends DefaultQueueConfiguration { }


    private final String name;

    boolean enabled = true;

    int concurrency;
    boolean leaderOnly;
    boolean followerOnly;

    @Nullable private String cron;
    @Nullable private Duration fixedDelay;
    @Nullable private Duration initialDelay;
    @Nullable private Duration fixedRate;
    @NotBlank private String scheduler = TaskExecutors.SCHEDULED;

    private ConsumerQueueConfiguration consumer = new ConsumerQueueConfiguration();
    private ProducerQueueConfiguration producer = new ProducerQueueConfiguration();

    public DefaultJobConfiguration(@Parameter String name) {
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    @Override
    public boolean isLeaderOnly() {
        return leaderOnly;
    }

    public void setLeaderOnly(boolean leaderOnly) {
        this.leaderOnly = leaderOnly;
    }

    @Override
    public boolean isFollowerOnly() {
        return followerOnly;
    }

    public void setFollowerOnly(boolean followerOnly) {
        this.followerOnly = followerOnly;
    }

    @Override
    @Nullable
    public String getCron() {
        return cron;
    }

    public void setCron(@Nullable String cron) {
        this.cron = cron;
    }

    @Override
    @Nullable
    public Duration getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(@Nullable Duration fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    @Override
    @Nullable
    public Duration getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(@Nullable Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    @Override
    @Nullable
    public Duration getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(@Nullable Duration fixedRate) {
        this.fixedRate = fixedRate;
    }

    @Override
    @NotBlank
    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(@NotBlank String scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public ConsumerQueueConfiguration getConsumer() {
        return consumer;
    }

    public void setConsumer(ConsumerQueueConfiguration consumer) {
        this.consumer = consumer;
    }

    @Override
    public ProducerQueueConfiguration getProducer() {
        return producer;
    }

    public void setProducer(ProducerQueueConfiguration producer) {
        this.producer = producer;
    }

    @Override
    public JobConfiguration mergeWith(JobConfiguration overrides) {
        if (!overrides.isEnabled()) {
            this.enabled = overrides.isEnabled();
        }

        if (overrides.getConcurrency() > 0) {
            this.concurrency = overrides.getConcurrency();
        }

        if (overrides.isLeaderOnly()) {
            this.leaderOnly = overrides.isLeaderOnly();
        }

        if (overrides.isFollowerOnly()) {
            this.followerOnly = overrides.isFollowerOnly();
        }

        if (overrides.getCron() != null) {
            this.cron = overrides.getCron();
        }

        if (overrides.getFixedDelay() != null) {
            this.fixedDelay = overrides.getFixedDelay();

        }

        if (overrides.getInitialDelay() != null) {
            this.initialDelay = overrides.getInitialDelay();

        }

        if (overrides.getFixedRate() != null) {
            this.fixedRate = overrides.getFixedRate();

        }

        if (overrides.getScheduler() != null && !overrides.getScheduler().equals(TaskExecutors.SCHEDULED)) {
            this.scheduler = overrides.getScheduler();

        }

        consumer.mergeWith(overrides.getConsumer());
        producer.mergeWith(overrides.getProducer());

        return this;
    }
}
