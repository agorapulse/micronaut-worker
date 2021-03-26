package com.agorapulse.worker.job;

import com.agorapulse.worker.JobConfiguration;

import java.util.function.Consumer;

public class SimpleJob extends AbstractJob {

    private final Runnable task;

    public SimpleJob(JobConfiguration configuration, Runnable task) {
        super(configuration);
        this.task = task;
    }

    @Override
    public String getSource() {
        return toString();
    }

    @Override
    protected void doRun(Consumer<Throwable> onError) {
        try {
            task.run();
        } catch (Throwable th) {
            onError.accept(th);
        }
    }
}
