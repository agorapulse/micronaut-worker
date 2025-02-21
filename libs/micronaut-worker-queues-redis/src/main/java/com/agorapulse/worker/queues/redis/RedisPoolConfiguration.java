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
package com.agorapulse.worker.queues.redis;

import io.lettuce.core.support.BasePoolConfig;
import io.lettuce.core.support.BoundedPoolConfig;
import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("redis.pool")
public class RedisPoolConfiguration {

    private int minIdle = BoundedPoolConfig.DEFAULT_MIN_IDLE;
    private int maxIdle = BoundedPoolConfig.DEFAULT_MAX_IDLE;
    private int maxTotal = BoundedPoolConfig.DEFAULT_MAX_TOTAL;
    private boolean testOnCreate = BasePoolConfig.DEFAULT_TEST_ON_CREATE;
    private boolean testOnAcquire = BasePoolConfig.DEFAULT_TEST_ON_ACQUIRE;
    private boolean testOnRelease = BasePoolConfig.DEFAULT_TEST_ON_RELEASE;

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public boolean isTestOnCreate() {
        return testOnCreate;
    }

    public void setTestOnCreate(boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    public boolean isTestOnAcquire() {
        return testOnAcquire;
    }

    public void setTestOnAcquire(boolean testOnAcquire) {
        this.testOnAcquire = testOnAcquire;
    }

    public boolean isTestOnRelease() {
        return testOnRelease;
    }

    public void setTestOnRelease(boolean testOnRelease) {
        this.testOnRelease = testOnRelease;
    }
}
