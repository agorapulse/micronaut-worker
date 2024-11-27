/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2024 Agorapulse.
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
package com.agorapulse.worker.processor;

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.JobConfigurationException;
import com.agorapulse.worker.JobManager;
import com.agorapulse.worker.JobScheduler;
import com.agorapulse.worker.WorkerConfiguration;
import com.agorapulse.worker.annotation.*;
import com.agorapulse.worker.annotation.Job;
import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.env.Environment;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A {@link ExecutableMethodProcessor} for the {@link Job} annotation.
 * <p>
 * Based on {@link io.micronaut.scheduling.processor.ScheduledMethodProcessor} with additional option to configure
 * jobs using configuration properties, reacting on incoming messages from a queue and publishing results of the
 * execution to a queue.
 *
 * @since 1.0
 */
@Singleton
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
    private final Environment environment;
    private final WorkerConfiguration workerConfiguration;

    /**
     * @param beanContext              The bean context for DI of beans annotated with {@link jakarta.inject.Inject}
     * @param taskExceptionHandler     The default task exception handler
     * @param jobMethodInvoker         The job invoker
     * @param jobManager               The job manager
     * @param applicationConfiguration The application configuration
     * @param jobScheduler             The job scheduler
     * @param workerConfiguration
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public MethodJobProcessor(
        BeanContext beanContext,
        TaskExceptionHandler<?, ?> taskExceptionHandler,
        MethodJobInvoker jobMethodInvoker,
        JobManager jobManager,
        ApplicationConfiguration applicationConfiguration,
        JobScheduler jobScheduler,
        Environment environment,
        WorkerConfiguration workerConfiguration
    ) {
        this.beanContext = beanContext;
        this.taskExceptionHandler = taskExceptionHandler;
        this.jobMethodInvoker = jobMethodInvoker;
        this.jobManager = jobManager;
        this.applicationConfiguration = applicationConfiguration;
        this.jobScheduler = jobScheduler;
        this.environment = environment;
        this.workerConfiguration = workerConfiguration;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (!(beanContext instanceof ApplicationContext)) {
            return;
        }

        JobConfiguration configuration = getJobConfiguration(beanDefinition, method);

        com.agorapulse.worker.Job task = new MethodJob<>(
            configuration,
            method,
            beanDefinition,
            beanContext,
            jobMethodInvoker,
            taskExceptionHandler
        );

        jobManager.register(task);

        if (!configuration.isEnabled()) {
            LOG.info("Job {} is disabled in the configuration. Remove jobs.{}.enabled = false configuration to re-enable it.", configuration.getName(), configuration.getName());
            return;
        }

        try {
            jobScheduler.schedule(task);
        } catch (JobConfigurationException e) {
            LOG.error("Job declared in method " + method +  " is ignored because it is not correctly configured", e);
        }
    }

    private String getJobName(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        Optional<String> valueFromJob = method.stringValue(Job.class).map(NameUtils::hyphenate);

        if (valueFromJob.isPresent()) {
            return valueFromJob.get();
        }

        Optional<String> valueFromJavaxNamed = method.stringValue("jakarta.inject.Named").map(NameUtils::hyphenate);

        if (valueFromJavaxNamed.isPresent()) {
            return valueFromJavaxNamed.get();
        }

        Optional<String> valueFromJakartaNamed = method.stringValue("jakarta.inject.Named").map(NameUtils::hyphenate);

        if (valueFromJakartaNamed.isPresent()) {
            return valueFromJakartaNamed.get();
        }

        // there are more then one job definition
        if (beanDefinition.getExecutableMethods().size() > 1) {
            return JobManager.getDefaultJobName(method.getDeclaringType(), method.getMethodName());
        }

        return JobManager.getDefaultJobName(method.getDeclaringType());
    }

    private JobConfiguration getJobConfiguration(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        String jobName = getJobName(beanDefinition, method);

        JobConfiguration configurationOverrides = beanContext.findBean(JobConfiguration.class, Qualifiers.byName(jobName))
            .orElseGet(() -> new DefaultJobConfiguration(jobName, workerConfiguration));

        DefaultJobConfiguration configuration = new DefaultJobConfiguration(jobName, workerConfiguration);

        method.stringValue(Job.class, MEMBER_CRON).ifPresent(configuration::setCron);
        method.stringValue(Job.class, MEMBER_FIXED_DELAY).ifPresent(fixedDelay -> configuration.setFixedDelay(convertDuration(jobName, fixedDelay, "fixed delay")));
        method.stringValue(Job.class, MEMBER_FIXED_RATE).ifPresent(fixedRate -> configuration.setFixedRate(convertDuration(jobName, fixedRate, "fixed rate")));
        method.stringValue(Job.class, MEMBER_INITIAL_DELAY).ifPresent(initialDelay -> configuration.setInitialDelay(convertDuration(jobName, initialDelay, "initial delay")));
        method.stringValue(Job.class, MEMBER_SCHEDULER).ifPresent(configuration::setScheduler);


        configuration.setLeaderOnly(method.findAnnotation(LeaderOnly.class).isPresent());
        configuration.setFollowerOnly(method.findAnnotation(FollowerOnly.class).isPresent());

        method.findAnnotation(Concurrency.class).flatMap(a -> a.getValue(Integer.class)).ifPresent(configuration::setConcurrency);
        method.findAnnotation(Fork.class).flatMap(a -> a.getValue(Integer.class)).ifPresent(configuration::setFork);

        configureConsumerQueue(jobName, method.findAnnotation(Consumes.class), configuration.getConsumer());
        configureQueue(method.findAnnotation(Produces.class), configuration.getProducer());

        configuration.mergeWith(configurationOverrides);

        if (configuration.getConsumer().getQueueName() == null) {
            configuration.getConsumer().setQueueName(extractQueueNameFromMethod(method));
        }

        if (configuration.getProducer().getQueueName() == null) {
            configuration.getProducer().setQueueName(extractQueueNameFromMethod(method));
        }

        return configuration;
    }

    private <A extends Annotation> void configureQueue(Optional<AnnotationValue<A>> consumesAnnotation, MutableJobConfiguration.MutableQueueConfiguration queueConfiguration) {
        if (consumesAnnotation.isPresent()) {
            AnnotationValue<A> annotationValue = consumesAnnotation.get();
            queueConfiguration.setQueueName(annotationValue.getRequiredValue(String.class));

            annotationValue.stringValue("type").ifPresent(type -> {
                if (StringUtils.isNotEmpty(type)) {
                    queueConfiguration.setQueueType(type);
                }
            });
        }
    }

    private void configureConsumerQueue(String jobName, Optional<AnnotationValue<Consumes>> consumesAnnotation, MutableJobConfiguration.MutableConsumerQueueConfiguration queueConfiguration) {
        if (consumesAnnotation.isPresent()) {
            configureQueue(consumesAnnotation, queueConfiguration);

            AnnotationValue<Consumes> annotationValue = consumesAnnotation.get();

            annotationValue.stringValue("waitingTime").ifPresent(waitingTime -> {
                if (StringUtils.isNotEmpty(waitingTime)) {
                    queueConfiguration.setWaitingTime(convertDuration(jobName, waitingTime, "waiting time"));
                }
            });

            annotationValue.intValue("maxMessages").ifPresent(maxMessages -> {
                if (maxMessages > 0 && maxMessages != JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES) {
                    queueConfiguration.setMaxMessages(maxMessages);
                }
            });
        }
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
        Optional<Duration> converted = environment.convert(durationString, Duration.class);

        if (converted.isPresent()) {
            return converted.get();
        }

        throw new IllegalArgumentException("Invalid " + humanReadableProperty + "  definition for job " + jobName + " : " + durationString);
    }
}
