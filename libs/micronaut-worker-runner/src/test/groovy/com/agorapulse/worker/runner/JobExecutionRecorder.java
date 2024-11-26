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
package com.agorapulse.worker.runner;

import com.agorapulse.worker.event.JobExecutionFinishedEvent;
import com.agorapulse.worker.event.JobExecutionResultEvent;
import com.agorapulse.worker.event.JobExecutionStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class JobExecutionRecorder {

    private final List<JobExecutionStartedEvent> startedEvents = new ArrayList<>();
    private final List<JobExecutionFinishedEvent> finishedEvents = new ArrayList<>();
    private final List<JobExecutionResultEvent> resultEvents = new ArrayList<>();

    @EventListener
    public void onJobStarted(JobExecutionStartedEvent event) {
        startedEvents.add(event);
    }

    @EventListener
    public void onJobFinished(JobExecutionFinishedEvent event) {
        finishedEvents.add(event);
    }

    @EventListener
    public void onJobResult(JobExecutionResultEvent event) {
        resultEvents.add(event);
    }

    public final List<JobExecutionStartedEvent> getStartedEvents() {
        return List.copyOf(startedEvents);
    }

    public final List<JobExecutionFinishedEvent> getFinishedEvents() {
        return List.copyOf(finishedEvents);
    }

    public final List<JobExecutionResultEvent> getResultEvents() {
        return List.copyOf(resultEvents);
    }

    @Override
    public String toString() {
        return "JobExecutionRecorder{startedEvents=%s, finishedEvents=%s, resultEvents=%s}".formatted(startedEvents, finishedEvents, resultEvents);
    }

}
