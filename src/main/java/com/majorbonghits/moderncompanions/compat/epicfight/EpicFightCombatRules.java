package com.majorbonghits.moderncompanions.compat.epicfight;

/** Keeps native ranged or spell AI from being replaced by Epic Fight melee goals. */
final class EpicFightCombatRules {
    private EpicFightCombatRules() {}

    static boolean keepsNativeCombatAI(boolean nativeRanged, boolean spellcaster) {
        return nativeRanged || spellcaster;
    }
}
