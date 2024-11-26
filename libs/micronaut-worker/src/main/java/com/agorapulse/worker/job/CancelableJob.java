package com.agorapulse.worker.job;

import com.agorapulse.worker.Job;

public interface CancelableJob extends Job {

    void cancel();

}
