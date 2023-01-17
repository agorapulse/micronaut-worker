/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2023 Agorapulse.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StacktraceSerializer extends StdSerializer<Throwable> {

    public StacktraceSerializer() {
        super(Throwable.class);
    }

    protected StacktraceSerializer(Class<Throwable> t) {
        super(t);
    }

    protected StacktraceSerializer(JavaType type) {
        super(type);
    }

    protected StacktraceSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    protected StacktraceSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(Throwable value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        value.printStackTrace(printWriter);
        gen.writeString(writer.toString());
    }

}
