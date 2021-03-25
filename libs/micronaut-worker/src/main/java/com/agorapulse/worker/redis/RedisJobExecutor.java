/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
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
package com.agorapulse.worker.redis;

import com.agorapulse.worker.executor.DistributedJobExecutor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.micronaut.configuration.lettuce.DefaultRedisConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.reactivex.Maybe;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Singleton
@Requires(beans = { StatefulRedisConnection.class }, property = "redis.uri")
public class RedisJobExecutor implements DistributedJobExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisJobExecutor.class);

    public static final String HOSTNAME_PARAMETER_NAME = "redis-job-executor-hostname";

    private static final String LIBRARY_PREFIX = "APMW::";
    private static final String PREFIX_LEADER = LIBRARY_PREFIX + "LEADER::";
    private static final String PREFIX_COUNT = LIBRARY_PREFIX + "COUNT::";
    private static final int LEADER_INACTIVITY_TIMEOUT = 500;
    private static final int LOCK_TIMEOUT = 60;
    private static final int COUNT_TIMEOUT = 600;
    private static final String OK_RESPONSE = "OK";

    private static final String LEADER_CHECK =
            "redis.call('set', KEYS[1], KEYS[2], 'nx', 'ex', KEYS[3])\n"
                    + "local result = redis.call('get', KEYS[1])\n"
                    + "if result and result == KEYS[2]\n"
                    + "then redis.call('expire', KEYS[1], KEYS[3]) end\n"
                    + "return result";

    private final StatefulRedisConnection<String, String> connection;
    private final String hostname;

    public RedisJobExecutor(StatefulRedisConnection<String, String> connection, @Named(HOSTNAME_PARAMETER_NAME) String hostname) {
        this.connection = connection;
        this.hostname = hostname;
    }

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(String jobName, Callable<R> supplier) {
        RedisAsyncCommands<String, String> commands = connection.async();

        return readMasterHostname(jobName, commands).flatMap(h -> {
            if (hostname.equals(h)) {
                return Maybe.fromCallable(supplier);
            }
            return Maybe.empty();
        }).toFlowable();
    }

    @Override
    public <R> Publisher<R> executeConcurrently(String jobName, int maxConcurrency, Callable<R> supplier) {
        // TODO: rewrite with inc/decr
        RedisAsyncCommands<String, String> commands = connection.async();

        String key = PREFIX_COUNT + jobName;
        // concurrent jobs have more graceful timeout as the may run for a long time
        SetArgs args = maxConcurrency <= 1 ? SetArgs.Builder.nx().ex(LOCK_TIMEOUT) : SetArgs.Builder.ex(COUNT_TIMEOUT);

        return getInteger(commands, key)
                .flatMap(count -> {
                    if (count >= maxConcurrency) {
                        if (LOGGER.isTraceEnabled()) {
                            LOGGER.trace("Skipping execution of the job {} as the concurrency level {} is already reached", jobName, maxConcurrency);
                        }
                        return Maybe.empty();
                    }

                    return Maybe.fromFuture(commands.set(key, String.valueOf(count + 1), args))
                            .flatMap(r -> {
                                if (!OK_RESPONSE.equals(r)) {
                                    LOGGER.warn("Failed to increase run count for {}", jobName);
                                }
                                return Maybe.fromCallable(supplier);
                            })
                            .doFinally(() ->
                                    getInteger(commands, key)
                                            .flatMap(newCount ->
                                                    Maybe.fromFuture(commands.set(key, String.valueOf(newCount - 1), args))
                                            )
                                            .subscribe(r -> {
                                                if (!OK_RESPONSE.equals(r)) {
                                                    LOGGER.warn("Failed to decrease run count for {}", jobName);
                                                }
                                            })
                            );
                }).toFlowable();
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(String jobName, Callable<R> supplier) {
        RedisAsyncCommands<String, String> commands = connection.async();
        return readMasterHostname(jobName, commands).flatMap(h -> {
            if (!"".equals(h) && h.equals(hostname)) {
                return Maybe.empty();
            }
            return Maybe.fromCallable(supplier);
        }).toFlowable();
    }

    private Maybe<Integer> getInteger(RedisAsyncCommands<String, String> commands, String key) {
        return Maybe.fromFuture(commands.get(key))
                .defaultIfEmpty("")
                .map(r -> StringUtils.isEmpty(r) ? 0 : Integer.parseInt(r, 10));
    }

    private Maybe<Object> readMasterHostname(String jobName, RedisAsyncCommands<String, String> commands) {
        return Maybe.fromFuture(commands.eval(
                LEADER_CHECK,
                ScriptOutputType.VALUE,
                PREFIX_LEADER + jobName, hostname, String.valueOf(LEADER_INACTIVITY_TIMEOUT)
        )).defaultIfEmpty("");
    }
}
