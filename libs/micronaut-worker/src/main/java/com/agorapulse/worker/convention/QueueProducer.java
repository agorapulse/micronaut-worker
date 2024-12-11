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
package com.agorapulse.worker.convention;

import com.agorapulse.worker.WorkerConfiguration;
import com.agorapulse.worker.annotation.Job;
import com.agorapulse.worker.annotation.Produces;
import io.micronaut.context.annotation.AliasFor;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Conventional annotation for queue producer combines {@link Job} and {@link Produces} annotations.
 * <p>
 * At least one of the {@link Job#cron()}, {@link Job#fixedDelay()}, {@link Job#initialDelay()} or {@link Job#fixedRate()}
 * must be specified either in the annotation or in the configuration to make the job executable.
 */
@Job
@Produces
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface QueueProducer {

    /**
     * Allows to override the default name of the job which is <code>JobClassName</code> if there is only one executable
     * method (e.g. job definition) in the class or <code>JobClassName-methodName</code> if there is more then one executable method in the class.
     * <p>
     * Either the job name specified here or the default name is converted using {@link io.micronaut.core.naming.NameUtils#hyphenate(String)}.
     *
     * @return the name of the job used for configuration
     */
    @AliasFor(annotation = Named.class, member = "value")
    String name() default "";

    /**
     * @return the name of the work queue to produce items to
     */
    @AliasFor(annotation = Produces.class, member = "value")
    String value() default "";

    /**
     * @return the preferred type of the queue implementation, such as sqs or redis
     */
    @AliasFor(annotation = Produces.class, member = "type")
    String type() default "";


    /**
     * @return The CRON expression
     */
    @AliasFor(annotation = Job.class, member = "cron")
    String cron() default "";

    /**
     * A String representation of the {@link java.time.Duration} between the time of the last execution and the
     * beginning of the next. For example 10m == 10 minutes
     *
     * @return The fixed delay
     */
    @AliasFor(annotation = Job.class, member = "fixedDelay")
    String fixedDelay() default "";

    /**
     * A String representation of the {@link java.time.Duration} before starting executions. For example
     * 10m == 10 minutes
     *
     * @return The fixed delay
     */
    @AliasFor(annotation = Job.class, member = "initialDelay")
    String initialDelay() default "";

    /**
     * A String representation of the {@link java.time.Duration} between executions. For example 10m == 10 minutes
     *
     * @return The fixed rate
     */
    @AliasFor(annotation = Job.class, member = "fixedRate")
    String fixedRate() default "";

    /**
     * The name of the task executor to use to execute the job. If default value is usd then new scheduled executor
     * is created for each job with the number of threads equal to the fork value.
     *
     * @return The name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    @AliasFor(annotation = Job.class, member = "scheduler")
    String scheduler() default WorkerConfiguration.DEFAULT_SCHEDULER;

    /**
     * @return whether the job contains code that can be executed on virtual threads, e.g. there is no use of <code>synchronized</code> keyword anywhere in the code
     */
    @AliasFor(annotation = Job.class, member = "virtualThreadCompatible")
    boolean virtualThreadCompatible() default WorkerConfiguration.DEFAULT_VIRTUAL_THREAD_COMPATIBLE;


}
