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
package com.agorapulse.worker.sqs.v1

import com.agorapulse.micronaut.aws.sqs.SimpleQueueService
import com.agorapulse.worker.local.LocalQueues
import com.amazonaws.SdkClientException
import com.amazonaws.auth.AWSCredentialsProvider
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.env.Environment
import spock.lang.Specification

class SqsQueuesFactorySpec extends Specification {

    ObjectMapper mapper = new ObjectMapper()

    AWSCredentialsProvider provider = Mock()
    SimpleQueueService simpleQueueService = Mock()
    Environment environment = Mock()

    SqsQueuesFactory factory = new SqsQueuesFactory()

    void 'create sqs if there is no issue'() {
        expect:
            factory.sqsQueues(provider, mapper, simpleQueueService, Optional.empty(), environment) instanceof SqsQueues
    }

    void 'create local if there is issue'() {
        when:
            factory.sqsQueues(provider, mapper, simpleQueueService, Optional.empty(), environment) instanceof LocalQueues

        then:
            1 * provider.credentials >> { throw new SdkClientException('login failed') }
            1 * environment.activeNames >> [Environment.CLOUD]
    }

}
