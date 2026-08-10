package com.majorbonghits.moderncompanions.entity.ai;

/** Small, world-free gates for the companion mount state machine. */
public final class CompanionMountRules {
    private CompanionMountRules() {}

    public static boolean shouldAutoMount(boolean following, boolean sitting, boolean ownerMounted,
            boolean companionMounted, boolean inCombat, boolean sameDimension) {
        return following && !sitting && ownerMounted && !companionMounted && !inCombat && sameDimension;
    }

    public static boolean shouldDismount(boolean companionMounted, boolean ownerMounted) {
        return companionMounted && !ownerMounted;
    }

    public static boolean shouldDismount(boolean companionMounted, boolean ownerMounted,
            boolean reconciliationPending) {
        return companionMounted && !ownerMounted && !reconciliationPending;
    }
}
