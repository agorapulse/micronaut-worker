package com.agorapulse.worker.console;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobStatus;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConsoleJobManager implements JobManager {

    private final JobManager delegate;

    public ConsoleJobManager(JobManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void register(Job job) {
        delegate.register(job);
    }

    @Override
    public Optional<Job> getJob(String name) {
        return delegate.getJob(name);
    }

    @Override
    public Set<String> getJobNames() {
        return delegate.getJobNames();
    }

    @Override
    public void enqueue(String jobName, Object message) {
        delegate.enqueue(jobName, message);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @JsonValue
    public List<JobStatus> getJobStates() {
        return getJobNames().stream().map(this::getJob).map(Optional::get).map(Job::getStatus).collect(Collectors.toList());
    }

}
