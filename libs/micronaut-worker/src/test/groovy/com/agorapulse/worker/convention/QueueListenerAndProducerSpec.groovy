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
package com.agorapulse.worker.convention

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import com.agorapulse.worker.annotation.Cron
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.Duration

@MicronautTest
@SuppressWarnings(['GrUnnecessaryPublicModifier', 'GrUnnecessarySemicolon'])
class QueueListenerAndProducerSpec extends Specification {

    // tag::quickstart[]
    public record Message(String message) { }                                           // <1>

    @Cron('0 0 0/1 ? * *')                                                              // <2>
    @QueueProducer("my-queue")                                                          // <3>
    public Flux<Message> produceToMyQueue() {                                           // <4>
        return Flux.just("Hello", "World").map(Message::new);
    }

    @QueueListener("my-queue")                                                          // <5>
    public void listenToMyQueue(Message message) {                                      // <6>
        // your code here
    }

    @QueueConsumer("my-queue")                                                          // <7>
    public void consumeToMyQueue(Message message) {                                     // <8>
        // your code here
    }
    // end::quickstart[]

    @com.agorapulse.worker.annotation.Job('queue-listener-job')
    @QueueConsumer(value = 'my-queue', maxMessages = 5, waitingTime = '10s')
    public void listenToMyQueueMultiple(Message message) {
        // your code here
    }

    @Named('queue-producer-job')
    @Cron('0 0 0/4 ? * *')
    @QueueProducer('my-queue')
    public Flux<Message> produceToMyQueueMultiple() {
        return Flux.just("Hello", "World").map(Message::new);
    }

    @Named('not-a-consumer-job')
    @QueueConsumer('my-queue')
    public void notAConsumer() {
        // your code here
    }

    @Named('not-a-producer')
    @QueueProducer('my-queue')
    public void notAProducer() {
        // your code here
    }

    @Inject JobManager jobManager

    void 'listener job is registered'() {
        given:
            String jobName = 'queue-listener-and-producer-spec-listen-to-my-queue'
        expect:
            jobName in jobManager.jobNames

        when:
            Job job = jobManager.getJob(jobName).get()
        then:
            job.configuration.consumer.queueName == 'my-queue'
            job.configuration.consumer.maxMessages == Integer.MAX_VALUE
            job.configuration.initialDelay == Duration.ofSeconds(30)
    }

    void 'producer job is registered'() {
        given:
            String jobName = 'queue-listener-and-producer-spec-produce-to-my-queue'
        expect:
            jobName in jobManager.jobNames

        when:
            Job job = jobManager.getJob(jobName).get()
        then:
            job.configuration.producer.queueName == 'my-queue'
            job.configuration.cron == '0 0 0/1 ? * *'
    }

    void 'multiple annotations are composed for consumer'() {
        given:
            String jobName = 'queue-listener-job'
        expect:
            jobName in jobManager.jobNames

        when:
            Job job = jobManager.getJob(jobName).get()
        then:
            verifyAll(job.configuration) {
                fixedRate == Duration.ofSeconds(10)
                consumer.queueName == 'my-queue'
                consumer.maxMessages == 5
                consumer.waitingTime == Duration.ofSeconds(10)
                fork == 5
            }

    }

    void 'multiple annotations are composed for producer'() {
        given:
            String jobName = 'queue-producer-job'
        expect:
            jobName in jobManager.jobNames

        when:
            Job job = jobManager.getJob(jobName).get()
        then:
            verifyAll(job.configuration) {
                cron == '0 0 0/4 ? * *'
                producer.queueName == 'my-queue'
            }
    }

    void 'not a consumer job fails to be configured'() {
        expect:
            jobManager.getJob('not-a-consumer-job').empty
    }

    void 'not a producer job fails to be configured'() {
        expect:
            jobManager.getJob('not-a-producer').empty
    }

}
