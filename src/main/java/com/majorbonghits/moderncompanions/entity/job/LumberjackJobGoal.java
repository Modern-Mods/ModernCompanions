package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lightweight lumberjack loop: scan for a natural log with adjacent leaves,
 * walk over, break logs with a tool-speed delay, and stash drops in the
 * companion inventory. The companion will clear connected logs, break leaves if
 * stuck, and replant a sapling if available.
 */
public class LumberjackJobGoal extends ResumableJobGoal {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModernCompanions-Lumberjack");
    private static final int SEARCH_COOLDOWN_TICKS = 40;
    private static final int MAX_LOGS_PER_TREE = 256;
    private static final int SEARCH_COLUMNS_PER_TICK = 128;
    private static final int MAX_LEAF_CLEAR_TICKS = 20;
    private static final double TREE_FELL_RANGE_SQR = 1024.0D;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private BlockPos targetLog;
    private BlockPos standPos;
    private BlockPos stumpPos;
    private Block expectedSapling;
    private final Queue<BlockPos> pendingLogs = new PriorityQueue<>(this::compareLogPriority);
    private int searchCooldown;
    private int breakTicksRemaining;
    private int stuckTicks;
    private int swingCooldown;
    private boolean replantedThisTree;
    private int stuckAfterDepositCooldown;
    private int idleNavTicks;
    private int debugCooldown;
    private int progressIdleTicks;
    private BlockPos scanCenter;
    private int scanRadius;
    private int scanColumn;
    private static final int STALL_KICK_TICKS = 120;
    private static final int MAX_STAND_SEARCH_RADIUS = 1;

