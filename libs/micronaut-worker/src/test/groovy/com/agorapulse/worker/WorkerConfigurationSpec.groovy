package com.agorapulse.worker

import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named

class WorkerConfigurationSpec extends Specification {

    @AutoCleanup ApplicationContext context

    @Inject WorkerConfiguration workerConfiguration
    @Inject @Named('one') JobConfiguration one
    @Inject @Named('two') JobConfiguration two

    void setup() {
        context = ApplicationContext.build(
            'worker.enabled': true,
            'worker.queue-type': 'foo',
            'worker.jobs.one.consumer.queue-type': 'bar',
            'worker.jobs.two.producer.queue-type': 'bar'
        ).build()

        context.start()
        context.inject(this)
    }

    void 'check configuration'() {
        expect:
            workerConfiguration.enabled
            workerConfiguration.queueType == 'foo'
            one.consumer.queueType == 'bar'
            one.producer.queueType == 'foo'
            two.consumer.queueType == 'foo'
            two.producer.queueType == 'bar'
    }

}
