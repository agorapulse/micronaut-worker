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

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.WorkerConfiguration;
import com.agorapulse.worker.annotation.Consumes;
import com.agorapulse.worker.annotation.FixedRate;
import com.agorapulse.worker.annotation.Fork;
import com.agorapulse.worker.annotation.Job;
import io.micronaut.context.annotation.AliasFor;
import jakarta.inject.Named;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Consumes
@Fork(JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES)
@FixedRate(JobConfiguration.ConsumerQueueConfiguration.DEFAULT_WAITING_TIME_STRING)
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface QueueConsumer {

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
     * @return the name of the work queue to consume items from
     */
    @AliasFor(annotation = Consumes.class, member = "value")
    String value();

    /**
     * @return the preferred type of the queue implementation, such as sqs or redis
     */
    @AliasFor(annotation = Consumes.class, member = "type")
    String type() default "";

    /**
     * The time to wait for the next message to be available and also the time to wait for the next run.
     * @return the maximum waiting time as duration string
     */
    @AliasFor(annotation = Consumes.class, member = "waitingTime")
    @AliasFor(annotation = FixedRate.class, member = "value")
    String waitingTime() default JobConfiguration.ConsumerQueueConfiguration.DEFAULT_WAITING_TIME_STRING;

    /**
     * The number of messages to consume and also the number of threads to use to consume the messages.
     * @return the maximum of messages consumed in a single run, defaults to {@link JobConfiguration.ConsumerQueueConfiguration#DEFAULT_MAX_MESSAGES}
     */
    @AliasFor(annotation = Fork.class, member = "value")
    @AliasFor(annotation = Consumes.class, member = "maxMessages")
    int maxMessages() default JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES;

    /**
     * The name of the task executor to use to execute the job. If default value is usd then new scheduled executor
     * is created for each job with the number of threads equal to the fork value.
     *
     * @return The name of a {@link jakarta.inject.Named} bean that is a
     * {@link java.util.concurrent.ScheduledExecutorService} to use to schedule the task
     */
    @AliasFor(annotation = Job.class, member = "scheduler")
    String scheduler() default WorkerConfiguration.DEFAULT_SCHEDULER;

}
