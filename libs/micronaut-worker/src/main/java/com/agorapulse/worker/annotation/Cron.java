package com.agorapulse.worker.annotation;

import io.micronaut.context.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Distributed version of {@link io.micronaut.scheduling.annotation.Scheduled}.
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Cron {

    @AliasFor(member = "cron", annotation = Job.class)
    String value();

}
