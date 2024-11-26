package com.agorapulse.worker.runner;

import io.micronaut.context.annotation.Primary;
import io.micronaut.function.executor.FunctionExitHandler;
import jakarta.inject.Singleton;

@Primary
@Singleton
public class TestFunctionExitHandler implements FunctionExitHandler {

    private boolean success;
    private Exception error;

    @Override
    public void exitWithError(Exception error, boolean debug) {
        this.error = error;
    }

    @Override
    public void exitWithSuccess() {
        success = true;
    }

    @Override
    public void exitWithNoData() {
        success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }


}
