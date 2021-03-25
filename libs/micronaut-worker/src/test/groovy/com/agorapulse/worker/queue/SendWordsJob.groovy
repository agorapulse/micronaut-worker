package com.agorapulse.worker.queue

import com.agorapulse.worker.annotation.Job
import io.micronaut.context.annotation.Requires
import io.reactivex.Flowable

import javax.inject.Singleton

@Singleton
@Requires(env = AbstractQueuesSpec.QUEUE_SPEC_ENV_NAME)
class SendWordsJob {

    List<String> words = []

    @Job(initialDelay = '50ms')
    Flowable<String> hello() {
        return Flowable.just('Hello', 'World')
    }

    @Job(fixedRate = '100ms', initialDelay = '100ms')
    void listen(String word) {
        words.add(word)
    }

}
