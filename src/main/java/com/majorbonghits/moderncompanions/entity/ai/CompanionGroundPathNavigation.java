package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.block.Blocks;

/** Ground navigation that does not route companions onto pointed dripstone. */
public final class CompanionGroundPathNavigation extends GroundPathNavigation {
    public CompanionGroundPathNavigation(AbstractHumanCompanionEntity companion, Level level) {
        super(companion, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new CompanionWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    private static final class CompanionWalkNodeEvaluator extends WalkNodeEvaluator {
        @Override
        public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isPointedDripstoneObstacle(context.level(), pos)) {
                return PathType.BLOCKED;
            }
            return super.getPathTypeOfMob(context, x, y, z, mob);
        }
    }

    private static boolean isPointedDripstoneObstacle(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.POINTED_DRIPSTONE)
                || level.getBlockState(pos.below()).is(Blocks.POINTED_DRIPSTONE);
    }
}