    public LumberjackJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        super(companion, CompanionJob.LUMBERJACK);
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) {
            return false;
        }
        if (targetLog != null || !pendingLogs.isEmpty()) {
            if (stumpPos != null && !reserve("tree:" + stumpPos.asLong())) {
                waiting("Tree reserved");
                return false;
            }
            phase(JobPhase.TRAVELLING, "Travelling", targetLog == null ? pendingLogs.peek() : targetLog);
            return true;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        if (!prepareTreeTargets()) {
            targetLog = null;
            stumpPos = null;
            replantedThisTree = false;
            logDebug("no_target", "state", "scan_empty");
            return false;
        }
        replantedThisTree = false;
        searchCooldown = SEARCH_COOLDOWN_TICKS;
        if (stumpPos == null || !reserve("tree:" + stumpPos.asLong())) {
            waiting("Tree reserved");
            return false;
        }
        logDebug("start", "target", targetLog, "pending", pendingLogs.size());
        phase(JobPhase.TRAVELLING, "Travelling", targetLog);
        return targetLog != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && (targetLog != null || !pendingLogs.isEmpty());
    }

    @Override
    public void start() {
        moveToTarget();
    }

    @Override
    public void stop() {
        // Preemption (combat/delivery/Work off) pauses this tree; only successful work pops logs.
        companion.getNavigation().stop();
        breakTicksRemaining = 0;
        stuckTicks = 0;
        swingCooldown = 0;
        replantedThisTree = false;
        stuckAfterDepositCooldown = 0;
    }

    @Override
    public void tick() {
        if (!isActiveJob()) {
            return;
        }
        if (stuckAfterDepositCooldown > 0) {
            stuckAfterDepositCooldown--;
        }
        // If we're actively pathing or chopping, we're making progress.
        if (!companion.getNavigation().isDone() || breakTicksRemaining > 0) {
            progressIdleTicks = 0;
        } else {
            progressIdleTicks++;
        }

        // If navigation finished unexpectedly while we still have a target, re-issue the path after a short pause.
        if (targetLog != null && companion.getNavigation().isDone()) {
            idleNavTicks++;
            if (idleNavTicks >= 20) { // about 1 second
                logDebug("idle_repath", "target", targetLog, "pos", companion.blockPosition());
                moveToTarget();
                idleNavTicks = 0;
            }
        } else {
            idleNavTicks = 0;
        }
        if (targetLog == null || !isTreeLog(targetLog)) {
            targetLog = nextLogTarget();
        }
        if (targetLog == null) {
            phase(JobPhase.COLLECTING, "Collecting", stumpPos);
            if (!replantedThisTree) {
                tryReplantSapling();
                replantedThisTree = true;
            }
            return;
        }
        if (standPos == null || !WorkerSite.isSafeStand(companion.level(), standPos)) {
            targetLog = nextLogTarget();
            return;
        }
        double horizDist = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (horizDist > 2.25D) {
            companion.setJobStatus("Travelling");
            // Reissuing moveTo every tick resets path progress and leaves workers travelling forever.
            if (companion.getNavigation().isDone()) moveToTarget();
            // Clear the leaf directly blocking a failed route before abandoning this tree.
            var nav = companion.getNavigation();
            var path = nav.getPath();
            boolean noPath = path == null || nav.isDone();
            if (noPath && hasNearbyLeaves(companion.blockPosition())) {
                stuckTicks++;
                if (stuckTicks >= MAX_LEAF_CLEAR_TICKS * 2) { // give ~2s before breaking leaves
                    logDebug("clear_leaves", "around", companion.blockPosition());
                    clearLeavesNear(companion.blockPosition());
                    stuckTicks = 0;
                }
            } else {
                stuckTicks = 0;
            }
            return;
        }
        if (!WorkerSite.canActFromStandIgnoringSight(companion, targetLog, standPos, TREE_FELL_RANGE_SQR)) {
            breakTicksRemaining = 0;
            return;
        }
        if (breakTicksRemaining <= 0) {
            phase(JobPhase.WORKING, "Chopping", targetLog);
            breakTicksRemaining = computeBreakTicks(targetLog);
            swingCooldown = 0;
            logDebug("begin_break", "pos", targetLog, "ticks", breakTicksRemaining);
        }
        companion.getLookControl().setLookAt(Vec3.atCenterOf(targetLog));
        if (swingCooldown-- <= 0) {
            companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            swingCooldown = 6;
        }
        breakTicksRemaining--;
        if (breakTicksRemaining <= 0) {
            if (chopLog(targetLog)) {
                logDebug("chopped", "pos", targetLog, "queue", pendingLogs.size());
                targetLog = nextLogTarget();
                if (targetLog == null && !replantedThisTree) {
                    phase(JobPhase.COLLECTING, "Collecting", stumpPos);
                    tryReplantSapling();
                    replantedThisTree = true;
                }
            }
            breakTicksRemaining = 0;
        }
        if (targetLog != null && progressIdleTicks >= STALL_KICK_TICKS) {
            recoverFromStall();
            progressIdleTicks = 0;
        }
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.LUMBERJACK) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasAxe()) { companion.setJobStatus("No axe"); return false; }
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("Assign chest"); return false; }
        return true;
    }

    public void forceRescanAfterDeposit() {
        // Clear current plan and allow immediate rescan next tick.
        pendingLogs.clear();
        targetLog = null;
        stumpPos = null;
        searchCooldown = 0;
        replantedThisTree = false;
        stuckAfterDepositCooldown = 20;
        idleNavTicks = 0;
        logDebug("post_deposit_rescan", "pos", companion.blockPosition());
    }

    private boolean prepareTreeTargets() {
        pendingLogs.clear();
        stumpPos = null;
        targetLog = null;

        Level level = companion.level();
        BlockPos origin = companion.getWorkCenter().orElse(companion.blockPosition());
        int effectiveRadius = Math.min(128, Math.max(searchRadius, companion.getPatrolRadius()));
        BlockPos start = companion.getJobCheckpointTarget()
                .filter(this::isNaturalTreeLog)
                .filter(companion::isInWorkArea)
                .orElse(null);
        if (start == null) start = findTreeIncrementally(origin, effectiveRadius);
        if (start == null) return false;
        start = findTreeBase(start);
        if (!isNaturalTreeBase(start)) return false;
        expectedSapling = saplingFor(start);
        Block treeBlock = level.getBlockState(start).getBlock();

        Deque<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(start);
        visited.add(start);
        stumpPos = start;
        int count = 0;

        while (!frontier.isEmpty() && count < MAX_LOGS_PER_TREE) {
            BlockPos current = frontier.poll();
            pendingLogs.add(current);
            if (current.getY() < stumpPos.getY()) {
                stumpPos = current;
            }
            count++;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        boolean diagonal = Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1;
                        if (diagonal && (current.getY() <= stumpPos.getY() + 1 || current.getY() + dy <= stumpPos.getY() + 1)) continue;
                        BlockPos adj = current.offset(dx, dy, dz);
                        if (visited.contains(adj) || level.getBlockState(adj).getBlock() != treeBlock) continue;
                        if (Math.abs(adj.getX() - stumpPos.getX()) > 7 || Math.abs(adj.getZ() - stumpPos.getZ()) > 7
                                || adj.getY() - stumpPos.getY() > 32) {
                            pendingLogs.clear();
                            companion.setJobStatus("Tree too large");
                            return false;
                        }
                        visited.add(adj.immutable());
                        frontier.add(adj.immutable());
                    }
                }
            }
        }
        if (!frontier.isEmpty()) {
            pendingLogs.clear();
            companion.setJobStatus("Tree too large");
            return false;
        }

        // Keep one ground-level stand beside the stump so upper logs are never discarded.
        standPos = WorkerSite.findApproachStand(companion, stumpPos, 1);
        // A leaf wall can make the probe fail before the worker gets a chance to clear its approach.
        if (standPos == null) standPos = WorkerSite.findSafeApproachStand(companion, stumpPos, 1);
        if (standPos == null) return false;
        targetLog = nextLogTarget();
        return targetLog != null;
    }

    /** Scan surface columns in small slices so an empty work area never freezes a server tick. */
    private BlockPos findTreeIncrementally(BlockPos center, int radius) {
        if (scanCenter == null || !scanCenter.equals(center) || scanRadius != radius) {
            scanCenter = center.immutable();
            scanRadius = radius;
            scanColumn = 0;
        }
        Level level = companion.level();
        int side = radius * 2 + 1;
        int total = side * side;
        for (int budget = 0; budget < SEARCH_COLUMNS_PER_TICK && scanColumn < total; budget++) {
            long offset = WorkerSafetyPredicates.spiralOffset(scanColumn++);
            BlockPos column = center.offset((int) (offset >> 32), 0, (int) offset);
            if (center.distSqr(column) > (long) radius * radius) continue;
            int x = column.getX();
            int z = column.getZ();
            if (!level.hasChunkAt(column)) continue;
            int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
            int bottom = Math.max(level.getMinBuildHeight(), top - 48);
            for (int y = top; y >= bottom; y--) {
                BlockPos candidate = new BlockPos(x, y, z);
                if (isNaturalTreeLog(candidate)) return candidate;
            }
        }
        if (scanColumn >= total) {
            scanColumn = 0;
            searchCooldown = SEARCH_COOLDOWN_TICKS;
            companion.setJobStatus("No mature trees");
        }
        return null;
    }

    private BlockPos findTreeBase(BlockPos log) {
        Block block = companion.level().getBlockState(log).getBlock();
        BlockPos base = log;
        while (base.getY() > companion.level().getMinBuildHeight()
                && companion.level().getBlockState(base.below()).getBlock() == block) base = base.below();
        return base.immutable();
    }

    private boolean isNaturalTreeBase(BlockPos base) {
        BlockState ground = companion.level().getBlockState(base.below());
        return (ground.is(BlockTags.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.PODZOL)
                || ground.is(Blocks.MYCELIUM) || ground.is(Blocks.MOSS_BLOCK)) && hasCanopyAbove(base);
    }

    private boolean hasCanopyAbove(BlockPos base) {
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-4, 1, -4), base.offset(4, 32, 4))) {
            if (companion.level().getBlockState(pos).is(BlockTags.LEAVES)) return true;
        }
        return false;
    }

    private boolean isTreeLog(BlockPos pos) {
        return companion.level().getBlockState(pos).is(BlockTags.LOGS);
    }

    private boolean isNaturalTreeLog(BlockPos pos) {
        if (!isTreeLog(pos)) return false;
        return hasNearbyLeaves(pos);
    }

    private boolean chopLog(BlockPos pos) {
        if (standPos == null || !WorkerBlockActions.breakReservedTreeBlock(companion, pos, standPos, TREE_FELL_RANGE_SQR)) return false;
        companion.incrementLumberLogsSession();
        return true;
    }

    private double horizontalDistanceTo(BlockPos pos) {
        double dx = (pos.getX() + 0.5D) - companion.getX();
        double dz = (pos.getZ() + 0.5D) - companion.getZ();
        return dx * dx + dz * dz;
    }

    private void moveToTarget() {
        if (targetLog == null) {
            return;
        }
        BlockPos stand = standPos;
        double tx = stand != null ? stand.getX() + 0.5D : targetLog.getX() + 0.5D;
        double tz = stand != null ? stand.getZ() + 0.5D : targetLog.getZ() + 0.5D;
        double baseY = stumpPos != null ? stumpPos.getY() + 0.05D : targetLog.getY();
        double ty = stand != null ? stand.getY() + 0.05D : Math.min(baseY, companion.getY());
        logDebug("nav", "target", targetLog, "stand", stand, "pos", companion.blockPosition(), "pending", pendingLogs.size());
        companion.getNavigation().moveTo(tx, ty, tz, 1.1D);
    }

    private BlockPos nextLogTarget() {
        BlockPos next;
        while ((next = pendingLogs.poll()) != null) {
            if (isTreeLog(next)) {
                return next;
            }
        }
        return null;
    }

    private boolean hasNearbyLeaves(BlockPos pos) {
        Level level = companion.level();
        for (BlockPos leafPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 3, 2))) {
            if (level.getBlockState(leafPos).is(BlockTags.LEAVES)) {
        return true;
    }
        }
        return false;
    }

    private void clearLeavesNear(BlockPos target) {
        Level level = companion.level();
        BlockPos actionStand = WorkerSite.isSafeStand(level, companion.blockPosition()) ? companion.blockPosition() : standPos;
        if (actionStand == null) return;
        for (BlockPos leafPos : BlockPos.betweenClosed(target.offset(-1, 0, -1), target.offset(1, 2, 1))) {
            BlockState state = level.getBlockState(leafPos);
            if (state.is(BlockTags.LEAVES)) {
                if (WorkerBlockActions.breakBlock(companion, leafPos, actionStand, TREE_FELL_RANGE_SQR)) {
                    companion.setJobStatus("Clearing leaves");
                    return;
                }
            }
        }
    }

    private void recoverFromStall() {
        logDebug("stall_recover", "target", targetLog, "queue", pendingLogs.size(), "pos", companion.blockPosition());
        moveToTarget();
        var nav = companion.getNavigation();
        if (nav.isDone() || nav.getPath() == null) {
            breakTicksRemaining = 0;
            swingCooldown = 0;
            stuckTicks = 0;
            idleNavTicks = 0;
            // Keep the log: a blocked route is not proof that this tree changed.
            companion.setJobStatus("Route blocked");
            logDebug("stall_wait", "target", targetLog);
        }
    }

    private BlockPos findGroundStandPos() {
        BlockPos base = stumpPos != null ? stumpPos : targetLog;
        if (base == null) return null;
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-MAX_STAND_SEARCH_RADIUS, 0, -MAX_STAND_SEARCH_RADIUS),
                base.offset(MAX_STAND_SEARCH_RADIUS, 0, MAX_STAND_SEARCH_RADIUS))) {
            if (!level.getBlockState(pos).isAir()) continue;
            BlockState below = level.getBlockState(pos.below());
            if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            double d = pos.distSqr(companion.blockPosition());
            if (d < bestDist) {
                bestDist = d;
                best = pos.immutable();
            }
        }
        if (best == null && level.getBlockState(base).isAir()) {
            best = base.immutable();
        }
        return best;
    }

    private int computeBreakTicks(BlockPos pos) {
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        ItemStack tool = companion.getMainHandItem();
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return 20;
        float speed = tool.getDestroySpeed(state);
        if (!tool.isCorrectToolForDrops(state)) {
            speed = Math.max(1.0F, speed / 3.0F);
        }
        float relative = speed > 0 ? (speed / hardness) : 0.05F;
        int ticks = (int) Math.ceil(20.0F / Math.max(0.05F, relative));
        // Slow down to feel like multiple swings per log.
        ticks *= 2;
        return Math.max(20, Math.min(120, ticks));
    }

    private int compareLogPriority(BlockPos a, BlockPos b) {
        if (a.getY() != b.getY()) {
            return Integer.compare(a.getY(), b.getY()); // lower logs first
        }
        if (stumpPos != null) {
            double da = a.distSqr(stumpPos);
            double db = b.distSqr(stumpPos);
            return Double.compare(da, db);
        }
        return 0;
    }

    private boolean tryReplantSapling() {
        if (stumpPos == null || !(companion.level() instanceof ServerLevel server)) return false;
        BlockPos placePos = stumpPos;
        BlockPos ground = stumpPos.below();
        BlockState groundState = server.getBlockState(ground);
        BlockState airCheck = server.getBlockState(placePos);
        if (!airCheck.isAir()) return false;

        if (!groundState.is(BlockTags.DIRT)
                && !groundState.is(Blocks.GRASS_BLOCK)
                && !groundState.is(Blocks.PODZOL)
                && !groundState.is(Blocks.MYCELIUM)
                && !groundState.is(Blocks.MOSS_BLOCK)) {
            return false;
        }

        Predicate<ItemStack> saplingMatcher = stack -> stack.getItem() instanceof BlockItem bi
                && bi.getBlock() == expectedSapling;
        ItemStack sapling = ItemStack.EMPTY;
        if (saplingMatcher.test(companion.getMainHandItem())) {
            sapling = companion.getMainHandItem();
        } else {
            int slot = findInventorySlot(saplingMatcher);
            if (slot >= 0) {
                sapling = companion.getInventory().getItem(slot);
            }
        }
        if (sapling.isEmpty()) {
            companion.setJobStatus("Needs sapling");
            return false;
        }
        BlockItem bi = (BlockItem) sapling.getItem();
        BlockState saplingState = bi.getBlock().defaultBlockState();
        if (!saplingState.canSurvive(server, placePos)) return false;
        BlockPos stand = WorkerSite.findStand(companion, placePos, 2);
        if (stand != null && WorkerBlockActions.place(companion, placePos, stand, saplingState)) {
            sapling.shrink(1);
            stumpPos = null;
            return true;
        }
        return false;
    }

    private int findInventorySlot(Predicate<ItemStack> matcher) {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    private Block saplingFor(BlockPos log) {
        ResourceLocation logId = BuiltInRegistries.BLOCK.getKey(companion.level().getBlockState(log).getBlock());
        if (logId == null || !logId.getPath().endsWith("_log")) return Blocks.AIR;
        ResourceLocation saplingId = ResourceLocation.fromNamespaceAndPath(logId.getNamespace(),
                logId.getPath().substring(0, logId.getPath().length() - 4) + "_sapling");
        Block sapling = BuiltInRegistries.BLOCK.get(saplingId);
        return sapling.defaultBlockState().is(BlockTags.SAPLINGS) ? sapling : Blocks.AIR;
    }

    private boolean hasAxe() {
        return companion.getMainHandItem().getItem() instanceof AxeItem;
    }

    private boolean hasTool(java.util.function.Predicate<ItemStack> matcher) {
        if (matcher.test(companion.getMainHandItem())) return true;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinWorkArea(double ownerMax) {
        LivingEntity owner = companion.getOwner();
        if (owner != null && companion.distanceToSqr(owner) <= ownerMax * ownerMax) {
            return true;
        }
        return false;
    }

    private boolean isWithinPatrolArea() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }

    private void logDebug(String msg, Object... kv) {
        if (!LOGGER.isInfoEnabled()) return;
        StringBuilder sb = new StringBuilder("[Lumberjack ").append(companion.getId()).append("] ").append(msg);
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append(" | ").append(kv[i]).append('=').append(kv[i + 1]);
        }
        sb.append(" | pos=").append(companion.blockPosition());
        LOGGER.info(sb.toString());
    }
}
