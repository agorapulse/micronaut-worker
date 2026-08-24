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
package com.agorapulse.worker.runner

import com.agorapulse.worker.JobManager
import com.agorapulse.worker.WorkerConfiguration
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Specification

class JobRunnerEnvironmentSpec extends Specification {

    @AutoCleanup ApplicationContext context

    void 'the FUNCTION environment survives an explicit micronaut.environments and disables jobs by default'() {
        given: 'the deployment environment is passed exactly as the Dockerfile does (-Dmicronaut.environments)'
            String previous = System.getProperty('micronaut.environments')
            System.setProperty('micronaut.environments', 'beta')

        and: 'a context built the way JobRunner (FunctionInitializer) builds it'
            context = ApplicationContext.builder(Environment.FUNCTION)
                .environments('job')
                .build()
                .start()

        expect: 'the FUNCTION environment is still active alongside beta, not replaced by it'
            context.environment.activeNames.contains(Environment.FUNCTION)
            context.environment.activeNames.contains('beta')

        and: 'so the worker kill switch keeps worker disabled by default'
            !context.getBean(WorkerConfiguration).enabled

        and: 'a job carrying no explicit enabled flag is registered but disabled, so it is never scheduled'
            JobManager jobs = context.getBean(JobManager)
            'test-job-one' in jobs.jobNames
            !jobs.getJob('test-job-one').get().configuration.enabled

        cleanup:
            if (previous == null) {
                System.clearProperty('micronaut.environments')
            } else {
                System.setProperty('micronaut.environments', previous)
            }
    }

}
