package com.agorapulse.worker.job;

public interface MutableCancelableJob extends CancelableJob {

    /**
     * Updates the action being called when the job is canceled.
     * @param runnable the action to be called when the job is canceled
     */
    void cancelAction(Runnable runnable);

}
