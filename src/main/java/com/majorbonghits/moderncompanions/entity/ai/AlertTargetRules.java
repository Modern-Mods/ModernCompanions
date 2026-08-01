package com.majorbonghits.moderncompanions.entity.ai;

/** Shared Alert safety boundary, kept dependency-free for regression checks. */
final class AlertTargetRules {
    private AlertTargetRules() {}

    static boolean shouldTarget(boolean isMonster, boolean isUnsafe, boolean isConfiguredExcluded) {
        return isMonster && !isUnsafe && !isConfiguredExcluded;
    }
}
