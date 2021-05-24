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
package com.agorapulse.worker

import com.agorapulse.worker.console.ConsoleSpec
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.Duration

class JobConfigurationIntegrationSpec extends Specification {

    private static final String JOB_NAME = 'sample-job'

    @AutoCleanup ApplicationContext context

    void 'sample job is not present in the test environment'() {
        expect:
            !(JOB_NAME in manager().jobNames)
    }

    void 'sample job is not present in the function environment'() {
        expect:
            !(JOB_NAME in manager(Environment.FUNCTION).jobNames)
    }

    void 'jobs can be disabled'() {
        expect:
            !(JOB_NAME in manager('disabled').jobNames)
    }

    void 'jobs can be disabled individually'() {
        expect:
            !(JOB_NAME in manager('disabled-individual').jobNames)
    }

    void 'jobs can switch to different type'() {
        given:
            Job job = manager('cron').getJob(JOB_NAME).orElse(null)
        expect:
            job.configuration.cron == '0 0 0/1 ? * *'
            !job.configuration.initialDelay
    }

    void 'jobs can change the concurrency levels'() {
        given:
            JobManager manager = manager('concurrency')
            Job job = manager.getJob(JOB_NAME).orElse(null)
        expect:
            job
            job.configuration.concurrency == 10
            job.configuration.followerOnly
    }

    void 'jobs can change the queue settings'() {
        given:
            JobManager manager = manager('queues')
            Job job = manager.getJob(JOB_NAME).orElse(null)
        expect:
            job
            job.configuration.producer
            job.configuration.producer.queueName == 'Firehose'
            job.configuration.producer.queueType == 'local'
            job.configuration.consumer
            job.configuration.consumer.queueName == 'OtherQueue'
            job.configuration.consumer.queueType == 'local'
            job.configuration.consumer.maxMessages == 100
            job.configuration.consumer.waitingTime == Duration.ofMillis(100)
    }

    private JobManager manager(String... environment) {
        List<String> envs = [ConsoleSpec.CONSOLE_SPEC_ENVIRONMENT]
        envs.addAll(environment)
        context = ApplicationContext.run(envs as String[])
        Thread.sleep(100)
        return context.getBean(JobManager)
    }

}
