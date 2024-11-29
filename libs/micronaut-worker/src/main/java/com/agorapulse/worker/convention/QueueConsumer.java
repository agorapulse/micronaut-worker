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
import com.agorapulse.worker.annotation.Consumes;
import com.agorapulse.worker.annotation.FixedRate;
import io.micronaut.context.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Consumes
@FixedRate("20s")
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface QueueConsumer {

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
     * @return the maximum waiting time as duration string
     */
    @AliasFor(annotation = Consumes.class, member = "value")
    @AliasFor(annotation = FixedRate.class, member = "value")
    String waitingTime() default "";

    /**
     * @return the maximum of messages consumed in a single run, defaults to {@link JobConfiguration.ConsumerQueueConfiguration#DEFAULT_MAX_MESSAGES}
     */
    @AliasFor(annotation = Consumes.class, member = "value")
    int maxMessages() default JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES;

}
