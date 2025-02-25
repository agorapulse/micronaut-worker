package com.agorapulse.worker.tck.queue;

import com.agorapulse.worker.annotation.InitialDelay;
import com.agorapulse.worker.convention.QueueConsumer;
import com.agorapulse.worker.convention.QueueProducer;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@Requires(env = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
public class NonBlockingJob {

    public record NumberMessage(int number) { }

    private final List<Integer> retrieved = new CopyOnWriteArrayList<>();
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

    public List<Integer> getRetrieved() {
        return retrieved;
    }

}
