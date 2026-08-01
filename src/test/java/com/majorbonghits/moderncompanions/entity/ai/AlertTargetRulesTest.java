package com.majorbonghits.moderncompanions.entity.ai;

/** No-world regression check for Alert's shared hostile boundary. */
public final class AlertTargetRulesTest {
    public static void main(String[] args) {
        assert AlertTargetRules.shouldTarget(true, false, false);
        assert !AlertTargetRules.shouldTarget(false, false, false);
        assert !AlertTargetRules.shouldTarget(true, true, false);
        assert !AlertTargetRules.shouldTarget(true, false, true);
    }
}
