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
package com.agorapulse.worker.sqs.v2

import com.agorapulse.micronaut.amazon.awssdk.sqs.SimpleQueueService
import com.agorapulse.worker.queue.JobQueues
import com.agorapulse.worker.queue.QueueMessage
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.core.type.Argument
import reactor.core.publisher.Flux
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.SqsException
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

class SqsQueuesUnitSpec extends Specification {

    private static final String QUEUE_NAME = 'my-queue'
    private static final int MAX_MESSAGES = 2
    private static final Duration WAIT_TIME = Duration.ofSeconds(1)

    @Shared ObjectMapper mapper = new ObjectMapper()

    SimpleQueueService simpleQueueService = Mock()

    JobQueues sqsQueues = new SqsQueues(simpleQueueService, mapper)

    void 'can read legacy messages'() {
        when:
            List<List<Long>> values = Flux.from(
                sqsQueues.readMessages(QUEUE_NAME, MAX_MESSAGES, WAIT_TIME, Argument.listOf(Long))
            ).map(QueueMessage::getMessage).collectList().block()
        then:
            values
            values.size() == 2
            values.first() instanceof List
            values.first()[0] == 1L
            values.last() instanceof List
            values.last()[0] == 3L

            1 * simpleQueueService.receiveMessages(QUEUE_NAME, MAX_MESSAGES, 0, WAIT_TIME.seconds) >> {
                [
                    Message.builder()
                        .body('1,2,3')
                        .receiptHandle('one')
                        .build(),
                    Message.builder()
                        .body('3,2,1')
                        .receiptHandle('two')
                        .build(),
                ]
            }
    }

    void 'can read legacy string messages'() {
        when:
            List<List<String>> values = Flux.from(
                sqsQueues.readMessages(QUEUE_NAME, MAX_MESSAGES, WAIT_TIME, Argument.listOf(String))
            ).map(QueueMessage::getMessage).collectList().block()
        then:
            values
            values.size() == 2
            values.first() instanceof List
            values.first()[0] == 'one'
            values.last() instanceof List
            values.last()[0] == 'three'

            1 * simpleQueueService.receiveMessages(QUEUE_NAME, MAX_MESSAGES, 0, WAIT_TIME.seconds) >> {
                [
                    Message.builder()
                        .body('one,two,three')
                        .receiptHandle('one')
                        .build(),
                    Message.builder()
                        .body('three,two,one')
                        .receiptHandle('two')
                        .build(),
                ]
            }
    }

    void 'message not deleted on error'() {
        when:
            Flux.from(
                sqsQueues.readMessages(QUEUE_NAME, MAX_MESSAGES, WAIT_TIME, Argument.mapOf(String, String))
            ).map(QueueMessage::getMessage).collectList().block()
        then:
            thrown IllegalArgumentException

            1 * simpleQueueService.receiveMessages(QUEUE_NAME, MAX_MESSAGES, 0, WAIT_TIME.seconds) >> {
                [
                    Message.builder()
                        .body('this is not JSON')
                        .receiptHandle('one')
                        .build(),
                ]
            }

            0 * simpleQueueService.deleteMessage(QUEUE_NAME, 'one')
    }

    void 'send message'() {
        when:
            sqsQueues.sendMessage(QUEUE_NAME, [one: '1'])

        then:
            1 * simpleQueueService.sendMessage(QUEUE_NAME, mapper.writeValueAsString(one: '1'))
    }

    void 'retry on concurrent exception'() {
        when:
            sqsQueues.sendMessage(QUEUE_NAME, [one: '1'])

        then:
            1 * simpleQueueService.sendMessage(QUEUE_NAME, _) >> {
                throw SqsException.builder().message('Concurrent access: Queue already exists').build()
            }

            1 * simpleQueueService.sendMessage(QUEUE_NAME, _)
    }

    void 'do not retry on another sqs exceptions'() {
        given:
            String json = mapper.writeValueAsString(one: '1')

        when:
            sqsQueues.sendMessage(QUEUE_NAME, [one: '1'])

        then:
            thrown SqsException
            1 * simpleQueueService.sendMessage(QUEUE_NAME, json) >> {
                throw SqsException.builder().message('Something wrong with the queue').build()
            }
    }

    void 'send wrong message'() {
        given:
            ObjectMapper objectMapper = new ObjectMapper()
            SqsQueues queues = new SqsQueues(simpleQueueService, objectMapper)

        when:
            queues.sendMessage(QUEUE_NAME, new Object())

        then:
            thrown IllegalArgumentException
    }

}
