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
package com.agorapulse.worker.scheduling

import com.agorapulse.worker.JobManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(rebuildContext = true, environments = ScheduledJobNamesSpec.SPEC_ENV)
@Property(name = 'worker.enabled', value = 'true')
@Property(name = 'worker.scheduled-job-names', value = 'gating-included-job')
class ScheduledJobNamesSpec extends Specification {

    public static final String SPEC_ENV = 'scheduled-job-names-spec'

    @Inject JobManager jobManager
    @Inject GatingIncludedJob includedJob
    @Inject GatingExcludedJob excludedJob

    void 'only the allow-listed job is scheduled; the excluded one is registered but never runs'() {
        given:
            PollingConditions conditions = new PollingConditions(timeout: 10)

        expect: 'both jobs are registered with the manager'
            'gating-included-job' in jobManager.jobNames
            'gating-excluded-job' in jobManager.jobNames

        when: 'the context has been running for a while'
            conditions.eventually {
                includedJob.executions >= 3
            }

        then: 'the excluded job was never scheduled, so it never executed'
            excludedJob.executions == 0
    }

    @Property(name = 'worker.scheduled-job-names', value = 'gating-included-job,gating-excluded-job')
    void 'a comma-separated allow-list schedules every listed job'() {
        given:
            PollingConditions conditions = new PollingConditions(timeout: 10)

        expect:
            conditions.eventually {
                includedJob.executions >= 3
                excludedJob.executions >= 3
            }
    }

}
