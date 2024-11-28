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
package com.agorapulse.worker.redis

import com.agorapulse.worker.event.JobExecutorEvent
import com.agorapulse.worker.executor.ExecutorId
import com.agorapulse.worker.tck.executor.AbstractJobExecutorSpec
import com.agorapulse.worker.tck.executor.JobExecutorEventCollector
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Retry
import spock.lang.Shared

@Retry(delay = 500)
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
                        'redis.uri': "redis://$redis.host:${redis.getMappedPort(6379)}",
                        'worker.jobs.long-running-job-execute-producer.enabled': 'true',
                        'worker.jobs.long-running-job-execute-on-leader.enabled': 'true',
                        'worker.jobs.long-running-job-execute-on-follower.enabled': 'true',
                        'worker.jobs.long-running-job-execute-consecutive.enabled': 'true',
                        'worker.jobs.long-running-job-execute-unlimited.enabled': 'true',
                        'worker.jobs.long-running-job-execute-concurrent.enabled': 'true',
                        'worker.jobs.long-running-job-execute-fork.enabled': 'true'
                )
                .environments(CONCURRENT_JOB_TEST_ENVIRONMENT)
                .build()

        ctx.registerSingleton(new ExecutorId(UUID.randomUUID().toString()))

        return ctx.start()
    }

    @Override
    @SuppressWarnings('UnnecessaryCollectCall')
    protected boolean verifyExecutorEvents(
        ApplicationContext first,
        ApplicationContext second,
        ApplicationContext third
    ) {
        List<JobExecutorEvent> allEvents = [first, second, third].collect { it.getBean(JobExecutorEventCollector) }.collectMany { it.events }
        assert allEvents.every { it.executor == 'redis' }

        List<JobExecutorEvent> leaderEvents = allEvents.findAll { it.status.name == 'long-running-job-execute-on-leader' }

        assert leaderEvents.count { it.execution == JobExecutorEvent.Execution.SKIP } == 2
        assert leaderEvents.count { it.execution == JobExecutorEvent.Execution.EXECUTE } == 1

        List<JobExecutorEvent> producerEvents = allEvents.findAll { it.status.name == 'long-running-job-execute-producer' }

        assert producerEvents.count { it.execution == JobExecutorEvent.Execution.SKIP } == 2
        assert producerEvents.count { it.execution == JobExecutorEvent.Execution.EXECUTE } == 1

        return true
    }

}
