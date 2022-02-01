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
package com.agorapulse.worker.console

import com.agorapulse.gru.Gru
import com.agorapulse.worker.annotation.InitialDelay
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(environments = CONSOLE_SPEC_ENVIRONMENT)
@Property(name = 'worker.jobs.sample-job.enabled', value = 'true')
class ConsoleSpec extends Specification {

    public static final String CONSOLE_SPEC_ENVIRONMENT = 'console-spec-environment'

    @AutoCleanup @Inject Gru gru

    void 'fetch variables'() {
        expect:
            gru.test {
                get '/console/dsl/text'
                expect {
                    text 'variables.txt'
                }
            }
    }

    void 'fetch gdsl'() {
        expect:
            gru.test {
                get '/console/dsl/gdsl'
                expect {
                    text 'console.gdsl'
                }
            }
    }

    void 'display jobs'() {
        expect:
            gru.test {
                post '/console/execute/result', {
                    content('listJobs.groovy', 'text/groovy')
                }
                expect {
                    text 'listJobsResponse.txt'
                }
            }
    }

    void 'render jobs'() {
        expect:
            gru.test {
                post '/console/execute', {
                    content('listJobs.groovy', 'text/groovy')
                }
                expect {
                    json 'listJobsResponse.json'
                }
            }
    }

    void 'display job'() {
        expect:
            gru.test {
                post '/console/execute/result', {
                    content('oneJob.groovy', 'text/groovy')
                }
                expect {
                    text 'oneJobResponse.txt'
                }
            }
    }

    void 'render job'() {
        expect:
            gru.test {
                post '/console/execute', {
                    content('oneJob.groovy', 'text/groovy')
                }
                expect {
                    json 'oneJobResponse.json'
                }
            }
    }

    void 'render failing job'() {
        expect:
            gru.test {
                post '/console/execute', {
                    content('oneJobWithException.groovy', 'text/groovy')
                }
                expect {
                    json 'oneJobWithException.json'
                }
            }
    }

}

@Singleton
@SuppressWarnings('EmptyMethod')
@Requires(env = ConsoleSpec.CONSOLE_SPEC_ENVIRONMENT)
class SampleJob {

    @InitialDelay('10m')
    void run() {
        // some code here
    }

}
