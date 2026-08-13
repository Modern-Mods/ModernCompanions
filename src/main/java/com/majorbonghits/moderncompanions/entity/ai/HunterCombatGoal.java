package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Shared authoritative attack phase for Hunters. Target selection remains in
 * HunterJobGoal; this goal prevents subclass-specific attack goals from
 * silently taking over the profession's target.
 */
public final class HunterCombatGoal extends Goal {
    private static final double MELEE_RANGE_SQR = 3.0D * 3.0D;
    private static final double RANGED_RANGE_SQR = 20.0D * 20.0D;
    private static final int BOW_DRAW_TICKS = 20;

    private final AbstractHumanCompanionEntity companion;
    private int meleeCooldown;
    private int bowCooldown;
    private int seeTime;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private int crossbowDelay;

    public HunterCombatGoal(AbstractHumanCompanionEntity companion) {
        this.companion = companion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isActiveTarget() && hasExecutableWeapon();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveTarget() && hasExecutableWeapon();
    }

    @Override
    public void start() {
        companion.setAggressive(true);
        meleeCooldown = 0;
        bowCooldown = 0;
        seeTime = 0;
        crossbowDelay = 0;
        crossbowState = isCrossbow() && CrossbowItem.isCharged(companion.getMainHandItem())
                ? CrossbowState.CHARGED : CrossbowState.UNCHARGED;
    }

    @Override
    public void stop() {
        companion.setAggressive(false);
        companion.stopUsingItem();
        if (companion instanceof CrossbowAttackMob crossbow) crossbow.setChargingCrossbow(false);
        companion.getNavigation().stop();
        meleeCooldown = 0;
        bowCooldown = 0;
        seeTime = 0;
        crossbowDelay = 0;
        crossbowState = CrossbowState.UNCHARGED;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = companion.getTarget();
        if (target == null) return;
        companion.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (isMelee()) {
            tickMelee(target);
        } else if (isBow()) {
            tickBow(target);
        } else if (isCrossbow()) {
            tickCrossbow(target);
        }
    }

    private void tickMelee(LivingEntity target) {
        double reach = Math.max(MELEE_RANGE_SQR,
                Math.pow(companion.getBbWidth() * 2.0F + target.getBbWidth(), 2.0D));
        if (companion.distanceToSqr(target) > reach) {
            companion.getNavigation().moveTo(target, 1.15D);
            return;
        }
        companion.getNavigation().stop();
        if (meleeCooldown > 0) meleeCooldown--;
        if (meleeCooldown <= 0) {
            // doHurtTarget owns protection, stamina, durability, and swing state.
            meleeCooldown = companion.doHurtTarget(target) ? 20 : 5;
        }
    }

    private void tickBow(LivingEntity target) {
        boolean canSee = companion.getSensing().hasLineOfSight(target);
        if (canSee) seeTime++; else seeTime--;
        double distance = companion.distanceToSqr(target);
        if (distance > RANGED_RANGE_SQR || seeTime < 5) {
            companion.getNavigation().moveTo(target, 1.0D);
        } else {
            companion.getNavigation().stop();
        }
        if (companion.isUsingItem()) {
            if (!canSee && seeTime < -60) {
                companion.stopUsingItem();
            } else if (canSee && companion.getTicksUsingItem() >= BOW_DRAW_TICKS) {
                ItemStack bow = companion.getUseItem();
                float power = bow.getItem() instanceof BowItem
                        ? BowItem.getPowerForTime(companion.getTicksUsingItem()) : 1.0F;
                companion.stopUsingItem();
                fireBow(target, power);
                bowCooldown = 20;
            }
        } else if (--bowCooldown <= 0 && canSee && seeTime >= 0) {
            companion.startUsingItem(ProjectileUtil.getWeaponHoldingHand(companion,
                    item -> item instanceof BowItem));
        }
    }

