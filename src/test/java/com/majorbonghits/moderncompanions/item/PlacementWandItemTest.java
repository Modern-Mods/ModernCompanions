package com.majorbonghits.moderncompanions.item;

/** Small deterministic check for the placement wand's inventory-capacity contract. */
public final class PlacementWandItemTest {
    private PlacementWandItemTest() {}

    public static void main(String[] args) {
        assert PlacementWandCaptureRules.captureLimit(4, 5) == 4 : "a full fifth companion must remain in the world";
        assert PlacementWandCaptureRules.captureLimit(0, 5) == 0 : "a full inventory captures nothing";
        assert PlacementWandCaptureRules.captureLimit(8, 3) == 3 : "capacity cannot exceed nearby companions";
    }
}
