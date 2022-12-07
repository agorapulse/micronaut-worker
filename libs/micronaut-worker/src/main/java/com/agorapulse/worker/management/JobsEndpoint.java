/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
package com.agorapulse.worker.management;

import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.console.ConsoleJobManager;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.management.endpoint.EndpointConfiguration;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;

@Endpoint(
    value = "worker",
    defaultSensitive = true,
    defaultEnabled = true
)
@Requires(classes = EndpointConfiguration.class)
public class JobsEndpoint {

    private final JobManager manager;

    public JobsEndpoint(JobManager manager) {
        this.manager = manager;
    }

    @Read
    JobManager getJobs() {
        return new ConsoleJobManager(manager);
    }

}
