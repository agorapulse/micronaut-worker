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
import io.micronaut.context.Qualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.LoomSupport;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Singleton;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public class DefaultExecutorServiceProvider implements ExecutorServiceProvider, Closeable {

    private final Map<String, ExecutorService> createdExecutors = new ConcurrentHashMap<>();

    private final BeanContext beanContext;

    public DefaultExecutorServiceProvider(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void close() {
        for (ExecutorService executor : createdExecutors.values()) {
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
        if (createdExecutors.containsKey(schedulerName)) {
            return createdExecutors.get(schedulerName);
        }

        Qualifier<ExecutorService> byName = Qualifiers.byName(schedulerName);

        return beanContext
            .findBean(ExecutorService.class, byName)
            .filter(ScheduledExecutorService.class::isInstance)
            .orElseGet(() -> {
                // TODO: also add configuration to the job
                ExecutorService service = Executors.newScheduledThreadPool(
                        LoomSupport.isSupported() ? 0 : fork,
                        LoomSupport.isSupported() ? LoomSupport.newVirtualThreadFactory(schedulerName) : new NamedThreadFactory(schedulerName)
                );

                createdExecutors.put(schedulerName, service);

                if (beanContext.findBean(ExecutorService.class, byName).isEmpty()) {
                    beanContext.registerSingleton(ExecutorService.class, service, byName);
                }

                return service;
            });
    }
}
