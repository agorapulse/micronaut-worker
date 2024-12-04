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
package com.agorapulse.worker.event;

import com.agorapulse.worker.queue.QueueMessage;
import io.micronaut.core.annotation.Introspected;

import java.util.Optional;

/**
 * Event dispatched after successful job execution.
 */
@Introspected
public class JobExecutionStartedEvent {

    private final String name;
    private final String id;
    private final QueueMessage<?> message;

    public JobExecutionStartedEvent(String name, String id) {
        this.name = name;
        this.id = id;
        this.message = null;
    }

    public JobExecutionStartedEvent(String name, String id, QueueMessage<?> message) {
        this.name = name;
        this.id = id;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Optional<Object> getMessage() {
        return Optional.ofNullable(message).map(QueueMessage::getMessage);
    }

    public Optional<String> getMessageId() {
        return Optional.ofNullable(message).map(QueueMessage::getId);
    }

    @Override
    public String toString() {
        return "JobExecutionStartedEvent{name='%s', id='%s', message=%s}".formatted(name, id, message);
    }

}
