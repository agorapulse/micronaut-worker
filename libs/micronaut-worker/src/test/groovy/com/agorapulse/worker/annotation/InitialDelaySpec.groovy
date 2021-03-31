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

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
])

@MicronautTest
class InitialDelaySpec extends Specification {

    // tag::job-method[]
    @InitialDelay('10m')
    public void initialDelay() {
        // your code here
    }

    @InitialDelay('10m') @FixedRate('5m')
    public void fixedRate() {
        // your code here
    }

    @InitialDelay('10m') @FixedDelay('15m')
    public void fixedDelay() {
        // your code here
    }
    // end::job-method[]

    @Inject
    JobManager jobManager

    void 'initial delay job is registered'() {
        expect:
            'initial-delay-spec-initial-delay' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-initial-delay').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
    }

    void 'initial delay and fixed rate job is registered'() {
        expect:
            'initial-delay-spec-fixed-rate' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-fixed-rate').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
            job.configuration.fixedRate == Duration.ofMinutes(5)
    }

    void 'initial delay and fixed delay job is registered'() {
        expect:
            'initial-delay-spec-fixed-delay' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-fixed-delay').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
            job.configuration.fixedDelay == Duration.ofMinutes(15)
    }

}
