/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2024 Agorapulse.
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
package com.agorapulse.worker.sqs.v1

import com.agorapulse.worker.tck.queue.AbstractQueuesSpec
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest

/**
 * Tests for SQS queues.
 */
@MicronautTest(environments = QUEUE_SPEC_ENV_NAME)
@Property(name = 'aws.sqs.auto-create-queue', value = 'true')
@Property(name = 'worker.jobs.send-words-job-listen.enabled', value = 'true')
@Property(name = 'worker.jobs.send-words-job-hello.enabled', value = 'true')
class SqsQueuesSpec extends AbstractQueuesSpec {

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getExpectedImplementation() { return SqsQueues }

}
