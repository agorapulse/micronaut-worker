package com.agorapulse.worker.tck.queue;

import com.agorapulse.worker.annotation.InitialDelay;
import com.agorapulse.worker.convention.QueueConsumer;
import com.agorapulse.worker.convention.QueueProducer;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

@Singleton
public class NonBlockingJob {

    public record NumberMessage(int number) { }

    private final List<Integer> retrieved = new CopyOnWriteArrayList<>();

    @InitialDelay("10ms")
    @QueueProducer("numbers")
    public Publisher<NumberMessage> numbers() {
        return Flux.range(1, 20).map(NumberMessage::new);
    }

    @QueueConsumer("numbers")
    public void consume(NumberMessage message) throws InterruptedException {
        System.out.println(message);
        if (message.number() % 3 == 0) {
            Thread.sleep(100);
        }
        retrieved.add(message.number);
    }

    public List<Integer> getRetrieved() {
        return retrieved;
    }

}
