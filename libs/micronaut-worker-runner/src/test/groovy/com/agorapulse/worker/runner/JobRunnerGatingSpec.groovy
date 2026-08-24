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
import com.agorapulse.worker.tck.event.JobExecutionRecorder
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(rebuildContext = true, environments = JobRunnerGatingSpec.SPEC_ENV)
@Property(name = 'worker.enabled', value = 'true')
@Property(name = 'worker.jobs.runner-gating-consumer.initial-delay', value = '1ms')
class JobRunnerGatingSpec extends Specification {

    public static final String SPEC_ENV = 'job-runner-gating-spec'

    @Inject ApplicationContext context
    @Inject JobManager jobManager
    @Inject RunnerGatingConsumer consumer
    @Inject JobExecutionRecorder recorder

    @Property(name = 'worker.forced-job-names', value = 'runner-gating-target')
    void 'running a single job leaves the other jobs infinite-poll consumers unscheduled'() {
        when: 'a message is waiting on the other consumer and the runner runs only its target job'
            jobManager.enqueue('runner-gating-consumer', 'hello')
            new JobRunner(context).run('runner-gating-target')

        and: 'the consumer has had ample time to poll had it been scheduled'
            Thread.sleep(500)

        then: 'the target ran, but the excluded consumer never started and never drained its queue'
            'runner-gating-target' in recorder.finishedEvents*.name
            'runner-gating-consumer' !in recorder.startedEvents*.name
            consumer.consumed == 0
    }

    void 'without the allow-list every consumer is scheduled and drains its queue'() {
        given:
            PollingConditions conditions = new PollingConditions(timeout: 5)

        when: 'no allow-list is set, so the consumer is scheduled at startup'
            jobManager.enqueue('runner-gating-consumer', 'hello')

        then: 'it drains the message on its own without the runner touching it'
            conditions.eventually {
                consumer.consumed == 1
            }
    }

}
