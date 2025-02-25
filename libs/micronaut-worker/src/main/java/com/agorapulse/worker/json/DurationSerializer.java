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
package com.agorapulse.worker.json;

import com.agorapulse.worker.report.JobReport;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

    public DurationSerializer() {
        super(Duration.class);
    }

    protected DurationSerializer(Class<Duration> t) {
        super(t);
    }

    protected DurationSerializer(JavaType type) {
        super(type);
    }

    protected DurationSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    protected DurationSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(JobReport.humanReadableFormat(value));
    }

}
