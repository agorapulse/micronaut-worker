package com.agorapulse.worker.queue

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

