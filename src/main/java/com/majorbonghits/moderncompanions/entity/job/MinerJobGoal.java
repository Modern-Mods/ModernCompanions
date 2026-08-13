package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miner job: scan a 3D patrol cube for ores and tunnel to them using a
 * walkable staircase (never straight down). Keeps drops and leaves a
 * traversable tunnel (<=1 block deltas, 2-block headroom).
 */
public class MinerJobGoal extends ResumableJobGoal {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModernCompanions-Miner");
    private static final int SEARCH_COOLDOWN = 20;
    private static final int BREAK_COOLDOWN = 3;
    private static final int MAX_PLAN_STEPS = 8192;
    private static final TagKey<Block>[] ORE_TAGS = new TagKey[]{
            BlockTags.COAL_ORES, BlockTags.COPPER_ORES, BlockTags.IRON_ORES, BlockTags.GOLD_ORES,
            BlockTags.REDSTONE_ORES, BlockTags.LAPIS_ORES, BlockTags.DIAMOND_ORES, BlockTags.EMERALD_ORES,
            Tags.Blocks.ORES
    };

    private final AbstractHumanCompanionEntity companion;
    private final int baseRadius;
    private final boolean enabled;
    private final Set<Block> allowBlocks = new HashSet<>();
    private final Set<Block> denyBlocks = new HashSet<>();

    private BlockPos targetOre;
    private final List<BlockPos> oreQueue = new ArrayList<>();
    private int oreIndex = 0;
    private enum RouteAction { BREAK, WALK, PLACE }
    /** Destructive steps retain the feet cell approved during route planning. */
    private record RouteStep(RouteAction action, BlockPos pos, ResourceLocation blockId, BlockPos stand) {
        private RouteStep(RouteAction action, BlockPos pos) {
            this(action, pos, null, null);
        }

        private RouteStep(RouteAction action, BlockPos pos, ResourceLocation blockId) {
            this(action, pos, blockId, null);
        }
    }
    private record RouteNode(BlockPos pos, double cost, double score, BlockPos previous, int bridges) {}
    private final ArrayDeque<RouteStep> digQueue = new ArrayDeque<>();
    /** Feet cells actually entered on the approved excavation route. */
    private final List<BlockPos> returnRouteCells = new ArrayList<>();
    private boolean nativeReturnValidated;
    private int bridgePlacements;
    private int searchCooldown;
    private int breakTicksRemaining;
    private int swingCooldown;
    private int progressStallTicks = 0;
    private Vec3 lastProgressPos = Vec3.ZERO;
    private int globalRescanTicker = 0;
    private boolean sessionPlanned = false;
    private final Map<BlockPos, Long> oreBackoffUntil = new HashMap<>();
    private int lastActionTick = 0;
    private int idleTicks = 0;
    private BlockPos plannedCenter = BlockPos.ZERO;
    private int plannedRadius = 0;
    private int plannedUp = 0;
    private int plannedDown = 0;
    private boolean announcedNoWork = false;
    private static final int SURVEY_BUDGET = 4096;
    private boolean surveyInProgress;
    private int surveyColumn, surveyY, surveyMinY, surveyMaxY;
    private boolean restoredJobPlan;
    private boolean returnRouteBlocked;
    private static final int MAX_ROUTE_NODES = 8192;
    private static final int MAX_BRIDGE_PLACEMENTS = 4;
    private int actionBackoffTicks;
    private int failedActionAttempts;

