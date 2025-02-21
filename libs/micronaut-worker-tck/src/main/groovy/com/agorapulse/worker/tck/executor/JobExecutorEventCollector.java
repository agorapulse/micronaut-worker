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
package com.agorapulse.worker.tck.executor;

import com.agorapulse.worker.event.JobExecutionFinishedEvent;
import com.agorapulse.worker.event.JobExecutorEvent;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshEventListener;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Primary
@Singleton
@Refreshable
public class JobExecutorEventCollector implements ApplicationEventPublisher<JobExecutorEvent>, RefreshEventListener {

    private final List<JobExecutorEvent> events = new ArrayList<>();
    private final List<JobExecutionFinishedEvent> finishedEvents = new ArrayList<>();

    /**
     * For usage from micronaut tests.
     * @param event the event to be collected
     */
    @EventListener
    void onEvent(JobExecutorEvent event) {
        events.add(event);
    }

    @EventListener
    void onFinishedEvent(JobExecutionFinishedEvent finishedEvent) {
        finishedEvents.add(finishedEvent);
    }

    /**
     * For direct usage in unit tests
     * @param event The event to publish
     */
    @Override
    public void publishEvent(@NonNull JobExecutorEvent event) {
        events.add(event);
    }

    @Override
    public void onApplicationEvent(RefreshEvent event) {
        events.clear();
    }

    public List<JobExecutorEvent> getEvents() {
        return events;
    }

    public List<JobExecutionFinishedEvent> getFinishedEvents() {
        return finishedEvents;
    }

    @Override
    public @NonNull Set<String> getObservedConfigurationPrefixes() {
        return Set.of();
    }

}
