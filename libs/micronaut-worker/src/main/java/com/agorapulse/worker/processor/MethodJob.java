package com.agorapulse.worker.processor;

import com.agorapulse.worker.Job;
import com.agorapulse.worker.JobConfiguration;
import com.agorapulse.worker.job.AbstractJob;
import com.agorapulse.worker.queue.JobQueues;
import io.micronaut.context.BeanContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Qualifier;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class MethodJob<B, R> extends AbstractJob {

    private final ExecutableMethod<B, R> method;

    private final BeanDefinition<?>beanDefinition;

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

    @Override
    public io.micronaut.context.Qualifier<JobQueues> getJobQueueQualifier() {
        return method.getAnnotationTypeByStereotype(javax.inject.Qualifier.class)
                .map(type -> Qualifiers.<JobQueues>byAnnotation(method, type))
                .orElse(null);
    }

    public ExecutableMethod<B, R> getMethod() {
        return method;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doRun() {
        io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
                .getAnnotationTypeByStereotype(Qualifier.class)
                .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                .orElse(null);

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
        B bean = null;
        try {
            bean = (B) beanContext.getBean(beanType, qualifer);
            jobMethodInvoker.invoke(this, bean);
        } catch (Throwable e) {
            setLastException(e);
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
}
