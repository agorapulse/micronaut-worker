package com.agorapulse.worker.job;

import com.agorapulse.worker.JobRunStatus;
import jakarta.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface JobRunContext {
    static JobRunContext create(JobRunStatus status) {
        return new DefaultJobRunContext(status);
    }

    JobRunContext onMessage(BiConsumer<JobRunStatus, Object> onMessage);

    JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError);

    JobRunContext onFinished(Consumer<JobRunStatus> onFinished);

    JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult);

    void message(@Nullable Object event);

    void error(Throwable error);

    void finished();

    void result(@Nullable Object result);

    JobRunStatus getStatus();
}
