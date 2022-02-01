/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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

import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named

class WorkerConfigurationSpec extends Specification {

    @AutoCleanup ApplicationContext context

    @Inject WorkerConfiguration workerConfiguration
    @Inject @Named('one') JobConfiguration one
    @Inject @Named('two') JobConfiguration two

    void setup() {
        context = ApplicationContext.builder(
            'worker.enabled': true,
            'worker.queue-type': 'foo',
            'worker.jobs.one.consumer.queue-type': 'bar',
            'worker.jobs.two.producer.queue-type': 'bar'
        ).build()

        context.start()
        context.inject(this)
    }

    void 'check configuration'() {
        expect:
            workerConfiguration.enabled
            workerConfiguration.queueType == 'foo'
            one.consumer.queueType == 'bar'
            one.producer.queueType == 'foo'
            two.consumer.queueType == 'foo'
            two.producer.queueType == 'bar'
    }

}
