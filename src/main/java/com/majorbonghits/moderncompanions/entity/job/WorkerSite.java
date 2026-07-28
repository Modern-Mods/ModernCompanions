package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Shared, conservative work-site checks used before worker movement or actions. */
public final class WorkerSite {
    private static final double INTERACT_RANGE_SQR = 20.25D;

    private WorkerSite() {}

    public static boolean isSafeStand(Level level, BlockPos stand) {
        BlockState floor = level.getBlockState(stand.below());
        return floor.isFaceSturdy(level, stand.below(), Direction.UP)
                && !hazardous(floor)
                && WorkerSafetyPredicates.hasTwoBlockHeadroom(clear(level, stand), clear(level, stand.above()));
    }

    public static boolean isValid(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand) {
        if (!isSafeStand(companion.level(), stand) || !visible(companion, target)) return false;
        if (Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(target)) > INTERACT_RANGE_SQR) return false;
        PathNavigation navigation = companion.getNavigation();
        var path = navigation.createPath(stand, 0);
        return path != null && path.canReach();
    }

    @Nullable
    public static BlockPos findStand(AbstractHumanCompanionEntity companion, BlockPos target, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos stand : BlockPos.betweenClosed(target.offset(-radius, -1, -radius), target.offset(radius, 1, radius))) {
            if (!isValid(companion, target, stand)) continue;
            double distance = stand.distSqr(companion.blockPosition());
            if (distance < bestDistance) {
                best = stand.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    public static boolean hazardous(BlockState state) {
        return state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.MAGMA_BLOCK)
                || !state.getFluidState().isEmpty();
    }

    private static boolean clear(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && !hazardous(state);
    }

    private static boolean visible(AbstractHumanCompanionEntity companion, BlockPos target) {
        BlockHitResult hit = companion.level().clip(new ClipContext(companion.getEyePosition(), Vec3.atCenterOf(target),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, companion));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target);
    }
}
