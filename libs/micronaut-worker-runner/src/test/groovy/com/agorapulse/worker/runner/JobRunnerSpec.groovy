package com.agorapulse.worker.runner

import com.agorapulse.worker.JobManager
import io.micronaut.context.ApplicationContext
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

}
