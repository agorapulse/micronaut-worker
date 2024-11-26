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
package com.agorapulse.worker.sqs.v1;

import com.agorapulse.micronaut.aws.sqs.SimpleQueueService;
import com.agorapulse.worker.queue.JobQueues;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class SqsQueues implements JobQueues {

    private final SimpleQueueService simpleQueueService;
    private final ObjectMapper objectMapper;

    public SqsQueues(SimpleQueueService simpleQueueService, ObjectMapper objectMapper) {
        this.simpleQueueService = simpleQueueService;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Publisher<T> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument) {
        return Flux.merge(simpleQueueService.receiveMessages(queueName, maxNumberOfMessages, 0, Math.toIntExact(waitTime.getSeconds())).stream().map(m ->
            readMessageInternal(queueName, argument, m.getBody(), m.getReceiptHandle(), true)
        ).toList());
    }

    @Override
    public void sendMessage(String queueName, Object result) {
        try {
            sendRawMessage(queueName, objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal object " + result + " to JSON", e);
        }
    }

    @Override
    public void sendRawMessage(String queueName, Object result) {
        try {
            simpleQueueService.sendMessage(queueName, result.toString());
        } catch (AmazonSQSException sqsException) {
            if (sqsException.getMessage() != null && sqsException.getMessage().contains("Concurrent access: Queue already exists")) {
                sendMessage(queueName, result);
                return;
            }
            throw sqsException;
        }
    }

    private <T> Mono<T> readMessageInternal(String queueName, Argument<T> argument, String body, String handle, boolean tryReformat) {
        try {
            Mono<T> result = Mono.just(objectMapper.readValue(body, JacksonConfiguration.constructType(argument, objectMapper.getTypeFactory())));
            simpleQueueService.deleteMessage(queueName, handle);
            return result;
        } catch (JsonProcessingException e) {
            if (tryReformat) {
                if (String.class.isAssignableFrom(argument.getType())) {
                    Mono<T> result = Mono.just(argument.getType().cast(body));
                    simpleQueueService.deleteMessage(queueName, handle);
                    return result;
                }
                if (Collection.class.isAssignableFrom(argument.getType())) {
                    if (argument.getTypeParameters().length > 0 && CharSequence.class.isAssignableFrom(argument.getTypeParameters()[0].getType())) {
                        String quoted = Arrays.stream(body.split(",\\s*")).map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
                        return readMessageInternal(queueName, argument, "[" + quoted + "]", handle, false);
                    }
                    return readMessageInternal(queueName, argument, "[" + body + "]", handle, false);
                }
            }
            return Mono.error(new IllegalArgumentException("Cannot convert to " + argument + "from message\n" + body, e));
        }
    }
}
