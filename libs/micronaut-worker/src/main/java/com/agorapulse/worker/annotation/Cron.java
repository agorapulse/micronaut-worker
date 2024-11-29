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

import com.agorapulse.worker.WorkerConfiguration;
import io.micronaut.context.annotation.AliasFor;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A cron {@link Job}
 */
@Job
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Cron {

    @AliasFor(annotation = Job.class, member = "cron")
    String value();

    @AliasFor(annotation = Job.class, member = "value")
    @AliasFor(annotation = Named.class, member = "value")
    String name() default "";

    /**
     * @return The name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    @AliasFor(annotation = Job.class, member = "scheduler")
    String scheduler() default WorkerConfiguration.DEFAULT_SCHEDULER;

}
