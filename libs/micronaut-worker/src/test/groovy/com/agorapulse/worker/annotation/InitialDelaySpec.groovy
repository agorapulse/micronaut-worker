package com.agorapulse.worker.annotation

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import java.time.Duration

@MicronautTest
class InitialDelaySpec extends Specification {

    @InitialDelay('10m')
    void initialDelay() {
        // your code here
    }

    @InitialDelay('10m') @FixedRate('5m')
    void fixedRate() {
        // your code here
    }

    @InitialDelay('10m') @FixedDelay('15m')
    void fixedDelay() {
        // your code here
    }

    @Inject
    JobManager jobManager

    void 'initial delay job is registered'() {
        expect:
            'initial-delay-spec-initial-delay' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-initial-delay').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
    }

    void 'initial delay and fixed rate job is registered'() {
        expect:
            'initial-delay-spec-fixed-rate' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-fixed-rate').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
            job.configuration.fixedRate == Duration.ofMinutes(5)
    }

    void 'initial delay and fixed delay job is registered'() {
        expect:
            'initial-delay-spec-fixed-delay' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('initial-delay-spec-fixed-delay').get()
        then:
            job.configuration.initialDelay == Duration.ofMinutes(10)
            job.configuration.fixedDelay == Duration.ofMinutes(15)
    }

}
