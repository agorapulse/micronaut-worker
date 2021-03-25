package com.agorapulse.worker.redis

import com.agorapulse.worker.executor.AbstractConcurrencySpec
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared

@Testcontainers
class RedisJobSpec extends AbstractConcurrencySpec {

    @Shared
    GenericContainer redis = new GenericContainer('redis:3-alpine').withExposedPorts(6379)

    Class<?> getRequiredExecutorType() { RedisJobExecutor }

    protected ApplicationContext buildContext(String... envs) {
        ApplicationContext ctx = ApplicationContext
                .builder(
                        'redis.host': redis.containerIpAddress,
                        'redis.port': redis.getMappedPort(6379),
                )
                .environments(envs)
                .build()

        ctx.registerSingleton(String, UUID.randomUUID().toString(), Qualifiers.byName(RedisJobExecutor.HOSTNAME_PARAMETER_NAME))

        return ctx
    }

}
