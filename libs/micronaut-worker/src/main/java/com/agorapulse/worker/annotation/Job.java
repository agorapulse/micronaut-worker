/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2024 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.worker.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Parallel;
import io.micronaut.core.annotation.EntryPoint;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;

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
@Executable(processOnStartup = true)
@Parallel
@EntryPoint
public @interface Job {

    /**
     * Allows to override the default name of the job which is <code>JobClassName</code> if there is only one executable
     * method (e.g. job definition) in the class or <code>JobClassName-methodName</code> if there is more then one executable method in the class.
     *
     * Either the job name specified here or the default name is converted using {@link io.micronaut.core.naming.NameUtils#hyphenate(String)}.
     *
     * @return the name of the job used for configuration
     */
    @AliasFor(annotation = Named.class, member = "value")
    String value() default "";


    /**
     * @return The CRON expression
     */
    String cron() default "";

    /**
     * A String representation of the {@link java.time.Duration} between the time of the last execution and the
     * beginning of the next. For example 10m == 10 minutes
     *
     * @return The fixed delay
     */
    String fixedDelay() default "";

    /**
     * A String representation of the {@link java.time.Duration} before starting executions. For example
     * 10m == 10 minutes
     *
     * @return The fixed delay
     */
    String initialDelay() default "";

    /**
     * A String representation of the {@link java.time.Duration} between executions. For example 10m == 10 minutes
     *
     * @return The fixed rate
     */
    String fixedRate() default "";

    /**
     * @return The name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    String scheduler() default TaskExecutors.SCHEDULED;

}
