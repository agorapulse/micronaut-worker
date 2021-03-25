package com.agorapulse.worker;

import io.micronaut.core.naming.NameUtils;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface JobManager {

    /**
     * Registers a new job.
     *
     * @param job the job to be registered
     */
    void register(Job job);

    /**
     * @param name the name of the job
     * @return the job if present
     */
    Optional<Job> getJob(String name);

    /**
     * @return the set of all jobs' names
     */
    Set<String> getJobNames();

    /**
     * Execute the job synchronously.
     *
     * @param jobName the name of the job
     */
    default void run(String jobName) {
        getJob(jobName).ifPresent(Runnable::run);
    }

    /**
     * Sends a message to the job queue to be consumed during one of the next executions.
     *
     * @param jobName the name of the job
     * @param message the message for the job
     */
    void enqueue(String jobName, Object message);

    default void run(Class<?> jobClass) {
        run(NameUtils.hyphenate(jobClass.getSimpleName()));
    }

    default <T> void enqueue(Class<? extends Consumer<? extends T>> jobClass, T message) {
        enqueue(NameUtils.hyphenate(jobClass.getSimpleName()), message);
    }
}
