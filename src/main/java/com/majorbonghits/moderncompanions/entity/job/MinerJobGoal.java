package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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
    private enum RouteAction { BREAK, WALK }
    private record RouteStep(RouteAction action, BlockPos pos) {}
    private final ArrayDeque<RouteStep> digQueue = new ArrayDeque<>();
    private int searchCooldown;
    private int breakTicksRemaining;
    private int swingCooldown;
    private int progressStallTicks = 0;
    private Vec3 lastProgressPos = Vec3.ZERO;
    private int globalRescanTicker = 0;
    private boolean sessionPlanned = false;
    private final Set<BlockPos> unreachableOres = new HashSet<>();
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
            if (targetOre != null && !reserve("ore:" + targetOre.asLong())) {
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
        if (planned && targetOre != null && !reserve("ore:" + targetOre.asLong())) {
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
        info("Goal stopped; state persisted (remaining=%d, mined=%d)", oreQueue.size(), companion.getMinerOresMined());
    }

    @Override
    public void tick() {
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
                        if (!planPathToOre(targetOre)) companion.setJobStatus("job_status.modern_companions.route_blocked");
                    }
                } else {
                    lastProgressPos = companion.position();
                    progressStallTicks = 0;
                }
            }
            return;
        }

        // If the current block was removed externally, pop and continue.
        if (companion.level().getBlockState(current).isAir()) {
            digQueue.pollFirst();
            moveToCurrentDigPos();
            return;
        }

        // Hard pause detection: if we haven't swung or mined for a while, force a replan.
        if (companion.tickCount - lastActionTick > 60) {
            lastActionTick = companion.tickCount;
            info("Stall detected near %s (ore=%s digQueue=%d remaining=%d mined=%d)",
                    fmt(companion.blockPosition()), fmt(targetOre), digQueue.size(), oreQueue.size(), companion.getMinerOresMined());
            if (!tryPlanNextOre()) {
                companion.getNavigation().stop();
                searchCooldown = 0;
            }
            abandonCurrentOre();
            dumpDigQueue();
            return;
        }

        // Navigate toward current dig position.
        if (companion.distanceToSqr(Vec3.atCenterOf(current)) > WorkerSite.INTERACT_RANGE_SQR) {
            moveToCurrentDigPos();
            ensureDiggingProgress();
            return;
        }
        // Stall detection: if we haven't moved meaningfully for a while, replan.
        if (companion.position().distanceToSqr(lastProgressPos) < 0.25) {
            progressStallTicks++;
            if (progressStallTicks > 100) {
                progressStallTicks = 0;
                info("Movement stall at %s (ore=%s digQueue=%d)", fmt(companion.blockPosition()), fmt(targetOre), digQueue.size());
                if (!planPathToOre(targetOre)) {
                    companion.setJobStatus("job_status.modern_companions.route_blocked");
                }
                return;
            }
        } else {
            lastProgressPos = companion.position();
            progressStallTicks = 0;
        }

        if (currentDigStand(current) == null) {
            breakTicksRemaining = 0;
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
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
            WorkerActionResult result = mine(current);
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
                breakTicksRemaining = 0;
                moveToCurrentDigPos();
                return;
            }
            digQueue.pollFirst();
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

    private boolean surveyAndPersist(boolean resetMined) {
        if (!surveyInProgress) {
            oreQueue.clear();
            unreachableOres.clear();
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
            unreachableOres.clear();
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
        if (targetOre != null) unreachableOres.add(targetOre);
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
        if (targetOre != null && !unreachableOres.contains(targetOre) && planPathToOre(targetOre)) {
            return true;
        }

        clampOreIndex();
        for (int i = oreIndex; i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (unreachableOres.contains(ore)) continue;
            debug("Planning path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore unreachable; marking and continuing: %s", fmt(ore));
            unreachableOres.add(ore);
        }

        for (int i = 0; i < oreIndex && i < oreQueue.size(); i++) {
            BlockPos ore = oreQueue.get(i);
            if (unreachableOres.contains(ore)) continue;
            debug("Planning wrap-around path to ore[%d/%d] at %s", i, oreQueue.size(), fmt(ore));
            if (planPathToOre(ore)) {
                targetOre = ore;
                oreIndex = i;
                companion.setMinerOreIndex(i);
                persistPlanProgress();
                progressStallTicks = 0;
                globalRescanTicker = 0;
                announcedNoWork = false;
                return true;
            }
            debug("Ore unreachable; marking and continuing: %s", fmt(ore));
            unreachableOres.add(ore);
        }

        notifyNoWork();
        companion.getNavigation().stop();
        searchCooldown = 0; // allow quick retry next tick
        return false;
    }

    private boolean planPathToOre(BlockPos ore) {
        if (ore == null) return false;
        digQueue.clear();
        BlockPos cursor = companion.blockPosition();
        Level level = companion.level();
        // Do not begin irreversible digging without a native route back from current safe ground.
        if (!hasReturnPath()) return false;

        // Existing caves are cheaper and safer than excavation, so use native navigation first.
        BlockPos caveStand = WorkerSite.findApproachStand(companion, ore, 2);
        if (caveStand != null) {
            if (companion.distanceToSqr(Vec3.atCenterOf(caveStand)) > 2.25D) {
                digQueue.addLast(new RouteStep(RouteAction.WALK, caveStand));
            }
            digQueue.addLast(new RouteStep(RouteAction.BREAK, ore));
            return true;
        }

        BlockPos destination = findOreApproach(ore, cursor);
        if (destination == null) return false;
        int steps = 0;
        debug("Planning path to %s from %s", fmt(ore), fmt(cursor));

        // First, descend (or stay level) toward ore with staircase pattern.
        while (cursor.getY() > destination.getY() && steps++ < MAX_PLAN_STEPS) {
            int dx = Integer.compare(destination.getX(), cursor.getX());
            int dz = Integer.compare(destination.getZ(), cursor.getZ());
            // Ensure there is a horizontal component so we never dig straight down.
            if (dx == 0 && dz == 0) {
                // Nudge along X first to create a stair landing.
                dx = (cursor.getX() + 1 <= workCenter().getX() + horizontalRadius()) ? 1 : -1;
            }
            BlockPos next = cursor.offset(dx, -1, dz);
            if (!withinVolume(next)) {
                debug("Path abort: next stair outside volume %s", fmt(next));
                return false;
            }
            if (!safeStep(level, cursor, next)) {
                info("Path abort: hazard at %s", fmt(next));
                return false;
            }
            if (Math.abs(next.getY() - cursor.getY()) != 1) {
                debug("Path abort: invalid step delta from %s to %s", fmt(cursor), fmt(next));
                return false;
            }

            enqueueStep(cursor, next, level);
            cursor = next;
        }

        // Horizontal / upward approach.
        while (!cursor.equals(destination) && steps++ < MAX_PLAN_STEPS) {
            int dx = Integer.compare(destination.getX(), cursor.getX());
            int dz = Integer.compare(destination.getZ(), cursor.getZ());
            int dy = Integer.compare(destination.getY(), cursor.getY());

            BlockPos next;
            if (dy > 0) { // need to go up
                if (dx == 0 && dz == 0) dx = cursor.getX() < workCenter().getX() + horizontalRadius() ? 1 : -1;
                next = cursor.offset(dx, 1, dz);
            } else {
                // prefer horizontal step first
                if (Math.abs(destination.getX() - cursor.getX()) >= Math.abs(destination.getZ() - cursor.getZ())) {
                    next = cursor.offset(dx, 0, 0);
                } else {
                    next = cursor.offset(0, 0, dz);
                }
            }

            if (!withinVolume(next)) {
                debug("Path abort: next step outside volume %s", fmt(next));
                return false;
            }
            if (!safeStep(level, cursor, next)) {
                info("Path abort: hazard at %s", fmt(next));
                return false;
            }
            if (Math.abs(next.getY() - cursor.getY()) > 1) {
                debug("Path abort: too-steep step from %s to %s", fmt(cursor), fmt(next));
                return false;
            }

            enqueueStep(cursor, next, level);
            cursor = next;
        }

        if (!cursor.equals(destination) || !withinVolume(ore)) return false;
        digQueue.addLast(new RouteStep(RouteAction.BREAK, ore));
        debug("Planned %d dig steps toward %s", digQueue.size(), fmt(ore));
        return !digQueue.isEmpty();
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
    private void enqueueStep(BlockPos from, BlockPos to, Level level) {
        // `to` is the future feet cell. Its floor is `to.below()` and must stay intact.
        // Descending exposes the upper face first; other steps expose the feet block first.
        if (WorkerSafetyPredicates.excavationHeadFirst(from.getY(), to.getY())) {
            addIfMineable(level, to.above());
            addIfMineable(level, to);
        } else {
            addIfMineable(level, to);
            addIfMineable(level, to.above());
        }
        digQueue.addLast(new RouteStep(RouteAction.WALK, to.immutable()));
    }

    private void addIfMineable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (isMineableBlock(state)) {
            digQueue.addLast(new RouteStep(RouteAction.BREAK, pos.immutable()));
        }
    }

    private boolean safeStep(Level level, BlockPos from, BlockPos to) {
        return WorkerSafetyPredicates.stepHeightIsSafe(from.getY(), to.getY()) && safeCell(level, to);
    }

    private boolean safeCell(Level level, BlockPos to) {
        BlockState feet = level.getBlockState(to);
        BlockState head = level.getBlockState(to.above());
        BlockState ceiling = level.getBlockState(to.above(2));
        BlockState floor = level.getBlockState(to.below());
        return canOpen(feet) && canOpen(head)
                && !isHazard(feet) && !isHazard(head) && !isHazard(ceiling) && !isHazard(floor)
                && !hasAdjacentFluid(level, to) && !hasAdjacentFluid(level, to.above())
                && !(feet.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)
                && !(head.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)
                && !(ceiling.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)
                && !floor.isAir() && floor.isFaceSturdy(level, to.below(), net.minecraft.core.Direction.UP);
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
            if (!WorkerSite.isSafeStand(companion.level(), current)) {
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return;
            }
            companion.getNavigation().moveTo(current.getX() + 0.5D, current.getY(), current.getZ() + 0.5D, 1.05D);
            return;
        }
        BlockPos stand = currentDigStand(current);
        if (stand == null) {
            // Preserve ore and route for retry after a transient obstruction/path update.
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            return;
        }
        debug("Navigating toward dig target %s (stand at %s)", fmt(current), fmt(stand));
        companion.getNavigation().moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 1.05D);
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

    private WorkerActionResult mine(BlockPos pos) {
        if (!(companion.level() instanceof ServerLevel server)) return WorkerActionResult.RETRYABLE_BLOCKED;
        BlockState state = server.getBlockState(pos);
        if (!isMineableBlock(state)) return WorkerActionResult.INVALID_TARGET;
        boolean wasOre = isOreState(state);
        BlockPos stand = currentDigStand(pos);
        if (stand == null) return WorkerActionResult.RETRYABLE_BLOCKED;
        WorkerActionResult result = WorkerBlockActions.breakPlannedExcavationBlock(
                companion, pos, stand, WorkerSite.INTERACT_RANGE_SQR);
        if (result != WorkerActionResult.SUCCESS) return result;
        // Stop this plan if the completed step has severed native return navigation.
        if (!hasReturnPath()) {
            abandonCurrentOre();
            return WorkerActionResult.SUCCESS; // action completed; abandon only future route work.
        }
        if (wasOre) {
            companion.incrementMinerOresMined();
            removeOreFromPlan(pos);
            info("Mined ore at %s (mined=%d / counted=%d / remaining=%d)",
                    fmt(pos), companion.getMinerOresMined(), companion.getMinerOresCounted(), oreQueue.size());
        }
        return WorkerActionResult.SUCCESS;
    }

    /** A tunnel's next block has no navigable stand yet; mine it from current safe feet first. */
    private BlockPos currentDigStand(BlockPos target) {
        BlockPos current = companion.blockPosition();
        if (WorkerSite.isSafeStand(companion.level(), current)
                && WorkerSite.canActFromStandIgnoringSight(companion, target, current, WorkerSite.INTERACT_RANGE_SQR)) return current;
        return WorkerSite.findStand(companion, target, 2);
    }

    /** Chest itself is solid; return navigation must target its safe adjacent feet cell. */
    private boolean hasReturnPath() {
        BlockPos chestStand = WorkerSite.findSafeApproachStand(companion, workCenter(), 2);
        if (chestStand == null) return false;
        var path = companion.getNavigation().createPath(chestStand, 0);
        return path != null && path.canReach();
    }

    private void removeOreFromPlan(BlockPos pos) {
        boolean changed = oreQueue.remove(pos);
        changed = companion.getMinerOreMemory().remove(pos) || changed;
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
