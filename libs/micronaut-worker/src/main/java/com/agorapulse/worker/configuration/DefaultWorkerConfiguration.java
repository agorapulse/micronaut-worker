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

import com.agorapulse.worker.WorkerConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;

@ConfigurationProperties("worker")
public class DefaultWorkerConfiguration implements WorkerConfiguration {

    private boolean enabled;
    private boolean virtualThreadsCompatible = WorkerConfiguration.DEFAULT_VIRTUAL_THREAD_COMPATIBLE;
    private String queueType;
    private String scheduler = WorkerConfiguration.DEFAULT_SCHEDULER;

    public DefaultWorkerConfiguration(Environment env) {
        // disable for tests and functions
        this.enabled = !env.getActiveNames().contains(Environment.FUNCTION)
            && !env.getActiveNames().contains(Environment.TEST)
            && !env.getActiveNames().contains(Environment.CLI);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    @Override
    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public boolean isVirtualThreadsCompatible() {
        return virtualThreadsCompatible;
    }

    public void setVirtualThreadsCompatible(boolean virtualThreadsCompatible) {
        this.virtualThreadsCompatible = virtualThreadsCompatible;
    }

}
