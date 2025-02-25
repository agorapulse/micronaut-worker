/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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

import io.micronaut.core.util.ArgumentUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates new named threads.
 * <p>
 *
 * Copied from Micronaut codebase because of package-private visibility.
 */
class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    /**
     * The constructor.
     *
     * @param name new thread's prefix
     */
    NamedThreadFactory(String name) {
        ArgumentUtils.check("name", name).notNull();
        group = Thread.currentThread().getThreadGroup();
        namePrefix = name + "-thread-";
    }

    /**
     * Constructs a new {@code Thread}.
     *
     * @param runnable The Runnable
     * @return new thread
     */
    @Override
    public Thread newThread(Runnable runnable) {
        Thread newThread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
        if (newThread.isDaemon()) {
            newThread.setDaemon(false);
        }
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        return newThread;
    }
}
