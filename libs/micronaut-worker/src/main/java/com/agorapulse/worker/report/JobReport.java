package com.agorapulse.worker.report;

import com.agorapulse.worker.JobManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public class JobReport {

    private static final String BORDER = "|%-142s|";
    private static final String TABLE = "| %-50s | %-8s | %-25s | %-25s | %-20s |";

    public static String report(JobManager manager) {
        return report(manager, manager.getJobNames());
    }

    public static String report(JobManager manager, Set<String> jobNames) {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);

        out.println(String.format(BORDER, '=').replace(' ', '='));
        out.println(String.format(TABLE, "Name", "Running", "Last Triggered", "Last Finished", "Took"));
        out.println(String.format(BORDER, '=').replace(' ', '='));
        jobNames.stream()
                .map(manager::getJob)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(j -> {
                    out.println(String.format(
                            TABLE,
                            j.getName(),
                            j.getCurrentExecutionCount(),
                            j.getLastTriggered(),
                            j.getLastFinished(),
                            humanReadableFormat(j.getLastDuration())
                    ));
                    if (j.getLastException() != null) {
                        out.println(String.format(BORDER, '-').replace(' ', '-'));
                        j.getLastException().printStackTrace(out);
                    }
                    out.println(String.format(BORDER, '=').replace(' ', '='));
                });
        return writer.toString();
    }

    // https://stackoverflow.com/a/40487511/227419
    private static String humanReadableFormat(Duration duration) {
        if (duration == null) {
            return "";
        }
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
