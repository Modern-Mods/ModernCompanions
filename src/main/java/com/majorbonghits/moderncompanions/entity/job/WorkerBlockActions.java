package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** One server-side gate for worker world edits; callers must supply an approved site. */
public final class WorkerBlockActions {
    private WorkerBlockActions() {}

    public static boolean breakBlock(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand) {
        return breakBlock(companion, target, stand, WorkerSite.INTERACT_RANGE_SQR);
    }

    public static boolean breakBlock(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return breakBlockResult(companion, target, stand, interactRangeSqr) == WorkerActionResult.SUCCESS;
    }

    /** Lumberjack-only reserved-tree action: foliage cannot hide an already-approved trunk from its stump stand. */
    public static boolean breakReservedTreeBlock(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return breakReservedTreeBlockResult(companion, target, stand, interactRangeSqr) == WorkerActionResult.SUCCESS;
    }

    public static WorkerActionResult breakReservedTreeBlockResult(AbstractHumanCompanionEntity companion, BlockPos target,
                                                                   BlockPos stand, double interactRangeSqr) {
        return breakBlockResult(companion, target, stand, interactRangeSqr, true);
    }

    /** Miner-only planned excavation: an adjacent queued block may be hidden by the other half of the same tunnel step. */
    public static WorkerActionResult breakPlannedExcavationBlock(AbstractHumanCompanionEntity companion, BlockPos target,
                                                                  BlockPos stand, double interactRangeSqr) {
        return breakBlockResult(companion, target, stand, interactRangeSqr, true);
    }

    public static WorkerActionResult breakBlockResult(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return breakBlockResult(companion, target, stand, interactRangeSqr, false);
    }

    private static WorkerActionResult breakBlockResult(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand,
                                                       double interactRangeSqr, boolean ignoreSight) {
        if (!(companion.level() instanceof ServerLevel level)) return WorkerActionResult.RETRYABLE_BLOCKED;
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return WorkerActionResult.PROTECTED;
        boolean validStand = ignoreSight
                ? WorkerSite.canActFromStandIgnoringSight(companion, target, stand, interactRangeSqr)
                : WorkerSite.canActFromStand(companion, target, stand, interactRangeSqr);
        if (!level.hasChunkAt(target) || !validStand) return WorkerActionResult.RETRYABLE_BLOCKED;
        ItemStack tool = companion.getMainHandItem();
        BlockState state = level.getBlockState(target);
        if (state.isAir()) return WorkerActionResult.INVALID_TARGET;
        if (tool.isEmpty()) return WorkerActionResult.TOOL_MISSING;
        var drops = Block.getDrops(state, level, target, level.getBlockEntity(target), companion, tool);
        if (!canStoreAll(companion, drops)) return WorkerActionResult.INVENTORY_FULL;
        if (!level.destroyBlock(target, false, companion)) return WorkerActionResult.PROTECTED;
        for (ItemStack drop : drops) {
            ItemStack leftover = companion.getInventory().addItem(drop.copy());
            if (!leftover.isEmpty()) return WorkerActionResult.RETRYABLE_BLOCKED; // world changed after simulation
        }
        tool.hurtAndBreak(1, companion, EquipmentSlot.MAINHAND);
        return WorkerActionResult.SUCCESS;
    }

    public static boolean place(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, net.minecraft.world.level.block.state.BlockState state) {
        return companion.level() instanceof ServerLevel level
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && WorkerSite.canActFromStand(companion, target, stand, WorkerSite.INTERACT_RANGE_SQR)
                && level.getBlockState(target).isAir() && level.setBlock(target, state, 3);
    }

    private static boolean canStoreAll(AbstractHumanCompanionEntity companion, List<ItemStack> drops) {
        List<ItemStack> slots = new ArrayList<>();
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            slots.add(companion.getInventory().getItem(slot).copy());
        }
        for (ItemStack drop : drops) {
            ItemStack remaining = drop.copy();
            for (ItemStack slot : slots) {
                if (!ItemStack.isSameItemSameComponents(slot, remaining)) continue;
                int moved = Math.min(remaining.getCount(), slot.getMaxStackSize() - slot.getCount());
                if (moved > 0) {
                    slot.grow(moved);
                    remaining.shrink(moved);
                }
            }
            for (int index = 0; index < slots.size() && !remaining.isEmpty(); index++) {
                if (!slots.get(index).isEmpty()) continue;
                int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                slots.set(index, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
            if (!remaining.isEmpty()) return false;
        }
        return true;
    }
}
