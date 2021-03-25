package com.agorapulse.worker.local

import com.agorapulse.worker.queue.AbstractQueuesSpec
import io.micronaut.context.ApplicationContext


class LocalQueuesSpec extends AbstractQueuesSpec {

    Class<?> getExpectedImplementation() { LocalQueues }

    @Override
    ApplicationContext buildContext(String[] envs) {
        return ApplicationContext.build(envs).build()
    }

}
