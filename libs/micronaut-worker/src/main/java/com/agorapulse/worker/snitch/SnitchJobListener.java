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
package com.agorapulse.worker.snitch;

import com.agorapulse.micronaut.snitch.SnitchService;
import com.agorapulse.worker.event.JobExecutionFinishedEvent;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Singleton;

@Singleton
@Requires(classes = SnitchService.class)
public class SnitchJobListener {

    private final BeanContext context;

    public SnitchJobListener(BeanContext context) {
        this.context = context;
    }

    @EventListener
    void onJobFinished(JobExecutionFinishedEvent event) {
        context.findBean(SnitchService.class, Qualifiers.byName(event.getName())).ifPresent(SnitchService::snitch);
    }

}
