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

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobConfigurationException;
import com.agorapulse.worker.JobScheduler;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import io.micronaut.scheduling.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.Closeable;
import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Singleton
@Requires(property = "jobs.enabled", notEquals = "false")
public class DefaultJobScheduler implements JobScheduler, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobScheduler.class);

    private final BeanContext beanContext;
    private final Queue<ScheduledFuture<?>> scheduledTasks = new ConcurrentLinkedDeque<>();

    /**
     * @param beanContext               The bean context for DI of beans annotated with {@link jakarta.inject.Inject}
     */
    public DefaultJobScheduler(
            BeanContext beanContext
    ) {
        this.beanContext = beanContext;
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
        TaskScheduler taskScheduler = getTaskScheduler(job);

        Duration initialDelay = configuration.getInitialDelay();

        if (StringUtils.isNotEmpty(configuration.getCron())) {
            if (LOG.isWarnEnabled() && initialDelay != null && !initialDelay.isZero()) {
                LOG.warn("Ignoring initial delay {} of cron job {} [{}] for {}", initialDelay, configuration.getName(), configuration.getCron(), job.getSource());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling cron job {} [{}] for {}", configuration.getName(), configuration.getCron(), job.getSource());
            }
            taskScheduler.schedule(configuration.getCron(), job);
        } else if (configuration.getFixedRate() != null) {
            Duration duration = configuration.getFixedRate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling fixed rate job {} [{}] for {}", configuration.getName(), duration, job.getSource());
            }

            for (int i = 0; i < configuration.getFork(); i++) {
                ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(initialDelay, duration, job);
                scheduledTasks.add(scheduledFuture);
            }
        } else if (configuration.getFixedDelay() != null) {
            Duration duration = configuration.getFixedDelay();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Scheduling fixed delay task {} [{}] for {}", configuration.getName(), duration, job.getSource());
            }

            for (int i = 0; i < configuration.getFork(); i++) {
                ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleWithFixedDelay(initialDelay, duration, job);
                scheduledTasks.add(scheduledFuture);
            }
        } else if (initialDelay != null) {
            for (int i = 0; i < configuration.getFork(); i++) {
                ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(initialDelay, job);
                scheduledTasks.add(scheduledFuture);
            }
        } else {
            throw new JobConfigurationException(job, "Failed to schedule job " + configuration.getName() + " declared in "  + job.getSource() + ". Invalid definition");
        }
    }

    private TaskScheduler getTaskScheduler(com.agorapulse.worker.Job job) {
        JobConfiguration configuration = job.getConfiguration();
        Optional<TaskScheduler> optionalTaskScheduler = beanContext.findBean(TaskScheduler.class, Qualifiers.byName(configuration.getScheduler()));

        if (!optionalTaskScheduler.isPresent()) {
            optionalTaskScheduler = beanContext.findBean(ExecutorService.class, Qualifiers.byName(configuration.getScheduler()))
                    .filter(ScheduledExecutorService.class::isInstance)
                    .map(ScheduledExecutorTaskScheduler::new);
        }

        return optionalTaskScheduler.orElseThrow(() -> new JobConfigurationException(job, "No scheduler of type TaskScheduler configured for name: " + configuration.getScheduler()));
    }
}
