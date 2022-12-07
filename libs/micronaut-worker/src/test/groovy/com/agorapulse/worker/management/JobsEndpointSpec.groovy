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
package com.agorapulse.worker.management

import com.agorapulse.gru.Gru
import com.agorapulse.worker.annotation.InitialDelay
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(environments = JOBS_ENDPOINT_SPEC_ENVIRONMENT)
@Property(name = 'worker.jobs.sample-job.enabled', value = 'true')
@Property(name = 'endpoints.worker.enabled', value = 'true')
@Property(name = 'endpoints.worker.sensitive', value = 'false')
@Property(name = 'endpoints.routes.enabled', value = 'true')
@Property(name = 'endpoints.routes.sensitive', value = 'false')
class JobsEndpointSpec extends Specification {

    public static final String JOBS_ENDPOINT_SPEC_ENVIRONMENT = 'jobs-endpoint-spec-environment'

    @AutoCleanup @Inject Gru gru

    void 'render routes'() {
        expect:
            gru.test {
                get '/routes'
                expect {
                    json 'routes.json', IGNORING_EXTRA_FIELDS
                }
            }
    }

    void 'render job'() {
        expect:
            gru.test {
                get '/worker'
                expect {
                    json 'jobs.json'
                }
            }
    }

}

@CompileStatic
@Singleton
@SuppressWarnings('EmptyMethod')
@Requires(env = JobsEndpointSpec.JOBS_ENDPOINT_SPEC_ENVIRONMENT)
class SampleJob {

    @InitialDelay('10m')
    void run() {
        // some code here
    }

}
