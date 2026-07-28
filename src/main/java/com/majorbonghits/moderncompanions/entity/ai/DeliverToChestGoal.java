package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
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
    private final AbstractHumanCompanionEntity companion;
    private final double speed;
    private BlockPos targetChest;
    private BlockPos chestStand;
    private int stuckTicks;

    public DeliverToChestGoal(AbstractHumanCompanionEntity companion, double speed) {
        this.companion = companion;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override public boolean canUse() {
        if (!(companion.level() instanceof ServerLevel server) || !companion.isTame() || companion.isOrderedToSit()
                || companion.getJob() == CompanionJob.NONE || companion.getTarget() != null) return false;
        boolean forced = companion.isForceDeliverRequested();
        if (!companion.isPatrolling() && !forced) return false;
        if (!companion.hasDeliverableCargo() && !forced) return false;
        if (!forced && !(companion.isInventoryFull() || server.getGameTime() - companion.getLastDeliveryGameTime() >= 24000L)) return false;
        Optional<BlockPos> chest = companion.getAssignedChest();
        Optional<net.minecraft.resources.ResourceKey<Level>> dimension = companion.getAssignedChestDimension();
        if (chest.isEmpty() || dimension.isEmpty() || !server.dimension().equals(dimension.get())) return false;
        targetChest = chest.get();
        companion.refreshDeliveryChunkTicket(server);
        if (!server.isLoaded(targetChest)) {
            companion.alertChestUnloaded();
            return false;
        }
        chestStand = WorkerSite.findStand(companion, targetChest, 2);
        if (chestStand == null) {
            reportStuck();
            return false;
        }
        return true;
    }

    @Override public boolean canContinueToUse() {
        return targetChest != null && chestStand != null && companion.hasDeliverableCargo()
                && companion.getTarget() == null && companion.getJob() != CompanionJob.NONE
                && WorkerSite.isValid(companion, targetChest, chestStand);
    }

    @Override public void start() { moveTowardChest(); }

    @Override public void stop() {
        targetChest = null;
        chestStand = null;
        stuckTicks = 0;
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
        if (!WorkerSite.isValid(companion, targetChest, chestStand)) {
            reportStuck();
            stop();
            return;
        }
        if (companion.distanceToSqr(Vec3.atCenterOf(chestStand)) > 2.25D) {
            moveTowardChest();
            if (++stuckTicks >= STUCK_ALERT_TICKS) {
                reportStuck();
                stop();
            }
            return;
        }
        switch (companion.deliverInventoryToChest(server, targetChest)) {
            case FULL -> companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.full"));
            case MISSING -> companion.notifyCourierOwnerText(net.minecraft.network.chat.Component.translatable("message.modern_companions.courier.missing"));
            case SUCCESS -> { }
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
