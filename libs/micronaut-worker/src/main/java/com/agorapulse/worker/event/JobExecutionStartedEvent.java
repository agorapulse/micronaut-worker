package com.agorapulse.worker.event;

/**
 * Event dispatched after successful job execution.
 */
public class JobExecutionStartedEvent {

    private final String name;

    public JobExecutionStartedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
