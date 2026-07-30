package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** One server-side gate for worker world edits; callers must supply an approved site. */
public final class WorkerBlockActions {
    private WorkerBlockActions() {}

    public static boolean breakBlock(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand) {
        return breakBlock(companion, target, stand, WorkerSite.INTERACT_RANGE_SQR);
    }

    public static boolean breakBlock(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        if (!(companion.level() instanceof ServerLevel level) || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || !level.hasChunkAt(target)
                || !WorkerSite.isValid(companion, target, stand, interactRangeSqr)) return false;
        ItemStack tool = companion.getMainHandItem();
        BlockState state = level.getBlockState(target);
        if (tool.isEmpty() || state.isAir()) return false;
        var drops = Block.getDrops(state, level, target, level.getBlockEntity(target), companion, tool);
        if (!level.destroyBlock(target, false, companion)) return false;
        for (ItemStack drop : drops) {
            ItemStack leftover = companion.getInventory().addItem(drop.copy());
            if (!leftover.isEmpty()) companion.spawnAtLocation(leftover);
        }
        tool.hurtAndBreak(1, companion, EquipmentSlot.MAINHAND);
        return true;
    }

    public static boolean place(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, net.minecraft.world.level.block.state.BlockState state) {
        return companion.level() instanceof ServerLevel level && WorkerSite.isValid(companion, target, stand)
                && level.getBlockState(target).isAir() && level.setBlock(target, state, 3);
    }
}
