/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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

import com.agorapulse.worker.WorkerConfiguration;
import io.micronaut.context.annotation.AliasFor;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A fixed rate {@link Job}.
 */
@Job
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface FixedRate {

    @AliasFor(annotation = Job.class, member = "fixedRate")
    String value();

    @AliasFor(annotation = Job.class, member = "value")
    @AliasFor(annotation = Named.class, member = "value")
    String name() default "";

    /**
     * The name of the task executor to use to execute the job. If default value is usd then new scheduled executor
     * is created for each job with the number of threads equal to the fork value.
     *
     * @return The name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    @AliasFor(annotation = Job.class, member = "scheduler")
    String scheduler() default  WorkerConfiguration.DEFAULT_SCHEDULER;

    /**
     * @return whether the job contains code that can be executed on virtual threads, e.g. there is no use of <code>synchronized</code> keyword anywhere in the code
     */
    @AliasFor(annotation = Job.class, member = "virtualThreadCompatible")
    boolean virtualThreadCompatible() default WorkerConfiguration.DEFAULT_VIRTUAL_THREAD_COMPATIBLE;

}
