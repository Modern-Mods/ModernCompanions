package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.entity.job.JobReservations;
import com.majorbonghits.moderncompanions.entity.job.JobPhase;
import com.majorbonghits.moderncompanions.entity.job.WorkerSite;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

/** Courier only uses a reachable chest-side stand; inaccessible chests never trigger terrain edits. */
public class DeliverToChestGoal extends Goal {
    private static final int STUCK_ALERT_TICKS = 200;
    private static final int RETURN_STALL_TICKS = 120;
    private static final long BULK_DELIVERY_TICKS = 2400L;
    private final AbstractHumanCompanionEntity companion;
    private final double speed;
    private BlockPos targetChest;
    private BlockPos chestStand;
    private BlockPos returnTarget;
    private boolean returning;
    private int stuckTicks;
    private int returnStallTicks;
    private int lastReturnRepathTick = Integer.MIN_VALUE;
    private double lastReturnDistance = Double.MAX_VALUE;
    private double lastDistance = Double.MAX_VALUE;

    public DeliverToChestGoal(AbstractHumanCompanionEntity companion, double speed) {
        this.companion = companion;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override public boolean canUse() {
        if (!(companion.level() instanceof ServerLevel server) || !companion.isTame() || companion.isOrderedToSit()
                || companion.getJob() == CompanionJob.NONE || companion.getTarget() != null) return false;
        if (!companion.isWorkEnabled()) return false;
        if (companion.isJobReturnPending()) {
            if (server.getGameTime() < companion.getDeliveryRetryAt()) {
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                return false;
            }
            returnTarget = companion.getJobReturnPosition().orElse(null);
            if (returnTarget == null) {
                companion.finishJobReturn();
                companion.setJobStatus("job_status.modern_companions.searching");
                return false;
            }
            if (!WorkerSite.isSafeStand(server, returnTarget)) {
                BlockPos alternate = findReachableReturnStand(server);
                if (alternate == null) alternate = findSafeReturnStand(server);
                if (alternate == null) {
                    companion.setJobStatus("job_status.modern_companions.route_blocked");
                    companion.deferDelivery(server.getGameTime() + 100L);
                    return false;
                }
                returnTarget = alternate;
                companion.setJobReturnPosition(alternate);
            }
            returning = true;
            returnStallTicks = 0;
            lastReturnRepathTick = Integer.MIN_VALUE;
            lastReturnDistance = Double.MAX_VALUE;
            return true;
        }
        if (server.getGameTime() < companion.getDeliveryRetryAt()) return false;
        boolean forced = companion.isForceDeliverRequested();
        if (!companion.hasDeliverableCargo() && !forced) return false;
        if (!forced && !(companion.isInventoryFull() ||
                com.majorbonghits.moderncompanions.entity.job.WorkerSafetyPredicates.bulkDeliveryDue(
                        server.getGameTime(), companion.getLastDeliveryGameTime(), BULK_DELIVERY_TICKS))) return false;
        Optional<BlockPos> chest = companion.getAssignedChest();
        Optional<net.minecraft.resources.ResourceKey<Level>> dimension = companion.getAssignedChestDimension();
        if (chest.isEmpty() || dimension.isEmpty() || !server.dimension().equals(dimension.get())) return false;
        targetChest = chest.get();
        if (companion.getOwner() instanceof net.minecraft.world.entity.player.Player owner
                && !owner.mayInteract(server, targetChest)) {
            companion.setJobStatus("job_status.modern_companions.chest_protected");
            companion.deferDelivery(server.getGameTime() + 100L);
            return false;
        }
        if (!JobReservations.claim(server, "chest:" + targetChest.asLong(), companion.getUUID(), server.getGameTime(), 20L * 30L)) {
            companion.setJobStatus("job_status.modern_companions.chest_reserved");
            return false;
        }
        companion.refreshDeliveryChunkTicket(server);
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            companion.setJobStatus("job_status.modern_companions.chest_unloaded");
            companion.deferDelivery(server.getGameTime() + 100L);
            return false;
        }
        chestStand = WorkerSite.findApproachStand(companion, targetChest, 2);
        // Navigation probes can reject an open chest-side tile before movement begins.
        if (chestStand == null) chestStand = WorkerSite.findSafeApproachStand(companion, targetChest, 2);
        if (chestStand == null) {
            companion.setJobStatus("job_status.modern_companions.chest_unreachable");
            companion.deferDelivery(server.getGameTime() + 100L);
            reportStuck();
            return false;
        }
        companion.beginJobDelivery();
        companion.checkpointJob(JobPhase.DELIVERING, targetChest, chestStand, "chest:" + targetChest.asLong());
        return true;
    }

