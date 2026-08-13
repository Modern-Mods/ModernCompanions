package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private boolean treePlanActive;
    private boolean searchingForNextTree;
    private boolean treeScanExhausted;
    private final List<BlockPos> treeFootprint = new ArrayList<>();
    private final Deque<ReplantSite> replantDebt = new ArrayDeque<>();
    private boolean restoredPlan;
    private static final int STALL_KICK_TICKS = 120;
    private static final int MAX_STAND_SEARCH_RADIUS = 1;
    private static final int MAX_ACTION_RETRIES = 3;

    private record ReplantSite(ArrayDeque<BlockPos> remaining, Block sapling) { }

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
        if (!replantDebt.isEmpty()) {
            normalizeReplantDebt();
            BlockPos next = nextReplantTarget();
            if (next == null) return false;
            standPos = WorkerSite.findStand(companion, next, 2);
            phase(JobPhase.COLLECTING, "job_status.modern_companions.replanting", next);
            return standPos != null;
        }
        if (targetLog != null || !pendingLogs.isEmpty() || treePlanActive) {
            searchingForNextTree = false;
            if (stumpPos != null && !reserve("tree:" + stumpPos.asLong())) {
                waiting("job_status.modern_companions.tree_reserved");
                return false;
            }
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling", targetLog == null ? pendingLogs.peek() : targetLog);
            return true;
        }
        if (searchingForNextTree) {
            if (prepareTreeTargets()) {
                return startPreparedTree();
            }
            if (treeScanExhausted) {
                return finishBatchSearch();
            }
            companion.setJobStatus("job_status.modern_companions.searching");
            return true;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        if (treeScanExhausted) {
            // A completed empty pass waits briefly before checking for newly grown trees.
            treeScanExhausted = false;
            scanColumn = 0;
        }
        if (!prepareTreeTargets()) {
            targetLog = null;
            stumpPos = null;
            replantedThisTree = false;
            if (treeScanExhausted) {
                return finishBatchSearch();
            }
            searchingForNextTree = true;
            companion.setJobStatus("job_status.modern_companions.searching");
            logDebug("searching", "state", "scan_in_progress");
            return true;
        }
        return startPreparedTree();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && (!replantDebt.isEmpty() || targetLog != null || !pendingLogs.isEmpty() || treePlanActive || searchingForNextTree);
    }

    @Override
    public void start() {
        if (!replantDebt.isEmpty()) moveToReplant(); else moveToTarget();
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
        if (!retryReady()) return;
        if (!replantDebt.isEmpty()) {
            normalizeReplantDebt();
            if (replantDebt.isEmpty()) return;
            tickReplant();
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
            if (treePlanActive) {
                phase(JobPhase.COLLECTING, "job_status.modern_companions.collecting", stumpPos);
                finishFelledTree();
            }
            // Keep this goal alive while the bounded scan looks for the next tree;
            // stopping here is what previously left the lumberjack standing after one tree.
            searchingForNextTree = true;
            if (companion.isInventoryFull() && companion.hasDeliverableCargo()) {
                finishBatchSearch();
                return;
            }
            if (prepareTreeTargets()) {
                if (startPreparedTree()) {
                    moveToTarget();
                }
            } else if (treeScanExhausted) {
                finishBatchSearch();
            } else {
                companion.setJobStatus("job_status.modern_companions.searching");
            }
            return;
        }
        if (standPos == null || !WorkerSite.isSafeStand(companion.level(), standPos)) {
            targetLog = nextLogTarget();
            return;
        }
        double horizDist = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (horizDist > 2.25D) {
            companion.setJobStatus("job_status.modern_companions.travelling");
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
            phase(JobPhase.WORKING, "job_status.modern_companions.chopping", targetLog);
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
            WorkerActionResult result = chopLog(targetLog);
            if (result == WorkerActionResult.SUCCESS) {
                logDebug("chopped", "pos", targetLog, "queue", pendingLogs.size());
                targetLog = nextLogTarget();
                if (targetLog == null) {
                    phase(JobPhase.COLLECTING, "job_status.modern_companions.collecting", stumpPos);
                    finishFelledTree();
                }
                savePlan();
            } else if (result == WorkerActionResult.INVALID_TARGET) {
                targetLog = nextLogTarget();
                savePlan();
            } else {
                retry(result == WorkerActionResult.INVENTORY_FULL
                        ? "job_status.modern_companions.inventory_full"
                        : "job_status.modern_companions.tree_blocked", 3);
            }
            breakTicksRemaining = 0;
        }
        if (targetLog != null && progressIdleTicks >= STALL_KICK_TICKS) {
            recoverFromStall();
            progressIdleTicks = 0;
        }
    }

    private boolean isActiveJob() {
        restorePlan();
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.LUMBERJACK) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        companion.ensureJobToolEquipped();
        if (!hasAxe()) { companion.setJobStatus("job_status.modern_companions.no_axe"); return false; }
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    public void forceRescanAfterDeposit() {
        // Do not discard a partially felled tree when ordinary full-inventory delivery preempts it.
        if (treePlanActive || targetLog != null || !pendingLogs.isEmpty()) {
            return;
        }
        // Clear an exhausted batch and allow immediate rescan after the chest trip.
        pendingLogs.clear();
        targetLog = null;
        stumpPos = null;
        treeFootprint.clear();
        searchCooldown = 0;
        replantedThisTree = false;
        searchingForNextTree = false;
        treeScanExhausted = false;
        scanColumn = 0;
        stuckAfterDepositCooldown = 20;
        idleNavTicks = 0;
        logDebug("post_deposit_rescan", "pos", companion.blockPosition());
    }

    private boolean prepareTreeTargets() {
        if (treeScanExhausted) return false;
        pendingLogs.clear();
        stumpPos = null;
        targetLog = null;
        standPos = null;
        treeFootprint.clear();
        treePlanActive = false;

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
        List<BlockPos> componentLogs = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(start);
        visited.add(start);
        stumpPos = start;
        int count = 0;

        while (!frontier.isEmpty() && count < MAX_LOGS_PER_TREE) {
            BlockPos current = frontier.poll();
            componentLogs.add(current.immutable());
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
                            companion.setJobStatus("job_status.modern_companions.tree_too_large");
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
            companion.setJobStatus("job_status.modern_companions.tree_too_large");
            return false;
        }

        int lowestY = componentLogs.stream().mapToInt(BlockPos::getY).min().orElse(stumpPos.getY());
        treeFootprint.clear();
        componentLogs.stream()
                .filter(pos -> pos.getY() == lowestY)
                .sorted((left, right) -> {
                    int x = Integer.compare(left.getX(), right.getX());
                    return x != 0 ? x : Integer.compare(left.getZ(), right.getZ());
                })
                .forEach(pos -> treeFootprint.add(pos.immutable()));
        if (treeFootprint.isEmpty()) treeFootprint.add(stumpPos.immutable());
        stumpPos = treeFootprint.get(0);
        pendingLogs.addAll(componentLogs);

        // Keep one ground-level stand beside the stump so upper logs are never discarded.
        standPos = WorkerSite.findApproachStand(companion, stumpPos, 1);
        // A leaf wall can make the probe fail before the worker gets a chance to clear its approach.
        if (standPos == null) standPos = WorkerSite.findSafeApproachStand(companion, stumpPos, 1);
        if (standPos == null) return false;
        targetLog = nextLogTarget();
        treePlanActive = targetLog != null;
        savePlan();
        return treePlanActive;
    }

    private boolean startPreparedTree() {
        if (!treePlanActive || targetLog == null) return false;
        searchingForNextTree = false;
        replantedThisTree = false;
        searchCooldown = SEARCH_COOLDOWN_TICKS;
        if (stumpPos == null || !reserve("tree:" + stumpPos.asLong())) {
            waiting("job_status.modern_companions.tree_reserved");
            return false;
        }
        logDebug("start", "target", targetLog, "pending", pendingLogs.size());
        phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling", targetLog);
        return true;
    }

    private boolean finishBatchSearch() {
        searchingForNextTree = false;
        searchCooldown = SEARCH_COOLDOWN_TICKS;
        if (companion.hasDeliverableCargo()
                && companion.getAssignedChest().isPresent()
                && companion.requestImmediateDelivery(null)) {
            companion.getAssignedChest().ifPresent(chest ->
                    phase(JobPhase.DELIVERING, "job_status.modern_companions.delivering", chest));
            logDebug("batch_complete", "state", "delivery_requested");
            return false;
        }
        companion.setJobStatus("job_status.modern_companions.no_mature_trees");
        logDebug("batch_complete", "state", "area_empty");
        return false;
    }

    /** Scan surface columns in small slices so an empty work area never freezes a server tick. */
    private BlockPos findTreeIncrementally(BlockPos center, int radius) {
        if (scanCenter == null || !scanCenter.equals(center) || scanRadius != radius) {
            scanCenter = center.immutable();
            scanRadius = radius;
            scanColumn = 0;
            treeScanExhausted = false;
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
            treeScanExhausted = true;
            companion.setJobStatus("job_status.modern_companions.no_mature_trees");
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
        return isConfiguredTreeGround(ground) && hasCanopyAbove(base);
    }

    private boolean isConfiguredTreeGround(BlockState ground) {
        boolean allowed = false;
        for (String raw : ModConfig.safeGet(ModConfig.JOB_LUMBERJACK_GROUND_BLOCKS)) {
            if (raw == null || raw.isBlank()) continue;
            String idText = raw.startsWith("#") ? raw.substring(1) : raw;
            ResourceLocation id = ResourceLocation.tryParse(idText);
            if (id == null) continue;
            if (raw.startsWith("#")) {
                if (ground.is(TagKey.create(Registries.BLOCK, id))) {
                    allowed = true;
                    break;
                }
            } else if (BuiltInRegistries.BLOCK.containsKey(id) && ground.is(BuiltInRegistries.BLOCK.get(id))) {
                allowed = true;
                break;
            }
        }
        return allowed;
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

    private WorkerActionResult chopLog(BlockPos pos) {
        if (standPos == null) return WorkerActionResult.RETRYABLE_BLOCKED;
        WorkerActionResult result = WorkerBlockActions.breakReservedTreeBlockResult(companion, pos, standPos, TREE_FELL_RANGE_SQR);
        if (result != WorkerActionResult.SUCCESS) return result;
        companion.incrementLumberLogsSession();
        return WorkerActionResult.SUCCESS;
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
                    companion.setJobStatus("job_status.modern_companions.clearing_leaves");
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
            companion.setJobStatus("job_status.modern_companions.route_blocked");
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
        // Keep the native tool-speed relationship while exposing MCA-style pacing as config.
        ticks = (int) Math.ceil(ticks * ModConfig.safeGet(ModConfig.JOB_LUMBERJACK_BREAK_TIME_MULTIPLIER));
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

    private void finishFelledTree() {
        if (targetLog != null || !pendingLogs.isEmpty()) return;
        if (stumpPos != null) release("tree:" + stumpPos.asLong());
        prepareReplantDebt();
        treePlanActive = false;
        targetLog = null;
        standPos = null;
        stumpPos = null;
        expectedSapling = null;
        treeFootprint.clear();
        replantedThisTree = true;
        savePlan();
    }

    /** Add one deduplicated site only after every reserved log has disappeared. */
    private void prepareReplantDebt() {
        if (treeFootprint.isEmpty() || expectedSapling == null || expectedSapling == Blocks.AIR) {
            companion.setJobStatus("job_status.modern_companions.needs_sapling");
            return;
        }
        List<BlockPos> footprint = treeFootprint.stream().map(BlockPos::immutable).distinct().toList();
        boolean alreadyQueued = replantDebt.stream().anyMatch(site -> site.sapling() == expectedSapling
                && site.remaining().containsAll(footprint) && footprint.containsAll(site.remaining()));
        if (!alreadyQueued) replantDebt.addLast(new ReplantSite(new ArrayDeque<>(footprint), expectedSapling));
    }

    private BlockPos nextReplantTarget() {
        ReplantSite site = replantDebt.peekFirst();
        return site == null || site.remaining().isEmpty() ? null : site.remaining().peekFirst();
    }

    private void normalizeReplantDebt() {
        boolean changed = false;
        Level level = companion.level();
        while (!replantDebt.isEmpty()) {
            ReplantSite site = replantDebt.peekFirst();
            while (!site.remaining().isEmpty()) {
                BlockPos target = site.remaining().peekFirst();
                if (!level.hasChunkAt(target)) break;
                if (level.getBlockState(target).getBlock() != site.sapling()) break;
                site.remaining().pollFirst();
                changed = true;
            }
            if (!site.remaining().isEmpty()) break;
            replantDebt.removeFirst();
            changed = true;
        }
        if (changed) savePlan();
    }

    private void moveToReplant() {
        BlockPos target = nextReplantTarget();
        if (target == null || standPos == null) return;
        companion.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D, 1.0D);
    }

    private void tickReplant() {
        ReplantSite site = replantDebt.peekFirst();
        BlockPos target = nextReplantTarget();
        if (site == null || target == null) return;
        if (!(companion.level() instanceof ServerLevel server) || !server.isLoaded(target)) {
            waiting("job_status.modern_companions.route_blocked");
            return;
        }
        if (standPos == null || !WorkerSite.isSafeStand(companion.level(), standPos)) {
            standPos = WorkerSite.findStand(companion, target, 2);
            if (standPos == null) {
                waiting("job_status.modern_companions.route_blocked");
                return;
            }
        }
        double distance = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (distance > 2.25D) {
            phase(JobPhase.COLLECTING, "job_status.modern_companions.replanting", target);
            if (companion.getNavigation().isDone()) moveToReplant();
            return;
        }
        if (!WorkerSite.canActFromStand(companion, target, standPos, WorkerSite.INTERACT_RANGE_SQR)) {
            retry("job_status.modern_companions.route_blocked", MAX_ACTION_RETRIES);
            return;
        }
        if (!companion.level().getBlockState(target).isAir()) {
            waiting("job_status.modern_companions.replant_blocked");
            return;
        }
        BlockState saplingState = site.sapling().defaultBlockState();
        if (!saplingState.canSurvive(companion.level(), target)) {
            // The recorded ground is gone; this one footprint is permanently invalid.
            site.remaining().pollFirst();
            companion.setJobStatus("job_status.modern_companions.replanting");
            normalizeReplantDebt();
            return;
        }
        companion.getLookControl().setLookAt(Vec3.atCenterOf(target));
        WorkerActionResult result = WorkerBlockActions.placeResult(companion, target, standPos, saplingState);
        if (result == WorkerActionResult.SUCCESS) {
            site.remaining().pollFirst();
            standPos = null;
            normalizeReplantDebt();
            savePlan();
            return;
        }
        if (result == WorkerActionResult.INVENTORY_FULL || result == WorkerActionResult.TOOL_MISSING) {
            companion.setJobStatus("job_status.modern_companions.needs_sapling");
        } else {
            retry(result == WorkerActionResult.PROTECTED
                    ? "job_status.modern_companions.farm_protected"
                    : "job_status.modern_companions.route_blocked", MAX_ACTION_RETRIES);
        }
        savePlan();
    }

    private void restorePlan() {
        if (restoredPlan) return;
        restoredPlan = true;
        CompoundTag payload = companion.getJobPlanPayload();
        treePlanActive = payload.getBoolean("TreePlanActive");
        if (payload.contains("TreeStump")) stumpPos = BlockPos.of(payload.getLong("TreeStump"));
        if (payload.contains("TreeTarget")) targetLog = BlockPos.of(payload.getLong("TreeTarget"));
        if (targetLog == null && treePlanActive) targetLog = companion.getJobCheckpointTarget().orElse(null);
        if (payload.contains("TreeStand")) standPos = BlockPos.of(payload.getLong("TreeStand"));
        expectedSapling = blockFromId(payload.getString("TreeSapling"));
        for (long raw : payload.getLongArray("TreeFootprint")) treeFootprint.add(BlockPos.of(raw));
        for (long raw : payload.getLongArray("TreeLogs")) {
            BlockPos log = BlockPos.of(raw);
            if (isTreeLog(log) && !log.equals(targetLog)) pendingLogs.add(log);
        }
        ListTag debt = payload.getList("ReplantDebt", Tag.TAG_COMPOUND);
        for (int index = 0; index < debt.size(); index++) {
            CompoundTag entry = debt.getCompound(index);
            Block sapling = blockFromId(entry.getString("Sapling"));
            if (sapling == null || sapling == Blocks.AIR) continue;
            ArrayDeque<BlockPos> remaining = new ArrayDeque<>();
            for (long raw : entry.getLongArray("Remaining")) remaining.add(BlockPos.of(raw));
            if (!remaining.isEmpty()) replantDebt.addLast(new ReplantSite(remaining, sapling));
        }
        if (treePlanActive && targetLog == null && pendingLogs.isEmpty()) finishFelledTree();
    }

    private Block blockFromId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id != null && BuiltInRegistries.BLOCK.containsKey(id) ? BuiltInRegistries.BLOCK.get(id) : null;
    }

    private void savePlan() {
        CompoundTag payload = companion.getJobPlanPayload();
        payload.remove("TreePlanActive");
        payload.remove("TreeStump");
        payload.remove("TreeTarget");
        payload.remove("TreeStand");
        payload.remove("TreeSapling");
        payload.remove("TreeFootprint");
        payload.remove("TreeLogs");
        payload.remove("ReplantDebt");
        payload.putBoolean("TreePlanActive", treePlanActive);
        if (stumpPos != null) payload.putLong("TreeStump", stumpPos.asLong());
        if (targetLog != null) payload.putLong("TreeTarget", targetLog.asLong());
        if (standPos != null) payload.putLong("TreeStand", standPos.asLong());
        if (expectedSapling != null && expectedSapling != Blocks.AIR) {
            payload.putString("TreeSapling", BuiltInRegistries.BLOCK.getKey(expectedSapling).toString());
        }
        if (!treeFootprint.isEmpty()) payload.putLongArray("TreeFootprint", treeFootprint.stream().mapToLong(BlockPos::asLong).toArray());
        if (!pendingLogs.isEmpty()) payload.putLongArray("TreeLogs", pendingLogs.stream().mapToLong(BlockPos::asLong).toArray());
        ListTag debt = new ListTag();
        for (ReplantSite site : replantDebt) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Sapling", BuiltInRegistries.BLOCK.getKey(site.sapling()).toString());
            entry.putLongArray("Remaining", site.remaining().stream().mapToLong(BlockPos::asLong).toArray());
            debt.add(entry);
        }
        if (!debt.isEmpty()) payload.put("ReplantDebt", debt);
        companion.setJobPlanPayload(payload);
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
        return JobToolPolicy.matches(CompanionJob.LUMBERJACK, companion.getMainHandItem());
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
