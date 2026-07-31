package com.majorbonghits.moderncompanions.entity.job;

/** Reasoned server-side world-action result; queues advance only on success. */
public enum WorkerActionResult {
    SUCCESS,
    RETRYABLE_BLOCKED,
    INVALID_TARGET,
    PROTECTED,
    INVENTORY_FULL,
    TOOL_MISSING
}
