package com.majorbonghits.moderncompanions.item;

/** Pure inventory-capacity rule for the placement wand's capture action. */
public final class PlacementWandCaptureRules {
    private PlacementWandCaptureRules() {}

    public static int captureLimit(int availableSlots, int nearbyCompanions) {
        return Math.min(Math.max(0, availableSlots), Math.max(0, nearbyCompanions));
    }
}
