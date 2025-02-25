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
package com.agorapulse.worker.local

import com.agorapulse.worker.tck.queue.AbstractQueuesSpec
import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(environments = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
@Property(name = 'worker.jobs.send-words-job-listen.enabled', value = 'true')
@Property(name = 'worker.jobs.send-words-job-hello.enabled', value = 'true')
@Property(name = 'worker.jobs.non-blocking-job-numbers.enabled', value = 'true')
@Property(name = 'worker.jobs.non-blocking-job-consume.enabled', value = 'true')
@Property(name = 'worker.jobs.non-blocking-job-more-numbers.enabled', value = 'true')
@Property(name = 'worker.jobs.non-blocking-job-consume-ones.enabled', value = 'true')
class LocalQueuesSpec extends AbstractQueuesSpec {

    @Inject LocalQueues queues

    void 'can easily read messages from queue'() {
        when:
            queues.sendMessage('foo', 'bar')
        then:
            queues.getMessages('foo', Argument.STRING) == ['bar']
    }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getExpectedImplementation() { return LocalQueues }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    String getName() { return 'local' }

}
