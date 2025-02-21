/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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
package com.agorapulse.worker.queue;

public interface QueueMessage<T> {

    /**
     * Creates a new queue message which will be always requeued even if it was not deleted. This is suitable for
     * queue implementations that removed the message from the queue after reading.
     *
     * @param id the id of the message
     * @param message the message
     * @param doDelete the action to delete the message
     * @param doRequeue the action to requeue the message
     * @return the queue message
     * @param <T> the type of the message
     */
    static <T> QueueMessage<T> alwaysRequeue(String id, T message, Runnable doDelete, Runnable doRequeue) {
        return DefaultQueueMessage.alwaysRequeue(id, message, doDelete, doRequeue);
    }

    /**
     * Creates a new queue message which will be requeued only if it was deleted. This is suitable for
     * queue implementations that keep the message in the queue after reading.
     *
     * @param id the id of the message
     * @param message the message
     * @param doDelete the action to delete the message
     * @param doRequeue the action to requeue the message
     * @return the queue message
     * @param <T> the type of the message
     */
    static <T> QueueMessage<T> requeueIfDeleted(String id, T message, Runnable doDelete, Runnable doRequeue) {
        return DefaultQueueMessage.requeueIfDeleted(id, message, doDelete, doRequeue);
    }

    /**
     * Returns the payload of the message.
     * @return the payload of the message
     */
    T getMessage();

    /**
     * Returns the id of the message.
     * @return the id of the message
     */
    String getId();

    /**
     * Deletes the message from the queue.
     */
    void delete();

    /**
     * Requeues the message. Depending on the type of the message it might not be requeued if it was not deleted.
     */
    void requeue();

}
