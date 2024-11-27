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
package com.agorapulse.worker.executor.ecs;

import com.agorapulse.worker.executor.ExecutorId;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Factory
@Requires(property = "ecs.agent.uri")
public class EcsExecutorIdFactory {

    @Bean
    @Singleton
    public ExecutorId ecsExecutorId(@Value("${ecs.agent.uri}") String ecsAgentUri) {
        return new ExecutorId(extractId(ecsAgentUri));
    }

    private static String extractId(String ecsAgentUri) {
        return ecsAgentUri.substring(ecsAgentUri.lastIndexOf('/') + 1, ecsAgentUri.lastIndexOf("-"));
    }


}
