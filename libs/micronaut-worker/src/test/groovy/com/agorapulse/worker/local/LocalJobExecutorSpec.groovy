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
package com.agorapulse.worker.local

import com.agorapulse.worker.event.JobExecutorEvent
import com.agorapulse.worker.executor.ExecutorId
import com.agorapulse.worker.tck.executor.AbstractJobExecutorSpec
import io.micronaut.context.ApplicationContext
import io.micronaut.context.event.ApplicationEventPublisher
import spock.lang.Shared

import java.util.concurrent.Executors

class LocalJobExecutorSpec extends AbstractJobExecutorSpec {

    @Shared ApplicationEventPublisher<JobExecutorEvent> publisher = Mock()

    LocalJobExecutor executor = new LocalJobExecutor(
        Executors.newFixedThreadPool(10),
        publisher,
        new ExecutorId('test')
    )

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getRequiredExecutorType() { return LocalJobExecutor }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    int getExpectedFollowersCount() { return 3 }

    @SuppressWarnings('FactoryMethodName')
    protected ApplicationContext buildContext() {
        ApplicationContext ctx = ApplicationContext
            .builder(CONCURRENT_JOB_TEST_ENVIRONMENT)
            .properties(
                'worker.jobs.long-running-job-execute-producer.enabled': 'true',
                'worker.jobs.long-running-job-execute-on-leader.enabled': 'true',
                'worker.jobs.long-running-job-execute-on-follower.enabled': 'true',
                'worker.jobs.long-running-job-execute-consecutive.enabled': 'true',
                'worker.jobs.long-running-job-execute-unlimited.enabled': 'true',
                'worker.jobs.long-running-job-execute-concurrent.enabled': 'true',
                'worker.jobs.long-running-job-execute-fork.enabled': 'true'
            )
            .build()
            // register the same executor service to emulate concurrency
            .registerSingleton(LocalJobExecutor, executor)

        return ctx.start()
    }

}
