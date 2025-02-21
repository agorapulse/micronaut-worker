/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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
