package com.agorapulse.worker.runner

import com.agorapulse.worker.event.JobExecutionFinishedEvent
import com.agorapulse.worker.event.JobExecutionResultEvent
import com.agorapulse.worker.event.JobExecutionStartedEvent
import groovy.transform.CompileStatic
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton

@Singleton
@CompileStatic
class JobExecutionRecorder {

    final List<JobExecutionStartedEvent> startedEvents = []
    final List<JobExecutionFinishedEvent> finishedEvents = []
    final List<JobExecutionResultEvent> resultEvents = []

    @EventListener
    void onJobStarted(JobExecutionStartedEvent event) {
        startedEvents.add(event)
    }

    @EventListener
    void onJobFinished(JobExecutionFinishedEvent event) {
        finishedEvents.add(event)
    }

    @EventListener
    void onJobResult(JobExecutionResultEvent event) {
        resultEvents.add(event)
    }



}
