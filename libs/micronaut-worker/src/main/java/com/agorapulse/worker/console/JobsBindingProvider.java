/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
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
package com.agorapulse.worker.console;

import com.agorapulse.micronaut.console.BindingProvider;
import com.agorapulse.worker.JobManager;
import io.micronaut.core.naming.NameUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JobsBindingProvider implements BindingProvider {

    private final JobManager jobManager;

    public JobsBindingProvider(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public Map<String, ?> getBinding() {
        Map<String, Object> bindings = new HashMap<>();

        bindings.put("jobs", new ConsoleJobManager(jobManager));

        jobManager.getJobNames().forEach(n -> bindings.put(NameUtils.camelCase(n), new JobAccessor(n, jobManager)));

        return bindings;
    }


}
