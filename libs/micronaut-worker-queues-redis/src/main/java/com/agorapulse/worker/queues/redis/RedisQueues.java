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
package com.agorapulse.worker.queues.redis;

import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.queue.QueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.BoundedAsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RedisQueues implements JobQueues {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisQueues.class);
    private static final String PREFIX_DATED_QUEUE = "DATED_QUEUE::";

    private final ObjectMapper objectMapper;
    private final BoundedAsyncPool<StatefulRedisConnection<String, String>> pool;

    public RedisQueues(ObjectMapper objectMapper, RedisClient client, RedisPoolConfiguration redisPoolConfiguration) {
        this.objectMapper = objectMapper;

        BoundedPoolConfig config = BoundedPoolConfig
            .builder()
            .minIdle(redisPoolConfiguration.getMinIdle())
            .maxIdle(redisPoolConfiguration.getMaxIdle())
            .maxTotal(redisPoolConfiguration.getMaxTotal())
            .testOnAcquire(redisPoolConfiguration.isTestOnAcquire())
            .testOnCreate(redisPoolConfiguration.isTestOnCreate())
            .testOnRelease(redisPoolConfiguration.isTestOnRelease())
            .build();

        pool = new BoundedAsyncPool<>(new ConnectionFactory(client), config);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Publisher<QueueMessage<T>> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument) {
        TransactionResult result = withTransaction(redisCommands -> {
            String key = getKey(queueName);
            redisCommands.zrange(key, 0, maxNumberOfMessages - 1L);
            redisCommands.zremrangebyrank(key, 0, maxNumberOfMessages - 1L);
        });

        if (result == null) {
            return Flux.empty();
        }


        Object firstResponse = result.get(0);

        if (!(firstResponse instanceof List)) {
            throw new IllegalStateException("There result is not a list of Strings. Got: " + firstResponse);
        }

        List<String> messages = (List<String>) firstResponse;

        return Flux.fromIterable(messages).handle((body, sink) -> {
            try {
                T message = objectMapper.readValue(body, JacksonConfiguration.constructType(argument, objectMapper.getTypeFactory()));
                QueueMessage<T> queueMessage = QueueMessage.alwaysRequeue(
                    UUID.randomUUID().toString(),
                    message,
                    () -> {},
                    () -> sendRawMessage(queueName, body)
                );
                sink.next(queueMessage);
            } catch (JsonProcessingException e) {
                if (argument.equalsType(Argument.STRING)) {
                    QueueMessage<T> queueMessage = QueueMessage.alwaysRequeue(
                        UUID.randomUUID().toString(),
                        (T) body,
                        () -> {},
                        () -> sendRawMessage(queueName, body)
                    );
                    sink.next(queueMessage);
                } else {
                    sink.error(new IllegalArgumentException("Cannot convert to " + argument + "from message\n" + body, e));
                }
            }
        });
    }

    @Override
    public void sendMessage(String queueName, Object result) {
        try {
            String item = objectMapper.writeValueAsString(result);
            sendRawMessage(queueName, item);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot write " + result + " to JSON", e);
        }
    }

    @Override
    public void sendRawMessage(String queueName, Object result) {
        String item = result.toString();
        withRedis(redisCommands -> {
            String key = getKey(queueName);
            redisCommands.zscore(key, item).thenAccept(zscore -> {
                if (zscore == null) {
                    long time = System.currentTimeMillis();
                    redisCommands.zadd(key, time, item);
                }
            });
        });
    }

    private String getKey(String queueName) {
        return PREFIX_DATED_QUEUE + queueName;
    }

    private TransactionResult withTransaction(Consumer<RedisCommands<String, String>> action) {
        try {
            StatefulRedisConnection<String, String> connection = pool.acquire().get();
            RedisCommands<String, String> sync = connection.sync();
            try {
                sync.multi();
                action.accept(sync);
                return sync.exec();
            } finally {
                pool.release(connection);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception obtaining connection from the pool", e);
            return null;
        }
    }

    private void withRedis(
        Consumer<RedisAsyncCommands<String, String>> action
    ) {
        try {
            StatefulRedisConnection<String, String> connection = pool.acquire().get();
            RedisAsyncCommands<String, String> sync = connection.async();
            try {
                action.accept(sync);
            } finally {
                pool.release(connection);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception obtaining connection from the pool", e);
        }
    }

}
