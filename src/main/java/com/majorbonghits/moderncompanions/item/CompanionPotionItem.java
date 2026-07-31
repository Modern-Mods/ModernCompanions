package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModEffects;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/** One item implementation keeps player and companion potion rules identical. */
public final class CompanionPotionItem extends Item {
    public enum Kind { HEALTH, REGENERATION, STAMINA, MANA, REJUVENATION, SHIELD }

    private final Kind kind;

    public CompanionPotionItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public boolean isUsefulFor(AbstractHumanCompanionEntity companion) {
        return switch (kind) {
            case HEALTH, REGENERATION -> companion.getHealth() < companion.getMaxHealth() - 1.0F;
            case STAMINA -> companion.isStaminaEnabled()
                    && (companion.isSprintEnabled() || companion.getTarget() != null) && companion.getStamina() < 15;
            case MANA -> companion.hasMana() && companion.getTarget() != null && companion.getMana() < 10;
            case REJUVENATION -> depletedResources(companion) >= 2 || companion.getHealth() * 3.0F < companion.getMaxHealth();
            case SHIELD -> companion.getTarget() != null && !companion.hasEffect(ModEffects.COMPANION_SHIELD);
        };
    }

    private static int depletedResources(AbstractHumanCompanionEntity companion) {
        int depleted = companion.getHealth() < companion.getMaxHealth() - 4.0F ? 1 : 0;
        if (companion.isStaminaEnabled() && companion.getStamina() < companion.getStaminaMax() - 25) depleted++;
        if (companion.hasMana() && companion.getMana() < companion.getManaMax() - 25) depleted++;
        return depleted;
    }

    public void applyTo(LivingEntity living) {
        switch (kind) {
            case HEALTH -> living.heal(8.0F);
            case REGENERATION -> applyVisibleEffect(living, MobEffects.REGENERATION, ModEffects.COMPANION_REGENERATION, 20 * 18);
            case STAMINA -> {
                if (living instanceof AbstractHumanCompanionEntity companion) companion.restoreStamina(50);
                else applyVisibleEffect(living, MobEffects.MOVEMENT_SPEED, ModEffects.COMPANION_STAMINA, 20 * 30);
            }
            case MANA -> {
                if (living instanceof AbstractHumanCompanionEntity companion) companion.restoreMana(50);
                else applyVisibleEffect(living, MobEffects.DAMAGE_BOOST, ModEffects.COMPANION_MANA, 20 * 30);
            }
            case REJUVENATION -> {
                applyVisibleEffect(living, MobEffects.REGENERATION, ModEffects.COMPANION_REJUVENATION, 20 * 24);
                if (living instanceof AbstractHumanCompanionEntity companion) {
                    companion.restoreStamina(20);
                    companion.restoreMana(20);
                }
            }
            case SHIELD -> living.addEffect(new MobEffectInstance(ModEffects.COMPANION_SHIELD, 20 * 45));
        }
    }

    private static void applyVisibleEffect(LivingEntity living, Holder<MobEffect> vanilla, Holder<MobEffect> display, int duration) {
        living.addEffect(new MobEffectInstance(vanilla, duration, 0, false, true, false));
        living.addEffect(new MobEffectInstance(display, duration));
    }

    public ItemStack emptyVessel() {
        return new ItemStack(switch (kind) {
            case HEALTH, REGENERATION -> ModItems.EMPTY_ROUND_VESSEL.get();
            case STAMINA -> ModItems.EMPTY_RECTANGLE_VESSEL.get();
            case MANA -> ModItems.EMPTY_PYRAMID_VESSEL.get();
            case REJUVENATION -> ModItems.EMPTY_HEXAGON_VESSEL.get();
            case SHIELD -> ModItems.EMPTY_DROPLET_VESSEL.get();
        });
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!level.isClientSide()) applyTo(living);
        if (living instanceof Player player && player.getAbilities().instabuild) return stack;
        stack.shrink(1);
        return stack.isEmpty() ? emptyVessel() : stack;
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity living) { return 32; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.modern_companions.potion." + kind.name().toLowerCase()));
    }
}
