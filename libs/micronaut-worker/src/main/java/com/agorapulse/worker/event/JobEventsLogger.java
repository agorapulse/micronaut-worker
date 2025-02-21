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

import com.agorapulse.worker.Job;
import io.micronaut.runtime.event.annotation.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class JobEventsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    @EventListener
    void onJobExecutionStarted(JobExecutionStartedEvent event) {
        if (LOGGER.isDebugEnabled()) {
            Optional<Object> message = event.getMessage();
            if (message.isPresent()) {
                LOGGER.debug("Starting job {}#{} with message {}", event.getName(), event.getId(), message.get());
            } else {
                LOGGER.debug("Starting job {}#{}", event.getName(), event.getId());
            }
        }
    }

    @EventListener
    void onJobExecutionResult(JobExecutionResultEvent event) {
        if (LOGGER.isDebugEnabled()) {
            Object result = event.getResult();
            if (result != null) {
                LOGGER.debug("Job {}#{} emitted result {}", event.getName(), event.getId(), result);
            } else if (LOGGER.isTraceEnabled()){
                LOGGER.trace("No results emitted from job {}#{}", event.getName(), event.getId());
            }
        }
    }

    @EventListener
    void onJobExecutionFinished(JobExecutionFinishedEvent event) {
        if (LOGGER.isErrorEnabled() && event.getStatus().getException() != null) {
            LOGGER.error("Failed to execute job {}#{}", event.getName(), event.getStatus().getId(), event.getStatus().getException());

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finished executing job {}#{} in {}", event.getName(), event.getStatus().getId(), event.getStatus().getHumanReadableDuration());
        }
    }

}
