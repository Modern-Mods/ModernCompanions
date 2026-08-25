package com.majorbonghits.moderncompanions.entity.magic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regression check that fallback lightning damage keeps the caster in the kill/XP source. */
public final class LightningMageKillCreditTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/LightningMage.java");
    private static final Path EXPERIENCE_SOURCE = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/CompanionEvents.java");

    private LightningMageKillCreditTest() {}

    public static void main(String[] args) throws IOException {
        String source = Files.readString(SOURCE);
        assert source.contains("private DamageSource lightningDamage(ServerLevel server, LightningBolt bolt)");
        assert source.contains("new DamageSource(server.damageSources().lightningBolt().typeHolder(), bolt, this)");
        assert source.contains("target.hurt(lightningDamage(server, bolt), 5.0F)");
        assert source.contains("target.hurt(lightningDamage(server, bolt), 6.0F + bonus)");
        assert source.contains("!safeTarget(target, 2.5F)");
        assert source.contains("!safeTarget(target, 5.0F)");
        assert !source.contains("getEntitiesOfClass(LivingEntity.class");
        assert !source.contains("lightningDamage(server, bolt), magicDamage(");
        assert !source.contains("target.hurt(server.damageSources().lightningBolt()");
        assert !source.contains("victim.hurt(server.damageSources().lightningBolt()");

        String experienceSource = Files.readString(EXPERIENCE_SOURCE);
        assert experienceSource.contains("victim.getKillCredit()");
        assert experienceSource.contains("candidate.isEngagedWith(victim)");
    }
}
