package com.agorapulse.worker.sqs

import com.agorapulse.worker.queue.AbstractQueuesSpec
import com.agorapulse.worker.sqs.v1.SqsQueues
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
class SqsQueuesSpec extends AbstractQueuesSpec {

    Class<?> getExpectedImplementation() { SqsQueues }

    @Shared LocalStackContainer localstack = new LocalStackContainer().withServices(LocalStackContainer.Service.SQS)

    @Override
    ApplicationContext buildContext(String[] envs) {
        AmazonSQS sqs = AmazonSQSClientBuilder
                .standard()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS))
                .withCredentials(localstack.defaultCredentialsProvider)
                .build()


        return ApplicationContext.build(envs).properties('aws.sqs.auto-create-queue': 'true').build()
                .registerSingleton(AmazonSQS, sqs)
                .registerSingleton(AWSCredentialsProvider, localstack.defaultCredentialsProvider)
    }

}
