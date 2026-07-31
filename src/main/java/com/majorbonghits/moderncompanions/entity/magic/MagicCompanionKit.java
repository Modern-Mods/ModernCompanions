package com.majorbonghits.moderncompanions.entity.magic;

/** Approved, intentionally small real-spell kits. Part names resolve to Ars Nouveau glyph classes. */
public enum MagicCompanionKit {
    FIRE_MAGE("firebolt", "flaming_barrage", parts("MethodProjectile", "EffectIgnite"), parts("MethodProjectile", "EffectIgnite", "AugmentAmplify"), "evasion", parts("MethodSelf", "EffectBubble")),
    LIGHTNING_MAGE("lightning_bolt", "chain_lightning", parts("MethodProjectile", "EffectLightning"), parts("MethodProjectile", "EffectLightning", "AugmentSplit"), "charge", parts("MethodSelf", "EffectLaunch")),
    NECROMANCER("wither_skull", "raise_dead", parts("MethodProjectile", "EffectWither"), parts("MethodProjectile", "EffectSummonUndead"), "ray_of_siphoning", parts("MethodSelf", "EffectHeal")),
    CLERIC("heal", "healing_circle", parts("MethodProjectile", "EffectHeal"), parts("MethodSelf", "EffectBubble"), "cleanse", parts("MethodSelf", "EffectDispel")),
    WIZARD("magic_missile", "summon_swords", parts("MethodProjectile", "EffectHarm"), parts("EffectRune", "EffectHarm"), "counterspell", parts("MethodSelf", "EffectDispel")),
    SORCERER("firebolt", "chain_lightning", parts("MethodProjectile", "EffectIgnite"), parts("MethodProjectile", "EffectLightning", "AugmentAmplify"), "evasion", parts("MethodSelf", "EffectBubble")),
    WARLOCK("eldritch_blast", "abyssal_shroud", parts("MethodProjectile", "EffectWither"), parts("MethodProjectile", "EffectHex"), "blood_step", parts("MethodSelf", "EffectBubble")),
    WITCH("poison_splash", "root", parts("MethodProjectile", "EffectHex"), parts("EffectRune", "EffectSnare"), "oakskin", parts("MethodSelf", "EffectGrow")),
    HAG("fang_strike", "sculk_tentacles", parts("MethodProjectile", "EffectFangs"), parts("EffectLinger", "EffectHex"), "invisibility", parts("MethodSelf", "EffectInvisibility")),
    CRYOMANCER("icicle", "frostwave", parts("MethodProjectile", "EffectFreeze"), parts("EffectRune", "EffectFreeze"), "frost_step", parts("MethodSelf", "EffectSlowfall")),
    DRUID("firefly_swarm", "root", parts("MethodProjectile", "EffectGrow"), parts("MethodProjectile", "EffectSummonWolves"), "haste", parts("MethodSelf", "EffectSlowfall")),
    ILLUSIONIST("magic_arrow", "arcane_shackle", parts("MethodProjectile", "EffectHex"), parts("EffectLinger", "EffectSummonDecoy"), "invisibility", parts("MethodSelf", "EffectInvisibility")),
    BATTLEMAGE("spectral_hammer", "fang_ward", parts("MethodTouch", "EffectHarm"), parts("MethodSelf", "EffectFangs"), "haste", parts("MethodSelf", "EffectBubble"));

    final String ironBasic, ironHeavy, ironUtility;
    final String[] arsBasic, arsHeavy, arsUtility;

    MagicCompanionKit(String ironBasic, String ironHeavy, String[] arsBasic, String[] arsHeavy, String ironUtility, String[] arsUtility) {
        this.ironBasic = ironBasic;
        this.ironHeavy = ironHeavy;
        this.arsBasic = arsBasic;
        this.arsHeavy = arsHeavy;
        this.ironUtility = ironUtility;
        this.arsUtility = arsUtility;
    }

    private static String[] parts(String... parts) {
        return parts;
    }
}
