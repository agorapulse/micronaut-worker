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


import com.agorapulse.worker.annotation.FixedDelay;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Singleton
@Refreshable
@Requires(property = "event.producer.job.enabled", value = "true")
public class EventProducerJob implements ApplicationEventListener<RefreshEvent> {

    private final List<String> generatedIds = new ArrayList<>();

    @EventListener
    public void onResult(JobExecutionResultEvent event) {
        generatedIds.add(event.getResult().toString());
    }

    @FixedDelay(value = "1d", name = "flux-producer")
    public Flux<String> fluxProducer() {
        return Flux.just("flux");
    }

    @FixedDelay(value = "1d", name = "simple-producer")
    public String simpleProducer() {
        return "simple";
    }

    public List<String> getGeneratedIds() {
        return generatedIds;
    }

    @Override
    public void onApplicationEvent(RefreshEvent event) {
        generatedIds.clear();
    }

}
