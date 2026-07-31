package com.majorbonghits.moderncompanions.entity;

/** Small no-world regression check for resource clamping and recovery pacing. */
public final class CompanionResourceRulesTest {
    public static void main(String[] args) {
        assert CompanionResourceRules.bounded(100 - 8, 100) == 92;
        assert CompanionResourceRules.bounded(-1, 100) == 0;
        assert CompanionResourceRules.spend(100, 8, 100) == 92;
        assert CompanionResourceRules.spend(4, 8, 100) == 0;
        assert CompanionResourceRules.spend(100, 0, 100) == 100;
        assert CompanionResourceRules.regenInterval(true, 0, false) == 40;
        assert CompanionResourceRules.regenInterval(false, 100, false) == 10;
        assert CompanionResourceRules.regenInterval(false, 100, true) == 5;
    }
}
