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
package com.agorapulse.worker.sqs.v2;

import com.agorapulse.micronaut.amazon.awssdk.sqs.SimpleQueueService;
import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.queue.QueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SqsException;

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
    public <T> Publisher<QueueMessage<T>> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument) {
        return Flux.merge(simpleQueueService.receiveMessages(queueName, maxNumberOfMessages, 0, Math.toIntExact(waitTime.getSeconds())).stream().map(m ->
            readMessageInternal(queueName, argument, m.body(), m.receiptHandle(), true).map(
                message -> QueueMessage.requeueIfDeleted(
                    message,
                    () -> simpleQueueService.deleteMessage(queueName, m.receiptHandle()),
                    () -> simpleQueueService.sendMessage(queueName, m.body())
                )
            )
        ).toList());
    }

    @Override
    public void sendMessage(String queueName, Object result) {
        sendRawMessage(queueName, convertMessageToJson(result));
    }

    @Override
    public void sendRawMessage(String queueName, Object result) {
        try {
            simpleQueueService.sendMessage(queueName, result.toString());
        } catch (SqsException sqsException) {
            if (sqsException.getMessage() != null && sqsException.getMessage().contains("Concurrent access: Queue already exists")) {
                sendMessage(queueName, result);
                return;
            }
            throw sqsException;
        }
    }

    @Override
    public void sendMessages(String queueName, Publisher<?> result) {
        sendRawMessages(queueName, Flux.from(result).map(this::convertMessageToJson));
    }

    @Override
    public void sendRawMessages(String queueName, Publisher<?> result) {
        Flux.from(simpleQueueService.sendMessages(queueName, Flux.from(result).map(String::valueOf))).subscribe();
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

    private String convertMessageToJson(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot marshal object " + result + " to JSON", e);
        }
    }
}
