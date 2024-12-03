/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2024 Agorapulse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.worker.job;

import com.agorapulse.worker.JobRunStatus;
import com.agorapulse.worker.queue.QueueMessage;
import jakarta.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Job context is passed to the job execution and allows to listen to various events during the job execution.
 */
public interface JobRunContext {

    /**
     * Creates a new job run context with the given status.
     * @param status the status of the job
     * @return new job run context
     */
    static JobRunContext create(JobRunStatus status) {
        return new DefaultJobRunContext(status);
    }

    /**
     * Registers a listener for the message event. The listener is called when the message is received from the queue
     * with the given message or with <code>null</code> when the job is not a consumer. The listener is called only once
     * for non-consumer jobs and for each message for the consumer jobs.
     *
     * @param onMessage the listener to be called when the message is received
     * @return this context
     */
    JobRunContext onMessage(BiConsumer<JobRunStatus, QueueMessage<?>> onMessage);

    /**
     * Registers a listener for the error event. The listener is called when the job execution throws an exception. This
     * can happen only once for non-consumer jobs and up to message count for consumer jobs.
     * @param onError the listener to be called when the job execution throws an exception
     * @return this context
     */
    JobRunContext onError(BiConsumer<JobRunStatus, Throwable> onError);

    /**
     * Registers a listener for the finished event. The listener is called when the job execution is finished. This
     * can happen at most once.
     *
     * @param onFinished the listener to be called when the job execution is finished
     * @return this context
     */
    JobRunContext onFinished(Consumer<JobRunStatus> onFinished);

    /**
     * Registers a listener for the result event. The listener is called for every generated result from producer jobs.
     * @param onResult the listener to be called when the job execution generates a result
     * @return this context
     */
    JobRunContext onResult(BiConsumer<JobRunStatus, Object> onResult);

    /**
     * Registers a listener for the executed event. The listener is called when the job execution is executed because
     * there are no restrictions that would prevent the execution.
     *
     * @param onExecuted the listener to be called when the job execution is executed
     * @return this context
     */
    JobRunContext onExecuted(Consumer<JobRunStatus> onExecuted);


    /**
     * Registers a listener for the skipped event. The listener is called when the job execution is skipped because
     * there are restrictions that prevents the execution e.g. concurrency limit or leader/follower restrictions.
     *
     * @param onSkipped the listener to be called when the job execution is skipped
     * @return this context
     */
    JobRunContext onSkipped(Consumer<JobRunStatus> onSkipped);

    /**
     * Signals new incoming message.
     * @param event the message or <code>null</code> if the job is not a consumer
     */
    void message(@Nullable QueueMessage<?> event);

    /**
     * Singals an error during the job execution.
     * @param error the error
     */
    void error(Throwable error);

    /**
     * Signals the job execution is finished.
     */
    void finished();

    /**
     * Signals the job execution produced a result.
     * @param result the result of the job execution
     */
    void result(@Nullable Object result);

    /**
     * Signals the job is executed.
     */
    void executed();

    /**
     * Signals the job is skipped.
     */
    void skipped();

    /**
     * Returns the status of the job.
     * @return the status of the job
     */
    JobRunStatus getStatus();

    /**
     * Creates a copy of the job run context. The copy will still execute all the listeners but the original context
     * won't be affected by any new listeners added to the copy.
     *
     * @param idSuffix the id suffix for the new context
     * @return the copy of the job run context
     */
    JobRunContext createChildContext(String idSuffix);

}
