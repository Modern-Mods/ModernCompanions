package com.majorbonghits.moderncompanions.entity.job;

/** Reasoned server-side world-action result; queues advance only on success. */
public enum WorkerActionResult {
    SUCCESS,
    RETRYABLE_BLOCKED,
    INVALID_TARGET,
    PROTECTED,
    INVENTORY_FULL,
    TOOL_MISSING,
    UNLOADED,
    UNSAFE;

    /** A queue may advance only after the server confirmed the world change. */
    public boolean advancesPlan() {
        return this == SUCCESS;
    }
}
