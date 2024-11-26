package com.agorapulse.worker.runner

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(rebuildContext = true)
class JobRunnerSpec extends Specification {

    @Inject ApplicationContext context
    @Inject TestJob job
    @Inject TestFunctionExitHandler exitHandler
    @Inject JobExecutionRecorder recorder

    void 'single job is executed'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-one')
        then:
            'test-job-one' in recorder.finishedEvents*.name
            'test-job-two' !in recorder.finishedEvents*.name
            'test-job-three' !in recorder.finishedEvents*.name

            exitHandler.success
    }

    void 'runner waits until all events are generated'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-two')
        then:
            'test-job-one' !in recorder.finishedEvents*.name
            'test-job-two' in recorder.finishedEvents*.name
            'test-job-three' !in recorder.finishedEvents*.name

            recorder.resultEvents.any { result -> result.name == 'test-job-two' && result.result == 'foo' }

            exitHandler.success
    }

    void 'job failure is propagated'() {
        when:
            JobRunner runner = new JobRunner(context)
            runner.run('test-job-three')
        then:
            'test-job-one' !in recorder.finishedEvents*.name
            'test-job-two' !in recorder.finishedEvents*.name
            'test-job-three' in recorder.finishedEvents*.name

            !exitHandler.success
            exitHandler.error instanceof IllegalStateException
    }
}
