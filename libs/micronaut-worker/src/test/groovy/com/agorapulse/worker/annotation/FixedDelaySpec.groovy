package com.agorapulse.worker.annotation

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration

@MicronautTest
class FixedDelaySpec extends Specification {

    @FixedDelay('10m')
    void job() {
        // your code here
    }

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'fixed-delay-spec' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('fixed-delay-spec').get()
        then:
            job.configuration.fixedDelay == Duration.ofMinutes(10)
    }

}
