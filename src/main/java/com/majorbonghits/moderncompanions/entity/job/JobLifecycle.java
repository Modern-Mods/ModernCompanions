package com.majorbonghits.moderncompanions.entity.job;

/** Small shared state machine: pauses retain work; only terminal exits discard it. */
public final class JobLifecycle {
    public enum Exit { COMPLETED, SUSPENDED, RETRYABLE, ABANDONED }

    private JobPhase phase = JobPhase.SEARCHING;
    private JobPhase resumePhase = JobPhase.SEARCHING;
    private String reason = "";
    private int retries;

    public JobPhase phase() { return phase; }
    public String reason() { return reason; }
    public int retries() { return retries; }

    public void advance(JobPhase next) {
        phase = next;
        if (next != JobPhase.PAUSED && next != JobPhase.WAITING) resumePhase = next;
        reason = "";
        retries = 0;
    }

    public void pause(String why) {
        if (phase != JobPhase.PAUSED && phase != JobPhase.WAITING) resumePhase = phase;
        phase = JobPhase.PAUSED;
        reason = why;
    }

    public void waitFor(String why) {
        if (phase != JobPhase.PAUSED && phase != JobPhase.WAITING) resumePhase = phase;
        phase = JobPhase.WAITING;
        reason = why;
    }

    public void resume() {
        phase = resumePhase;
        reason = "";
    }

    public boolean retry(String why, int limit) {
        reason = why;
        return ++retries <= limit;
    }

    public void finish(Exit exit) {
        if (exit == Exit.SUSPENDED) {
            pause(reason);
        } else if (exit == Exit.COMPLETED || exit == Exit.ABANDONED) {
            advance(JobPhase.SEARCHING);
        }
    }
}
