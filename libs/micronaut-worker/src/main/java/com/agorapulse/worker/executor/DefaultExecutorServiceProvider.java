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
package com.agorapulse.worker.executor;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Singleton;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class DefaultExecutorServiceProvider implements ExecutorServiceProvider, Closeable {

    private final List<ExecutorService> createdExecutors = new CopyOnWriteArrayList<>();

    private final BeanContext beanContext;

    public DefaultExecutorServiceProvider(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void close() {
        for (ExecutorService executor : createdExecutors) {
            executor.shutdown();
        }
    }

    @Override
    public ExecutorService getExecutorService(Job job) {
        return getExecutor(ExecutorServiceProvider.getSchedulerName(job.getConfiguration()), job.getConfiguration().getFork());
    }

    @Override
    public TaskScheduler getTaskScheduler(Job job) {
        JobConfiguration configuration = job.getConfiguration();

        String schedulerName = ExecutorServiceProvider.getSchedulerName(configuration);

        Optional<TaskScheduler> optionalTaskScheduler = beanContext.findBean(TaskScheduler.class, Qualifiers.byName(schedulerName));

        if (optionalTaskScheduler.isEmpty()) {
            optionalTaskScheduler = beanContext.findBean(ExecutorService.class, Qualifiers.byName(schedulerName))
                .filter(ScheduledExecutorService.class::isInstance)
                .map(ScheduledExecutorTaskScheduler::new);
        }

        return optionalTaskScheduler.orElseGet(() -> {
            ExecutorService executor = getExecutor(schedulerName, configuration.getFork());
            ScheduledExecutorTaskScheduler scheduler = new ScheduledExecutorTaskScheduler(executor);
            beanContext.registerSingleton(TaskScheduler.class, scheduler, Qualifiers.byName(schedulerName));
            return scheduler;
        });
    }

    private ExecutorService getExecutor(String schedulerName, int fork) {
        return beanContext
            .findBean(ExecutorService.class, Qualifiers.byName(schedulerName))
            .orElseGet(() -> {
                ExecutorService service = Executors.newScheduledThreadPool(fork, new NamedThreadFactory(schedulerName));

                createdExecutors.add(service);

                beanContext.registerSingleton(ExecutorService.class, service, Qualifiers.byName(schedulerName));

                return service;
            });
    }
}
