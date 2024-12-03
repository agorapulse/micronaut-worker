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
package com.agorapulse.worker.tck.executor

import com.agorapulse.worker.executor.DistributedJobExecutor
import com.agorapulse.worker.local.LocalQueues
import com.agorapulse.worker.queue.JobQueues
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import reactor.core.publisher.Flux
import spock.lang.Retry
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

abstract class AbstractJobExecutorSpec extends Specification {

    public static final String CONCURRENT_JOB_TEST_ENVIRONMENT = 'concurrent-job-test'

    public static final String JOBS_INITIAL_DELAY = '100ms'
    public static final long LONG_RUNNING_JOB_DURATION = 500

    private final PollingConditions conditions = new PollingConditions(
        timeout: 30,
        initialDelay: 5
    )

    @Retry(count = 10)
    void 'jobs executed appropriate times on three servers'() {
        given:
            LocalQueues queues = LocalQueues.create().tap {
                sendMessages(LongRunningJob.CONCURRENT_CONSUMER_QUEUE_NAME, Flux.range(1, 10).map(Object::toString))
                sendMessages(LongRunningJob.REGULAR_CONSUMER_QUEUE_NAME, Flux.range(1, 10).map(Object::toString))
            }
            ApplicationContext one = buildContext(queues)
            ApplicationContext two = buildContext(queues)
            ApplicationContext three = buildContext(queues)

        expect:
            requiredExecutorType.isInstance(one.getBean(DistributedJobExecutor))

            conditions.eventually {
                LongRunningJob jobOne = one.getBean(LongRunningJob)
                LongRunningJob jobTwo = two.getBean(LongRunningJob)
                LongRunningJob jobThree = three.getBean(LongRunningJob)

                List<LongRunningJob> jobs = [jobOne, jobTwo, jobThree]

                // jobs are unique
                jobs.unique().size() == 3

                // unlimited jobs are executed on every server
                jobs.count { it.unlimited.get() == 1 } == 3

                // forked jobs are executed twice
                jobs.count { it.fork.get() == 2 } == 3

                // concurrent jobs are at most n-times
                jobs.count { it.concurrent.get() == 1 } == 2

                // concurrent consumer jobs should handle (workers * max messages) messages
                List<String> remainingRegularMessages = queues.getMessages(LongRunningJob.REGULAR_CONSUMER_QUEUE_NAME, Argument.STRING)
                remainingRegularMessages.size() == 1
                jobs.consumedRegularMessages.flatten().flatten().size() == 9

                // concurrent consumer jobs should handle (concurrency * max messages) messages
                List<String> remainingConcurrentMessages = queues.getMessages(LongRunningJob.CONCURRENT_CONSUMER_QUEUE_NAME, Argument.STRING)
                remainingConcurrentMessages.size() == 4
                jobs.consumedConcurrentMessages.flatten().flatten().size() == 6

                // leader job is executed only on leader
                jobs.count { it.leader.get() == 1 } == 1

                // follower job is executed only on followers
                jobs.count { it.follower.get() == 1  } == expectedFollowersCount

                //  consecutive job is only executed once on a random server
                jobs.count { it.consecutive.get() == 1 } == 1

                // producer job is executed only on leader
                jobs.count { it.producer.get() == 1 } == 1
            }

            verifyExecutorEvents(one, two, three)

        cleanup:
            closeQuietly one, two, three
    }

    protected abstract ApplicationContext buildContext(JobQueues queues)
    protected abstract Class<?> getRequiredExecutorType()
    protected abstract boolean verifyExecutorEvents(ApplicationContext first, ApplicationContext second, ApplicationContext third)

    // some implementation may not support followers, such as the local implementation
    protected int getExpectedFollowersCount() {
        return 2
    }

    @SuppressWarnings([
        'CatchException',
    ])
    private static void closeQuietly(ApplicationContext... contexts) {
        for (ApplicationContext c : contexts) {
            try {
                if (c.running) {
                    c.close()
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

}
