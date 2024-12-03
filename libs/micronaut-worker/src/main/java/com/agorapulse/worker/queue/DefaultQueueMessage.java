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
package com.agorapulse.worker.queue;

class DefaultQueueMessage<T> implements QueueMessage<T> {

    static <T> DefaultQueueMessage<T> alwaysRequeue(T message, Runnable doDelete, Runnable doRequeue) {
        return new DefaultQueueMessage<>(message, doDelete, doRequeue, true);
    }

    static <T> DefaultQueueMessage<T> requeueIfDeleted(T message, Runnable doDelete, Runnable doRequeue) {
        return new DefaultQueueMessage<>(message, doDelete, doRequeue, true);
    }

    private final T message;
    private final Runnable doDelete;
    private final Runnable doRequeue;
    private final boolean alwaysRequeue;
    private boolean deleted;

    private DefaultQueueMessage(T message, Runnable doDelete, Runnable doRequeue, boolean alwaysRequeue) {
        this.message = message;
        this.doDelete = doDelete;
        this.doRequeue = doRequeue;
        this.alwaysRequeue = alwaysRequeue;
    }

    @Override
    public T getMessage() {
        return message;
    }

    @Override
    public void delete() {
        doDelete.run();
        deleted = true;
    }

    @Override
    public void requeue() {
        if (deleted || alwaysRequeue) {
            doRequeue.run();
        }
    }

}
