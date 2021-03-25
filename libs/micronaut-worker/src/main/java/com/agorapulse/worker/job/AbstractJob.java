package com.agorapulse.worker.job;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractJob implements Job {

    private final AtomicInteger executionCount = new AtomicInteger(0);

    private final JobConfiguration configuration;

    private final AtomicReference<Instant> lastTriggered = new AtomicReference<>();
    private final AtomicReference<Instant> lastFinished = new AtomicReference<>();
    private final AtomicReference<Duration> lastDuration = new AtomicReference<>();
    private final AtomicReference<Throwable> lastException = new AtomicReference<>();

    protected AbstractJob(JobConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public JobConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public final Instant getLastTriggered() {
        return lastTriggered.get();
    }

    @Override
    public final Instant getLastFinished() {
        return lastFinished.get();
    }

    @Override
    public final Throwable getLastException() {
        return lastException.get();
    }

    @Override
    public final int getCurrentExecutionCount() {
        return executionCount.get();
    }

    @Override
    public final Duration getLastDuration() {
        return lastDuration.get();
    }

    @Override
    public final void run() {
        Instant start = Instant.now();
        executionCount.incrementAndGet();
        lastTriggered.set(start);

        try {
            doRun();
        } finally {
            executionCount.decrementAndGet();
            Instant finish = Instant.now();
            lastFinished.set(finish);
            lastDuration.set(Duration.between(start, finish));
        }

    }

    protected abstract void doRun();

    protected void setLastException(Throwable th) {
        lastException.set(th);
    }

}
