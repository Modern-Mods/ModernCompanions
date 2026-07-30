package com.majorbonghits.moderncompanions.entity.magic;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.ai.MageRangedAttackGoal;
import com.majorbonghits.moderncompanions.item.DaggerItem;
import com.majorbonghits.moderncompanions.item.QuarterstaffItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Shared behaviors for ranged spellcasting companions.
 */
public abstract class AbstractMageCompanion extends AbstractHumanCompanionEntity implements RangedAttackMob {
    protected int heavyCooldown;

    protected AbstractMageCompanion(net.minecraft.world.entity.EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        Goal castingGoal = new MageRangedAttackGoal<>(this, 1.05D, getLightIntervalTicks(), getPreferredRange());
        this.goalSelector.addGoal(2, castingGoal);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            equipCasterWeapon();
            if (heavyCooldown > 0) heavyCooldown--;
        }
        super.tick();
    }

    /** Align the real spell API's caster-facing vector with its direct target. */
    protected final void aimAt(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        this.setYRot((float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F);
        this.setXRot((float) -(Mth.atan2(dy, horizontal) * (180.0D / Math.PI)));
        this.setYHeadRot(this.getYRot());
        this.yBodyRot = this.getYRot();
    }

    /**
     * Attempt a heavy spell. Should set cooldown when actually performed.
     *
     * @return true if a heavy attack was executed
     */
    public abstract boolean tryHeavyAttack(LivingEntity target, float distanceFactor);

    /** Number of ticks between light casts when not strafing. */
    public abstract int getLightIntervalTicks();

    /** Range preference for kiting distance. */
    public float getPreferredRange() {
        return 18.0F;
    }

    /** Duration before another heavy cast can be attempted. */
    public int getHeavyRecoveryTicks() {
        return getLightIntervalTicks();
    }

    /**
     * Scales spell damage off Intelligence.
     */
    public final float magicDamage(float base) {
        float scale = 1.0F + Math.max(0.0F, (getIntelligence() - 4) * 0.08F);
        return base * scale;
    }

    /**
     * Swing the main hand to trigger the punch animation when casting.
     */
    protected void swingCast() {
        this.swing(InteractionHand.MAIN_HAND, true);
    }

    /**
     * Basic safety check to keep owners out of friendly fire radius.
     */
    protected boolean isOwnerInDanger(LivingEntity target, float safeRadius) {
        if (!this.isTame()) return false;
        if (!(this.getOwner() instanceof LivingEntity owner)) return false;
        return owner.distanceToSqr(target) <= (double) (safeRadius * safeRadius);
    }

    private void equipCasterWeapon() {
        ItemStack hand = this.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack firearm = getEquippedOrInventoryFirearm();
        if (!firearm.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(hand, firearm)) this.setItemSlot(EquipmentSlot.MAINHAND, firearm);
            setPreferredWeaponBonus(true);
            return;
        }
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && !isShieldItem(hand) ? hand : ItemStack.EMPTY;

        if (!hand.isEmpty() && !isPreferredWeapon(hand)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            hand = ItemStack.EMPTY;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (preferred.isEmpty() && isPreferredWeapon(stack)) {
                preferred = stack;
            }
            if (fallback.isEmpty() && !isShieldItem(stack)) {
                fallback = stack;
            }
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameComponents(hand, desired)) {
            this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        }
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameComponents(desired, preferred));
    }

    private boolean isPreferredWeapon(ItemStack stack) {
        return stack.getItem() instanceof QuarterstaffItem
                || stack.getItem() instanceof DaggerItem
                || stack.is(Items.STICK) // visual placeholder wand
                || stack.is(Items.BLAZE_ROD);
    }
}
