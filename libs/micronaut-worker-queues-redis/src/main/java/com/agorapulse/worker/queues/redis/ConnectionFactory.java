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

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.AsyncObjectFactory;

import java.util.concurrent.CompletableFuture;

public class ConnectionFactory implements AsyncObjectFactory<StatefulRedisConnection<String, String>> {

    private final RedisClient client;

    public ConnectionFactory(RedisClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<StatefulRedisConnection<String, String>> create() {
        return CompletableFuture.completedFuture(client.connect());
    }

    @Override
    public CompletableFuture<Void> destroy(StatefulRedisConnection<String, String> object) {
        return object.closeAsync();
    }

    @Override
    public CompletableFuture<Boolean> validate(StatefulRedisConnection<String, String> object) {
        return CompletableFuture.completedFuture(object.isOpen());
    }

}
