package com.majorbonghits.moderncompanions.block;

import com.majorbonghits.moderncompanions.menu.CompanionTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Vanilla-looking enchanting table whose interaction opens the Companion Table menu.
 * The inherited enchanting-table block entity keeps the animated book and client renderer.
 */
public final class CompanionTableBlock extends EnchantingTableBlock {
    public CompanionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        return openMenu(state, level, pos, player);
    }

    /** Open the table even while holding an input item so every resource can be inserted through the menu. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        return level.isClientSide ? ItemInteractionResult.SUCCESS : toItemResult(openMenu(state, level, pos, player));
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, ignored) -> new CompanionTableMenu(id, inventory, pos),
                Component.translatable("container.modern_companions.companion_table"));
    }

    private InteractionResult openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        player.openMenu(getMenuProvider(state, level, pos), buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.CONSUME
                ? ItemInteractionResult.CONSUME : ItemInteractionResult.SUCCESS;
    }
}
