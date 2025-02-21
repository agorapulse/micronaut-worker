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
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(rebuildContext = true)
class JobRunnerSpec extends Specification {

    @Inject ApplicationContext context
    @Inject TestFunctionExitHandler exitHandler
    @Inject JobExecutionRecorder recorder
    @Inject JobManager jobManager

    void 'single job is executed'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-one')
        then:
            'test-job-one' in recorder.finishedEvents*.name

            exitHandler.success
    }

    void 'non-existing job will not hang forever'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-one', 'test-job-zero', 'test-job-hundred')
        then:
            'test-job-one' in recorder.finishedEvents*.name
    }

    void 'runner waits until all events are generated'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-two')
        then:
            'test-job-two' in recorder.finishedEvents*.name

            recorder.resultEvents.any { result -> result.name == 'test-job-two' && result.result == 'foo' }

            exitHandler.success
    }

    void 'job failure is propagated'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-three')
        then:
            'test-job-three' in recorder.finishedEvents*.name

            !exitHandler.success
            exitHandler.error instanceof IllegalStateException
    }

    void 'job generation failure is propagated'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-four')
        then:
            'test-job-four' in recorder.finishedEvents*.name

            !exitHandler.success
            exitHandler.error instanceof IllegalStateException
    }

    void 'consumer job is executed'() {
        when:
            JobRunner runner = new JobRunner(context)
            jobManager.enqueue('test-job-five', 'foo')
            runner.run('test-job-five')
        then:
            'test-job-five' in recorder.finishedEvents*.name

            recorder.startedEvents.any { start -> start.name == 'test-job-five' && start.message.orElse(null) == 'foo' }

            exitHandler.success
    }

    void 'pipe job waits until all messages are produced'() {
        when:
            JobRunner runner = new JobRunner(context)
            jobManager.enqueue('test-job-six', 'Foo')
            runner.run('test-job-six')
        then:
            'test-job-six' in recorder.finishedEvents*.name

            recorder.startedEvents.any { start -> start.name == 'test-job-six' && start.message.orElse(null) == 'Foo' }
            recorder.resultEvents.any { result -> result.name == 'test-job-six' && result.result == 'FOO' }
            recorder.resultEvents.any { result -> result.name == 'test-job-six' && result.result == 'foo' }

            exitHandler.success
    }

    @Property(name = 'worker.jobs.test-job-three.enabled', value = 'true')
    @Property(name = 'worker.jobs.test-job-three.initial-delay', value = '5s')
    void 'only selected jobs are executed ignoring the enabled setting'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-one', 'test-job-two')
        and:
            Thread.sleep(30)
        then:
            'test-job-one' in recorder.finishedEvents*.name
            'test-job-two' in recorder.finishedEvents*.name
            'test-job-three' !in recorder.finishedEvents*.name

            exitHandler.success
    }

}