    private void tickCrossbow(LivingEntity target) {
        if (!(companion instanceof CrossbowAttackMob crossbow)) return;
        boolean canSee = companion.getSensing().hasLineOfSight(target);
        if (canSee) seeTime++; else seeTime--;
        double distance = companion.distanceToSqr(target);
        boolean needPath = distance > 12.0D * 12.0D || seeTime < 5;
        if (crossbowState == CrossbowState.UNCHARGED && needPath) {
            companion.getNavigation().moveTo(target, 1.0D);
        } else {
            companion.getNavigation().stop();
        }

        if (crossbowState == CrossbowState.UNCHARGED) {
            if (!needPath && canSee) {
                companion.startUsingItem(ProjectileUtil.getWeaponHoldingHand(companion,
                        item -> item instanceof CrossbowItem));
                crossbow.setChargingCrossbow(true);
                crossbowState = CrossbowState.CHARGING;
            }
        } else if (crossbowState == CrossbowState.CHARGING) {
            if (!companion.isUsingItem()) {
                crossbow.setChargingCrossbow(false);
                crossbowState = CrossbowState.UNCHARGED;
                return;
            }
            ItemStack stack = companion.getUseItem();
            if (companion.getTicksUsingItem() >= CrossbowItem.getChargeDuration(stack, companion)) {
                companion.releaseUsingItem();
                crossbow.setChargingCrossbow(false);
                crossbowDelay = 20;
                crossbowState = CrossbowState.CHARGED;
            }
        } else if (crossbowState == CrossbowState.CHARGED) {
            if (--crossbowDelay <= 0) crossbowState = CrossbowState.READY;
        } else if (canSee) {
            // CrossbowAttackMob delegates to CrossbowItem's native projectile
            // creation, preserving owner attribution and enchantments.
            crossbow.performCrossbowAttack(companion, 1.6F);
            crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private void fireBow(LivingEntity target, float power) {
        ItemStack bow = companion.getMainHandItem();
        Predicate<ItemStack> supported = bow.getItem() instanceof ProjectileWeaponItem weapon
                ? weapon.getSupportedHeldProjectiles() : stack -> stack.is(Items.ARROW);
        ItemStack projectile = ProjectileWeaponItem.getHeldProjectile(companion, supported);
        if (projectile.isEmpty()) projectile = Items.ARROW.getDefaultInstance();
        AbstractArrow arrow = ProjectileUtil.getMobArrow(companion, projectile, power, bow);
        double dx = target.getX() - companion.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - companion.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + horizontal * 0.20D, dz, 1.6F,
                (float) (companion.level().getDifficulty().getId() * 3));
        companion.playSound(SoundEvents.ARROW_SHOOT, 1.0F,
                1.0F / (companion.getRandom().nextFloat() * 0.4F + 0.8F));
        companion.level().addFreshEntity(arrow);
        bow.hurtAndBreak(1, companion, EquipmentSlot.MAINHAND);
    }

    private boolean isActiveTarget() {
        LivingEntity target = companion.getTarget();
        return companion.getJob() == CompanionJob.HUNTER && companion.isWorkEnabled()
                && !companion.isOrderedToSit() && target != null && target.isAlive()
                && companion.canHarm(target);
    }

    private boolean hasExecutableWeapon() {
        return supportsWeaponMode(isMelee(), isBow(), isCrossbow(), companion instanceof CrossbowAttackMob);
    }

    /** Pure weapon-mode contract used to keep bow/crossbow capability checks aligned. */
    public static boolean supportsWeaponMode(boolean melee, boolean bow, boolean crossbow, boolean crossbowMob) {
        return melee || bow || crossbow && crossbowMob;
    }

    private boolean isMelee() {
        ItemStack stack = companion.getMainHandItem();
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    private boolean isBow() {
        return companion.getMainHandItem().getItem() instanceof BowItem;
    }

    private boolean isCrossbow() {
        return companion.getMainHandItem().getItem() instanceof CrossbowItem;
    }

    private enum CrossbowState { UNCHARGED, CHARGING, CHARGED, READY }
}
