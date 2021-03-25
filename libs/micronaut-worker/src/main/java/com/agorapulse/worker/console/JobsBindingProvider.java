package com.agorapulse.worker.console;

import com.agorapulse.micronaut.console.BindingProvider;
import com.agorapulse.worker.JobManager;
import io.micronaut.core.naming.NameUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class JobsBindingProvider implements BindingProvider {

    private final JobManager jobManager;

    public JobsBindingProvider(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    @Override
    public Map<String, ?> getBinding() {
        Map<String, Object> bindings = new HashMap<>();

        bindings.put("jobs", jobManager);

        jobManager.getJobNames().forEach(n -> {
            bindings.put(NameUtils.camelCase(n), new JobAccessor(n, jobManager));
        });

        return bindings;
    }


}
