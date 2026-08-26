package com.majorbonghits.moderncompanions.entity.magic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Source-level regression checks for Alert gating and fallback fireball safety. */
public final class MageCombatSafetyTest {
    private static final Path ABSTRACT_COMPANION = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/AbstractHumanCompanionEntity.java");
    private static final Path MAGE_GOAL = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/ai/MageRangedAttackGoal.java");
    private static final Path RETALIATION_GOAL = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/ai/CustomHurtByTargetGoal.java");
    private static final Path FIRE_MAGE = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/FireMage.java");
    private static final Path PROTECTION = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/CompanionProtectionEvents.java");
    private static final Path SMALL_FIREBALL = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/projectile/NonIgnitingSmallFireball.java");
    private static final Path LARGE_FIREBALL = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/projectile/NonExplodingLargeFireball.java");
    private static final Path KNIGHT = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/Knight.java");
    private static final Path CLERIC = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/Cleric.java");
    private static final Path MAGIC_KIT = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/magic/MagicCompanionKit.java");
    private static final Path MAGIC_COMPAT = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/compat/magic/MagicCastingCompat.java");

    private MageCombatSafetyTest() {}

    public static void main(String[] args) throws IOException {
        String companion = Files.readString(ABSTRACT_COMPANION);
        String mageGoal = Files.readString(MAGE_GOAL);
        String retaliation = Files.readString(RETALIATION_GOAL);
        String fireMage = Files.readString(FIRE_MAGE);
        String protection = Files.readString(PROTECTION);
        String smallFireball = Files.readString(SMALL_FIREBALL);
        String largeFireball = Files.readString(LARGE_FIREBALL);
        String knight = Files.readString(KNIGHT);
        String cleric = Files.readString(CLERIC);
        String magicKit = Files.readString(MAGIC_KIT);
        String magicCompat = Files.readString(MAGIC_COMPAT);

        assert companion.contains("if (!value) this.setTarget(null);")
                : "turning Alert off must clear a retained combat target";
        assert mageGoal.contains("this.caster.isAlert()")
                : "the ranged casting goal must stop when Alert is off";
        assert retaliation.contains("!companion.isAlert()")
                : "revenge propagation must respect each companion's Alert state";
        assert fireMage.contains("!safeTarget(target, 2.5F)")
                && fireMage.contains("!safeTarget(target, 4.0F)")
                : "both fallback Fire Mage casts need the shared final target gate";
        assert protection.contains("getDirectSourceEntity()")
                && protection.contains("getAffectedEntities()")
                && protection.contains("canDamage(companion, living)")
                : "companion-owned explosions must filter friendly living entities";
        assert !smallFireball.contains("explode(null,") && !largeFireball.contains("explode(null,")
                : "fallback fireballs must keep their caster attribution";
        assert largeFireball.contains("if (living.hurt(source, 20.0F))")
                : "large fireballs may ignite only after accepted damage";
        assert !knight.contains("Fireball")
                : "Knight must not acquire a fireball path";
        assert !cleric.contains("Fireball")
                : "Cleric must not acquire a fireball path";
        assert !cleric.toLowerCase().contains("fire_breath")
                : "Cleric must not acquire Iron's Fire Breath path";
        assert cleric.contains("HolySparkProjectile")
                : "Cleric offense must remain the dedicated Holy Spark path";
        assert magicKit.contains("CLERIC(\"heal\", \"greater_heal\"")
                : "Cleric must retain its healing kit instead of a fire spell";
        assert companion.contains("hasMana() && MagicCastingCompat.isMagicItem(stack)")
                : "caster equipment must be restricted to magical companions";
        assert magicCompat.contains("caster instanceof AbstractMageCompanion")
                : "the native spell bridge must reject non-mage casters";
        assert companion.contains("migrateStaleCasterEquipment();")
                : "loaded non-mages must migrate stale caster equipment";
        assert companion.contains("storeOrDrop(staleMainHand);")
                : "stale caster equipment must be preserved during migration";
    }
}
