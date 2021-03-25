package com.agorapulse.worker.event;

/**
 * Event dispatched after successful job execution.
 */
public class JobExecutionFinishedEvent {

    private final String name;

    public JobExecutionFinishedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
