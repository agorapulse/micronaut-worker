/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
class SqsQueuesSpec extends AbstractQueuesSpec {

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getExpectedImplementation() { return SqsQueues }

    @Shared
    LocalStackContainer localstack = new LocalStackContainer().withServices(LocalStackContainer.Service.SQS)

    @Override
    ApplicationContext buildContext(String[] envs) {
        AmazonSQS sqs = AmazonSQSClientBuilder
            .standard()
            .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS))
            .withCredentials(localstack.defaultCredentialsProvider)
            .build()

        return ApplicationContext
            .builder(envs)
            .properties(
                'aws.sqs.auto-create-queue': 'true',
                'worker.jobs.send-words-job-listen.enabled': 'true',
                'worker.jobs.send-words-job-hello.enabled': 'true'
            )
            .build()
            .registerSingleton(AmazonSQS, sqs)
            .registerSingleton(AWSCredentialsProvider, localstack.defaultCredentialsProvider)
    }

}
