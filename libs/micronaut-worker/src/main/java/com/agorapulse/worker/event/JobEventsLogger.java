package com.agorapulse.worker.event;

import com.agorapulse.worker.Job;
import io.micronaut.runtime.event.annotation.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class JobEventsLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    @EventListener
    void onJobExecutionStarted(JobExecutionStartedEvent event) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting job {}", event.getName());
        }
    }

    @EventListener
    void onJobExecutionResult(JobExecutionResultEvent event) {
        if (LOGGER.isInfoEnabled()) {
            Object result = event.getResult();
            if (result != null) {
                LOGGER.info("Job {} emitted result {}", event.getName(), result);
            } else if (LOGGER.isTraceEnabled()){
                LOGGER.trace("No results emitted from job {}", event.getName());
            }
        }
    }

    @EventListener
    void onJobExecutionFinished(JobExecutionFinishedEvent event) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Finished executing job {} (some results can still be generated asynchronously later)", event.getName());
        }
    }

}
