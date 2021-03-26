package com.agorapulse.worker.annotation

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class CronSpec extends Specification {

    @Cron('0 0 0/1 ? * * *')
    void job() {
        // your code here
    }

    @Inject
    JobManager jobManager

    void 'job is registered'() {
        expect:
            'cron-spec' in jobManager.jobNames

        when:
            Job job = jobManager.getJob('cron-spec').get()
        then:
            job.configuration.cron == '0 0 0/1 ? * * *'
    }

}
