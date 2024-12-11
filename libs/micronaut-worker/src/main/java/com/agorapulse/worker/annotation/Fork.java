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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Sets the number of jobs running in parallel on a one server.
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Fork {
    /**
     * @return the number of jobs running in parallel on a one server
     */
    int value();

    /**
     * @return whether the job contains code that can be executed on virtual threads, e.g. there is no use of <code>synchronized</code> keyword anywhere in the code
     */
    boolean virtualThreadCompatible() default WorkerConfiguration.DEFAULT_VIRTUAL_THREAD_COMPATIBLE;
}
