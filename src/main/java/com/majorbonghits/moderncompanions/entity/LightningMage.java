package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Precision lightning caster with a provider path and the original lightning fallback. */
public class LightningMage extends IntegratedMageCompanion {
    private static final int LEGACY_HEAVY_COOLDOWN_TICKS = 150;

    public LightningMage(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) {
            super.performRangedAttack(target, distanceFactor);
            return;
        }
        if (!(this.level() instanceof ServerLevel server) || target == null
                || !canSpendMana(BASIC_MANA_COST) || !safeTarget(target, 2.5F)) return;
        aimAt(target);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        bolt.setVisualOnly(true);
        server.addFreshEntity(bolt);
        target.hurt(lightningDamage(server, bolt), 5.0F);
        spendMana(BASIC_MANA_COST);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) return super.tryHeavyAttack(target, distanceFactor);
        if (heavyCooldown > 0 || !(this.level() instanceof ServerLevel server)
                || target == null || !canSpendMana(HEAVY_MANA_COST) || !safeTarget(target, 5.0F)) return false;
        aimAt(target);
        // Keep the fallback heavy cast target-locked; never enumerate nearby living entities.
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return false;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        bolt.setVisualOnly(true);
        server.addFreshEntity(bolt);
        float bonus = server.isThundering() ? 2.0F : 0.0F;
        target.hurt(lightningDamage(server, bolt), 6.0F + bonus);
        spendMana(HEAVY_MANA_COST);
        heavyCooldown = LEGACY_HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return MagicCastingCompat.available() ? super.getLightIntervalTicks() : 26;
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return MagicCastingCompat.available() ? super.getHeavyRecoveryTicks() : LEGACY_HEAVY_COOLDOWN_TICKS;
    }

    /** Keep vanilla lightning damage while exposing this caster to kill/XP attribution. */
    private DamageSource lightningDamage(ServerLevel server, LightningBolt bolt) {
        return new DamageSource(server.damageSources().lightningBolt().typeHolder(), bolt, this);
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.LIGHTNING_MAGE; }
}
