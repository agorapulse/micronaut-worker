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
package com.agorapulse.worker.report;

import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobStatus;

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
                    JobStatus status = j.getStatus();
                    out.println(String.format(
                            TABLE,
                            j.getName(),
                            status.getExecutionCount(),
                            status.getLastTriggered(),
                            status.getLastFinished(),
                            humanReadableFormat(status.getLastDuration())
                    ));
                    if (status.getLastException() != null) {
                        out.println(String.format(BORDER, '-').replace(' ', '-'));
                        status.getLastException().printStackTrace(out);
                    }
                    out.println(String.format(BORDER, '=').replace(' ', '='));
                });
        return writer.toString();
    }

    // https://stackoverflow.com/a/40487511/227419
    public static String humanReadableFormat(Duration duration) {
        if (duration == null) {
            return "";
        }
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
