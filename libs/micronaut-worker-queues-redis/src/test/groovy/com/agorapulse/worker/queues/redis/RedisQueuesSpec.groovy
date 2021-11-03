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
package com.agorapulse.worker.queues.redis

import com.agorapulse.worker.tck.queue.AbstractQueuesSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
class RedisQueuesSpec extends AbstractQueuesSpec {

    @Shared
    GenericContainer redis = new GenericContainer('redis:3-alpine').withExposedPorts(6379)

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getExpectedImplementation() { return RedisQueues }

    @Override
    ApplicationContext buildContext(String[] envs) {
        return ApplicationContext
            .build(envs)
            .properties(
                'redis.uri': "redis://$redis.containerIpAddress:${redis.getMappedPort(6379)}",
                'worker.jobs.send-words-job-listen.enabled': 'true',
                'worker.jobs.send-words-job-hello.enabled': 'true'
            )
            .build()
    }

}
