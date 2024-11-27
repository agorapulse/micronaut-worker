package com.agorapulse.worker.executor.ecs

import com.agorapulse.worker.executor.ExecutorId
import spock.lang.Specification

class EcsExecutorIdFactorySpec extends Specification {

    void 'extract id'() {
        given:
            String id = 'http://169.254.170.2/api/c613006db31d4ac192bbb07b1577c40e-1280352332'
        expect:
            new EcsExecutorIdFactory().ecsExecutorId(id) == new ExecutorId('c613006db31d4ac192bbb07b1577c40e')
    }

}