    @Override public boolean canContinueToUse() {
        if (returning) {
            return returnTarget != null && companion.getJob() != CompanionJob.NONE
                    && companion.getTarget() == null && companion.isWorkEnabled()
                    && companion.isJobReturnPending();
        }
        return targetChest != null && chestStand != null && companion.hasDeliverableCargo()
                && companion.getTarget() == null && companion.getJob() != CompanionJob.NONE
                && companion.isWorkEnabled() && WorkerSite.isSafeStand(companion.level(), chestStand);
    }

    @Override public void start() {
        if (returning) {
            lastReturnRepathTick = companion.tickCount - 20;
            moveTowardReturn();
            lastReturnRepathTick = companion.tickCount;
            return;
        }
        lastDistance = companion.distanceToSqr(Vec3.atCenterOf(chestStand));
        moveTowardChest();
    }

    @Override public void stop() {
        if (companion.level() instanceof ServerLevel server && targetChest != null) {
            JobReservations.release(server, com.majorbonghits.moderncompanions.entity.job.ReservationType.CHEST,
                    "chest:" + targetChest.asLong(), companion.getUUID());
        }
        targetChest = null;
        chestStand = null;
        returnTarget = null;
        returning = false;
        stuckTicks = 0;
        returnStallTicks = 0;
        lastReturnRepathTick = Integer.MIN_VALUE;
        lastReturnDistance = Double.MAX_VALUE;
        lastDistance = Double.MAX_VALUE;
        companion.getNavigation().stop();
    }

