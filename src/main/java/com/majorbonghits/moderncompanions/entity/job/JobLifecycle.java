package com.majorbonghits.moderncompanions.entity.job;

/** Small shared state machine: pauses retain work; only terminal exits discard it. */
public final class JobLifecycle {
    public enum Exit { COMPLETED, SUSPENDED, RETRYABLE, ABANDONED }

    private JobPhase phase = JobPhase.SEARCHING;
    private JobPhase resumePhase = JobPhase.SEARCHING;
    private String reason = "";
    private int retries;
    private int retryCooldown;

    public JobPhase phase() { return phase; }
    public String reason() { return reason; }
    public int retries() { return retries; }

    public void advance(JobPhase next) {
        phase = next;
        if (next != JobPhase.PAUSED && next != JobPhase.WAITING) resumePhase = next;
        reason = "";
        retries = 0;
        retryCooldown = 0;
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

    /** Advance the bounded retry clock once per worker tick. */
    public void tick() {
        if (retryCooldown > 0) retryCooldown--;
    }

    public boolean retryReady() {
        return retryCooldown <= 0;
    }

    public int retryCooldown() {
        return retryCooldown;
    }

    public boolean retry(String why, int limit) {
        return retry(why, limit, 10, 200);
    }

    /** Bounded exponential backoff prevents a blocked site from being retried every tick. */
    public boolean retry(String why, int limit, int baseDelay, int maxDelay) {
        reason = why;
        int attempt = ++retries;
        if (attempt > Math.max(0, limit)) {
            waitFor(why);
            return false;
        }
        int shift = Math.min(5, attempt - 1);
        long delay = (long) Math.max(1, baseDelay) << shift;
        retryCooldown = (int) Math.min(Math.max(1, maxDelay), delay);
        return true;
    }

    public void finish(Exit exit) {
        if (exit == Exit.SUSPENDED) {
            pause(reason);
        } else if (exit == Exit.COMPLETED || exit == Exit.ABANDONED) {
            advance(JobPhase.SEARCHING);
        }
    }
}
