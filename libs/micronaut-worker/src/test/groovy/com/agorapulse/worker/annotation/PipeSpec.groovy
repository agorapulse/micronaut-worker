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
package com.agorapulse.worker.annotation

import com.agorapulse.worker.JobManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
    'UnnecessaryPublicModifier',
    'UnnecessarySemicolon',
    'UnnecessaryGString',
    'PrivateFieldCouldBeFinal',
])

@MicronautTest
@Property(name = 'worker.jobs.pipe-spec-listen-to-upper.enabled', value = 'true')
@Property(name = 'worker.jobs.my-pipe.enabled', value = 'true')
class PipeSpec extends Specification {

    private static List<String> messages = []

    // tag::job-method[]
    @Named("my-pipe")
    @FixedRate("50ms")
    @Consumes("AnyWords")
    @Produces("UpperWords")
    public String pipe(String message) {
        return message.toUpperCase();
    }
    // end::job-method[]

    @FixedRate('50ms')
    @Consumes("UpperWords")
    void listenToUpper(String upper) {
        messages.add(upper)
    }

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'my-pipe' in jobManager.jobNames

        when:
            Thread.sleep(100)

            // tag::enqueue[]
            jobManager.enqueue("my-pipe", "hello");
            // end::enqueue[]

            int counter = 0
            while (!('HELLO' in messages) || counter > 100) {
                Thread.sleep(10)
                counter++
            }
        then:
            'HELLO' in messages
    }

}
