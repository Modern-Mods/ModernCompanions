package com.majorbonghits.moderncompanions.entity.ai;

/** Pure follow-leash math kept separate so it can be checked without a world. */
public final class FollowLeashRules {
    private static final double TELEPORT_LEASH_BUFFER = 5.0D;

    private FollowLeashRules() {}

    public static double teleportDistanceSquared(int followRadius) {
        double radius = Math.max(1.0D, followRadius) + TELEPORT_LEASH_BUFFER;
        return radius * radius;
    }
}
