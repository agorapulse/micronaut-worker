package com.agorapulse.worker;

import com.agorapulse.worker.queue.JobQueues;
import io.micronaut.context.Qualifier;

import java.time.Duration;
import java.time.Instant;

/**
 * Job is a {@link Runnable} with a name.
 */
public interface Job extends Runnable {

    /**
     * @return the name of the job
     */
    default String getName() {
        return getConfiguration().getName();
    }

    /**
     * @return the source of the job, e.g. the class and method name
     */
    String getSource();

    /**
     * @return the qualifier to be used find the appropriate {@link com.agorapulse.worker.queue.JobQueues} bean.
     */
    Qualifier<JobQueues> getJobQueueQualifier();

    JobConfiguration getConfiguration();

    // TODO: move following properties into status object
    Instant getLastTriggered();
    Instant getLastFinished();
    Duration getLastDuration();
    Throwable getLastException();
    int getCurrentExecutionCount();
}
