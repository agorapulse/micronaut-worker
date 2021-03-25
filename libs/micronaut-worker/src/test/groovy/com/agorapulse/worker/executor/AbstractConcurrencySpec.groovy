package com.agorapulse.worker.executor

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

abstract class AbstractConcurrencySpec extends Specification {

    public static final String CONCURRENT_JOB_TEST_ENVIRONMENT = 'concurrent-job-test'
    public static final String JOBS_INITIAL_DELAY = '100ms'
    public static final long LONG_RUNNING_JOB_DURATION = 500
    public static final long SLEEP_BEFORE_CHECKING = 2000


    void 'jobs executed appropriate times on three servers'() {
        given:
            ApplicationContext one = buildContext(CONCURRENT_JOB_TEST_ENVIRONMENT).start()
            ApplicationContext two = buildContext(CONCURRENT_JOB_TEST_ENVIRONMENT).start()
            ApplicationContext three = buildContext(CONCURRENT_JOB_TEST_ENVIRONMENT).start()

        expect:
            requiredExecutorType.isInstance(one.getBean(DistributedJobExecutor))

        when:
            LongRunningJob jobOne = one.getBean(LongRunningJob)
            LongRunningJob jobTwo = two.getBean(LongRunningJob)
            LongRunningJob jobThree = three.getBean(LongRunningJob)

            Thread.sleep(SLEEP_BEFORE_CHECKING)

            List<LongRunningJob> jobs = [jobOne, jobTwo, jobThree]
        then:
            // unlimited jobs are executed on every server
            jobs.count { it.unlimited.get() == 1 } == 3

            // concurrent jobs are at most n-times
            jobs.count { it.concurrent.get() == 1 } == 2

            // leader job is executed only on leader
            jobs.count {it.leader.get() == 1 } == 1

            // follower job is executed only on followers
            jobs.count { it.follower.get() == 1  } == 2

            //  consecutive job is only executed once on a random server
            jobs.count { it.consecutive.get() == 1 } == 1

            // producer job is executed only on leader
            jobs.count {it.producer.get() == 1 } == 1
        cleanup:
            closeQuietly one, two, three
    }

    protected abstract ApplicationContext buildContext(String... envs)
    protected abstract Class<?> getRequiredExecutorType()

    private static closeQuietly(Closeable... closeable) {
        for (Closeable c : closeable) {
            try {
                c.close()
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

}
