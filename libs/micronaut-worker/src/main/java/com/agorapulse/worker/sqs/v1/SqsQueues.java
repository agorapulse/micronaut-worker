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
package com.agorapulse.worker.sqs.v1;

import com.agorapulse.micronaut.aws.sqs.SimpleQueueService;
import com.agorapulse.worker.queue.JobQueues;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;

import java.time.Duration;
import java.util.function.Consumer;

public class SqsQueues implements JobQueues {

    private final SimpleQueueService simpleQueueService;
    private final ObjectMapper objectMapper;

    public SqsQueues(SimpleQueueService simpleQueueService, ObjectMapper objectMapper) {
        this.simpleQueueService = simpleQueueService;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument, Consumer<T> action) {
        simpleQueueService.receiveMessages(queueName, maxNumberOfMessages, 0, Math.toIntExact(waitTime.getSeconds())).forEach(m -> {
            try {
                action.accept(objectMapper.readValue(m.getBody(), JacksonConfiguration.constructType(argument, objectMapper.getTypeFactory())));
                simpleQueueService.deleteMessage(m.getReceiptHandle());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Cannot convert to " + argument + "from message\n" + m.getBody(), e);
            }
        });
    }

    @Override
    public void sendMessage(String queueName, Object result) {
        try {
            simpleQueueService.sendMessage(queueName, objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal object " + result + " to JSON", e);
        } catch (AmazonSQSException sqsException) {
            if (sqsException.getMessage() != null && sqsException.getMessage().contains("Concurrent access: Queue already exists")) {
                sendMessage(queueName, result);
                return;
            }
            throw sqsException;
        }
    }
}
