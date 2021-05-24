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
package com.agorapulse.worker.redis

import com.agorapulse.worker.tck.executor.AbstractJobExecutorSpec
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
class RedisJobExecutorSpec extends AbstractJobExecutorSpec {

    @Shared
    GenericContainer redis = new GenericContainer('redis:3-alpine').withExposedPorts(6379)

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getRequiredExecutorType() { return RedisJobExecutor }

    @SuppressWarnings('FactoryMethodName')
    protected ApplicationContext buildContext() {
        ApplicationContext ctx = ApplicationContext
                .builder(
                        'redis.uri': "redis://$redis.containerIpAddress:${redis.getMappedPort(6379)}",
                        'worker.jobs.long-running-job-execute-producer.enabled': 'true',
                        'worker.jobs.long-running-job-execute-on-leader.enabled': 'true',
                        'worker.jobs.long-running-job-execute-on-follower.enabled': 'true',
                        'worker.jobs.long-running-job-execute-consecutive.enabled': 'true',
                        'worker.jobs.long-running-job-execute-unlimited.enabled': 'true',
                        'worker.jobs.long-running-job-execute-concurrent.enabled': 'true'
                )
                .environments(CONCURRENT_JOB_TEST_ENVIRONMENT)
                .build()

        ctx.registerSingleton(String, UUID.randomUUID().toString(), Qualifiers.byName(RedisJobExecutor.HOSTNAME_PARAMETER_NAME))

        return ctx.start()
    }

}
