package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.core.BlockPos;

/** Pure geometry/cost rules shared by the bounded Miner route planner and checks. */
public final class MinerRouteRules {
    private MinerRouteRules() {}

    /** A one-block stair is cardinal-only; diagonal steps have uncleared corner collision. */
    public static boolean isStairStep(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        return dy <= 1 && dx + dz == 1;
    }

    /** Existing air/cave cells are deliberately cheaper than controlled excavation. */
    public static double stepCost(boolean existingAir, boolean needsBridge, float hardness, int verticalDelta) {
        double cost = existingAir ? 1.0D : 4.0D + Math.max(0.0F, hardness);
        if (needsBridge) cost += 8.0D;
        if (verticalDelta != 0) cost += 1.5D;
        return cost;
    }

    /** Bridges are a bounded escape aid, never a general construction system. */
    public static boolean bridgeBudgetAvailable(int used, int limit) {
        return used >= 0 && limit > 0 && used < limit;
    }
}
