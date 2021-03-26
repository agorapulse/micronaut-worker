package com.agorapulse.worker;

import java.time.Duration;
import java.time.Instant;

public interface JobStatus {

    Instant getLastTriggered();
    Instant getLastFinished();
    Duration getLastDuration();
    Throwable getLastException();
    int getCurrentExecutionCount();

}
