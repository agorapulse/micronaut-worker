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