    @Override public void tick() {
        if (returning) {
            if (companion.level() instanceof ServerLevel server) tickReturn(server);
            else stop();
            return;
        }
        if (!(companion.level() instanceof ServerLevel server) || targetChest == null || chestStand == null) {
            stop();
            return;
        }
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            companion.deferDelivery(server.getGameTime() + 100L);
            stop();
            return;
        }
        double distance = companion.distanceToSqr(Vec3.atCenterOf(chestStand));
        if (distance > 2.25D) {
            companion.setJobStatus("job_status.modern_companions.delivering");
            companion.checkpointJob(JobPhase.DELIVERING, companion.getJobCheckpointTarget().orElse(targetChest));
            if (distance + 0.04D < lastDistance) {
                lastDistance = distance;
                stuckTicks = 0;
            } else {
                stuckTicks++;
            }
            if (companion.getNavigation().isDone()) moveTowardChest();
            if (stuckTicks >= STUCK_ALERT_TICKS) {
                reportStuck();
                companion.deferDelivery(server.getGameTime() + 100L);
                stop();
            }
            return;
        }
        if (!WorkerSite.canActFromStand(companion, targetChest, chestStand, WorkerSite.INTERACT_RANGE_SQR)) {
            reportStuck();
            companion.setJobStatus("job_status.modern_companions.chest_blocked");
            companion.deferDelivery(server.getGameTime() + 100L);
            stop();
            return;
        }
        switch (companion.deliverInventoryToChest(server, targetChest)) {
            case FULL -> {
                companion.setJobStatus("job_status.modern_companions.chest_full");
                companion.deferDelivery(server.getGameTime() + 100L);
                companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.full"));
            }
            case MISSING -> {
                companion.setJobStatus("job_status.modern_companions.chest_missing");
                companion.deferDelivery(server.getGameTime() + 100L);
                companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.missing"));
            }
            case SUCCESS -> {
                companion.finishJobDelivery();
                companion.setJobStatus("job_status.modern_companions.returning");
                if (companion.getJob() == CompanionJob.LUMBERJACK && companion.lumberjackGoal != null) {
                    companion.lumberjackGoal.forceRescanAfterDeposit();
                }
                beginReturn(server);
                return;
            }
            case PARTIAL -> {
                companion.setJobStatus("job_status.modern_companions.delivery_partial");
                companion.deferDelivery(server.getGameTime() + 40L);
            }
        }
        stop();
    }

    /** Start the physical return immediately so no other low-priority job can erase the route gap. */
    private void beginReturn(ServerLevel server) {
        if (targetChest != null) {
            JobReservations.release(server, com.majorbonghits.moderncompanions.entity.job.ReservationType.CHEST,
                    "chest:" + targetChest.asLong(), companion.getUUID());
        }
        targetChest = null;
        chestStand = null;
        returnTarget = companion.getJobReturnPosition().orElse(null);
        if (returnTarget == null) {
            companion.finishJobReturn();
            companion.setJobStatus("job_status.modern_companions.searching");
            returning = false;
            companion.getNavigation().stop();
            return;
        }
        if (!WorkerSite.isSafeStand(server, returnTarget)) {
            BlockPos alternate = findReachableReturnStand(server);
            if (alternate == null) alternate = findSafeReturnStand(server);
            if (alternate == null) {
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                companion.deferDelivery(server.getGameTime() + 100L);
                returning = false;
                companion.getNavigation().stop();
                return;
            }
            returnTarget = alternate;
            companion.setJobReturnPosition(alternate);
        }
        returning = true;
        returnStallTicks = 0;
        lastReturnRepathTick = companion.tickCount - 20;
        lastReturnDistance = Double.MAX_VALUE;
        moveTowardReturn();
    }

    private void tickReturn(ServerLevel server) {
        if (!companion.isJobReturnPending() || returnTarget == null) {
            stop();
            return;
        }
        if (!server.isLoaded(returnTarget)) {
            companion.setJobStatus("job_status.modern_companions.route_blocked");
            companion.deferDelivery(server.getGameTime() + 100L);
            stop();
            return;
        }
        double distance = companion.distanceToSqr(Vec3.atCenterOf(returnTarget));
        if (distance > 2.25D) {
            companion.setJobStatus("job_status.modern_companions.returning");
            if (distance + 0.04D < lastReturnDistance) {
                lastReturnDistance = distance;
                returnStallTicks = 0;
            } else {
                returnStallTicks++;
            }
            // Replanning only at a bounded cadence matters: moveTo() replaces
            // the active path, so calling it every stalled tick prevents the
            // navigation controller from ever making progress.
            if ((companion.getNavigation().isDone() || returnStallTicks >= 20)
                    && companion.tickCount - lastReturnRepathTick >= 20) {
                lastReturnRepathTick = companion.tickCount;
                if (!moveTowardReturn()) returnStallTicks = RETURN_STALL_TICKS;
            }
            if (returnStallTicks >= RETURN_STALL_TICKS) {
                BlockPos alternate = findReachableReturnStand(server);
                if (alternate == null) alternate = findSafeReturnStand(server);
                if (alternate != null && !alternate.equals(returnTarget)) {
                    returnTarget = alternate;
                    returnStallTicks = 0;
                    lastReturnDistance = Double.MAX_VALUE;
                    if (moveTowardReturn()) return;
                }
                companion.setJobStatus("job_status.modern_companions.route_blocked");
                companion.deferDelivery(server.getGameTime() + 100L);
                stop();
            }
            return;
        }
        companion.finishJobReturn();
        companion.setJobStatus("job_status.modern_companions.searching");
    }

    private void moveTowardChest() {
        if (chestStand != null) companion.getNavigation().moveTo(chestStand.getX() + .5D, chestStand.getY(), chestStand.getZ() + .5D, speed);
    }

    private boolean moveTowardReturn() {
        if (returnTarget == null) return false;
        var path = companion.getNavigation().createPath(returnTarget, 0);
        if (path != null && path.canReach()) return companion.getNavigation().moveTo(path, speed);
        // A native probe can reject a valid nearby checkpoint while the
        // navigation controller can still build a route on its next tick.
        return companion.getNavigation().moveTo(returnTarget.getX() + .5D, returnTarget.getY(),
                returnTarget.getZ() + .5D, speed);
    }

    /** Rebuild a nearby safe checkpoint when the exact saved feet cell changed after delivery. */
    private BlockPos findReachableReturnStand(ServerLevel server) {
        if (returnTarget == null || !server.isLoaded(returnTarget)) return null;
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(
                returnTarget.offset(-2, -1, -2), returnTarget.offset(2, 1, 2))) {
            if (!WorkerSite.isSafeStand(server, candidate)
                    || candidate.distSqr(returnTarget) > 6.25D) continue;
            var path = companion.getNavigation().createPath(candidate, 0);
            if (path == null || !path.canReach()) continue;
            double distance = candidate.distSqr(returnTarget);
            if (distance < bestDistance) {
                best = candidate.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    /** Choose a safe nearby checkpoint even when the first native probe is transiently stale. */
    private BlockPos findSafeReturnStand(ServerLevel server) {
        if (returnTarget == null || !server.isLoaded(returnTarget)) return null;
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(
                returnTarget.offset(-2, -1, -2), returnTarget.offset(2, 1, 2))) {
            if (!WorkerSite.isSafeStand(server, candidate)
                    || candidate.distSqr(returnTarget) > 6.25D) continue;
            double distance = candidate.distSqr(returnTarget);
            if (distance < bestDistance) {
                best = candidate.immutable();
                bestDistance = distance;
            }
        }
        return best;
    }

    private void reportStuck() {
        if (targetChest != null) companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable(
                "message.modern_companions.courier.stuck", targetChest.getX(), targetChest.getY(), targetChest.getZ()));
    }
}
