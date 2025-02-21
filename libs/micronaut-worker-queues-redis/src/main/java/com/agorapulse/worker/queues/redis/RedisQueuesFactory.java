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

import com.agorapulse.worker.queue.JobQueues;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
@Requires(classes = { RedisClient.class }, beans = { RedisClient.class }, property = "redis.uri")
public class RedisQueuesFactory {


    @Bean
    @Singleton
    @Named("redis")
    @Requires(property = "worker.queues.redis.enabled", value = "true", defaultValue = "true")
    public JobQueues redisQueues(
            RedisClient redisClient,
            ObjectMapper mapper,
            RedisPoolConfiguration redisPoolConfiguration
    ) {
        return new RedisQueues(mapper, redisClient, redisPoolConfiguration);
    }

}
