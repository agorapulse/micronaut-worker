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
package com.agorapulse.worker.groovy;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import io.micronaut.core.naming.NameUtils;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

public class WorkerStaticExtensions {

    @SuppressWarnings("unused")
    public static JobConfiguration build(
        JobConfiguration unused,
        String name,
        @DelegatesTo(value = MutableJobConfiguration.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.configuration.MutableJobConfiguration")
        Closure<?> configuration
    ) {
        return JobConfiguration.create(name, ConsumerWithDelegate.create(configuration));
    }

    public static Job build(
        Job ignored,
        String name,
        @DelegatesTo(value = JobBuilder.class, strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = SimpleType.class, options = "com.agorapulse.worker.groovy.JobBuilder")
            Closure<?> builder
    ) {
        if (!NameUtils.isHyphenatedLowerCase(name)) {
            name = NameUtils.hyphenate(name);
        }
        JobBuilder jobBuilder = new JobBuilder(name);
        ConsumerWithDelegate.create(builder).accept(jobBuilder);
        return jobBuilder.build();
    }
}
