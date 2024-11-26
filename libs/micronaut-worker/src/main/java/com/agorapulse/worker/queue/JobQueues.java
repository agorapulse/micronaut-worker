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
package com.agorapulse.worker.queue;

import io.micronaut.core.type.Argument;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.function.Consumer;

public interface JobQueues {

    /**
     * Reads messages from the queue and processes them with the given action.
     * @param queueName the name of the queue
     * @param maxNumberOfMessages the maximal number of messages to read
     * @param waitTime the maximal time to wait for the messages
     * @param argument the argument type
     * @param action the action to process the message
     * @param <T> the type of the message
     * @deprecated Use {@link #readMessages(String, int, Duration, Argument)} instead
     */
    @Deprecated(forRemoval = true)
    default <T> void readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument, Consumer<T> action) {
        Flux.from(readMessages(queueName, maxNumberOfMessages, waitTime, argument)).subscribe(action::accept);
    }

    <T> Publisher<T> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument);

    default void sendRawMessages(String queueName, Publisher<?> result) {
        Flux.from(result).subscribe(message -> sendRawMessage(queueName, message));
    }

    void sendRawMessage(String queueName, Object result);

    default void sendMessage(String queueName, Object result) {
        sendRawMessage(queueName, result);
    }

    default void sendMessages(String queueName, Publisher<?> result) {
        Flux.from(result).subscribe(message -> sendMessage(queueName, message));
    }

}
