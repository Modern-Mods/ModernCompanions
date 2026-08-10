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
    }
}
