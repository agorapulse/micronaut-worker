package com.agorapulse.worker.processor;

public interface MethodJobInvoker {

    /**
     * Invoke the method on given bean.
     * @param job the method job
     * @param bean the bean where the method is declared
     */
    <B> void invoke(MethodJob<B, ?> job, B bean);

}
