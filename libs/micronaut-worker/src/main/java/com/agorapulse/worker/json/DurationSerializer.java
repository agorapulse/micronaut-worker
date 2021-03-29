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
