package com.agorapulse.worker.redis;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Factory
public class HostnameFactory {

    @Bean
    @Singleton
    @Named(RedisJobExecutor.HOSTNAME_PARAMETER_NAME)
    public String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            return "localhost";
        }
    }

}
