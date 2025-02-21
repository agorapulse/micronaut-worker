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
package com.agorapulse.worker

import com.agorapulse.worker.job.DefaultJobRunStatus
import spock.lang.Specification

import java.time.Duration

class JobRunStatusSpec extends Specification {

    void 'duration is safe to compute'() {
        when:
            DefaultJobRunStatus status = DefaultJobRunStatus.create('test')
        then:
            status.duration == Duration.ZERO
            status.durationMillis == 0

        when:
            status.finish()
        then:
            status.duration > Duration.ZERO
            status.durationMillis > 0
    }

}
