package com.majorbonghits.moderncompanions.entity;

/** Pure math kept separate so resource pacing has a no-world check. */
public final class CompanionResourceRules {
    private CompanionResourceRules() {}

    public static int bounded(int value, int max) { return Math.max(0, Math.min(value, max)); }

    public static int spend(int current, int cost, int max) {
        return bounded(current - Math.max(0, cost), max);
    }

    public static int regenInterval(boolean inCombat, int graceTicks, boolean boosted) {
        int interval = inCombat ? 40 : (graceTicks >= 100 ? 10 : 20);
        return boosted ? Math.max(5, interval / 2) : interval;
    }
}
