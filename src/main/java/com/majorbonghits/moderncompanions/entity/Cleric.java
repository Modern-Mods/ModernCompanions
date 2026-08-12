package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import com.majorbonghits.moderncompanions.entity.projectile.HolySparkProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

/** Owner-focused healer that falls back to melee when support is not needed. */
public class Cleric extends IntegratedMageCompanion {
    private static final int CLERIC_SPELL_MANA_COST = 6;
    private static final int OWNER_HEAL_COOLDOWN_TICKS = 30;
    private static final int ALLY_HEAL_COOLDOWN_TICKS = 30;
    private static final int SELF_HEAL_COOLDOWN_TICKS = 30;
    private static final float HEAL_AMOUNT = 8.0F;
    private static final int SUPPORT_CAST_FALLBACK_TICKS = 10;
    private static final double HEALING_DISTANCE = 10.0D;
    private static final double ALLY_HEAL_RADIUS = 32.0D;
    private int ownerHealCooldown;
    private int allyHealCooldown;
    private int selfHealCooldown;

    public Cleric(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new Goal() {
            {
                setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            }

            @Override
            public boolean canUse() {
                return isHealingMode() && getTarget() != null && getTarget().isAlive();
            }

            @Override
            public boolean canContinueToUse() {
                return canUse();
            }

            @Override
            public void tick() {
                keepDistanceFrom(getTarget());
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return !isHealingMode() && !canUseRangedAttack() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isHealingMode() && !canUseRangedAttack() && super.canContinueToUse();
            }
        });
    }

    @Override
    protected boolean usesRangedCombat() {
        return true;
    }

    @Override
    public boolean canUseRangedAttack() {
        return !ownerNeedsHealing() && injuredAlly() == null && !selfNeedsHealing()
                && canSpendMana(CLERIC_SPELL_MANA_COST);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!canUseRangedAttack() || !safeTarget(target, 2.5F)) return;
        beginSpellCast(target, spellCastTimeTicks("heal", SUPPORT_CAST_FALLBACK_TICKS), () -> {
            if (!canUseRangedAttack() || !safeTarget(target, 2.5F)) return;
            HolySparkProjectile projectile = new HolySparkProjectile(level(), this, target);
            level().addFreshEntity(projectile);
            spendMana(CLERIC_SPELL_MANA_COST);
            swingCast();
        });
    }

    @Override
    protected int basicManaCost() {
        return CLERIC_SPELL_MANA_COST;
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        return false;
    }

    @Override
    protected boolean allowsUtilitySpell() {
        return false;
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !isSpellCasting()) {
            if (ownerHealCooldown > 0) ownerHealCooldown--;
            if (allyHealCooldown > 0) allyHealCooldown--;
            if (selfHealCooldown > 0) selfHealCooldown--;
            LivingEntity owner = getOwner();
            if (ownerNeedsHealing() && ownerHealCooldown <= 0 && owner != null && healOwner(owner)) {
                ownerHealCooldown = OWNER_HEAL_COOLDOWN_TICKS;
            } else if (!ownerNeedsHealing()) {
                AbstractHumanCompanionEntity ally = injuredAlly();
                if (ally != null && allyHealCooldown <= 0 && healAlly(ally)) {
                    allyHealCooldown = ALLY_HEAL_COOLDOWN_TICKS;
                } else if (ally == null && selfNeedsHealing() && selfHealCooldown <= 0 && healSelf()) {
                    selfHealCooldown = SELF_HEAL_COOLDOWN_TICKS;
                }
            }
        }
        super.tick();
    }

    private boolean healOwner(LivingEntity owner) {
        // Iron's mob cast is self-targeted; heal the player directly so the priority is real.
        return healDirect(owner);
    }

    private boolean healAlly(AbstractHumanCompanionEntity ally) {
        return healDirect(ally);
    }

    private boolean healDirect(LivingEntity target) {
        if (!canSpendMana(basicManaCost()) || target == null || !target.isAlive()
                || target.getHealth() >= target.getMaxHealth()) return false;
        return beginSpellCast(target, spellCastTimeTicks("heal", SUPPORT_CAST_FALLBACK_TICKS), () -> {
            if (!target.isAlive() || target.getHealth() >= target.getMaxHealth()
                    || !canSpendMana(basicManaCost())) return;
            float before = target.getHealth();
            target.heal(HEAL_AMOUNT);
            if (target.getHealth() <= before) return;
            spendMana(basicManaCost());
            swingCast();
        });
    }

    private boolean healSelf() {
        return healDirect(this);
    }

    private boolean ownerNeedsHealing() {
        LivingEntity owner = getOwner();
        return owner != null && owner.getHealth() < owner.getMaxHealth() - 0.5F;
    }

    private boolean selfNeedsHealing() {
        return getHealth() < getMaxHealth() - 0.5F;
    }

    private AbstractHumanCompanionEntity injuredAlly() {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        return level().getEntitiesOfClass(AbstractHumanCompanionEntity.class, getBoundingBox().inflate(ALLY_HEAL_RADIUS))
                .stream()
                .filter(ally -> ally != this && ally.isAlive() && ownerId.equals(ally.getOwnerUUID())
                        && ally.getHealth() < ally.getMaxHealth() - 0.5F)
                .min(Comparator.comparingDouble(ally -> ally.getHealth() / ally.getMaxHealth()))
                .orElse(null);
    }

    private boolean isHealingMode() {
        return canSpendMana(basicManaCost())
                && (ownerNeedsHealing() || injuredAlly() != null || selfNeedsHealing());
    }

    private void keepDistanceFrom(LivingEntity target) {
        if (target == null) return;
        double dx = getX() - target.getX();
        double dz = getZ() - target.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 0.001D) {
            dx = -getLookAngle().x;
            dz = -getLookAngle().z;
            distance = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        }
        if (distance < HEALING_DISTANCE) {
            getNavigation().moveTo(getX() + dx / distance * 2.5D, getY(),
                    getZ() + dz / distance * 2.5D, 1.1D);
        } else {
            getNavigation().stop();
        }
        getLookControl().setLookAt(target, 45.0F, 45.0F);
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.CLERIC; }
}
