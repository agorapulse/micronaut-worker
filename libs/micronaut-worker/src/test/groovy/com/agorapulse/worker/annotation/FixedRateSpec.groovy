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
package com.agorapulse.worker.annotation

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject
import java.time.Duration

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
    'UnnecessaryPublicModifier',
])

@MicronautTest
@Property(name = 'worker.jobs.fixed-rate-spec.enabled', value = 'true')
class FixedRateSpec extends Specification {

    // tag::job-method[]
    @FixedRate('10m')
    public void job() {
        // your code here
    }
    // end::job-method[]

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'fixed-rate-spec' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('fixed-rate-spec').get()
        then:
            job.configuration.fixedRate == Duration.ofMinutes(10)
    }

}
