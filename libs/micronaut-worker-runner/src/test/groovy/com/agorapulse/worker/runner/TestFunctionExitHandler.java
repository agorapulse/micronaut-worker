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
package com.agorapulse.worker.runner;

import io.micronaut.context.annotation.Primary;
import io.micronaut.function.executor.FunctionExitHandler;
import jakarta.inject.Singleton;

@Primary
@Singleton
public class TestFunctionExitHandler implements FunctionExitHandler {

    private boolean success;
    private Exception error;

    @Override
    public void exitWithError(Exception error, boolean debug) {
        this.error = error;
    }

    @Override
    public void exitWithSuccess() {
        success = true;
    }

    @Override
    public void exitWithNoData() {
        success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }


}
