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
package com.agorapulse.worker.tck.queue

import com.agorapulse.worker.queue.JobQueues
import com.agorapulse.worker.queue.QueueMessage
import io.micronaut.context.ApplicationContext
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.core.type.Argument
import io.micronaut.inject.qualifiers.Qualifiers
import reactor.core.publisher.Flux
import spock.lang.Specification

import jakarta.inject.Inject
import spock.util.concurrent.PollingConditions

import java.time.Duration

/**
 * Abstract specification for testing queues.
 */
abstract class AbstractQueuesSpec extends Specification {

    public static final String QUEUE_SPEC_ENV_NAME = 'queue-job-spec'

    @Inject ApplicationContext context
    @Inject SendWordsJob sendWordsJob
    @Inject NonBlockingJob nonBlockingJob

    void "jobs are executed"() {
        expect:
            expectedImplementation.isInstance(context.getBean(JobQueues))
        when:
            for (i in 0..<500) {
                if (sendWordsJob.words.size() >= 2) {
                    break
                }
                Thread.sleep(100)
            }
        then:
            sendWordsJob.words.join(' ').startsWith 'Hello World'
    }

    void 'can send raw messages to queue'() {
        given:
            JobQueues queues = context.getBean(JobQueues, Qualifiers.byName(name))
        when:
            queues.sendRawMessage('foo', 'one')
            queues.sendRawMessages('foo', Publishers.just('two'))
        and:
            List<String> messages = Flux.from(queues.readMessages('foo', 2, Duration.ofSeconds(1), Argument.STRING))
                .map(QueueMessage::getMessage)
                .collectList().block()
        then:
            messages == ['one', 'two']
    }

    void 'long running consumer jobs are not blocking the slow ones messages'() {
        given:
            PollingConditions pollingConditions = new PollingConditions(timeout: 30)

        expect:
            pollingConditions.eventually {
                nonBlockingJob.retrieved.size() == 20
            }

            nonBlockingJob.retrieved
            nonBlockingJob.retrieved.last() % 3 == 0
    }

    void 'can use infinite polling'() {
        given:
            PollingConditions pollingConditions = new PollingConditions(timeout: 30)

        expect:
            pollingConditions.eventually {
                nonBlockingJob.ones.size() >= 30
            }
    }

    abstract Class<?> getExpectedImplementation()
    abstract String getName()

}

