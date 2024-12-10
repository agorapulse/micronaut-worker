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
package com.agorapulse.worker.schedule;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobConfigurationException;
import com.agorapulse.worker.JobScheduler;
import com.agorapulse.worker.executor.ExecutorServiceProvider;
import com.agorapulse.worker.job.MutableCancelableJob;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;

@Singleton
@Requires(property = "jobs.enabled", notEquals = "false")
public class DefaultJobScheduler implements JobScheduler, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobScheduler.class);

    private final Queue<ScheduledFuture<?>> scheduledTasks = new ConcurrentLinkedDeque<>();
    private final ExecutorServiceProvider executorServiceProvider;

    /**
     */
    public DefaultJobScheduler(
        ExecutorServiceProvider executorServiceProvider
    ) {
        this.executorServiceProvider = executorServiceProvider;
    }

    @Override
    public void close() {
        for (ScheduledFuture<?> scheduledTask : scheduledTasks) {
            if (!scheduledTask.isCancelled()) {
                scheduledTask.cancel(false);
            }
        }
    }

    @Override
    public void schedule(com.agorapulse.worker.Job job) {
        JobConfiguration configuration = job.getConfiguration();
        TaskScheduler taskScheduler = executorServiceProvider.getTaskScheduler(job);

        ScheduledFuture<?> scheduled = doSchedule(job, configuration, taskScheduler);

        if (job instanceof MutableCancelableJob mj) {
            mj.cancelAction(() -> {
                if (!scheduled.isCancelled()) {
                    scheduled.cancel(false);
                }
            });
        }

        scheduledTasks.add(scheduled);
    }

    private ScheduledFuture<?> doSchedule(Job job, JobConfiguration configuration, TaskScheduler taskScheduler) {
        Duration initialDelay = configuration.getInitialDelay();

        if (StringUtils.isNotEmpty(configuration.getCron())) {
            if (LOG.isWarnEnabled() && initialDelay != null && !initialDelay.isZero()) {
                LOG.warn("Ignoring initial delay {} of cron job {} [{}] for {}", initialDelay, configuration.getName(), configuration.getCron(), job.getSource());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling cron job {} [{}] for {}", configuration.getName(), configuration.getCron(), job.getSource());
            }
            try {
                return taskScheduler.schedule(configuration.getCron(), job);
            } catch (IllegalArgumentException e) {
                throw new JobConfigurationException(job, "Failed to schedule job " + configuration.getName() + " declared in " + job.getSource() + ". Invalid CRON expression: " + configuration.getCron(), e);
            }
        }

        if (configuration.getFixedRate() != null) {
            Duration duration = configuration.getFixedRate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling fixed rate job {} [{}] for {}", configuration.getName(), duration, job.getSource());
            }

            return taskScheduler.scheduleAtFixedRate(initialDelay, duration, job);
        }

        if (configuration.getFixedDelay() != null) {
            Duration duration = configuration.getFixedDelay();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling fixed delay task {} [{}] for {}", configuration.getName(), duration, job.getSource());
            }

            return taskScheduler.scheduleWithFixedDelay(initialDelay, duration, job);
        }

        if (initialDelay != null) {
            return taskScheduler.schedule(initialDelay, job);
        }

        throw new JobConfigurationException(job, "Failed to schedule job " + configuration.getName() + " declared in " + job.getSource() + ". Invalid definition: " + configuration);
    }

}
