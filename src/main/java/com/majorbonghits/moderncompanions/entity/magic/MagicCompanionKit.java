package com.majorbonghits.moderncompanions.entity.magic;

/** Approved, intentionally small real-spell kits. Part names resolve to Ars Nouveau glyph classes. */
public enum MagicCompanionKit {
    FIRE_MAGE("firebolt", "flaming_barrage", parts("MethodProjectile", "EffectIgnite"), parts("MethodProjectile", "EffectIgnite", "AugmentAmplify"), "evasion", parts("MethodSelf", "EffectBubble")),
    // Lightning Mage uses direct-target spell forms; chain/split variants can hit bystanders.
    LIGHTNING_MAGE("lightning_bolt", "lightning_bolt", parts("MethodProjectile", "EffectLightning"),
            parts("MethodProjectile", "EffectLightning", "AugmentAmplify"), "charge", parts("MethodSelf", "EffectLaunch"), true),
    NECROMANCER("wither_skull", "raise_dead", parts("MethodProjectile", "EffectWither"), parts("MethodProjectile", "EffectSummonUndead"), "ray_of_siphoning", parts("MethodSelf", "EffectHeal")),
    CLERIC("heal", "greater_heal", parts("MethodProjectile", "EffectHeal"),
            parts("MethodProjectile", "EffectHeal", "AugmentAmplify"), "cleanse",
            parts("MethodSelf", "EffectDispel")),
    WIZARD("magic_missile", "summon_swords", parts("MethodProjectile", "EffectHarm"), parts("EffectRune", "EffectHarm"), "counterspell", parts("MethodSelf", "EffectDispel")),
    // Chain Lightning is a hostile-only kit; the caster-level harm gate also protects its chained hits.
    SORCERER("firebolt", "chain_lightning", parts("MethodProjectile", "EffectIgnite"), parts("MethodProjectile", "EffectLightning", "AugmentAmplify"), "evasion", parts("MethodSelf", "EffectBubble"), true),
    WARLOCK("eldritch_blast", "abyssal_shroud", parts("MethodProjectile", "EffectWither"), parts("MethodProjectile", "EffectHex"), "blood_step", parts("MethodSelf", "EffectBubble")),
    WITCH("poison_splash", "root", parts("MethodProjectile", "EffectHex"), parts("EffectRune", "EffectSnare"), "oakskin", parts("MethodSelf", "EffectGrow")),
    HAG("fang_strike", "sculk_tentacles", parts("MethodProjectile", "EffectFangs"), parts("EffectLinger", "EffectHex"), "invisibility", parts("MethodSelf", "EffectInvisibility")),
    CRYOMANCER("icicle", "frostwave", parts("MethodProjectile", "EffectFreeze"), parts("EffectRune", "EffectFreeze"), "frost_step", parts("MethodSelf", "EffectSlowfall")),
    DRUID("firefly_swarm", "root", parts("MethodProjectile", "EffectGrow"), parts("MethodProjectile", "EffectSummonWolves"), "haste", parts("MethodSelf", "EffectSlowfall")),
    ILLUSIONIST("magic_arrow", "arcane_shackle", parts("MethodProjectile", "EffectHex"), parts("EffectLinger", "EffectSummonDecoy"), "invisibility", parts("MethodSelf", "EffectInvisibility")),
    // Use known targeted spells: spectral_hammer can crash the upstream cast path and fang_ward is self-only.
    BATTLEMAGE("firebolt", "fang_strike", parts("MethodProjectile", "EffectHarm"), parts("MethodProjectile", "EffectHarm"), "haste", parts("MethodSelf", "EffectBubble"));

    final String ironBasic, ironHeavy, ironUtility;
    final String[] arsBasic, arsHeavy, arsUtility;
    final boolean hostileTargetsOnly;

    /** Physical starter kept in the live main hand while the kit uses native spell APIs. */
    String spawnWeaponId() {
        return switch (this) {
            case FIRE_MAGE, WIZARD, WITCH, CRYOMANCER, DRUID -> "wooden_quarterstaff";
            case LIGHTNING_MAGE, SORCERER, WARLOCK, HAG, ILLUSIONIST, BATTLEMAGE -> "iron_dagger";
            case NECROMANCER -> "stone_dagger";
            case CLERIC -> "golden_sword";
        };
    }

    MagicCompanionKit(String ironBasic, String ironHeavy, String[] arsBasic, String[] arsHeavy, String ironUtility, String[] arsUtility) {
        this(ironBasic, ironHeavy, arsBasic, arsHeavy, ironUtility, arsUtility, false);
    }

    MagicCompanionKit(String ironBasic, String ironHeavy, String[] arsBasic, String[] arsHeavy, String ironUtility, String[] arsUtility,
            boolean hostileTargetsOnly) {
        this.ironBasic = ironBasic;
        this.ironHeavy = ironHeavy;
        this.arsBasic = arsBasic;
        this.arsHeavy = arsHeavy;
        this.ironUtility = ironUtility;
        this.arsUtility = arsUtility;
        this.hostileTargetsOnly = hostileTargetsOnly;
    }

    private static String[] parts(String... parts) {
        return parts;
    }
}
