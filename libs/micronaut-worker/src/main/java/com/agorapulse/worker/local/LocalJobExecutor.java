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
