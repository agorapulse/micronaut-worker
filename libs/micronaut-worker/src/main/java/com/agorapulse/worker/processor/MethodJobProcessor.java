/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.worker.processor;

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobScheduler;
import com.agorapulse.worker.annotation.*;
import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 *
 * A {@link ExecutableMethodProcessor} for the {@link Job} annotation.
 *
 * Based on {@link io.micronaut.scheduling.processor.ScheduledMethodProcessor} with additional option to configure
 * jobs using configuration properties, reacting on incoming messages from a queue and publishing results of the
 * execution to a queue.
 *
 * @author graemerocher
 * @since 1.0
 */
@Singleton
@Requires(property = "jobs.enabled", notEquals = "false")
public class MethodJobProcessor implements ExecutableMethodProcessor<Job> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodJobProcessor.class);

    private static final List<String> SUFFIXES_TO_REMOVE = Arrays.asList("Job", "Consumer", "Producer");
    private static final String MEMBER_FIXED_RATE = "fixedRate";
    private static final String MEMBER_INITIAL_DELAY = "initialDelay";
    private static final String MEMBER_CRON = "cron";
    private static final String MEMBER_FIXED_DELAY = "fixedDelay";
    private static final String MEMBER_SCHEDULER = "scheduler";

    private final BeanContext beanContext;
    private final TaskExceptionHandler<?, ?> taskExceptionHandler;
    private final MethodJobInvoker jobMethodInvoker;
    private final JobManager jobManager;
    private final ApplicationConfiguration applicationConfiguration;
    private final JobScheduler jobScheduler;
    private final ConversionService<?> conversionService;

    /**
     * @param beanContext               The bean context for DI of beans annotated with {@link javax.inject.Inject}
     * @param taskExceptionHandler      The default task exception handler
     * @param jobMethodInvoker          The job invoker
     * @param jobManager                The job manager
     * @param applicationConfiguration  The application configuration
     * @param jobScheduler              The job scheduler
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MethodJobProcessor(
            BeanContext beanContext,
            TaskExceptionHandler<?, ?> taskExceptionHandler,
            MethodJobInvoker jobMethodInvoker,
            JobManager jobManager,
            ApplicationConfiguration applicationConfiguration,
            JobScheduler jobScheduler,
            Optional<ConversionService<?>> optionalConversionService
    ) {
        this.beanContext = beanContext;
        this.taskExceptionHandler = taskExceptionHandler;
        this.jobMethodInvoker = jobMethodInvoker;
        this.jobManager = jobManager;
        this.applicationConfiguration = applicationConfiguration;
        this.jobScheduler = jobScheduler;
        this.conversionService = optionalConversionService.orElse(ConversionService.SHARED);
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (!(beanContext instanceof ApplicationContext)) {
            return;
        }

        List<AnnotationValue<Job>> jobAnnotations = method.getAnnotationValuesByType(Job.class);
        for (AnnotationValue<Job> jobAnnotation : jobAnnotations) {

            JobConfiguration configuration = getJobConfiguration(beanDefinition, method, jobAnnotation);

            if (!configuration.isEnabled()) {
                LOG.info("Job {} is disabled in the configuration. Remove jobs.{}.enabled = false configuration to re-enable it.", configuration.getName(), configuration.getName());
                continue;
            }

            com.agorapulse.worker.Job task = new MethodJob<>(
                    configuration,
                    method,
                    beanDefinition,
                    beanContext,
                    jobMethodInvoker,
                    taskExceptionHandler
            );

            jobManager.register(task);
            jobScheduler.schedule(task);
        }
    }

    private String getJobName(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method, AnnotationValue<Job> jobAnnotation) {
        return NameUtils.hyphenate(jobAnnotation.stringValue().orElseGet(() -> {
            // there are more then one job definition
            if (beanDefinition.getExecutableMethods().size() > 1) {
                return method.getDeclaringType().getSimpleName() + "-" + method.getMethodName();
            }
            return method.getDeclaringType().getSimpleName();
        }));
    }

    private JobConfiguration getJobConfiguration(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method, AnnotationValue<Job> jobAnnotation) {
        String jobName = getJobName(beanDefinition, method, jobAnnotation);

        JobConfiguration configurationOverrides = beanContext.findBean(DefaultJobConfiguration.class, Qualifiers.byName(jobName))
                .orElseGet(() -> new DefaultJobConfiguration(jobName));

        DefaultJobConfiguration configuration = new DefaultJobConfiguration(jobName);

        jobAnnotation.get(MEMBER_CRON, String.class).ifPresent(configuration::setCron);
        jobAnnotation.get(MEMBER_FIXED_DELAY, String.class).ifPresent(fixedDelay -> configuration.setFixedDelay(convertDuration(jobName, fixedDelay, "fixed delay")));
        jobAnnotation.get(MEMBER_FIXED_RATE, String.class).ifPresent(fixedRate -> configuration.setFixedRate(convertDuration(jobName, fixedRate, "fixed rate")));
        jobAnnotation.get(MEMBER_INITIAL_DELAY, String.class).ifPresent(initialDelay -> configuration.setInitialDelay(convertDuration(jobName, initialDelay, "initial delay")));
        jobAnnotation.get(MEMBER_SCHEDULER, String.class).ifPresent(configuration::setScheduler);


        configuration.setLeaderOnly(method.findAnnotation(LeaderOnly.class).isPresent());
        configuration.setFollowerOnly(method.findAnnotation(FollowerOnly.class).isPresent());

        method.findAnnotation(Concurrency.class).flatMap(a -> a.getValue(Integer.class)).ifPresent(configuration::setConcurrency);
        method.findAnnotation(Consumes.class).flatMap(AnnotationValue::stringValue).ifPresent(configuration.getConsumer()::setQueueName);
        method.findAnnotation(Produces.class).flatMap(AnnotationValue::stringValue).ifPresent(configuration.getProducer()::setQueueName);

        configuration.mergeWith(configurationOverrides);

        if (configuration.getConsumer().getQueueName() == null) {
            configuration.getConsumer().setQueueName(extractQueueNameFromMethod(method));
        }

        if (configuration.getProducer().getQueueName() == null) {
            configuration.getProducer().setQueueName(extractQueueNameFromMethod(method));
        }

        return configuration;
    }

    private String extractQueueNameFromMethod(ExecutableMethod<?, ?> method) {
        String name = SUFFIXES_TO_REMOVE
                .stream()
                .reduce(
                        method.getDeclaringType().getSimpleName(),
                        (acc, suffix) -> acc.endsWith(suffix) ? acc.substring(0, acc.length() - suffix.length()) : acc
                );

        String queueName = applicationConfiguration.getName().map(n -> n + "_" + name).orElse(name);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Using default queue name {} queue for method {}#{}. Use @Consumes or @Produces to specify different name!", queueName, method.getDeclaringType().getName(), method.getMethodName());
        }
        return queueName;
    }

    private Duration convertDuration(String jobName, String durationString, String humanReadableProperty) {
        Optional<Duration> converted = conversionService.convert(durationString, Duration.class);

        if (converted.isPresent()) {
            return converted.get();
        }

        throw new IllegalArgumentException("Invalid " + humanReadableProperty + "  definition for job " + jobName + " : " + durationString);
    }
}