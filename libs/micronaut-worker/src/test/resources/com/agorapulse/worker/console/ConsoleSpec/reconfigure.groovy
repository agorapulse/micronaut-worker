import java.time.Duration

sampleJob.reconfigure {
    enabled true
    initialDelay Duration.ofMillis(1)
}
