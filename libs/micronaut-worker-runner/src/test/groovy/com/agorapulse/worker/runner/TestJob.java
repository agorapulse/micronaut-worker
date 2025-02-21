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
package com.agorapulse.worker.runner;

import com.agorapulse.worker.annotation.Job;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;

@Singleton
public class TestJob {

    @Job("testJobZero")
    public void recordingJobZero() {
        // do nothing
    }

    @Job("test-job-one")
    public void recordingJobOne() {
        // do nothing
    }

    @Job("test-job-two")
    public Flux<String> recordingJobTwo() {
        return Flux.from(Mono.delay(Duration.ofMillis(300)).map(ignore -> "foo"));
    }

    @Job("test-job-three")
    public void recordingJobThree() {
        throw new UnsupportedOperationException("This job is supposed to fail");
    }

    @Job("test-job-four")
    public Flux<String> recordingJobFour() {
        return Flux.error(new UnsupportedOperationException("This job is supposed to fail"));
    }

    @Job("test-job-five")
    public void recordingJobFive(String ignored) {
        // do nothing
    }

    @Job("test-job-six")
    public Flux<String> recordingJobSix(String input) {
        return Flux.just(input.toUpperCase(Locale.ROOT), input.toLowerCase(Locale.ROOT));
    }

}
