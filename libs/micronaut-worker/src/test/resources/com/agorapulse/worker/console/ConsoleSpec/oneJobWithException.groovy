import java.time.Duration

jobs.register('failing-job') {
    initialDelay Duration.ofMinutes(10)
    task {
        throw new IllegalArgumentException("Too short")
    }
}

Thread.sleep(100)

jobs.run('failing-job')

Thread.sleep(100)

jobs.getJob('failing-job')
