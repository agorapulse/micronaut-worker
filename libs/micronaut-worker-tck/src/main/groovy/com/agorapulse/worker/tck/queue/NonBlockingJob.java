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
package com.agorapulse.worker.tck.queue;

import com.agorapulse.worker.annotation.FixedRate;
import com.agorapulse.worker.annotation.InitialDelay;
import com.agorapulse.worker.convention.QueueConsumer;
import com.agorapulse.worker.convention.QueueListener;
import com.agorapulse.worker.convention.QueueProducer;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@Requires(env = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
public class NonBlockingJob {

    public record NumberMessage(int number) { }

    private final List<Integer> retrieved = new CopyOnWriteArrayList<>();
    private final List<Integer> ones = new CopyOnWriteArrayList<>();

    private final long delay;

    public NonBlockingJob(@Value("${non-blocking-job.delay:100}") long delay) {
        this.delay = delay;
    }

    @InitialDelay("10ms")
    @QueueProducer("numbers")
    public Publisher<NumberMessage> numbers() {
        return Flux.range(1, 20).map(NumberMessage::new);
    }

    @QueueConsumer(value = "numbers", maxMessages = 20)
    public void consume(NumberMessage message) throws InterruptedException {
        if (message.number() % 3 == 0) {
            Thread.sleep(delay);
        }
        retrieved.add(message.number);
    }

    @FixedRate("10ms")
    @QueueProducer("just-ones")
    public Publisher<NumberMessage> moreNumbers() {
        return Mono.just(new NumberMessage(1));
    }

    @QueueListener(value = "just-ones", initialDelay = "10ms")
    public void consumeOnes(NumberMessage message) {
        ones.add(message.number);
    }


    public List<Integer> getRetrieved() {
        return retrieved;
    }

    public List<Integer> getOnes() {
        return ones;
    }

}
