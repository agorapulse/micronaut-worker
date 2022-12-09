/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 Agorapulse.
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
import io.micronaut.context.ApplicationContext
import spock.lang.Retry
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

abstract class AbstractJobExecutorSpec extends Specification {

    public static final String CONCURRENT_JOB_TEST_ENVIRONMENT = 'concurrent-job-test'

    public static final String JOBS_INITIAL_DELAY = '100ms'
    public static final long LONG_RUNNING_JOB_DURATION = 500

    private final PollingConditions conditions = new PollingConditions(
        timeout: 10,
        initialDelay: 1
    )

    @Retry(count = 10)
    void 'jobs executed appropriate times on three servers'() {
        given:
            ApplicationContext one = buildContext()
            ApplicationContext two = buildContext()
            ApplicationContext three = buildContext()

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

                // leader job is executed only on leader
                jobs.count { it.leader.get() == 1 } == 1

                // follower job is executed only on followers
                jobs.count { it.follower.get() == 1  } == expectedFollowersCount

                //  consecutive job is only executed once on a random server
                jobs.count { it.consecutive.get() == 1 } == 1

                // producer job is executed only on leader
                jobs.count { it.producer.get() == 1 } == 1
            }

        cleanup:
            closeQuietly one, two, three
    }

    protected abstract ApplicationContext buildContext()
    protected abstract Class<?> getRequiredExecutorType()

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
