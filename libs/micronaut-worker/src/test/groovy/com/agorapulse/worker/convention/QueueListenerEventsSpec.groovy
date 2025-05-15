package com.agorapulse.worker.convention

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import com.agorapulse.worker.tck.event.JobExecutionRecorder
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.Duration

@MicronautTest
@Property(name = 'worker.jobs.queue-listener-events-spec.enabled', value = 'true')
@Property(name = 'worker.jobs.queue-listener-events-spec.initial-delay', value = '1ms')
class QueueListenerEventsSpec extends Specification {

    static record Event(String message) { }

    public static final String JOB_NAME = 'queue-listener-events-spec'
    public static final String QUEUE_NAME = 'events-test'

    @Shared
    private static List<Event> received = []

    @Inject JobManager jobs
    @Inject JobExecutionRecorder recorder

    @QueueListener(QUEUE_NAME)
    void listenToEventsTest(Event event) {
        received << event
    }

    void 'job is enabled'() {
        expect:
            JOB_NAME in jobs.jobNames

        when:
            Job job = jobs.getJob(JOB_NAME).get()
        then:
            job.configuration.enabled
            job.configuration.initialDelay == Duration.ofMillis(1)

            job.configuration.consumer.queueName == QUEUE_NAME
            job.configuration.consumer.maxMessages == Integer.MAX_VALUE
    }

    void 'test execution of events'() {
        given:
            PollingConditions conditions = new PollingConditions(timeout: 10)
            received.clear()

        when:
            jobs.enqueue(JOB_NAME, new Event(message:  'hello'))

        then:
            conditions.eventually {
                received.any { event -> event.message == 'hello' }

                recorder.startedEvents.size() == 1
                recorder.finishedEvents.size() == 1
            }
    }

}
