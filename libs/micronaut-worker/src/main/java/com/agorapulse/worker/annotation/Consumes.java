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

import com.agorapulse.worker.JobConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Consumes {

    /**
     * @return the name of the work queue to consume items from
     */
    String value() default "";

    /**
     * @return the preferred type of the queue implementation, such as sqs or redis
     */
    String type() default "";

    /**
     * @return the maximum waiting time as duration string
     */
    String waitingTime() default "";

    /**
     * @return the maximum of messages consumed in a single run, defaults to {@link JobConfiguration.ConsumerQueueConfiguration#DEFAULT_MAX_MESSAGES}
     */
    int maxMessages() default JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES;

}
