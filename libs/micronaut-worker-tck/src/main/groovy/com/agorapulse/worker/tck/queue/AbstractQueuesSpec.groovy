/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Agorapulse.
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
package com.agorapulse.worker.tck.queue

import com.agorapulse.worker.queue.JobQueues
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.inject.Inject

abstract class AbstractQueuesSpec extends Specification {

    public static final String QUEUE_SPEC_ENV_NAME = 'queue-job-spec'

    @AutoCleanup ApplicationContext context

    @Inject SendWordsJob sendWordsJob

    void setup() {
        context = buildContext(QUEUE_SPEC_ENV_NAME)

        context.start()

        context.inject(this)
    }

    void "jobs are executed"() {
        expect:
            expectedImplementation.isInstance(context.getBean(JobQueues))
        when:
            for (i in 0..<100) {
                if (sendWordsJob.words.size() >= 2) {
                    break
                }
                Thread.sleep(100)
            }
        then:
            sendWordsJob.words.join(' ').startsWith 'Hello World'
    }

    abstract ApplicationContext buildContext(String[] envs)
    abstract Class<?> getExpectedImplementation()

}

