package com.majorbonghits.moderncompanions.compat.epicfight;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.FirearmSpecialist;
import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Set;

/** Lets Epic Fight own companion melee timing while the companion still owns every hit effect. */
final class CompanionEpicFightPatch<T extends AbstractHumanCompanionEntity> extends MobPatch<T> {
    CompanionEpicFightPatch(T companion) {
        super(companion, Factions.NEUTRAL);
    }

    @Override
    public void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_RUN);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        commonAggressiveMobUpdateMotion(considerInaction);
    }

    @Override
    public float getAttackSpeed(InteractionHand hand) {
        // Companion base speed is below player-calibrated weapon penalties; Epic Fight cannot advance a zero/negative attack timeline.
        return Math.max(1.0F, super.getAttackSpeed(hand));
    }

    @Override
    protected void initAI() {
        super.initAI();
        // MobPatch avoids HumanoidMobPatch's held-item animation reset; its goals remain Epic Fight's.
        if (!isNativeRanged(this.original.getMainHandItem())) {
            installMeleeGoals(getHoldingItemCapability(InteractionHand.MAIN_HAND), this.original.getMainHandItem());
        }
    }

    @Override
    public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to,
                               InteractionHand hand) {
        if (this.original.level().isClientSide() || hand != InteractionHand.MAIN_HAND
                || isNativeRanged(to)) return;

        Set<Goal> toRemove = new java.util.HashSet<>();
        selectGoalToRemove(toRemove);
        toRemove.forEach(this.original.goalSelector::removeGoal);
        installMeleeGoals(toCap, to);
    }

    private void installMeleeGoals(CapabilityItem capability, ItemStack stack) {
        CombatBehaviors.Builder<MobPatch<?>> builder = companionWeaponBehavior(capability, stack);
        this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, true));
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // Epic Fight publishes humanoid builders with an invariant generic type.
    private CombatBehaviors.Builder<MobPatch<?>> companionWeaponBehavior(CapabilityItem capability, ItemStack stack) {
        var category = capability.getWeaponCategory();
        var style = capability.getStyle(this);
        if (category == CapabilityItem.WeaponCategories.GREATSWORD) return attackBehavior(Animations.GREATSWORD_AUTO1, 3.0D);
        if (category == CapabilityItem.WeaponCategories.UCHIGATANA) return attackBehavior(Animations.BIPED_MOB_UCHIGATANA1, 2.5D);
        if (category == CapabilityItem.WeaponCategories.LONGSWORD) return attackBehavior(Animations.BIPED_MOB_LONGSWORD1, 2.5D);
        if (category == CapabilityItem.WeaponCategories.TACHI) return attackBehavior(Animations.BIPED_MOB_LONGSWORD1, 2.5D);
        if (category == CapabilityItem.WeaponCategories.SPEAR)
            return attackBehavior(style == CapabilityItem.Styles.ONE_HAND ? Animations.BIPED_MOB_SPEAR_ONEHAND : Animations.BIPED_MOB_SPEAR_TWOHAND1, 3.0D);
        if (category == CapabilityItem.WeaponCategories.DAGGER)
            return attackBehavior(style == CapabilityItem.Styles.ONE_HAND ? Animations.BIPED_MOB_DAGGER_ONEHAND1 : Animations.BIPED_MOB_DAGGER_TWOHAND1, 2.0D);
        if (category == CapabilityItem.WeaponCategories.SWORD && style == CapabilityItem.Styles.TWO_HAND)
            return attackBehavior(Animations.BIPED_MOB_SWORD_DUAL1, 2.5D);
        return attackBehavior(stack.isEmpty() ? Animations.ZOMBIE_ATTACK1 : Animations.BIPED_MOB_ONEHAND1, stack.isEmpty() ? 1.8D : 2.25D);
    }

    private CombatBehaviors.Builder<MobPatch<?>> attackBehavior(AnimationAccessor<? extends StaticAnimation> animation, double range) {
        double rangeSqr = range * range;
        return CombatBehaviors.<MobPatch<?>>builder().newBehaviorSeries(
                CombatBehaviors.BehaviorSeries.<MobPatch<?>>builder().weight(100.0F).canBeInterrupted(true).looping(true)
                        // Companion height/target boxes vary from upstream humanoid mobs; range is the only gate.
                        .nextBehavior(CombatBehaviors.Behavior.<MobPatch<?>>builder().animationBehavior(animation)
                                .custom(patch -> patch.getTarget() != null && patch.getOriginal().distanceToSqr(patch.getTarget()) < rangeSqr)));
    }

    private boolean isNativeRanged(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem
                || this.original instanceof FirearmSpecialist || FirearmSupport.isTacZFirearm(stack);
    }

    @Override
    public yesman.epicfight.api.animation.AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        if (this.original.getVehicle() != null) return Animations.BIPED_HIT_ON_MOUNT;
        return switch (stunType) {
            case LONG -> Animations.BIPED_HIT_LONG;
            case SHORT, HOLD -> Animations.BIPED_HIT_SHORT;
            case KNOCKDOWN -> Animations.BIPED_KNOCKDOWN;
            case NEUTRALIZE -> Animations.BIPED_COMMON_NEUTRALIZED;
            case FALL -> Animations.BIPED_LANDING;
            case NONE -> null;
        };
    }

    @Override
    public boolean overrideRender() {
        // TacZ owns its gun pose; every other companion state uses Epic Fight's animated mesh.
        return !FirearmSupport.isTacZFirearm(this.original.getMainHandItem());
    }

    @Override
    protected void selectGoalToRemove(Set<Goal> toRemove) {
        for (WrappedGoal wrapped : this.original.goalSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            // Epic Fight replaces only melee pursuit; its ranged mixins animate existing companion goals.
            if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof TargetChasingGoal) {
                toRemove.add(goal);
            }
        }
    }
}
