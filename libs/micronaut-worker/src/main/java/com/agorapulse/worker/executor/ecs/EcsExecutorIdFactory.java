package com.agorapulse.worker.executor.ecs;

import com.agorapulse.worker.executor.ExecutorId;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Factory
@Requires(property = "ecs.agent.uri")
public class EcsExecutorIdFactory {

    @Bean
    @Singleton
    public ExecutorId ecsExecutorId(@Value("${ecs.agent.uri}") String ecsAgentUri) {
        return new ExecutorId(extractId(ecsAgentUri));
    }

    private static String extractId(String ecsAgentUri) {
        return ecsAgentUri.substring(ecsAgentUri.lastIndexOf('/') + 1, ecsAgentUri.lastIndexOf("-"));
    }


}
