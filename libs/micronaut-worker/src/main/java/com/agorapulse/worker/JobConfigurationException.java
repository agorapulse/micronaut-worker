package com.agorapulse.worker;

public class JobConfigurationException extends RuntimeException {

    private final Job job;

    public JobConfigurationException(Job job) {
        this.job = job;
    }

    public JobConfigurationException(Job job, String message) {
        super(message);
        this.job = job;
    }

    public JobConfigurationException(Job job, String message, Throwable cause) {
        super(message, cause);
        this.job = job;
    }

    public JobConfigurationException(Job job, Throwable cause) {
        super(cause);
        this.job = job;
    }

    public JobConfigurationException(Job job, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
