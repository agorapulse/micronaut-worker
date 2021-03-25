/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Agorapulse.
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
package com.agorapulse.worker.local;

import com.agorapulse.worker.executor.DistributedJobExecutor;
import io.micronaut.retry.annotation.Fallback;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Fallback
@Singleton
public class LocalJobExecutor implements DistributedJobExecutor {

    private final ConcurrentMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    @Override
    public <R> Publisher<R> executeOnlyOnLeader(String jobName, Callable<R> supplier) {
        return Maybe.fromCallable(supplier).toFlowable();
    }

    @Override
    public <R> Publisher<R> executeConcurrently(String jobName, int concurrency, Callable<R> supplier) {
        int originalCount = counts.computeIfAbsent(jobName, s -> new AtomicInteger(0)).get();
        if (originalCount >= concurrency) {
            return Flowable.empty();
        }

        return Flowable.generate(e -> {
            try {
                counts.get(jobName).incrementAndGet();
                R result = supplier.call();
                if (result != null) {
                    e.onNext(result);
                }
                counts.get(jobName).decrementAndGet();
                e.onComplete();
            } catch (Exception ex) {
                e.onError(ex);
            }
        });
    }

    @Override
    public <R> Publisher<R> executeOnlyOnFollower(String jobName, Callable<R> supplier) {
        return Maybe.fromCallable(supplier).toFlowable();
    }

}
