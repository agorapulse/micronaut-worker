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

import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.event.JobExecutionFinishedEvent;
import com.agorapulse.worker.event.JobExecutionResultEvent;
import com.agorapulse.worker.event.JobExecutionStartedEvent;
import com.agorapulse.worker.job.AbstractJob;
import com.agorapulse.worker.job.JobRunContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExceptionHandler;

import jakarta.inject.Qualifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MethodJob<B, R> extends AbstractJob {

    private final ExecutableMethod<B, R> method;

    private final BeanDefinition<?> beanDefinition;

    private final BeanContext beanContext;
    private final MethodJobInvoker jobMethodInvoker;
    private final TaskExceptionHandler<?, ?> taskExceptionHandler;

    public MethodJob(
            JobConfiguration configuration,
            ExecutableMethod<B, R> method,
            BeanDefinition<?> beanDefinition,
            BeanContext beanContext,
            MethodJobInvoker methodJobInvoker,
            TaskExceptionHandler<?, ?> taskExceptionHandler
    ) {
        super(configuration);
        this.method = method;
        this.beanDefinition = beanDefinition;
        this.beanContext = beanContext;
        this.jobMethodInvoker = methodJobInvoker;
        this.taskExceptionHandler = taskExceptionHandler;
    }

    @Override
    public String getSource() {
        return String.format("method %s#%s", method.getDeclaringType().getName(), method.getMethodName());
    }

    public ExecutableMethod<B, R> getMethod() {
        return method;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void doRun(JobRunContext context) {
        io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
                .getAnnotationTypeByStereotype(Qualifier.class)
                .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                .orElse(null);

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
        B bean = null;
        try {
            bean = (B) beanContext.getBean(beanType, qualifer);

            context
                .onMessage((status, message) -> beanContext.getEventPublisher(JobExecutionStartedEvent.class).publishEvent(new JobExecutionStartedEvent(getName(), status.getId(), message)))
                .onFinished(status -> beanContext.getEventPublisher(JobExecutionFinishedEvent.class).publishEvent(new JobExecutionFinishedEvent(getName(), status)))
                .onError((status, ex) -> beanContext.getEventPublisher(JobExecutionFinishedEvent.class).publishEvent(new JobExecutionFinishedEvent(getName(), status)))
                .onResult((status, result) -> beanContext.getEventPublisher(JobExecutionResultEvent.class).publishEvent(new JobExecutionResultEvent(getName(), status.getId(), result)));

            jobMethodInvoker.invoke(this, bean, context);


        } catch (Throwable e) {
            context.error(e);
            handleException(e, beanType, bean);
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handleException(Throwable e, Class<Object> beanType, B bean) {
        io.micronaut.context.Qualifier<TaskExceptionHandler> qualifier = Qualifiers.byTypeArguments(beanType, e.getClass());
        Collection<BeanDefinition<TaskExceptionHandler>> definitions = beanContext.getBeanDefinitions(TaskExceptionHandler.class, qualifier);
        Optional<BeanDefinition<TaskExceptionHandler>> mostSpecific = definitions.stream().filter(def -> {
            List<Argument<?>> typeArguments = def.getTypeArguments(TaskExceptionHandler.class);
            if (typeArguments.size() == 2) {
                return typeArguments.get(0).getType() == beanType && typeArguments.get(1).getType() == e.getClass();
            }
            return false;
        }).findFirst();

        TaskExceptionHandler finalHandler = mostSpecific.map(bd -> beanContext.getBean(bd.getBeanType(), qualifier)).orElse(taskExceptionHandler);
        finalHandler.handle(bean, e);
    }
}
