package com.agorapulse.worker.runner;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.report.JobReport;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.cli.CommandLine;
import io.micronaut.function.executor.FunctionInitializer;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("java:S6813")
public class JobRunner extends FunctionInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

    public static void main(String[] args) throws IOException {
        try (JobRunner runner = new JobRunner()) {
            runner.run(args);
        }
    }

    @Inject
    private JobManager jobManager;

    public JobRunner() {
        super();
    }

    public JobRunner(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    public JobRunner(ApplicationContext applicationContext, boolean inject) {
        super(applicationContext, inject);
    }

    public void run(String[] args) throws IOException {
        CommandLine cli = CommandLine.build().parse(args);

        run(args, ignored -> {
            if (!run(cli.getRemainingArgs())) {
                throw new IllegalStateException("Error running jobs! See the logs for more details.");
            }
            return true;
        });
    }

    @Override
    protected @NonNull ApplicationContextBuilder newApplicationContextBuilder() {
        return super.newApplicationContextBuilder().environments("job");
    }

    private boolean run(List<String> jobNames) {
        if (jobNames.isEmpty()) {
            LOGGER.error("No job name provided");
            return false;
        }

        boolean result = true;

        for (String jobName : jobNames) {
            try {
                Optional<Job> optionalJob = jobManager.getJob(jobName);

                if (optionalJob.isEmpty()) {
                    LOGGER.error("Job '{}' not found", jobName);
                    continue;
                }

                Job job = optionalJob.get();

                job.forceRun();

                waitUntilFinished(job);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Job '{}' executed in {}", jobName, JobReport.humanReadableFormat(job.getStatus().getLastDuration()));
                }

                if (job.getStatus().getLastException() != null) {
                    // the exception is already logged
                    result = false;
                }
            } catch (Exception e) {
                LOGGER.error("Error running job '{}'", jobName, e);

                result = false;
            }
        }

        return result;
    }

    private static void waitUntilFinished(Job job) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (job.getStatus().getLastDuration() != null) {
                scheduler.shutdown();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

}
