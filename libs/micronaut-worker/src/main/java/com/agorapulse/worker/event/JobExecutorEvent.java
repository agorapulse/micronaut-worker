package com.agorapulse.worker.event;

import com.agorapulse.worker.JobRunStatus;

public class JobExecutorEvent {

    public enum Execution {
        EXECUTE, SKIP
    }

    public enum Type {
        LEADER_ONLY, FOLLOWER_ONLY, CONCURRENT
    }

    public static JobExecutorEvent leaderOnly(String executor, Execution outcome, JobRunStatus status, String executorId) {
        return new JobExecutorEvent(executor, Type.LEADER_ONLY, outcome, status, 0, executorId);
    }

    public static JobExecutorEvent followerOnly(String executor, Execution outcome, JobRunStatus status, String executorId) {
        return new JobExecutorEvent(executor, Type.FOLLOWER_ONLY, outcome, status, 0, executorId);
    }

    public static JobExecutorEvent concurrent(String executor, Execution outcome, JobRunStatus status, int concurrency, String executorId) {
        return new JobExecutorEvent(executor, Type.CONCURRENT, outcome, status, concurrency, executorId);
    }

    private final String executor;
    private final JobRunStatus status;
    private final int concurrency;
    private final Execution execution;
    private final Type type;
    private final String executorId;

    public JobExecutorEvent(String executor, Type type, Execution execution, JobRunStatus status, int concurrency, String executorId) {
        this.executor = executor;
        this.status = status;
        this.concurrency = concurrency;
        this.execution = execution;
        this.type = type;
        this.executorId = executorId;
    }

    public String getExecutor() {
        return executor;
    }

    public JobRunStatus getStatus() {
        return status;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public Execution getExecution() {
        return execution;
    }

    public Type getType() {
        return type;
    }

    public String getExecutorId() {
        return executorId;
    }

}
