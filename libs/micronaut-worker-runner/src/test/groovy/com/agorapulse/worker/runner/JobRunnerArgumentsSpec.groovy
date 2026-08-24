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
package com.agorapulse.worker.runner

import spock.lang.Specification

class JobRunnerArgumentsSpec extends Specification {

    void 'the command-line job names become the comma-separated allow-list value'() {
        expect:
            JobRunner.scheduledJobNamesValue(['sample-job', 'other-job'] as String[]) == 'sample-job,other-job'
            JobRunner.scheduledJobNamesValue(['single-job'] as String[]) == 'single-job'
    }

    void 'no job names yields no allow-list, so every enabled job is scheduled as usual'() {
        expect:
            JobRunner.scheduledJobNamesValue([] as String[]) == null
    }

}
