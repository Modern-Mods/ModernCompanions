package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

import java.util.List;

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
                || !canSpendMana(BASIC_MANA_COST) || this.isAlliedTo(target)
                || (this.getOwner() != null && this.getOwner() == target)
                || isOwnerInDanger(target, 3.0F)) return;
        aimAt(target);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
        if (bolt == null) return;
        bolt.moveTo(target.getX(), target.getY(), target.getZ());
        bolt.setVisualOnly(true);
        server.addFreshEntity(bolt);
        target.hurt(server.damageSources().lightningBolt(), magicDamage(5.0F));
        spendMana(BASIC_MANA_COST);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) return super.tryHeavyAttack(target, distanceFactor);
        if (heavyCooldown > 0 || !(this.level() instanceof ServerLevel server)
                || target == null || !canSpendMana(HEAVY_MANA_COST)) return false;
        aimAt(target);
        List<LivingEntity> hostiles = server.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(5.0D),
                other -> other != this && other != target && !this.isAlliedTo(other) && other.isAlive());
        if (hostiles.isEmpty()) return false;
        hostiles.add(0, target);
        int boltsCast = 0;
        for (LivingEntity victim : hostiles) {
            if (!victim.isAlive() || this.isAlliedTo(victim) || (this.getOwner() != null && this.getOwner() == victim)
                    || this.isOwnerInDanger(victim, 3.0F)) continue;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(server);
            if (bolt == null) continue;
            bolt.moveTo(victim.getX(), victim.getY(), victim.getZ());
            bolt.setVisualOnly(true);
            server.addFreshEntity(bolt);
            float bonus = server.isThundering() ? 2.0F : 0.0F;
            victim.hurt(server.damageSources().lightningBolt(), magicDamage(6.0F + bonus));
            if (++boltsCast >= 4) break;
        }
        if (boltsCast == 0) return false;
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

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.LIGHTNING_MAGE; }
}
