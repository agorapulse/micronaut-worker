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
package com.agorapulse.worker.tck.queue

import com.agorapulse.worker.annotation.FixedRate
import com.agorapulse.worker.annotation.InitialDelay
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton
import reactor.core.publisher.Flux

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
    'UnnecessaryPublicModifier',
    'UnnecessarySemicolon',
    'UnnecessaryGString',
])

@Singleton
@CompileStatic
@Requires(env = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
class SendWordsJob {

    List<String> words = []

    // tag::simple-producer-method[]
    @InitialDelay("50ms")
    public Flux<String> hello() {
        return Flux.just("Hello", "World");
    }
    // end::simple-producer-method[]

    // tag::simple-consumer-method[]
    @FixedRate("100ms") @InitialDelay("100ms")
    public void listen(String word) {
        words.add(word);
    }
    // end::simple-consumer-method[]

}
