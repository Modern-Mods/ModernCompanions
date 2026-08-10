package com.majorbonghits.moderncompanions.entity.ai;

/** Small, world-free gates for the companion mount state machine. */
public final class CompanionMountRules {
    // Covers the full vanilla horse speed range while bounding unusual modded attributes.
    private static final double MAX_GUIDED_MOUNT_SPEED_MODIFIER = 6.0D;

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

    /** Compensate for MoveControl feeding its speed back as forward input. */
    public static double guidedMountSpeedModifier(double mountSpeed, double ownerMountSpeed) {
        if (!Double.isFinite(mountSpeed) || !Double.isFinite(ownerMountSpeed)
                || mountSpeed <= 0.0D || ownerMountSpeed <= 0.0D) {
            return 1.0D;
        }
        // AI movement is approximately (modifier * mountSpeed)^2, while ridden movement is
        // ownerMountSpeed; use the square-root target to reproduce ridden forward input.
        double modifier = Math.sqrt(ownerMountSpeed) / mountSpeed;
        return Math.max(1.0D, Math.min(MAX_GUIDED_MOUNT_SPEED_MODIFIER, modifier));
    }
}
