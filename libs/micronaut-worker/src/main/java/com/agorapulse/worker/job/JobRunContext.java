package com.agorapulse.worker.job;

import com.agorapulse.worker.JobRunStatus;
import jakarta.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JobRunContext {
    public static JobRunContext create(JobRunStatus status) {
        return new JobRunContext(status);
    }

    private final JobRunStatus status;

    private BiConsumer<JobRunStatus, Object> onMessage = (aStatus, m) -> { };
    private BiConsumer<JobRunStatus, Throwable> onError = (aStatus, e) -> { };
    private Consumer<JobRunStatus> onFinished = s -> { };
    private BiConsumer<JobRunStatus, Object> onResult = (aStatus, r) -> { };

    public JobRunContext(JobRunStatus status) {
        this.status = status;
    }

    public JobRunContext onMessage(BiConsumer<JobRunStatus, Object> onMessage) {
        this.onMessage = this.onMessage.andThen(onMessage);
        return this;
    }

    public JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError) {
        this.onError = this.onError.andThen(onError);
        return this;
    }

    public JobRunContext onFinished(Consumer<JobRunStatus> onFinished) {
        this.onFinished = this.onFinished.andThen(onFinished);
        return this;
    }

    public JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult) {
        this.onResult = this.onResult.andThen(onResult);
        return this;
    }

    public void message(@Nullable Object event) {
        onMessage.accept(status, event);
    }

    public void error(Throwable error) {
        onError.accept(status, error);
    }

    public void finished(JobRunStatus status) {
        onFinished.accept(status);
    }

    public void result(@Nullable Object result) {
        onResult.accept(status, result);
    }

}
