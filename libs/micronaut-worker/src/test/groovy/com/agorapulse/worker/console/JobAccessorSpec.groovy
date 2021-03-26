/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
