package com.majorbonghits.moderncompanions.compat.epicfight;

/** Guards the AI ownership split without constructing Minecraft entities. */
public final class CompanionEpicFightPatchTest {
    private CompanionEpicFightPatchTest() {}

    public static void main(String[] args) {
        assert EpicFightCombatRules.keepsNativeCombatAI(false, true);
        assert !EpicFightCombatRules.keepsNativeCombatAI(false, false);
        assert EpicFightCombatRules.keepsNativeCombatAI(true, false);
    }
}
