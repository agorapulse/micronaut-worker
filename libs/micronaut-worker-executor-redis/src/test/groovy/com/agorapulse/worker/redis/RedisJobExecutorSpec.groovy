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
package com.agorapulse.worker.redis

import com.agorapulse.worker.event.JobExecutionFinishedEvent
import com.agorapulse.worker.event.JobExecutorEvent
import com.agorapulse.worker.executor.ExecutorId
import com.agorapulse.worker.queue.JobQueues
import com.agorapulse.worker.tck.executor.AbstractJobExecutorSpec
import com.agorapulse.worker.tck.executor.JobExecutorEventCollector
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.GenericContainer
import spock.lang.Retry
import spock.lang.Shared

@Retry(delay = 500)
class RedisJobExecutorSpec extends AbstractJobExecutorSpec {

    @Shared
    GenericContainer redis = new GenericContainer('redis:3-alpine').withExposedPorts(6379)

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getRequiredExecutorType() { return RedisJobExecutor }

    @SuppressWarnings('FactoryMethodName')
    protected ApplicationContext buildContext(JobQueues queues) {
        if (!redis.running) {
            redis.start()
        }

        ApplicationContext ctx = ApplicationContext
                .builder(
                        'redis.uri': "redis://$redis.host:${redis.getMappedPort(6379)}"
                )
                .environments(CONCURRENT_JOB_TEST_ENVIRONMENT)
                .build()

        ctx.registerSingleton(new ExecutorId(UUID.randomUUID().toString()))
        ctx.registerSingleton(JobQueues, queues)

        return ctx.start()
    }

    @Override
    @SuppressWarnings('UnnecessaryCollectCall')
    protected boolean verifyExecutorEvents(
        ApplicationContext first,
        ApplicationContext second,
        ApplicationContext third
    ) {
        List<JobExecutorEvent> allEvents = [first, second, third].collect {
            it.getBean(JobExecutorEventCollector)
        }.collectMany { it.events }

        List<JobExecutionFinishedEvent> allFinishedEvents = [first, second, third].collect {
            it.getBean(JobExecutorEventCollector)
        }.collectMany { it.finishedEvents }

        assert allEvents.every { it.executor == 'redis' }

        List<JobExecutorEvent> leaderEvents = allEvents.findAll { it.status.name == 'long-running-job-execute-on-leader' }
        List<JobExecutionFinishedEvent> leaderFinishedEvents = allFinishedEvents.findAll { it.status.name == 'long-running-job-execute-on-leader' }

        assert leaderEvents.count { it.execution == JobExecutorEvent.Execution.SKIP } == 2
        assert leaderEvents.count { it.execution == JobExecutorEvent.Execution.EXECUTE } == 1
        assert leaderFinishedEvents.sum { it.status.executionCount } == 1

        List<JobExecutorEvent> producerEvents = allEvents.findAll { it.status.name == 'long-running-job-execute-producer' }
        List<JobExecutionFinishedEvent> producerFinishedEvents = allFinishedEvents.findAll { it.status.name == 'long-running-job-execute-producer' }

        assert producerEvents.count { it.execution == JobExecutorEvent.Execution.SKIP } == 2
        assert producerEvents.count { it.execution == JobExecutorEvent.Execution.EXECUTE } == 1
        assert producerFinishedEvents.sum { it.status.executionCount } == 1

        return true
    }

}