    public MinerJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        super(companion, CompanionJob.MINER);
        this.companion = companion;
        this.baseRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        loadConfigBlockLists();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (!digQueue.isEmpty()) {
            if (targetOre != null && !reserveActiveOre()) {
                waiting("job_status.modern_companions.ore_reserved");
                return false;
            }
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling", digQueue.peekFirst() == null ? targetOre : digQueue.peekFirst().pos());
            return true;
        }
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        bootstrapPlan();
        boolean planned = tryPlanNextOre();
        searchCooldown = surveyInProgress ? 0 : SEARCH_COOLDOWN;
        if (planned && targetOre != null && !reserveActiveOre()) {
            waiting("job_status.modern_companions.ore_reserved");
            return false;
        }
        if (planned && targetOre != null) phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling", targetOre);
        return planned;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && (!digQueue.isEmpty() || !oreQueue.isEmpty());
    }

    @Override
    public void start() {
        lastProgressPos = companion.position();
        progressStallTicks = 0;
        moveToCurrentDigPos();
        lastActionTick = companion.tickCount;
    }

    @Override
    public void stop() {
        persistPlanProgress();
        // Keep durable ore/route checkpoint while higher-priority goals run.
        companion.getNavigation().stop();
        breakTicksRemaining = 0;
        swingCooldown = 0;
        progressStallTicks = 0;
        sessionPlanned = false;
        lastActionTick = 0;
        idleTicks = 0;
        announcedNoWork = false;
        actionBackoffTicks = 0;
        failedActionAttempts = 0;
        info("Goal stopped; state persisted (remaining=%d, mined=%d)", oreQueue.size(), companion.getMinerOresMined());
    }

    @Override
    public void tick() {
        if (actionBackoffTicks > 0) {
            actionBackoffTicks--;
            return;
        }
        if (digQueue.isEmpty()) {
            // Immediately replan so we never sit at "end of plan".
            if (!tryPlanNextOre()) {
                companion.getNavigation().stop();
                searchCooldown = 0; // force quick rescan
                idleTicks++;
                debug("Tick idle=%d (oreQueue=%d digQueue=%d mined=%d counted=%d)", idleTicks, oreQueue.size(), digQueue.size(), companion.getMinerOresMined(), companion.getMinerOresCounted());
                return;
            }
            idleTicks = 0;
            if (digQueue.isEmpty()) return;
        }
        RouteStep step = digQueue.peekFirst();
        BlockPos current = step.pos();
        idleTicks = 0;

        if (step.action() == RouteAction.WALK) {
            if (companion.blockPosition().equals(current)) {
                digQueue.pollFirst();
                recordEnteredRouteCell(current);
                tryPlaceTorch(current);
                // Revalidate the return route after entering a newly excavated
                // feet cell, not after each head/feet block in the same step.
                // Native path probes can be transient while the world updates;
                // blocking between those two breaks is what strands buried miners.
                returnRouteBlocked = !hasReturnPath();
                persistPlanProgress();
                lastActionTick = companion.tickCount;
                lastProgressPos = companion.position();
                progressStallTicks = 0;
                moveToCurrentDigPos();
            } else {
                phase(JobPhase.TRAVELLING, "job_status.modern_companions.advancing_tunnel", current);
                if (companion.getNavigation().isDone()) moveToCurrentDigPos();
                if (companion.position().distanceToSqr(lastProgressPos) < 0.04D) {
                    if (++progressStallTicks > 100) {
                        progressStallTicks = 0;
                        // Keep the approved route on a movement stall; a failed replan must
                        // never erase the next excavation operation.
                        companion.setJobStatus("job_status.modern_companions.route_blocked");
                    }
                } else {
                    lastProgressPos = companion.position();
                    progressStallTicks = 0;
                }
            }
            return;
        }

        if (step.action() == RouteAction.PLACE) {
            tickPlaceStep(step);
            return;
        }

        // If the current block was removed externally, pop and continue.
        if (companion.level().getBlockState(current).isAir()) {
            if (current.equals(targetOre)) {
                removeOreFromPlan(current);
                targetOre = null;
            }
            digQueue.pollFirst();
            persistPlanProgress();
            moveToCurrentDigPos();
            return;
        }

        if (step.action() == RouteAction.BREAK && returnRouteBlocked) {
            if (hasReturnPath()) {
                returnRouteBlocked = false;
            } else {
                companion.getNavigation().stop();
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return;
            }
        }

        // Hard pause detection: if we haven't swung or mined for a while, force a replan.
        if (companion.tickCount - lastActionTick > 60) {
            lastActionTick = companion.tickCount;
            info("Stall detected near %s (ore=%s digQueue=%d remaining=%d mined=%d)",
                    fmt(companion.blockPosition()), fmt(targetOre), digQueue.size(), oreQueue.size(), companion.getMinerOresMined());
            companion.getNavigation().stop();
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }

        // Navigate toward current dig position.
        if (companion.distanceToSqr(Vec3.atCenterOf(current)) > WorkerSite.INTERACT_RANGE_SQR) {
            // Let an existing native path run. Rebuilding it every tick resets
            // path progress and leaves buried miners permanently travelling.
            if (companion.position().distanceToSqr(lastProgressPos) >= 0.04D) {
                lastProgressPos = companion.position();
                progressStallTicks = 0;
                lastActionTick = companion.tickCount;
            } else if (++progressStallTicks > 100) {
                progressStallTicks = 0;
                companion.getNavigation().stop();
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return;
            }
            if (companion.getNavigation().isDone()) moveToCurrentDigPos();
            ensureDiggingProgress();
            return;
        }

        // Being within the target's interaction radius is not the same as
        // standing on the approved feet cell. Wait for navigation to reach the
        // actual action stand before starting the break timer; otherwise a
        // buried step can spend its entire timer swinging from an invalid tile.
        BlockPos actionStand = currentDigStand(step);
        if (actionStand == null) {
            breakTicksRemaining = 0;
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }
        if (companion.distanceToSqr(Vec3.atCenterOf(actionStand)) > 2.25D) {
            breakTicksRemaining = 0;
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.advancing_tunnel", actionStand);
            if (companion.getNavigation().isDone()) moveToCurrentDigPos();
            return;
        }

        // Stall detection: if we haven't moved meaningfully for a while, replan.
        if (companion.position().distanceToSqr(lastProgressPos) < 0.25) {
            progressStallTicks++;
            if (progressStallTicks > 100) {
                progressStallTicks = 0;
                info("Movement stall at %s (ore=%s digQueue=%d)", fmt(companion.blockPosition()), fmt(targetOre), digQueue.size());
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return;
            }
        } else {
            lastProgressPos = companion.position();
            progressStallTicks = 0;
        }

        // Break timing: swing, then decrement.
        if (breakTicksRemaining <= 0) {
            phase(JobPhase.WORKING, "job_status.modern_companions.mining", current);
            breakTicksRemaining = computeBreakTicks(current);
            swingCooldown = 0;
        }
        if (swingCooldown-- <= 0) {
            companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            swingCooldown = BREAK_COOLDOWN;
            lastActionTick = companion.tickCount;
        }
        breakTicksRemaining--;

        if (breakTicksRemaining <= 0) {
            WorkerActionResult result = mine(current, actionStand);
            if (result != WorkerActionResult.SUCCESS) {
                if (result == WorkerActionResult.INVALID_TARGET) {
                    if (current.equals(targetOre)) removeOreFromPlan(current);
                    digQueue.clear();
                    targetOre = null;
                    tryPlanNextOre();
                    moveToCurrentDigPos();
                    return;
                }
                companion.setJobStatus(result == WorkerActionResult.INVENTORY_FULL ? "job_status.modern_companions.inventory_full" : "job_status.modern_companions.mining_blocked");
                // Keep the target and queue, but do not swing at a protected or
                // changing block every tick while the world catches up.
                failedActionAttempts = Math.min(5, failedActionAttempts + 1);
                actionBackoffTicks = Math.min(200, 10 << Math.min(4, failedActionAttempts - 1));
                breakTicksRemaining = 0;
                moveToCurrentDigPos();
                return;
            }
            failedActionAttempts = 0;
            actionBackoffTicks = 0;
            digQueue.pollFirst();
            if (current.equals(targetOre) && !isOre(current)) targetOre = null;
            persistPlanProgress();
            progressStallTicks = 0;
            lastActionTick = companion.tickCount;
            if (digQueue.isEmpty()) {
                tryPlanNextOre();
            }
            moveToCurrentDigPos();
            ensureDiggingProgress();
        }
    }

    /* -------------------- Planning -------------------- */

    /**
     * Find the nearest mineable block. If oresOnly is true, restrict to ores; otherwise
     * also allow filler stone-like blocks that are accessible (air-adjacent) to start tunneling.
     */
    private BlockPos findNearestMineable(boolean oresOnly) {
        BlockPos center = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-hr, -down, -hr),
                center.offset(hr, up, hr))) {
            if (oresOnly) {
                if (!isOre(pos)) continue;
            } else {
                if (!(isOre(pos) || isFiller(pos))) continue;
                if (!isAccessibleStart(level, pos)) continue;
            }
            double dist = pos.distSqr(companion.blockPosition());
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    // One-time per patrol session: load persisted ore plan or resurvey the cube so we can resume after reloads.
    private void bootstrapPlan() {
        if (sessionPlanned) return;
        restoreJobPlan();
        loadPersistedPlan();
        if (oreQueue.isEmpty() || workAreaChanged()) {
            surveyAndPersist(true);
        } else {
            companion.setMinerOresCounted(oreQueue.size());
            companion.setMinerOresMined(0);
        }
        sessionPlanned = true;
    }

    private void loadPersistedPlan() {
        oreQueue.clear();
        oreQueue.addAll(companion.getMinerOreMemory());
        oreIndex = Math.min(companion.getMinerOreIndex(), Math.max(oreQueue.size() - 1, 0));
        plannedCenter = companion.getMinerPlanCenter();
        plannedRadius = companion.getMinerPlanRadius();
        plannedUp = companion.getMinerPlanUp();
        plannedDown = companion.getMinerPlanDown();
        pruneInvalidOres();
    }

    /** Restore only durable work facts; route paths are rebuilt when no route was saved. */
    private void restoreJobPlan() {
        if (restoredJobPlan) return;
        restoredJobPlan = true;
        CompoundTag payload = companion.getJobPlanPayload();
        if (payload.contains("MinerTargetOre")) targetOre = BlockPos.of(payload.getLong("MinerTargetOre"));
        ListTag route = payload.getList("MinerRoute", Tag.TAG_COMPOUND);
        for (int index = 0; index < route.size(); index++) {
            CompoundTag step = route.getCompound(index);
            RouteAction action;
            try {
                action = RouteAction.valueOf(step.getString("Action"));
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            ResourceLocation blockId = null;
            if (step.contains("Block")) blockId = ResourceLocation.tryParse(step.getString("Block"));
            if (step.contains("Pos")) {
                BlockPos stand = step.contains("Stand") ? BlockPos.of(step.getLong("Stand")) : null;
                digQueue.addLast(new RouteStep(action, BlockPos.of(step.getLong("Pos")), blockId, stand));
            }
        }
        long[] savedReturnCells = payload.getLongArray("MinerReturnCells");
        for (long raw : savedReturnCells) returnRouteCells.add(BlockPos.of(raw));
        nativeReturnValidated = payload.getBoolean("MinerReturnValidated");
        bridgePlacements = payload.getInt("MinerBridgePlacements");
        if (targetOre == null) targetOre = companion.getJobCheckpointTarget().orElse(null);
        validateRestoredRoute();
    }

    /** Revalidate durable route facts against the current contract before executing them. */
    private void validateRestoredRoute() {
        if (targetOre == null) {
            digQueue.clear();
            return;
        }
        if (!withinVolume(targetOre) || !companion.level().hasChunkAt(targetOre) || !isOre(targetOre)) {
            targetOre = null;
            digQueue.clear();
            return;
        }
        for (BlockPos routeCell : returnRouteCells) {
            if (!withinVolume(routeCell) || !companion.level().hasChunkAt(routeCell)
                    || !safeCell(companion.level(), routeCell, false)) {
                returnRouteCells.clear();
                nativeReturnValidated = false;
                break;
            }
        }
        if (digQueue.isEmpty()) return;
        ArrayDeque<RouteStep> valid = new ArrayDeque<>();
        for (RouteStep step : digQueue) {
            BlockPos pos = step.pos();
            if (!withinVolume(pos) || !companion.level().hasChunkAt(pos)) {
                digQueue.clear();
                return;
            }
            if (step.action() != RouteAction.WALK && step.stand() == null) {
                // Older route payloads did not retain an action stand. Rebuild
                // the route from the durable ore rather than allowing an
                // unbound operation to mine through a wall after reload.
                digQueue.clear();
                return;
            }
            if (step.action() == RouteAction.WALK) {
                if (!safeCell(companion.level(), pos, true)) {
                    digQueue.clear();
                    return;
                }
                valid.addLast(step);
                continue;
            }
            if (step.action() == RouteAction.PLACE) {
                if (step.stand() == null || !withinVolume(step.stand())
                        || !companion.level().hasChunkAt(step.stand())
                        || !safeCell(companion.level(), step.stand(), false)
                        || (!companion.level().getBlockState(pos).isAir()
                        && !isStableFloor(companion.level().getBlockState(pos), pos))) {
                    digQueue.clear();
                    return;
                }
                valid.addLast(step);
                continue;
            }
            BlockState state = companion.level().getBlockState(pos);
            if (step.stand() == null || !withinVolume(step.stand())
                    || !companion.level().hasChunkAt(step.stand())
                    || !safeCell(companion.level(), step.stand(), false)) {
                digQueue.clear();
                return;
            }
            if (state.isAir()) continue;
            if (!isMineableBlock(state) || isHazard(state)) {
                digQueue.clear();
                return;
            }
            valid.addLast(step);
        }
        digQueue.clear();
        digQueue.addAll(valid);
    }

    private boolean surveyAndPersist(boolean resetMined) {
        if (!surveyInProgress) {
            oreQueue.clear();
            oreBackoffUntil.clear();
            plannedCenter = workCenter();
            plannedRadius = horizontalRadius();
            plannedUp = verticalRadiusUp();
            plannedDown = verticalRadiusDown();
            surveyColumn = 0;
            surveyMinY = Math.max(companion.level().getMinBuildHeight(), plannedCenter.getY() - plannedDown);
            surveyY = surveyMinY;
            surveyMaxY = Math.min(companion.level().getMaxBuildHeight() - 1, plannedCenter.getY() + plannedUp);
            surveyInProgress = true;
            if (resetMined) companion.setMinerOresMined(0);
        }
        Level level = companion.level();
        int budget = SURVEY_BUDGET;
        while (surveyInProgress && budget-- > 0) {
            long offset = WorkerSafetyPredicates.spiralOffset(surveyColumn);
            BlockPos column = plannedCenter.offset((int) (offset >> 32), 0, (int) offset);
            BlockPos pos = new BlockPos(column.getX(), surveyY, column.getZ());
            if (level.hasChunkAt(pos) && isOreState(level.getBlockState(pos))) oreQueue.add(pos.immutable());
            advanceSurvey();
        }
        if (surveyInProgress) return false;
        oreQueue.sort(Comparator.comparingDouble(p -> p.distSqr(companion.blockPosition())));
        oreIndex = 0;
        companion.setMinerOreIndex(0);
        companion.overwriteMinerOreMemory(oreQueue);
        companion.setMinerPlanCenter(plannedCenter);
        companion.setMinerPlanRadius(plannedRadius);
        companion.setMinerPlanUp(plannedUp);
        companion.setMinerPlanDown(plannedDown);
        companion.setMinerOresCounted(oreQueue.size());
        announcedNoWork = oreQueue.isEmpty();
        info("Surveyed %d ores in cube center=%s r=%d up=%d down=%d (resetMined=%s)",
                oreQueue.size(), fmt(plannedCenter), plannedRadius, plannedUp, plannedDown, resetMined);
        return true;
    }

    private void advanceSurvey() {
        if (++surveyY <= surveyMaxY) return;
        surveyY = surveyMinY;
        int side = plannedRadius * 2 + 1;
        if (++surveyColumn >= side * side) surveyInProgress = false;
    }

    private void mergeNewlyFoundOres() {
        BlockPos center = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        Level level = companion.level();
        Set<BlockPos> known = new HashSet<>(oreQueue);
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-hr, -down, -hr),
                center.offset(hr, up, hr))) {
            BlockState state = level.getBlockState(pos);
            if (!isOreState(state)) continue;
            BlockPos copy = pos.immutable();
            if (known.add(copy)) {
                oreQueue.add(copy);
                found++;
            }
        }
        if (found > 0) {
            oreQueue.sort(Comparator.comparingDouble(p -> p.distSqr(companion.blockPosition())));
            companion.setMinerOresCounted(companion.getMinerOresCounted() + found);
            oreBackoffUntil.clear();
            persistPlanProgress();
            announcedNoWork = false;
            info("Merged %d newly seen ores; total now %d", found, oreQueue.size());
        }
    }

    private void pruneInvalidOres() {
        Level level = companion.level();
        boolean changed = false;
        for (int i = oreQueue.size() - 1; i >= 0; i--) {
            BlockPos pos = oreQueue.get(i);
            if (!withinVolume(pos)) {
                oreQueue.remove(i);
                changed = true;
                continue;
            }
            if (!isOreState(level.getBlockState(pos))) {
                oreQueue.remove(i);
                changed = true;
                debug("Pruned externally removed ore without counting it: %s", fmt(pos));
            }
        }
        if (changed) {
            clampOreIndex();
            persistPlanProgress();
            info("After prune: counted=%d mined=%d remaining=%d", companion.getMinerOresCounted(), companion.getMinerOresMined(), oreQueue.size());
        }
    }

    private void clampOreIndex() {
        if (oreQueue.isEmpty()) {
            oreIndex = 0;
        } else {
            oreIndex = Math.max(0, Math.min(oreIndex, oreQueue.size() - 1));
        }
        companion.setMinerOreIndex(oreIndex);
    }

    private boolean workAreaChanged() {
        return plannedRadius != horizontalRadius()
                || plannedUp != verticalRadiusUp()
                || plannedDown != verticalRadiusDown()
                || !plannedCenter.equals(workCenter());
    }

    private void persistPlanProgress() {
        companion.overwriteMinerOreMemory(oreQueue);
        companion.setMinerOreIndex(Math.max(0, Math.min(oreIndex, oreQueue.size() - 1)));
        plannedCenter = workCenter();
        plannedRadius = horizontalRadius();
        plannedUp = verticalRadiusUp();
        plannedDown = verticalRadiusDown();
        companion.setMinerPlanCenter(plannedCenter);
        companion.setMinerPlanRadius(plannedRadius);
        companion.setMinerPlanUp(plannedUp);
        companion.setMinerPlanDown(plannedDown);
        saveJobPlan();
    }

    private void saveJobPlan() {
        CompoundTag payload = companion.getJobPlanPayload();
        payload.remove("MinerTargetOre");
        payload.remove("MinerRoute");
        payload.remove("MinerReturnCells");
        if (targetOre != null) payload.putLong("MinerTargetOre", targetOre.asLong());
        if (!digQueue.isEmpty()) {
            ListTag route = new ListTag();
            for (RouteStep step : digQueue) {
                CompoundTag entry = new CompoundTag();
                entry.putString("Action", step.action().name());
                entry.putLong("Pos", step.pos().asLong());
                if (step.blockId() != null) entry.putString("Block", step.blockId().toString());
                if (step.stand() != null) entry.putLong("Stand", step.stand().asLong());
                route.add(entry);
            }
            payload.put("MinerRoute", route);
        }
        if (!returnRouteCells.isEmpty()) {
            payload.putLongArray("MinerReturnCells", returnRouteCells.stream().mapToLong(BlockPos::asLong).toArray());
        }
        payload.putBoolean("MinerReturnValidated", nativeReturnValidated);
        payload.putInt("MinerBridgePlacements", bridgePlacements);
        companion.setJobPlanPayload(payload);
    }

    private void notifyNoWork() {
        if (announcedNoWork) return;
        announcedNoWork = true;
        if (!(companion.getOwner() instanceof net.minecraft.world.entity.player.Player player)) return;
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("chat.type.text",
                companion.getDisplayName(),
                net.minecraft.network.chat.Component.translatable("chat.modern_companions.miner.no_ore")));
        info("No ores left within patrol cube; notifying owner %s", player.getScoreboardName());
    }

    // A failed native path is not an excavation instruction; leave the site and choose another ore.
    private void ensureDiggingProgress() {
        if (digQueue.isEmpty()) return;
        BlockPos current = digQueue.peekFirst().pos();
        double dist = companion.distanceToSqr(Vec3.atCenterOf(current));
        if (dist < 9.0D) return; // already in range to swing
        // Retry the approved plan once; never force a direct tunnel through terrain.
        if (companion.getNavigation().isDone()) {
            moveToCurrentDigPos();
        }
    }

    private void abandonCurrentOre() {
        if (targetOre != null) backoffOre(targetOre);
        releaseActiveOreReservations();
        digQueue.clear();
        targetOre = null;
        companion.getNavigation().stop();
    }

    private void dumpDigQueue() {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (RouteStep step : digQueue) {
            if (i++ >= 8) break; // limit noise
            BlockPos p = step.pos();
            sb.append("[").append(fmt(p)).append(":").append(companion.level().getBlockState(p).getBlock().getName().getString()).append("] ");
        }
        info("Queue peek (len=%d): %s", digQueue.size(), sb.toString());
    }

    private void debug(String msg, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(tagged(String.format(msg, args)));
        }
    }

    private void info(String msg, Object... args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(tagged(String.format(msg, args)));
        }
    }

    private String tagged(String msg) {
        return "[Miner " + companion.getId() + "] " + msg;
    }

    private String fmt(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /**
     * Attempt to build a plan to the next reachable ore; if none found, return false.
     * Falls back to the nearest accessible filler start if no ore exists.
     */
    private boolean tryPlanNextOre() {
        bootstrapPlan();
        pruneInvalidOres();
        if (surveyInProgress) {
            surveyAndPersist(false);
        } else if (workAreaChanged()) {
            surveyAndPersist(true);
        }
        if (oreQueue.isEmpty()) {
            if (surveyInProgress) return false;
            notifyNoWork();
            companion.getNavigation().stop();
            searchCooldown = 0;
            return false;
        }

        // Reloads retain only a durable ore position; rebuild volatile route nodes from it first.
        if (targetOre == null) {
            companion.getJobCheckpointTarget().filter(this::isOre).filter(this::withinVolume).ifPresent(saved -> targetOre = saved);
        }
        if (targetOre != null && !isOre(targetOre)) {
            removeOreFromPlan(targetOre);
            targetOre = null;
        }
        if (targetOre != null && !isOreBackedOff(targetOre) && planPathToOre(targetOre)) {
            persistPlanProgress();
            oreBackoffUntil.remove(targetOre);
            return true;
        }

        clampOreIndex();
        boolean sawRetryableOre = false;
        boolean sawBackedOffOre = false;
        for (int i = oreIndex; i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (isOreBackedOff(ore)) {
                sawBackedOffOre = true;
                continue;
            }
            sawRetryableOre = true;
            debug("Planning path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                oreBackoffUntil.remove(ore);
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore route blocked; backing off and continuing: %s", fmt(ore));
            backoffOre(ore);
        }

        for (int i = 0; i < oreIndex && i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (isOreBackedOff(ore)) {
                sawBackedOffOre = true;
                continue;
            }
            sawRetryableOre = true;
            debug("Planning wrap-around path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                oreBackoffUntil.remove(ore);
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore route blocked; backing off and continuing: %s", fmt(ore));
            backoffOre(ore);
        }

        if (sawRetryableOre || sawBackedOffOre) {
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return false;
        }
        notifyNoWork();
        companion.getNavigation().stop();
        searchCooldown = 0; // allow quick retry next tick
        return false;
    }

    private boolean planPathToOre(BlockPos ore) {
        if (ore == null) return false;
        digQueue.clear();
        // Keep the entered feet cells as the miner's proven way home. Clearing
        // them here makes every subsequent ore look like a fresh surface job;
        // native pathfinding then cannot see through the already excavated
        // tunnel and the miner remains stuck at the first target.
        if (returnRouteCells.isEmpty()) nativeReturnValidated = false;
        bridgePlacements = 0;
        BlockPos cursor = companion.blockPosition();
        Level level = companion.level();
        if (!WorkerSite.isSafeStand(level, cursor)) return false;
        // Do not begin irreversible digging without a route back from current safe ground.
        if (!hasReturnPath()) return false;

        // Existing caves are cheaper and safer than excavation, so use native navigation first.
        BlockPos caveStand = WorkerSite.findApproachStand(companion, ore, 2);
        if (caveStand != null) {
            if (companion.distanceToSqr(Vec3.atCenterOf(caveStand)) > 2.25D) {
                digQueue.addLast(new RouteStep(RouteAction.WALK, caveStand));
            }
            digQueue.addLast(new RouteStep(RouteAction.BREAK, ore, null, caveStand.immutable()));
            return true;
        }

        List<BlockPos> route = findRouteToOreApproach(ore, cursor);
        if (route == null) return false;
        debug("Planning bounded route to %s from %s (%d feet cells)", fmt(ore), fmt(cursor), route.size());
        nativeReturnValidated = true;
        returnRouteCells.add(cursor.immutable());
        for (BlockPos next : route) {
            if (!enqueueStep(cursor, next, level)) {
                // A route that cannot queue every required edit is not a
                // route. Leave the ore for another bounded planning attempt.
                digQueue.clear();
                returnRouteCells.clear();
                nativeReturnValidated = false;
                bridgePlacements = 0;
                return false;
            }
            cursor = next;
        }
        if (!withinVolume(ore)) return false;
        digQueue.addLast(new RouteStep(RouteAction.BREAK, ore, null, cursor.immutable()));
        debug("Planned %d dig steps toward %s", digQueue.size(), fmt(ore));
        return !digQueue.isEmpty();
    }

    /**
     * Bounded best-first search over feet cells.  Air/cave cells win on cost;
     * solid cells are admitted only when both body blocks are mineable and the
     * supporting floor is stable or can receive one supplied bridge block.
     */
    private List<BlockPos> findRouteToOreApproach(BlockPos ore, BlockPos start) {
        Level level = companion.level();
        Set<BlockPos> goals = new HashSet<>();
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos candidate = ore.relative(direction);
            if (withinVolume(candidate) && canRouteCell(level, candidate, true)) goals.add(candidate.immutable());
        }
        if (goals.isEmpty()) return null;
        int margin = 8;
        int minX = Math.min(start.getX(), ore.getX()) - margin;
        int maxX = Math.max(start.getX(), ore.getX()) + margin;
        int minY = Math.min(start.getY(), ore.getY()) - margin;
        int maxY = Math.max(start.getY(), ore.getY()) + margin;
        int minZ = Math.min(start.getZ(), ore.getZ()) - margin;
        int maxZ = Math.max(start.getZ(), ore.getZ()) + margin;

        PriorityQueue<RouteNode> open = new PriorityQueue<>(Comparator.comparingDouble(RouteNode::score));
        Map<BlockPos, Double> bestCost = new HashMap<>();
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        open.add(new RouteNode(start.immutable(), 0.0D, routeHeuristic(start, goals), null, 0));
        bestCost.put(start.immutable(), 0.0D);
        int visited = 0;
        while (!open.isEmpty() && visited++ < MAX_ROUTE_NODES) {
            RouteNode node = open.poll();
            if (goals.contains(node.pos())) return rebuildRoute(previous, start, node.pos());
            if (node.cost() > bestCost.getOrDefault(node.pos(), Double.MAX_VALUE)) continue;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = node.pos().offset(dx, dy, dz);
                        if (next.getX() < minX || next.getX() > maxX || next.getY() < minY || next.getY() > maxY
                                || next.getZ() < minZ || next.getZ() > maxZ || !withinVolume(next)
                                || !MinerRouteRules.isStairStep(node.pos(), next)) continue;
                        boolean needsBridge = !isStableFloor(level.getBlockState(next.below()), next.below());
                        int bridges = node.bridges() + (needsBridge ? 1 : 0);
                        if (needsBridge && !MinerRouteRules.bridgeBudgetAvailable(bridges - 1, MAX_BRIDGE_PLACEMENTS)) continue;
                        if (!canRouteCell(level, next, true)) continue;
                        double cost = node.cost() + routeStepCost(level, next, needsBridge, dy);
                        if (cost >= bestCost.getOrDefault(next, Double.MAX_VALUE)) continue;
                        BlockPos copy = next.immutable();
                        bestCost.put(copy, cost);
                        previous.put(copy, node.pos());
                        open.add(new RouteNode(copy, cost, cost + routeHeuristic(copy, goals), node.pos(), bridges));
                    }
                }
            }
        }
        return null;
    }

    /** Lower-bound estimate for cardinal feet-cell travel; keeps long radii from exhausting the node cap. */
    private double routeHeuristic(BlockPos pos, Set<BlockPos> goals) {
        long best = Long.MAX_VALUE;
        for (BlockPos goal : goals) {
            long horizontal = Math.abs((long) goal.getX() - pos.getX())
                    + Math.abs((long) goal.getZ() - pos.getZ());
            long vertical = Math.abs((long) goal.getY() - pos.getY());
            best = Math.min(best, Math.max(horizontal, vertical));
        }
        return best == Long.MAX_VALUE ? 0.0D : best;
    }

    private List<BlockPos> rebuildRoute(Map<BlockPos, BlockPos> previous, BlockPos start, BlockPos goal) {
        List<BlockPos> route = new ArrayList<>();
        BlockPos cursor = goal;
        while (cursor != null && !cursor.equals(start)) {
            route.add(cursor.immutable());
            cursor = previous.get(cursor);
        }
        if (cursor == null) return null;
        java.util.Collections.reverse(route);
        return route;
    }

    private double routeStepCost(Level level, BlockPos pos, boolean needsBridge, int verticalDelta) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        boolean existingAir = feet.isAir() && head.isAir();
        float hardness = Math.max(0.0F, feet.getDestroySpeed(level, pos))
                + Math.max(0.0F, head.getDestroySpeed(level, pos.above()));
        return MinerRouteRules.stepCost(existingAir, needsBridge, hardness, verticalDelta);
    }

    private BlockPos findOreApproach(BlockPos ore, BlockPos from) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos candidate = ore.relative(direction);
            if (!withinVolume(candidate) || !safeCell(companion.level(), candidate)) continue;
            double distance = candidate.distSqr(from);
            if (distance < bestDistance) {
                best = candidate.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * Adds the floor/headroom blocks for a step into the dig queue if they are solid and
     * mineable (ore or filler). Leaves air untouched.
     */
    private boolean enqueueStep(BlockPos from, BlockPos to, Level level) {
        if (!MinerRouteRules.isStairStep(from, to)) return false;
        BlockState floor = level.getBlockState(to.below());
        if (!isStableFloor(floor, to.below())) {
            Block bridge = findBridgeBlock(to.below());
            if (bridge == null || bridgePlacements >= MAX_BRIDGE_PLACEMENTS) return false;
            digQueue.addLast(new RouteStep(RouteAction.PLACE, to.below().immutable(),
                    BuiltInRegistries.BLOCK.getKey(bridge), from.immutable()));
            bridgePlacements++;
        }
        // `to` is the future feet cell. Its floor is `to.below()` and must stay intact.
        // Descending exposes the upper face first; other steps expose the feet block first.
        if (WorkerSafetyPredicates.excavationHeadFirst(from.getY(), to.getY())) {
            if (!addIfMineable(level, to.above(), from) || !addIfMineable(level, to, from)) return false;
        } else {
            if (!addIfMineable(level, to, from) || !addIfMineable(level, to.above(), from)) return false;
        }
        digQueue.addLast(new RouteStep(RouteAction.WALK, to.immutable()));
        return true;
    }

    private boolean addIfMineable(Level level, BlockPos pos, BlockPos stand) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return true;
        if (isMineableBlock(state)) {
            digQueue.addLast(new RouteStep(RouteAction.BREAK, pos.immutable(), null, stand.immutable()));
            return true;
        }
        return false;
    }

    private boolean safeStep(Level level, BlockPos from, BlockPos to) {
        return MinerRouteRules.isStairStep(from, to) && safeCell(level, to, true);
    }

    private boolean safeCell(Level level, BlockPos to) {
        return safeCell(level, to, false);
    }

    private boolean safeCell(Level level, BlockPos to, boolean allowBridge) {
        BlockState feet = level.getBlockState(to);
        BlockState head = level.getBlockState(to.above());
        BlockState ceiling = level.getBlockState(to.above(2));
        BlockState floor = level.getBlockState(to.below());
        return canOpen(feet) && canOpen(head)
                && !isHazard(feet) && !isHazard(head) && !isHazard(ceiling) && !isHazard(floor)
                && !hasAdjacentFluid(level, to) && !hasAdjacentFluid(level, to.above())
                && !(feet.getBlock() instanceof FallingBlock)
                && !(head.getBlock() instanceof FallingBlock)
                && !(ceiling.getBlock() instanceof FallingBlock)
                && (isStableFloor(floor, to.below()) || allowBridge && findBridgeBlock(to.below()) != null);
    }

    private boolean canRouteCell(Level level, BlockPos to, boolean allowBridge) {
        return level.hasChunkAt(to) && safeCell(level, to, allowBridge);
    }

    private boolean isStableFloor(BlockState floor, BlockPos pos) {
        return !floor.isAir() && !isHazard(floor)
                && !(floor.getBlock() instanceof FallingBlock)
                && !floor.hasBlockEntity()
                && floor.isFaceSturdy(companion.level(), pos, net.minecraft.core.Direction.UP);
    }

    private Block findBridgeBlock(BlockPos target) {
        if (bridgePlacements >= MAX_BRIDGE_PLACEMENTS) return null;
        for (int slot = -1; slot < companion.getInventory().getContainerSize(); slot++) {
            ItemStack stack = slot < 0 ? companion.getMainHandItem() : companion.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof BlockItem blockItem) || stack.isEmpty()) continue;
            Block block = blockItem.getBlock();
            if (isBridgeBlock(block, target)) return block;
        }
        return null;
    }

    private BlockState placementState(RouteStep step) {
        if (step.blockId() != null) {
            Block block = BuiltInRegistries.BLOCK.get(step.blockId());
            if (block != null && isBridgeBlock(block, step.pos())) return block.defaultBlockState();
        }
        Block block = findBridgeBlock(step.pos());
        return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    private boolean isBridgeBlock(Block block, BlockPos target) {
        if (block == null || block == Blocks.AIR || block instanceof FallingBlock) return false;
        BlockState state = block.defaultBlockState();
        return state.getFluidState().isEmpty()
                && !state.hasBlockEntity()
                && !state.is(BlockTags.LEAVES)
                && !state.getCollisionShape(companion.level(), target).isEmpty()
                && state.isCollisionShapeFullBlock(companion.level(), target)
                && state.canSurvive(companion.level(), target);
    }

    private boolean canOpen(BlockState state) {
        return state.isAir() || isMineableBlock(state);
    }

    private boolean hasAdjacentFluid(Level level, BlockPos pos) {
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            if (!level.getFluidState(pos.relative(direction)).isEmpty()) return true;
        }
        return false;
    }

    /* -------------------- Block classification -------------------- */

    private boolean isOre(BlockPos pos) {
        return isOreState(companion.level().getBlockState(pos));
    }

    private boolean isOreState(BlockState state) {
        if (state.is(Tags.Blocks.ORES)) return true;
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) return true;
        }
        return false;
    }

    private boolean isMineableBlock(BlockState state) {
        Block block = state.getBlock();
        if (!allowBlocks.isEmpty() && !allowBlocks.contains(block)) return false;
        if (denyBlocks.contains(block)) return false;

        // Ores are always valid targets.
        if (state.is(Tags.Blocks.ORES)) return true;
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) return true;
        }

        // Filler materials we can tunnel through.
        if (state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.BASE_STONE_OVERWORLD)) return true;
        if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MOSSY_COBBLESTONE)) return true;
        return false;
    }

    private boolean isFiller(BlockPos pos) {
        BlockState state = companion.level().getBlockState(pos);
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private boolean isAccessibleStart(Level level, BlockPos pos) {
        // Require some air neighbor so we aren't picking a buried block we cannot path to.
        for (BlockPos air : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(air).isAir()) {
                return true;
            }
        }
        return false;
    }

    private boolean isHazard(BlockState state) {
        // Any fluid is unsafe for a mine route; workers never step into water or lava.
        if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.MAGMA_BLOCK)) return true;
        if (!state.getFluidState().isEmpty()) return true;
        return false;
    }

    /* -------------------- Movement & mining -------------------- */

    private void moveToCurrentDigPos() {
        RouteStep step = digQueue.peekFirst();
        if (step == null) return;
        BlockPos current = step.pos();
        if (step.action() == RouteAction.WALK) {
            if (!safeCell(companion.level(), current, false)) {
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return;
            }
            companion.getNavigation().moveTo(current.getX() + 0.5D, current.getY(), current.getZ() + 0.5D, 1.05D);
            return;
        }
        BlockPos stand = currentDigStand(step);
        if (stand == null) {
            // Preserve ore and route for retry after a transient obstruction/path update.
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }
        debug("Navigating toward dig target %s (stand at %s)", fmt(current), fmt(stand));
        companion.getNavigation().moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 1.05D);
    }

    private void tickPlaceStep(RouteStep step) {
        BlockPos target = step.pos();
        if (returnRouteBlocked && !hasReturnPath()) {
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }
        BlockState existing = companion.level().getBlockState(target);
        if (!existing.isAir()) {
            if (isStableFloor(existing, target)) {
                digQueue.pollFirst();
                persistPlanProgress();
                moveToCurrentDigPos();
            } else {
                companion.setJobStatus("job_status.modern_companions.bridge_blocked");
            }
            return;
        }
        BlockPos stand = currentDigStand(step);
        if (stand == null) {
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }
        if (companion.distanceToSqr(Vec3.atCenterOf(stand)) > 2.25D) {
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.advancing_tunnel", stand);
            if (companion.getNavigation().isDone()) moveToCurrentDigPos();
            return;
        }
        phase(JobPhase.WORKING, "job_status.modern_companions.bridging", target);
        companion.getLookControl().setLookAt(Vec3.atCenterOf(target));
        BlockState placement = placementState(step);
        if (placement.isAir()) {
            companion.setJobStatus("job_status.modern_companions.no_bridge_blocks");
            return;
        }
        WorkerActionResult result = WorkerBlockActions.placePlannedResult(companion, target, stand, placement);
        if (result != WorkerActionResult.SUCCESS) {
            companion.setJobStatus(result == WorkerActionResult.INVENTORY_FULL
                    ? "job_status.modern_companions.inventory_full"
                    : result == WorkerActionResult.TOOL_MISSING
                    ? "job_status.modern_companions.no_bridge_blocks"
                    : "job_status.modern_companions.bridge_blocked");
            return;
        }
        digQueue.pollFirst();
        persistPlanProgress();
        moveToCurrentDigPos();
    }

    private void recordEnteredRouteCell(BlockPos cell) {
        if (returnRouteCells.isEmpty() || !returnRouteCells.get(returnRouteCells.size() - 1).equals(cell)) {
            returnRouteCells.add(cell.immutable());
        }
    }

    /** Optional fixed-spacing lighting using only supplied vanilla torch items. */
    private void tryPlaceTorch(BlockPos feet) {
        if (returnRouteCells.size() < 2 || returnRouteCells.size() % 8 != 0 || !hasTorch()) return;
        Level level = companion.level();
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos target = feet.relative(direction);
            if (!level.getBlockState(target).isAir()
                    || !level.getBlockState(target.below()).isFaceSturdy(level, target.below(), net.minecraft.core.Direction.UP)
                    || !level.getBlockState(target).canBeReplaced()) continue;
            if (returnRouteCells.stream().anyMatch(cell -> cell.equals(target))) continue;
            WorkerActionResult result = WorkerBlockActions.placePlannedResult(
                    companion, target, feet, Blocks.TORCH.defaultBlockState());
            if (result == WorkerActionResult.SUCCESS) return;
        }
    }

    private boolean hasTorch() {
        if (companion.getMainHandItem().is(Items.TORCH)) return true;
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            if (companion.getInventory().getItem(slot).is(Items.TORCH)) return true;
        }
        return false;
    }

    private Optional<BlockPos> findAdjacentAir(BlockPos target) {
        Level level = companion.level();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-1, -1, -1), target.offset(1, 1, 1))) {
            if (!level.getBlockState(pos).isAir()) continue;
            double d = pos.distSqr(companion.blockPosition());
            if (d < bestDist) {
                bestDist = d;
                best = pos.immutable();
            }
        }
        return Optional.ofNullable(best);
    }

    private WorkerActionResult mine(BlockPos pos, BlockPos stand) {
        if (!(companion.level() instanceof ServerLevel server)) return WorkerActionResult.RETRYABLE_BLOCKED;
        BlockState state = server.getBlockState(pos);
        if (!isMineableBlock(state) || isHazard(state) || state.getBlock() instanceof FallingBlock) {
            return WorkerActionResult.INVALID_TARGET;
        }
        boolean wasOre = isOreState(state);
        if (stand == null) return WorkerActionResult.RETRYABLE_BLOCKED;
        WorkerActionResult result = WorkerBlockActions.breakPlannedExcavationBlock(
                companion, pos, stand, WorkerSite.INTERACT_RANGE_SQR);
        if (result != WorkerActionResult.SUCCESS) return result;
        // The block action succeeded, so the queue may advance, but a changed
        // route must never discard the remaining approved excavation plan.
        if (wasOre) {
            companion.incrementMinerOresMined();
            removeOreFromPlan(pos);
            info("Mined ore at %s (mined=%d / counted=%d / remaining=%d)",
                    fmt(pos), companion.getMinerOresMined(), companion.getMinerOresCounted(), oreQueue.size());
        }
        return WorkerActionResult.SUCCESS;
    }

    private boolean reserveActiveOre() {
        if (targetOre == null || !(companion.level() instanceof ServerLevel server)) return true;
        String oreKey = "ore:" + targetOre.asLong();
        if (!reserve(oreKey)) return false;
        String routeKey = "route:" + targetOre.asLong();
        if (reserve(routeKey)) return true;
        JobReservations.release(server, ReservationType.BLOCK, oreKey, companion.getUUID());
        return false;
    }

    private void releaseActiveOreReservations() {
        if (!(companion.level() instanceof ServerLevel server) || targetOre == null) return;
        String oreKey = "ore:" + targetOre.asLong();
        String routeKey = "route:" + targetOre.asLong();
        JobReservations.release(server, ReservationType.BLOCK, oreKey, companion.getUUID());
        JobReservations.release(server, ReservationType.ROUTE, routeKey, companion.getUUID());
    }

    /** Only use the feet cell recorded for the queued operation; never mine from a new wall-side stand. */
    private BlockPos currentDigStand(RouteStep step) {
        BlockPos stand = step.stand();
        if (stand == null || !withinVolume(stand) || !companion.level().hasChunkAt(stand)
                || !safeCell(companion.level(), stand, false)) return null;
        return stand;
    }

    /** Chest itself is solid; return navigation must target any reachable safe adjacent feet cell. */
    private boolean hasReturnPath() {
        if (companion.getWorkCenter().isEmpty()) return false;
        if (!returnRouteCells.isEmpty()) {
            int currentIndex = -1;
            for (int index = returnRouteCells.size() - 1; index >= 0; index--) {
                if (returnRouteCells.get(index).equals(companion.blockPosition())) {
                    currentIndex = index;
                    break;
                }
            }
            if (currentIndex < 0 || !nativeReturnValidated) return false;
            for (int index = currentIndex; index >= 0; index--) {
                BlockPos cell = returnRouteCells.get(index);
                if (!withinVolume(cell) || !companion.level().hasChunkAt(cell)
                        || !safeCell(companion.level(), cell, false)) return false;
                if (index > 0 && !MinerRouteRules.isStairStep(returnRouteCells.get(index - 1), cell)) return false;
            }
            // The original chest route was validated before excavation.  The
            // controlled tunnel cells above are the only new navigation facts;
            // probing the native path from the buried position would reject a
            // valid route until the tunnel is fully visible to vanilla pathing.
            return true;
        }
        BlockPos chest = workCenter();
        for (BlockPos stand : BlockPos.betweenClosed(chest.offset(-2, -1, -2), chest.offset(2, 1, 2))) {
            if (!WorkerSite.isSafeStand(companion.level(), stand)
                    || Vec3.atCenterOf(stand).distanceToSqr(Vec3.atCenterOf(chest)) > WorkerSite.INTERACT_RANGE_SQR) {
                continue;
            }
            var path = companion.getNavigation().createPath(stand, 0);
            if (path != null && path.canReach()) return true;
        }
        return false;
    }

    private void removeOreFromPlan(BlockPos pos) {
        boolean changed = oreQueue.remove(pos);
        changed = companion.getMinerOreMemory().remove(pos) || changed;
        oreBackoffUntil.remove(pos);
        if (pos.equals(targetOre)) releaseActiveOreReservations();
        if (changed) {
            clampOreIndex();
            persistPlanProgress();
        }
    }

    /* -------------------- Job state -------------------- */

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.MINER) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        companion.ensureJobToolEquipped();
        if (!JobToolPolicy.matches(CompanionJob.MINER, companion.getMainHandItem())) { companion.setJobStatus("job_status.modern_companions.no_pickaxe"); return false; }
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    private void loadConfigBlockLists() {
        List<? extends String> allowIds = ModConfig.safeGet(ModConfig.JOB_MINER_ALLOW_BLOCKS);
        List<? extends String> denyIds = ModConfig.safeGet(ModConfig.JOB_MINER_DENY_BLOCKS);
        resolveBlocksInto(allowIds, allowBlocks);
        resolveBlocksInto(denyIds, denyBlocks);
    }

    private void resolveBlocksInto(List<? extends String> ids, Set<Block> targetSet) {
        if (ids == null) return;
        for (String raw : ids) {
            try {
                ResourceLocation id = ResourceLocation.parse(raw);
                Block block = BuiltInRegistries.BLOCK.get(id);
                if (block != null && !block.defaultBlockState().isAir()) {
                    targetSet.add(block);
                }
            } catch (Exception ignored) {
                // Keep running on malformed ids.
            }
        }
    }

    private boolean hasPickaxe() {
        return JobToolPolicy.has(companion, CompanionJob.MINER);
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

    /* -------------------- Volume helpers -------------------- */

    private BlockPos workCenter() {
        return companion.getWorkCenter().orElse(companion.blockPosition());
    }

    private int horizontalRadius() {
        return Math.min(128, Math.max(baseRadius, companion.getPatrolRadius()));
    }

    private int verticalRadiusUp() {
        return Math.min(64, Math.max(16, companion.getPatrolRadius()));
    }

    private int verticalRadiusDown() {
        return Math.min(128, Math.max(16, companion.getPatrolRadius()));
    }

    private boolean withinVolume(BlockPos pos) {
        BlockPos c = workCenter();
        int hr = horizontalRadius();
        int up = verticalRadiusUp();
        int down = verticalRadiusDown();
        return pos.getX() >= c.getX() - hr && pos.getX() <= c.getX() + hr
                && pos.getZ() >= c.getZ() - hr && pos.getZ() <= c.getZ() + hr
                && pos.getY() >= c.getY() - down && pos.getY() <= c.getY() + up;
    }

    private boolean isOreBackedOff(BlockPos pos) {
        if (!(companion.level() instanceof ServerLevel server)) return false;
        Long retryAt = oreBackoffUntil.get(pos);
        if (retryAt == null) return false;
        if (retryAt <= server.getGameTime()) {
            oreBackoffUntil.remove(pos);
            return false;
        }
        return true;
    }

    private void backoffOre(BlockPos pos) {
        if (pos == null || !(companion.level() instanceof ServerLevel server)) return;
        oreBackoffUntil.put(pos.immutable(), server.getGameTime() + 100L);
    }

    /* -------------------- Break timing -------------------- */

    private int computeBreakTicks(BlockPos pos) {
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        ItemStack tool = companion.getMainHandItem();
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return 40;
        float speed = tool.getDestroySpeed(state);
        if (!tool.isCorrectToolForDrops(state)) {
            speed = Math.max(1.0F, speed / 3.0F);
        }
        float relative = speed > 0 ? (speed / hardness) : 0.05F;
        int ticks = (int) Math.ceil(20.0F / Math.max(0.05F, relative));
        ticks = Math.max(20, Math.min(120, ticks * 2));
        return ticks;
    }
}
