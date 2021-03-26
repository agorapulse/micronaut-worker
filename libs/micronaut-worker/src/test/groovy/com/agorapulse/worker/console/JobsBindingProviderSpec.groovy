package com.agorapulse.worker.console

import com.agorapulse.worker.JobManager
import spock.lang.Specification

class JobsBindingProviderSpec extends Specification {

    JobManager manager = Mock()

    void 'add bindings'() {
        when:
            Map<String, ?> bindings = new JobsBindingProvider(manager).getBinding()
        then:
            bindings.jobs instanceof JobManager
            bindings.testJob instanceof JobAccessor

            1 * manager.jobNames >> ['test-job']
    }
}
