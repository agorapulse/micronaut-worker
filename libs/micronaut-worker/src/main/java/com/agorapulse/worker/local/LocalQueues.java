package com.agorapulse.worker.local;

import com.agorapulse.worker.queue.JobQueues;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.retry.annotation.Fallback;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Fallback
@Singleton
public class LocalQueues implements JobQueues {

    private final ConcurrentMap<String, ConcurrentLinkedDeque<Object>> queues = new ConcurrentHashMap<>();
    private final ConversionService<?> conversionService;

    public LocalQueues(Optional<ConversionService<?>> conversionService) {
        this.conversionService = conversionService.orElse(ConversionService.SHARED);
    }

    @Override
    public <T> void readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument, Consumer<T> action) {
        ConcurrentLinkedDeque<Object> objects = queues.get(queueName);
        if (objects == null) {
            return;
        }

        for (int i = 0; i < maxNumberOfMessages && !objects.isEmpty(); i++) {
            action.accept(conversionService.convertRequired(objects.removeFirst(), argument));
        }
    }

    @Override
    public void sendMessage(String queueName, Object result) {
        queues.computeIfAbsent(queueName, key -> new ConcurrentLinkedDeque<>()).addLast(result);
    }
}
