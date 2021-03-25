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
package com.agorapulse.worker;

public class JobConfigurationException extends RuntimeException {

    private final Job job;

    public JobConfigurationException(Job job) {
        this.job = job;
    }

    public JobConfigurationException(Job job, String message) {
        super(message);
        this.job = job;
    }

    public JobConfigurationException(Job job, String message, Throwable cause) {
        super(message, cause);
        this.job = job;
    }

    public JobConfigurationException(Job job, Throwable cause) {
        super(cause);
        this.job = job;
    }

    public JobConfigurationException(Job job, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
