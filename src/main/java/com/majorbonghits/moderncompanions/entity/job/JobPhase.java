package com.majorbonghits.moderncompanions.entity.job;

/** Durable, player-visible job phases. Native paths and scan cursors stay transient. */
public enum JobPhase {
    SEARCHING,
    TRAVELLING,
    WORKING,
    COLLECTING,
    DELIVERING,
    RETURNING,
    PAUSED,
    WAITING
}
