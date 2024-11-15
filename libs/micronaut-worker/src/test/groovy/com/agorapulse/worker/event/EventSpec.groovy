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
package com.agorapulse.worker.event

import com.agorapulse.worker.JobManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = 'event.producer.job.enabled', value = 'true')
class EventSpec extends Specification {

    @Inject EventProducerJob job
    @Inject JobManager jobs

    void 'flux job sends particular items as results'() {
        given:
            PollingConditions conditions = new PollingConditions()
        when:
            jobs.forceRun('flux-producer')
        then:
            conditions.eventually {
                job.generatedIds.contains('flux')
            }
    }

    void 'simple job sends particular items as results'() {
        given:
            PollingConditions conditions = new PollingConditions()
        when:
            jobs.forceRun('simple-producer')
        then:
            conditions.eventually {
                job.generatedIds.contains('simple')
            }
    }

}
