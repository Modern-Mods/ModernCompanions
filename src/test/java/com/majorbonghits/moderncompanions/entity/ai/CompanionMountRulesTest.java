package com.majorbonghits.moderncompanions.entity.ai;

/** No-world regression checks for the automatic mount state gates. */
public final class CompanionMountRulesTest {
    public static void main(String[] args) {
        assert CompanionMountRules.shouldAutoMount(true, false, true, false, false, true);
        assert !CompanionMountRules.shouldAutoMount(false, false, true, false, false, true);
        assert !CompanionMountRules.shouldAutoMount(true, true, true, false, false, true);
        assert !CompanionMountRules.shouldAutoMount(true, false, true, false, true, true);
        assert CompanionMountRules.shouldDismount(true, false);
        assert !CompanionMountRules.shouldDismount(true, true);
        assert !CompanionMountRules.shouldDismount(true, false, true);
        assert CompanionMountRules.shouldDismount(true, false, false);
        assert Math.abs(CompanionMountRules.guidedMountSpeedModifier(0.1125D, 0.225D)
                - Math.sqrt(0.225D) / 0.1125D) < 1.0E-9D;
        assert CompanionMountRules.guidedMountSpeedModifier(0.3375D, 0.1125D) == 1.0D;
        assert CompanionMountRules.guidedMountSpeedModifier(0.05D, 0.3375D) == 6.0D;
        assert CompanionMountRules.guidedMountSpeedModifier(0.0D, 0.225D) == 1.0D;
    }
}
