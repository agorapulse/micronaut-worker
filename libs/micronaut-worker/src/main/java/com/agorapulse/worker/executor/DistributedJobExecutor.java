package com.agorapulse.worker.executor;

import io.micronaut.validation.Validated;
import org.reactivestreams.Publisher;

import java.util.concurrent.Callable;

@Validated
public interface DistributedJobExecutor {

    /**
     * Executes the tasks only on the leader server.
     *
     * @param jobName       the name of the job
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls to the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R>executeOnlyOnLeader(String jobName, Callable<R> task);

    /**
     * Executes the tasks only if it's not already running.
     *
     * @param jobName       the name of the job
     * @param concurrency   the maximal count of jobs running at the same time
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R> executeConcurrently(String jobName, int concurrency, Callable<R> task);

    /**
     * Executes the tasks only on the follower server.
     *
     * @param jobName       the name of the job
     * @param task          the task to be performed
     * @param <R>           the type of the task's result
     * @return publisher which calls the original supplier or empty publisher if the task should not be executed
     */
    <R> Publisher<R>executeOnlyOnFollower(String jobName, Callable<R> task);

}
