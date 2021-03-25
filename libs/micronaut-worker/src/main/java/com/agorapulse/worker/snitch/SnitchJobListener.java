package com.agorapulse.worker.snitch;

import com.agorapulse.micronaut.snitch.SnitchService;
import com.agorapulse.worker.event.JobExecutionFinishedEvent;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.event.annotation.EventListener;

import javax.inject.Singleton;

@Singleton
@Requires(classes = SnitchService.class)
public class SnitchJobListener {

    private final BeanContext context;

    public SnitchJobListener(BeanContext context) {
        this.context = context;
    }

    @EventListener
    void onJobFinished(JobExecutionFinishedEvent event) {
        context.findBean(SnitchService.class, Qualifiers.byName(event.getName())).ifPresent(SnitchService::snitch);
    }

}
