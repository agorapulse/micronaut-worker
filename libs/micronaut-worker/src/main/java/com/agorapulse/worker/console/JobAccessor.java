package com.agorapulse.worker.console;

import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.report.JobReport;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class JobAccessor implements Callable<Void>, Consumer<Object> {

    private final String jobName;
    private final JobManager jobManager;

    public JobAccessor(String jobName, JobManager jobManager) {
        this.jobName = jobName;
        this.jobManager = jobManager;
    }

    // groovy DSL sugar
    @Override
    public Void call() {
        jobManager.run(jobName);
        return null;
    }

    @Override
    public void accept(Object o) {
        jobManager.enqueue(jobName, o);
    }

    // groovy DSL sugar
    public void call(Object object) {
        accept(object);
    }

    public String toString() {
        return JobReport.report(jobManager, Collections.singleton(jobName));
    }

}
