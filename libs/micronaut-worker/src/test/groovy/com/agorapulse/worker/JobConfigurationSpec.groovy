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

import com.agorapulse.worker.configuration.DefaultJobConfiguration
import spock.lang.Specification

import java.time.Duration

class JobConfigurationSpec extends Specification {

    void 'merge configurations'() {
        when:
            JobConfiguration first = createJobConfiguration()

            JobConfiguration second = new DefaultJobConfiguration('first-job')
            second.consumer.queueName = 'prod_FirstQueue'
            second.consumer.waitingTime = Duration.ofMinutes(1)
            second.consumer.queueQualifier = 'sqs'
            second.producer.maxMessages = 5
            second.cron = '0 0 0/2 ? * * *'
            second.scheduler = 'other'
            second.concurrency = 5

            first.mergeWith(second)
        then:
            first.consumer.maxMessages == 5
            first.consumer.queueName == 'prod_FirstQueue'
            first.consumer.waitingTime == Duration.ofMinutes(1)
            first.consumer.queueQualifier == 'sqs'
            first.producer.queueName == 'test_SecondQueue'
            first.producer.maxMessages == 5
            first.cron == '0 0 0/2 ? * * *'
            first.leaderOnly
            first.scheduler == 'other'
            first.concurrency == 5
    }

    void 'disable job'() {
        when:
            JobConfiguration first = createJobConfiguration()
            JobConfiguration second = createJobConfiguration {
                enabled false
            }
            first.mergeWith(second)
        then:
            !first.enabled
    }

    void 'switch to fixed rate'() {
        when:
            JobConfiguration first = createJobConfiguration()
            JobConfiguration second = createJobConfiguration {
                fixedRate Duration.ofMinutes(10)
                initialDelay Duration.ofMinutes(1)
            }
            first.mergeWith(second)
        then:
            !first.cron
            first.fixedRate == Duration.ofMinutes(10)
            first.initialDelay == Duration.ofMinutes(1)
    }

    void 'switch to fixed delay'() {
        when:
            JobConfiguration first = createJobConfiguration()
            JobConfiguration second = createJobConfiguration {
                fixedDelay Duration.ofMinutes(10)
                initialDelay Duration.ofMinutes(1)
            }
            first.mergeWith(second)
        then:
            !first.cron
            first.fixedDelay == Duration.ofMinutes(10)
            first.initialDelay == Duration.ofMinutes(1)
    }

    void 'switch to follower only'() {
        when:
            JobConfiguration first = createJobConfiguration()
            JobConfiguration second = createJobConfiguration {
                followerOnly true
            }
            first.mergeWith(second)
        then:
            first.followerOnly
            !first.leaderOnly
    }

    void 'switch to leader only'() {
        when:
            JobConfiguration first = createJobConfiguration() {
                followerOnly true
            }
            JobConfiguration second = createJobConfiguration {
                leaderOnly true
            }
            first.mergeWith(second)
        then:
            !first.followerOnly
            first.leaderOnly
    }

    private static JobConfiguration createJobConfiguration(
        @DelegatesTo(value = DefaultJobConfiguration, strategy = Closure.DELEGATE_FIRST) Closure<?> adjust = {
            consumer.maxMessages = 5
            consumer.queueName = 'test_FirstQueue'
            producer.waitingTime = Duration.ofSeconds(40)
            producer.queueName = 'test_SecondQueue'
            cron = '0 0 0/1 ? * * *'
            concurrency = 1
            leaderOnly = true
        }
    ) {
        return new DefaultJobConfiguration('first-job').tap(adjust)
    }

}
