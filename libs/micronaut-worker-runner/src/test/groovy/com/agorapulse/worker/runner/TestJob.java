package com.agorapulse.worker.runner;

import com.agorapulse.worker.annotation.Job;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Singleton
public class TestJob {

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

}
