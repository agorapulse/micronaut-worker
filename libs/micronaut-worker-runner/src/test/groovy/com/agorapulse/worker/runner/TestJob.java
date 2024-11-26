package com.agorapulse.worker.runner;

import com.agorapulse.worker.annotation.Job;
import io.micronaut.context.BeanContext;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Singleton
public class TestJob {

    public record ExecutionEvent(String job) {}

    private final BeanContext context;

    public TestJob(BeanContext context) {
        this.context = context;
    }

    @Job("test-job-one")
    public void recordingJobOne() {
        context.getEventPublisher(ExecutionEvent.class).publishEvent(new ExecutionEvent("test-job-one"));
    }

    @Job("test-job-two")
    public Flux<String> recordingJobTwo() {
        context.getEventPublisher(ExecutionEvent.class).publishEvent(new ExecutionEvent("test-job-two"));
        return Flux.from(Mono.delay(Duration.ofMillis(300)).map(ignore -> "foo"));
    }

    @Job("test-job-three")
    public void recordingJobThree() {
        context.getEventPublisher(ExecutionEvent.class).publishEvent(new ExecutionEvent("test-job-three"));
        throw new UnsupportedOperationException("This job is supposed to fail");
    }

}
