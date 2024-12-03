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
package com.agorapulse.worker.local;

import com.agorapulse.worker.queue.JobQueues;
import com.agorapulse.worker.queue.QueueMessage;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Secondary
@Singleton
@Named("local")
@Requires(property = "worker.queues.local.enabled", value = "true", defaultValue = "true")
public class LocalQueues implements JobQueues {

    static class LocalQueue {

        private final ConcurrentNavigableMap<String, Object> messages = new ConcurrentSkipListMap<>();
        private final String format = UUID.randomUUID() + "-%015d";
        private final AtomicLong counter = new AtomicLong(1);

        void add(Object message) {
            messages.put(format.formatted(counter.getAndIncrement()), message);
        }

        <T> QueueMessage<T> readMessage(ConversionService env, Argument<T> argument) {
            Map.Entry<String, Object> entry = messages.pollFirstEntry();
            return QueueMessage.alwaysRequeue(
                entry.getKey(),
                env.convertRequired(entry.getValue(), argument),
                () -> messages.remove(entry.getKey()),
                () -> {
                    // ensure the message is removed and add it to the end of the queue
                    messages.remove(entry.getKey());
                    add(entry.getValue());
                }
            );
        }

        boolean isEmpty() {
            return messages.isEmpty();
        }

        Collection<Object> getMessages() {
            return messages.values();
        }

    }

    public static LocalQueues create() {
        return create(ConversionService.SHARED);
    }

    public static LocalQueues create(ConversionService conversionService) {
        return new LocalQueues(conversionService);
    }

    private final ConcurrentMap<String, LocalQueue> queues = new ConcurrentHashMap<>();
    private final ConversionService conversionService;

    public LocalQueues(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public <T> Publisher<QueueMessage<T>> readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument) {
        LocalQueue queue = queues.get(queueName);

        if (queue == null || queue.isEmpty()) {
            return Flux.empty();
        }

        return Flux.generate(() -> maxNumberOfMessages, (state, sink) -> {
            if (queue.isEmpty()) {
                sink.complete();
                return 0;
            }

            if (state > 0) {
                sink.next(queue.readMessage(conversionService, argument));
            }

            if (state == 1) {
                sink.complete();
            }

            return state - 1;
        });
    }

    @Override
    public void sendRawMessage(String queueName, Object result) {
        queues.computeIfAbsent(queueName, key -> new LocalQueue()).add(result);
    }

    public <T> List<T> getMessages(String queueName, Argument<T> type) {
        return List.copyOf(queues.get(queueName).getMessages().stream().map(o -> conversionService.convertRequired(o, type)).toList());
    }

}
