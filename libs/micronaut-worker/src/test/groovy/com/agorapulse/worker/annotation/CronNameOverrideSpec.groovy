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
package com.agorapulse.worker.annotation

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@SuppressWarnings([
    'EmptyMethod',
    'GrUnnecessaryPublicModifier',
    'UnnecessaryPublicModifier',
])

@MicronautTest
@Property(name = 'worker.jobs.cron-spec-override.enabled', value = 'true')
class CronNameOverrideSpec extends Specification {

    @Cron(value = '0 0 0/2 ? * *', name = 'cron-spec-override', scheduler = 'io')
    public void job() {
        // your code here
    }

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'cron-spec-override' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('cron-spec-override').get()
        then:
            job.configuration.cron == '0 0 0/2 ? * *'
            job.configuration.scheduler == 'io'
    }

}
