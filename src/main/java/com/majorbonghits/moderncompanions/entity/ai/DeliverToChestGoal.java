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
    private static final long BULK_DELIVERY_TICKS = 2400L;
    private final AbstractHumanCompanionEntity companion;
    private final double speed;
    private BlockPos targetChest;
    private BlockPos chestStand;
    private int stuckTicks;
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
        boolean forced = companion.isForceDeliverRequested();
        if (!companion.hasDeliverableCargo() && !forced) return false;
        if (!forced && !(companion.isInventoryFull() ||
                com.majorbonghits.moderncompanions.entity.job.WorkerSafetyPredicates.bulkDeliveryDue(
                        server.getGameTime(), companion.getLastDeliveryGameTime(), BULK_DELIVERY_TICKS))) return false;
        Optional<BlockPos> chest = companion.getAssignedChest();
        Optional<net.minecraft.resources.ResourceKey<Level>> dimension = companion.getAssignedChestDimension();
        if (chest.isEmpty() || dimension.isEmpty() || !server.dimension().equals(dimension.get())) return false;
        targetChest = chest.get();
        if (!JobReservations.claim(server, "chest:" + targetChest.asLong(), companion.getUUID(), server.getGameTime(), 20L * 30L)) {
            companion.setJobStatus("Chest reserved");
            return false;
        }
        companion.refreshDeliveryChunkTicket(server);
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            companion.setJobStatus("Chest unloaded");
            return false;
        }
        chestStand = WorkerSite.findApproachStand(companion, targetChest, 2);
        // Navigation probes can reject an open chest-side tile before movement begins.
        if (chestStand == null) chestStand = WorkerSite.findSafeApproachStand(companion, targetChest, 2);
        if (chestStand == null) {
            companion.setJobStatus("Chest unreachable");
            reportStuck();
            return false;
        }
        return true;
    }

    @Override public boolean canContinueToUse() {
        return targetChest != null && chestStand != null && companion.hasDeliverableCargo()
                && companion.getTarget() == null && companion.getJob() != CompanionJob.NONE
                && companion.isWorkEnabled() && WorkerSite.isSafeStand(companion.level(), chestStand);
    }

    @Override public void start() {
        lastDistance = companion.distanceToSqr(Vec3.atCenterOf(chestStand));
        moveTowardChest();
    }

    @Override public void stop() {
        targetChest = null;
        chestStand = null;
        stuckTicks = 0;
        lastDistance = Double.MAX_VALUE;
        companion.getNavigation().stop();
    }

    @Override public void tick() {
        if (!(companion.level() instanceof ServerLevel server) || targetChest == null || chestStand == null) {
            stop();
            return;
        }
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            stop();
            return;
        }
        double distance = companion.distanceToSqr(Vec3.atCenterOf(chestStand));
        if (distance > 2.25D) {
            companion.setJobStatus("Delivering");
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
                stop();
            }
            return;
        }
        if (!WorkerSite.canActFromStand(companion, targetChest, chestStand, WorkerSite.INTERACT_RANGE_SQR)) {
            reportStuck();
            companion.setJobStatus("Chest blocked");
            stop();
            return;
        }
        switch (companion.deliverInventoryToChest(server, targetChest)) {
            case FULL -> {
                companion.setJobStatus("Chest full");
                companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.full"));
            }
            case MISSING -> {
                companion.setJobStatus("Chest missing");
                companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.missing"));
            }
            case SUCCESS -> {
                companion.checkpointJob(JobPhase.RETURNING, companion.getJobCheckpointTarget().orElse(targetChest));
                companion.setJobStatus("Returning");
            }
        }
        stop();
    }

    private void moveTowardChest() {
        if (chestStand != null) companion.getNavigation().moveTo(chestStand.getX() + .5D, chestStand.getY(), chestStand.getZ() + .5D, speed);
    }

    private void reportStuck() {
        if (targetChest != null) companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable(
                "message.modern_companions.courier.stuck", targetChest.getX(), targetChest.getY(), targetChest.getZ()));
    }
}
