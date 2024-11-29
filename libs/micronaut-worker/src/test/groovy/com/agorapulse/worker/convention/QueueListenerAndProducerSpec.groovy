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
package com.agorapulse.worker.convention

import com.agorapulse.worker.Job
import com.agorapulse.worker.JobManager
import com.agorapulse.worker.annotation.Cron
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
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

    @QueueConsumer("my-queue")                                                          // <5>
    public void listenToMyQueue(Message message) {                                      // <6>
        // your code here
    }
    // end::quickstart[]

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
            job.configuration.consumer.maxMessages == 10
            job.configuration.fixedRate == Duration.ofSeconds(20)
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

}
