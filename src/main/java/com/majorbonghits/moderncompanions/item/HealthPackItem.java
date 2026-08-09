package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Heals the user or a player/Modern Companions target to full health. */
public final class HealthPackItem extends Item {
    private static final int COOLDOWN_TICKS = 20 * 30;

    public HealthPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        if (!(target instanceof Player || target instanceof AbstractHumanCompanionEntity)
                || !canUse(player, target)) {
            return InteractionResult.PASS;
        }
        if (target.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        healToFull(target);
        consume(stack, player);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUse(player, player)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        healToFull(player);
        consume(stack, player);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.consume(stack);
    }

    private boolean canUse(Player user, LivingEntity target) {
        return !user.getCooldowns().isOnCooldown(this)
                && target.isAlive()
                && target.getHealth() < target.getMaxHealth();
    }

    private static void healToFull(LivingEntity target) {
        target.setHealth(target.getMaxHealth());
    }

    private static void consume(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.modern_companions.health_pack"));
    }
}
