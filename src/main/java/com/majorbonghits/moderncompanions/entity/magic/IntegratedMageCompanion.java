package com.majorbonghits.moderncompanions.entity.magic;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Shared AI bridge: one basic cast, one guarded signature cast, and one self utility per kit. */
public abstract class IntegratedMageCompanion extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 160;
    private static final int BASIC_MANA_COST = 10;
    private static final int UTILITY_MANA_COST = 20;
    private static final int HEAVY_MANA_COST = 35;
    private int utilityCooldown;

    protected IntegratedMageCompanion(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    protected abstract MagicCompanionKit kit();

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && utilityCooldown > 0) utilityCooldown--;
        if (!level().isClientSide() && utilityCooldown <= 0 && getHealth() * 2.0F < getMaxHealth() && canSpendMana(UTILITY_MANA_COST)) {
            MagicCompanionKit kit = kit();
            if (MagicCastingCompat.cast(this, this, kit.ironUtility, kit.arsUtility)) {
                spendMana(UTILITY_MANA_COST);
                swingCast();
                utilityCooldown = 200;
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!safeTarget(target, 2.5F)) return;
        aimAt(target);
        MagicCompanionKit kit = kit();
        if (canSpendMana(BASIC_MANA_COST) && MagicCastingCompat.cast(this, target, kit.ironBasic, kit.arsBasic)) {
            spendMana(BASIC_MANA_COST);
            swingCast();
        }
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (heavyCooldown > 0 || !safeTarget(target, 5.0F) || !canSpendMana(HEAVY_MANA_COST)) return false;
        aimAt(target);
        MagicCompanionKit kit = kit();
        if (!MagicCastingCompat.cast(this, target, kit.ironHeavy, kit.arsHeavy)) return false;
        spendMana(HEAVY_MANA_COST);
        heavyCooldown = HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return 30;
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return HEAVY_COOLDOWN_TICKS;
    }

    private boolean safeTarget(LivingEntity target, float radius) {
        if (!target.isAlive() || !getSensing().hasLineOfSight(target) || target instanceof net.minecraft.world.entity.player.Player || isAlliedTo(target) || isOwnerInDanger(target, radius)) return false;
        return level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius), this::isAllyNearTarget).isEmpty();
    }

    private boolean isAllyNearTarget(LivingEntity entity) {
        if (entity == this || entity == getTarget()) return false;
        return isAlliedTo(entity) || entity == getOwner() || (entity instanceof TamableAnimal tame && getOwner() != null && getOwner().equals(tame.getOwner()));
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other) || other == getOwner();
    }
}
