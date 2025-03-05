/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021-2025 Agorapulse.
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

import com.agorapulse.worker.*;
import com.agorapulse.worker.annotation.Job;
import com.agorapulse.worker.annotation.*;
import com.agorapulse.worker.configuration.DefaultJobConfiguration;
import com.agorapulse.worker.configuration.MutableJobConfiguration;
import com.agorapulse.worker.convention.QueueConsumer;
import com.agorapulse.worker.convention.QueueListener;
import com.agorapulse.worker.convention.QueueProducer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.env.Environment;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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
    private static final String MEMBER_FORK = "fork";
    private static final String MEMBER_FIXED_DELAY = "fixedDelay";
    private static final String MEMBER_MAX_MESSAGES = "maxMessages";
    private static final String MEMBER_WAITING_TIME = "waitingTime";
    private static final String MEMBER_SCHEDULER = "scheduler";
    private static final String MEMBER_VIRTUAL_THREADS_COMPATIBLE = "scheduler";
    private static final String MEMBER_TYPE = "type";
    private static final String MEMBER_VALUE = "value";
    private static final String MEMBER_NAME = "name";

    private static final Map<String, String> ANNOTATION_TO_NAME_VALUE_MAP = Map.of(
        "jakarta.inject.Named", MEMBER_VALUE,
        "javax.inject.Named", MEMBER_VALUE,
        Job.class.getName(), MEMBER_VALUE,
        QueueConsumer.class.getName(), MEMBER_NAME,
        QueueListener.class.getName(), MEMBER_NAME,
        QueueProducer.class.getName(), MEMBER_NAME,
        FixedRate.class.getName(), MEMBER_NAME,
        InitialDelay.class.getName(), MEMBER_NAME,
        FixedDelay.class.getName(), MEMBER_NAME,
        Cron.class.getName(), MEMBER_NAME
    );

    private static final Map<String, String> ANNOTATION_TO_CRON_MAP = Map.of(
        Cron.class.getName(), MEMBER_VALUE,
        QueueProducer.class.getName(), MEMBER_CRON,
        Job.class.getName(), MEMBER_CRON
    );

    private static final Map<String, String> ANNOTATION_TO_FIXED_RATE_MAP = Map.of(
        FixedRate.class.getName(), MEMBER_VALUE,
        QueueConsumer.class.getName(), MEMBER_WAITING_TIME,
        QueueProducer.class.getName(), MEMBER_FIXED_RATE,
        Job.class.getName(), MEMBER_FIXED_RATE
    );

    private static final Map<String, String> ANNOTATION_TO_INITIAL_DELAY_MAP = Map.of(
        InitialDelay.class.getName(), MEMBER_VALUE,
        QueueProducer.class.getName(), MEMBER_INITIAL_DELAY,
        QueueListener.class.getName(), MEMBER_INITIAL_DELAY,
        Job.class.getName(), MEMBER_INITIAL_DELAY
    );

    private static final Map<String, String> ANNOTATION_TO_FIXED_DELAY_MAP = Map.of(
        FixedDelay.class.getName(), MEMBER_VALUE,
        QueueProducer.class.getName(), MEMBER_FIXED_DELAY,
        Job.class.getName(), MEMBER_FIXED_DELAY
    );

    private static final Map<String, String> ANNOTATION_TO_SCHEDULER_MAP = Map.of(
        Job.class.getName(), MEMBER_SCHEDULER,
        Cron.class.getName(), MEMBER_SCHEDULER,
        FixedRate.class.getName(), MEMBER_SCHEDULER,
        InitialDelay.class.getName(), MEMBER_SCHEDULER,
        FixedDelay.class.getName(), MEMBER_SCHEDULER,
        QueueConsumer.class.getName(), MEMBER_SCHEDULER,
        QueueProducer.class.getName(), MEMBER_SCHEDULER,
        QueueListener.class.getName(), MEMBER_SCHEDULER
    );
    private static final Map<String, String> ANNOTATION_TO_VIRTUAL_THREADS_COMPATIBLE_MAP = Map.of(
        Job.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        Cron.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        FixedRate.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        InitialDelay.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        FixedDelay.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        QueueConsumer.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        QueueProducer.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        QueueListener.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE,
        Fork.class.getName(), MEMBER_VIRTUAL_THREADS_COMPATIBLE
    );

    private static final Map<String, String> ANNOTATION_TO_FORK_MAP = Map.of(
        Fork.class.getName(), MEMBER_VALUE,
        QueueConsumer.class.getName(), MEMBER_MAX_MESSAGES,
        QueueListener.class.getName(), MEMBER_FORK
    );

    private static final Map<String, String> ANNOTATION_TO_MAX_MESSAGES_MAP = Map.of(
        Consumes.class.getName(), MEMBER_MAX_MESSAGES,
        QueueConsumer.class.getName(), MEMBER_MAX_MESSAGES
    );

    private static final Map<String, String> ANNOTATION_TO_CONSUMER_TYPE_MAP = Map.of(
        Consumes.class.getName(), MEMBER_TYPE,
        QueueConsumer.class.getName(), MEMBER_TYPE,
        QueueListener.class.getName(), MEMBER_TYPE
    );

    public static final Map<String, String> ANNOTATION_TO_PRODUCER_TYPE_MAP = Map.of(
        Produces.class.getName(), MEMBER_TYPE,
        QueueProducer.class.getName(), MEMBER_TYPE
    );

    private static final Map<String, String> ANNOTATION_TO_WAITING_TIME_MAP = Map.of(
        Consumes.class.getName(), MEMBER_WAITING_TIME,
        QueueConsumer.class.getName(), MEMBER_WAITING_TIME
    );

    private static final Map<String, String> ANNOTATION_TO_CONSUMER_QUEUE_NAME_MAP = Map.of(
        Consumes.class.getName(), MEMBER_VALUE,
        QueueConsumer.class.getName(), MEMBER_VALUE,
        QueueListener.class.getName(), MEMBER_VALUE
    );

    private static final Map<String, String> ANNOTATION_TO_PRODUCER_QUEUE_NAME_MAP = Map.of(
        Produces.class.getName(), MEMBER_VALUE,
        QueueProducer.class.getName(), MEMBER_VALUE
    );

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

        try {
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

            jobScheduler.schedule(task);
        } catch (JobConfigurationException e) {
            LOG.error("Job declared in method {} is ignored because it is not correctly configured", method, e);
        }
    }

    private <T> Optional<T> getFirstAnnotationValue(Map<String, String> annotationTypeToValueMap, BiFunction<String, String, Optional<T>> extractor, Predicate<T> validator) {
        return annotationTypeToValueMap
            .entrySet()
            .stream()
            .map(e -> extractor.apply(e.getKey(), e.getValue()).filter(validator))
            .flatMap(Optional::stream)
            .findFirst();
    }

    private String getJobName(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        Optional<String> maybeName = getFirstAnnotationValue(ANNOTATION_TO_NAME_VALUE_MAP, method::stringValue, StringUtils::isNotEmpty);

        if (maybeName.isPresent()) {
            return maybeName.get();
        }

        // there are more than one job definition
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

        getFirstAnnotationValue(ANNOTATION_TO_CRON_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(configuration::setCron);
        getFirstAnnotationValue(ANNOTATION_TO_FIXED_DELAY_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(fixedDelay -> configuration.setFixedDelay(convertDuration(jobName, fixedDelay, "fixed delay")));
        getFirstAnnotationValue(ANNOTATION_TO_FIXED_RATE_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(fixedRate -> configuration.setFixedRate(convertDuration(jobName, fixedRate, "fixed rate")));
        getFirstAnnotationValue(ANNOTATION_TO_INITIAL_DELAY_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(initialDelay -> configuration.setInitialDelay(convertDuration(jobName, initialDelay, "initial delay")));
        getFirstAnnotationValue(ANNOTATION_TO_SCHEDULER_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(configuration::setScheduler);
        getFirstAnnotationValue(ANNOTATION_TO_VIRTUAL_THREADS_COMPATIBLE_MAP, method::booleanValue, Boolean::booleanValue).ifPresent(configuration::setVirtualThreadsCompatible);

        boolean consumer = method.getArguments().length == 1;
        boolean producer = !method.getReturnType().getType().equals(void.class);

        if (method.hasAnnotation(QueueConsumer.class) && !consumer) {
            throw new JobConfigurationException(com.agorapulse.worker.Job.create(configuration, () -> {
            }), "Method annotated with @QueueListener must have exactly one argument");
        }

        if (method.hasAnnotation(QueueProducer.class)) {
            if (!producer) {
                throw new JobConfigurationException(com.agorapulse.worker.Job.create(configuration, () -> {
                }), "Method annotated with @QueueProducer must have a return type. Use reactive type to the best performance.");
            }
            if (configuration.getCron() == null && configuration.getFixedDelay() == null && configuration.getInitialDelay() == null && configuration.getFixedRate() == null) {
                throw new JobConfigurationException(com.agorapulse.worker.Job.create(configuration, () -> {
                }), "One of the cron, fixedDelay, initialDelay or fixedRate must be specified for a method annotated with @QueueProducer or one of @Cron, @FixedDelay, @InitialDelay or @FixedRate must be specified for the method.");
            }
        }

        configuration.setLeaderOnly(producer && !consumer || method.findAnnotation(LeaderOnly.class).isPresent());
        configuration.setFollowerOnly(method.findAnnotation(FollowerOnly.class).isPresent());

        if (configuration.isLeaderOnly() && configuration.isFollowerOnly()) {
            throw new JobConfigurationException(com.agorapulse.worker.Job.create(configuration, () -> {
            }), "Cannot use @FollowerOnly on a producer method or method annotated with @LeaderOnly");
        }

        method.findAnnotation(Concurrency.class).flatMap(a -> a.getValue(Integer.class)).ifPresent(configuration::setConcurrency);
        getFirstAnnotationValue(ANNOTATION_TO_FORK_MAP, (annotation, member) -> method.intValue(annotation, member).stream().boxed().findAny(), i -> i > 0).ifPresent(configuration::setFork);

        configureConsumerQueue(jobName, method, ANNOTATION_TO_CONSUMER_QUEUE_NAME_MAP, ANNOTATION_TO_CONSUMER_TYPE_MAP, configuration.getConsumer());
        configureQueue(method, ANNOTATION_TO_PRODUCER_QUEUE_NAME_MAP, ANNOTATION_TO_PRODUCER_TYPE_MAP, configuration.getProducer());

        configuration.mergeWith(configurationOverrides);

        if (configuration.getConsumer().getQueueName() == null) {
            configuration.getConsumer().setQueueName(extractQueueNameFromMethod(method));
        }

        if (configuration.getProducer().getQueueName() == null) {
            configuration.getProducer().setQueueName(extractQueueNameFromMethod(method));
        }

        if (method.getArguments().length > 1) {
            throw new JobConfigurationException(com.agorapulse.worker.Job.create(configuration, () -> {
            }), "Cannot have more than one argument in a method annotated with @Job");
        }

        return configuration;
    }

    private void configureQueue(ExecutableMethod<?, ?> method, Map<String, String> annotationsToQueue, Map<String, String> annotationsToType, MutableJobConfiguration.MutableQueueConfiguration queueConfiguration) {
        getFirstAnnotationValue(annotationsToQueue, method::stringValue, StringUtils::isNotEmpty).ifPresent(queueConfiguration::setQueueName);
        getFirstAnnotationValue(annotationsToType, method::stringValue, StringUtils::isNotEmpty).ifPresent(queueConfiguration::setQueueType);
    }

    private void configureConsumerQueue(String jobName, ExecutableMethod<?, ?> method, Map<String, String> annotationsToQueue, Map<String, String> annotationsToType, MutableJobConfiguration.MutableConsumerQueueConfiguration queueConfiguration) {
        configureQueue(method, annotationsToQueue, annotationsToType, queueConfiguration);

        getFirstAnnotationValue(ANNOTATION_TO_WAITING_TIME_MAP, method::stringValue, StringUtils::isNotEmpty).ifPresent(waitingTime -> queueConfiguration.setWaitingTime(convertDuration(jobName, waitingTime, "waiting time")));

        getFirstAnnotationValue(
            ANNOTATION_TO_MAX_MESSAGES_MAP,
            (anno, member) -> method.intValue(anno, member).stream().boxed().findAny(),
            maxMessages -> maxMessages > 0 && maxMessages != JobConfiguration.ConsumerQueueConfiguration.DEFAULT_MAX_MESSAGES
        ).ifPresent(queueConfiguration::setMaxMessages);
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
