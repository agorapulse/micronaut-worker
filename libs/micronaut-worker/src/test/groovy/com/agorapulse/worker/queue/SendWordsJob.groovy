/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
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
package com.agorapulse.worker.queue

import com.agorapulse.worker.annotation.Job
import io.micronaut.context.annotation.Requires
import io.reactivex.Flowable

import javax.inject.Singleton

@Singleton
@Requires(env = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
class SendWordsJob {

    List<String> words = []

    @Job(initialDelay = '50ms')
    Flowable<String> hello() {
        return Flowable.just('Hello', 'World')
    }

    @Job(fixedRate = '100ms', initialDelay = '100ms')
    void listen(String word) {
        words.add(word)
    }

}
