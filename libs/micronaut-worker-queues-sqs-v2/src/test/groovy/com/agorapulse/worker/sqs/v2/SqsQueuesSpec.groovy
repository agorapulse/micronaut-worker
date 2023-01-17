/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Agorapulse.
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
package com.agorapulse.worker.sqs.v2

import com.agorapulse.worker.tck.queue.AbstractQueuesSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import spock.lang.Shared

@Testcontainers
class SqsQueuesSpec extends AbstractQueuesSpec {

    @SuppressWarnings('GetterMethodCouldBeProperty')
    Class<?> getExpectedImplementation() { return SqsQueues }

    @Shared
    LocalStackContainer localstack = new LocalStackContainer().withServices(LocalStackContainer.Service.SQS)

    @Override
    ApplicationContext buildContext(String[] envs) {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey))
        SqsClient sqs = SqsClient
            .builder()
            .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
            .credentialsProvider(credentialsProvider)
            .region(Region.EU_WEST_1)
            .build()

        return ApplicationContext
            .builder(envs)
            .properties(
                'aws.sqs.auto-create-queue': 'true',
                'worker.jobs.send-words-job-listen.enabled': 'true',
                'worker.jobs.send-words-job-hello.enabled': 'true'
            )
            .build()
            .registerSingleton(SqsClient, sqs)
            .registerSingleton(AwsCredentialsProvider, credentialsProvider)
    }

}
