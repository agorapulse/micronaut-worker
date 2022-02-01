/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
    'UnnecessaryPublicModifier',
])

@MicronautTest
@Property(name = 'worker.jobs.cron-spec.enabled', value = 'true')
class CronSpec extends Specification {

    // tag::job-method[]
    @Cron('0 0 0/1 ? * *')
    public void job() {
        // your code here
    }
    // end::job-method[]

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'cron-spec' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('cron-spec').get()
        then:
            job.configuration.cron == '0 0 0/1 ? * *'
    }

}
