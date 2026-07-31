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
    public static final double INTERACT_RANGE_SQR = 20.25D;

    private WorkerSite() {}

    public static boolean isSafeStand(Level level, BlockPos stand) {
        BlockState floor = level.getBlockState(stand.below());
        return floor.isFaceSturdy(level, stand.below(), Direction.UP)
                && !hazardous(floor)
                && WorkerSafetyPredicates.hasTwoBlockHeadroom(clear(level, stand), clear(level, stand.above()));
    }

    public static boolean isValid(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand) {
        return isValid(companion, target, stand, INTERACT_RANGE_SQR);
    }

    /** Planning checks a future stand. It deliberately does not require remote line of sight. */
    public static boolean canPlanStand(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        if (!companion.level().hasChunkAt(target) || !isSafeStand(companion.level(), stand)) return false;
        if (Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(target)) > interactRangeSqr) return false;
        PathNavigation navigation = companion.getNavigation();
        var path = navigation.createPath(stand, 0);
        return path != null && path.canReach();
    }

    /** Action checks happen only after arrival at the approved stand. */
    public static boolean canActFromStand(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return canActFromStandIgnoringSight(companion, target, stand, interactRangeSqr)
                && visible(companion, target);
    }

    /** Reserved-tree felling may pass through its own foliage, never through distance, safety, or arrival checks. */
    public static boolean canActFromStandIgnoringSight(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return companion.level().hasChunkAt(target) && isSafeStand(companion.level(), stand)
                && companion.distanceToSqr(Vec3.atCenterOf(stand)) <= 2.25D
                && Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(target)) <= interactRangeSqr;
    }

    /** Legacy callers use action validation; discovery must call {@link #canPlanStand}. */
    public static boolean isValid(AbstractHumanCompanionEntity companion, BlockPos target, BlockPos stand, double interactRangeSqr) {
        return canActFromStand(companion, target, stand, interactRangeSqr);
    }

    @Nullable
    public static BlockPos findStand(AbstractHumanCompanionEntity companion, BlockPos target, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos stand : BlockPos.betweenClosed(target.offset(-radius, -1, -radius), target.offset(radius, 1, radius))) {
            if (!canPlanStand(companion, target, stand, INTERACT_RANGE_SQR)) continue;
            double distance = stand.distSqr(companion.blockPosition());
            if (distance < bestDistance) {
                best = stand.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Finds a safe adjacent destination before the worker has line of sight; actions still use {@link #isValid}. */
    @Nullable
    public static BlockPos findApproachStand(AbstractHumanCompanionEntity companion, BlockPos target, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos stand : BlockPos.betweenClosed(target.offset(-radius, -1, -radius), target.offset(radius, 1, radius))) {
            if (!canPlanStand(companion, target, stand, INTERACT_RANGE_SQR)
                    || Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(target)) > INTERACT_RANGE_SQR) continue;
            double distance = stand.distSqr(companion.blockPosition());
            if (distance < bestDistance) {
                best = stand.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Safe fallback for destinations whose native probe falsely reports no path; caller must retry movement and action validation. */
    @Nullable
    public static BlockPos findSafeApproachStand(AbstractHumanCompanionEntity companion, BlockPos target, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos stand : BlockPos.betweenClosed(target.offset(-radius, -1, -radius), target.offset(radius, 1, radius))) {
            if (!isSafeStand(companion.level(), stand)
                    || Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(target)) > INTERACT_RANGE_SQR) continue;
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
