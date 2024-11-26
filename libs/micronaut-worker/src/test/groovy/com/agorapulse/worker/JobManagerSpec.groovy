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
package com.agorapulse.worker

import com.agorapulse.worker.annotation.FixedRate
import com.agorapulse.worker.annotation.InitialDelay
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

@MicronautTest(environments = MANAGER_SPEC_ENVIRONMENT)
@Property(name = 'worker.jobs.consumer-job.enabled', value = 'true')
@SuppressWarnings('AbcMetric')
class JobManagerSpec extends Specification {

    public static final String MANAGER_SPEC_ENVIRONMENT = 'manager-spec-environment'

    @Inject JobManager manager
    @Inject ConsumerJob consumerJob
    @Inject InLongFutureJob inLongFutureJob

    void 'can register new jobs'() {
        given:
            boolean executed = false
        when:
            manager.register('new-job') {
                enabled true
                concurrency 2
                leaderOnly true
                fixedDelay Duration.ofMinutes(10)
                initialDelay Duration.ofMinutes(1)

                consumer {
                    queueName 'NewQueue'
                    queueType 'local'
                    waitingTime Duration.ofMillis(90)
                    maxMessages 10
                }

                producer {
                    queueName 'AnotherQueue'
                    queueType 'redis'
                }

                task {
                    executed = true
                }
            }
        then:
            !executed

        when:
            Job job = manager.getJob('new-job').get()
        then:
            job.configuration
            job.configuration.enabled
            job.configuration.concurrency == 2
            job.configuration.leaderOnly
            job.configuration.fixedDelay == Duration.ofMinutes(10)
            job.configuration.initialDelay == Duration.ofMinutes(1)
            job.configuration.consumer
            job.configuration.consumer.queueName == 'NewQueue'
            job.configuration.consumer.queueType == 'local'
            job.configuration.consumer.waitingTime == Duration.ofMillis(90)
            job.configuration.consumer.maxMessages == 10
            job.configuration.producer.queueName == 'AnotherQueue'
            job.configuration.producer.queueType == 'redis'

        when:
            manager.run('new-job')
        then:
            executed
    }

    void 'can enqueue'() {
        expect:
            'consumer-job' in manager.jobNames
        when:
            manager.enqueue(ConsumerJob, 'Hello')

            Thread.sleep(200)
        then:
            consumerJob.messages.contains('Hello')
    }

    void 'can force run'() {
        given:
            AtomicBoolean executed = new AtomicBoolean()

            String jobName = 'disabled-job'
            Job job = Job.build(jobName) {
                enabled false
                task {
                    executed.set(true)
                }
            }
        when:
            manager.register job

        and:
            manager.run jobName

        then:
            !executed.get()

        when:
            manager.forceRun jobName
        then:
            executed.get()
    }

    void 'can reconfigure'() {
        expect:
            'in-long-future-job' in manager.jobNames
        when:
            manager.enqueue(InLongFutureJob, 'Hello')

            manager.reconfigure('in-long-future-job') {
                enabled true
                initialDelay Duration.ofMillis(1)
            }

            Thread.sleep(100)
        then:
            inLongFutureJob.messages.contains('Hello')
    }

}

@Singleton
@Requires(env = JobManagerSpec.MANAGER_SPEC_ENVIRONMENT)
class ConsumerJob implements Consumer<String> {

    List<String> messages = []

    @Override
    @FixedRate('10ms')
    void accept(String message) {
        messages << message
    }

}

@Singleton
@Requires(env = JobManagerSpec.MANAGER_SPEC_ENVIRONMENT)
class InLongFutureJob implements Consumer<String> {

    List<String> messages = []

    @Override
    @InitialDelay('24h')
    void accept(String message) {
        messages << message
    }

}
