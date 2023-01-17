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
package com.agorapulse.worker.sqs.v1;

import com.agorapulse.micronaut.aws.sqs.SimpleQueueService;
import com.agorapulse.worker.local.LocalQueues;
import com.agorapulse.worker.queue.JobQueues;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

@Factory
@Requires(classes = { SimpleQueueService.class }, beans = { SimpleQueueService.class, AWSCredentialsProvider.class })
public class SqsQueuesFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsQueuesFactory.class);

    @Bean
    @Singleton
    @Named("sqs")
    public JobQueues sqsQueues(
            AWSCredentialsProvider provider,
            ObjectMapper mapper,
            SimpleQueueService service,
            Environment environment
    ) {
        try {
            provider.getCredentials();
            return new SqsQueues(service, mapper);
        } catch (SdkClientException e) {
            if (environment.getActiveNames().contains(Environment.CLOUD)) {
                LOGGER.warn("AWS SDK is not authenticated correctly, Using local job queues", e);
            } else if (LOGGER.isInfoEnabled()) {
                LOGGER.info("AWS SDK is not authenticated correctly, Using local job queues");
            }
            return new LocalQueues(environment);
        }
    }

}
