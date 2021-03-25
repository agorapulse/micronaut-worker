package com.agorapulse.worker.queue;

import io.micronaut.core.type.Argument;

import java.time.Duration;
import java.util.function.Consumer;

public interface JobQueues {

    <T> void readMessages(String queueName, int maxNumberOfMessages, Duration waitTime, Argument<T> argument, Consumer<T> action);
    void sendMessage(String queueName, Object result);

}
