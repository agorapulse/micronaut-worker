package com.agorapulse.worker.console


import com.agorapulse.worker.JobManager
import spock.lang.Specification

class JobAccessorSpec extends Specification {

    JobManager jobManager = Mock()
    JobAccessor accessor = new JobAccessor('test-job', jobManager)

    void 'call accessor'() {
        when:
            accessor()
        then:
            1 * jobManager.run('test-job')
    }

    void 'call accessor with argument'() {
        when:
            accessor 'foo'
        then:
            1 * jobManager.enqueue('test-job', 'foo')
    }

}
