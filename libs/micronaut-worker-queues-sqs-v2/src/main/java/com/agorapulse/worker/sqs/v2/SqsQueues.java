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
package com.agorapulse.worker.sqs.v2;

import com.agorapulse.micronaut.amazon.awssdk.sqs.SimpleQueueService;
import com.agorapulse.worker.convention.QueueListener;
import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.queue.QueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.JacksonConfiguration;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SqsQueues implements JobQueues {

    private static final int MAX_MESSAGES_TO_POLL = 10;
    private static final int MIN_PARALLELISM = 4;
    private static final int MAX_WAITING_TIME = 20;

    private final SimpleQueueService simpleQueueService;
    private final ObjectMapper objectMapper;

    public SqsQueues(SimpleQueueService simpleQueueService, ObjectMapper objectMapper) {
        this.simpleQueueService = simpleQueueService;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Publisher<QueueMessage<T>> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument) {
        Instant pollStart = Instant.now();
        return Flux.<List<Message>, Integer>generate(() -> maxNumberOfMessages, (remainingNumberOfMessages, sink) -> {
                try {
                    List<Message> messages = simpleQueueService.receiveMessages(
                        queueName,
                        Math.min(MAX_MESSAGES_TO_POLL, remainingNumberOfMessages),
                        0,
                        Math.min(MAX_WAITING_TIME, Math.toIntExact(waitTime.getSeconds())));

                    if (messages.isEmpty() && waitTime.getSeconds() <= MAX_WAITING_TIME && !QueueListener.Utils.isInfinitePoll(maxNumberOfMessages, waitTime)) {
                        sink.complete();
                        return 0;
                    }

                    sink.next(messages);

                    if (QueueListener.Utils.isInfinitePoll(maxNumberOfMessages, waitTime)) {
                        return maxNumberOfMessages;
                    }

                    int nextRemaining = remainingNumberOfMessages - messages.size();

                    if (nextRemaining <= 0 || pollStart.plus(waitTime).isBefore(Instant.now())) {
                        sink.complete();
                    }

                    return nextRemaining;
                } catch (Throwable th) {
                    sink.error(th);
                    return 0;
                }
            })
            .flatMap(Flux::fromIterable)
            .flatMap(m ->
                readMessageInternal(queueName, argument, m.body(), true).map(
                    message -> QueueMessage.requeueIfDeleted(
                        m.messageId(),
                        message,
                        () -> simpleQueueService.deleteMessage(queueName, m.receiptHandle()),
                        () -> simpleQueueService.sendMessage(queueName, m.body())
                    )
                )
            )
            .parallel(Math.max(Schedulers.DEFAULT_POOL_SIZE, MIN_PARALLELISM));
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

    private <T> Mono<T> readMessageInternal(String queueName, Argument<T> argument, String body, boolean tryReformat) {
        try {
            return Mono.just(objectMapper.readValue(body, JacksonConfiguration.constructType(argument, objectMapper.getTypeFactory())));
        } catch (JsonProcessingException e) {
            if (tryReformat) {
                if (String.class.isAssignableFrom(argument.getType())) {
                    return Mono.just(argument.getType().cast(body));
                }
                if (Collection.class.isAssignableFrom(argument.getType())) {
                    if (argument.getTypeParameters().length > 0 && CharSequence.class.isAssignableFrom(argument.getTypeParameters()[0].getType())) {
                        String quoted = Arrays.stream(body.split(",\\s*")).map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
                        return readMessageInternal(queueName, argument, "[" + quoted + "]", false);
                    }
                    return readMessageInternal(queueName, argument, "[" + body + "]", false);
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
