package com.agorapulse.worker.event;

/**
 * Event dispatched after successful job execution.
 */
public class JobExecutionResultEvent {

    private final String name;
    private final Object result;

    public JobExecutionResultEvent(String name, Object result) {
        this.name = name;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public String getName() {
        return name;
    }
}
