package com.agorapulse.worker

import com.agorapulse.worker.console.ConsoleSpec
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import java.time.Duration

class JobConfigurationIntegrationSpec extends Specification {

    private static final String JOB_NAME = 'sample-job'

    @AutoCleanup ApplicationContext context

    void 'sample job is present'() {
        expect:
            JOB_NAME in manager().jobNames
    }

    void 'jobs can be disabled'() {
        expect:
            !(JOB_NAME in manager('disabled').jobNames)
    }

    void 'jobs can be disabled individually'() {
        expect:
            !(JOB_NAME in manager('disabled-individual').jobNames)
    }

    void 'jobs can switch to different type'() {
        given:
            Job job = manager('cron').getJob(JOB_NAME).orElse(null)
        expect:
            job.configuration.cron == '0 0 0/1 ? * *'
            !job.configuration.initialDelay
    }

    void 'jobs can change the concurrency levels'() {
        given:
            JobManager manager = manager('concurrency')
            Job job = manager.getJob(JOB_NAME).orElse(null)
        expect:
            job
            job.configuration.concurrency == 10
            job.configuration.followerOnly
    }

    void 'jobs can change the queue settings'() {
        given:
            JobManager manager = manager('queues')
            Job job = manager.getJob(JOB_NAME).orElse(null)
        expect:
            job
            job.configuration.producer
            job.configuration.producer.queueName == 'Firehose'
            job.configuration.producer.queueType == 'local'
            job.configuration.consumer
            job.configuration.consumer.queueName == 'OtherQueue'
            job.configuration.consumer.queueType == 'local'
            job.configuration.consumer.maxMessages == 100
            job.configuration.consumer.waitingTime == Duration.ofMillis(100)
    }

    private JobManager manager(String... environment) {
        List<String> envs = [ConsoleSpec.CONSOLE_SPEC_ENVIRONMENT]
        envs.addAll(environment)
        context = ApplicationContext.run(envs as String[])
        Thread.sleep(100)
        context.getBean(JobManager)
    }

}
