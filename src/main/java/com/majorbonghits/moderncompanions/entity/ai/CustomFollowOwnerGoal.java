package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Lightweight follow-owner goal that respects the companion's follow flag.
 */
public class CustomFollowOwnerGoal extends Goal {
    private static final double TELEPORT_DISTANCE_SQ = 35.0D * 35.0D; // Companion snaps back once ~35 blocks away.
    private static final int TELEPORT_ATTEMPTS = 10;
    private static final int TELEPORT_RANGE = 3;

    private final AbstractHumanCompanionEntity companion;
    private final double speedModifier;
    private final boolean teleport;
    private LivingEntity owner;
    private int timeToRecalc;

    public CustomFollowOwnerGoal(AbstractHumanCompanionEntity companion, double speed, boolean teleport) {
        this.companion = companion;
        this.speedModifier = speed;
        this.teleport = teleport;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!companion.isFollowing() || companion.isOrderedToSit()) {
            return false;
        }
        LivingEntity livingentity = companion.getOwner();
        if (livingentity == null || livingentity.isSpectator() || livingentity.level() != companion.level()) {
            return false;
        }
        if (companion.distanceToSqr(livingentity) < leashDistanceSquared()) {
            return false;
        }
        this.owner = livingentity;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null
                && !companion.getNavigation().isDone()
                && companion.isFollowing()
                && !companion.isOrderedToSit()
                && owner.level() == companion.level()
                && companion.distanceToSqr(owner) > returnDistanceSquared();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }

        if (owner.level() != companion.level()) {
            return;
        }

        companion.getLookControl().setLookAt(owner, 10.0F, companion.getMaxHeadXRot());

        if (--timeToRecalc <= 0) {
            timeToRecalc = 10;
            double distanceSq = companion.distanceToSqr(owner);
            if (distanceSq >= TELEPORT_DISTANCE_SQ && teleport) {
                if (!tryTeleportCloseToOwner()) {
                    companion.getNavigation().moveTo(owner, speedModifier); // Fallback if no safe spot found.
                }
            } else {
                // Return to the companion's selected radius, not directly onto the owner.
                Vec3 direction = companion.position().subtract(owner.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
                Vec3 returnPoint = owner.position().add(direction.scale(returnDistance()));
                companion.getNavigation().moveTo(returnPoint.x, returnPoint.y, returnPoint.z, speedModifier);
            }
        }
    }

    private double leashDistanceSquared() {
        double radius = Math.max(1.0D, companion.getPatrolRadius());
        return radius * radius;
    }

    private double returnDistanceSquared() {
        double distance = returnDistance();
        return distance * distance;
    }

    private double returnDistance() {
        return Math.max(1.0D, companion.getPatrolRadius() * 0.75D);
    }

    /**
     * Mimics vanilla pet recall: look for a nearby open spot around the owner before teleporting.
     */
    private boolean tryTeleportCloseToOwner() {
        BlockPos ownerPos = owner.blockPosition();
        int radius = Math.max(1, Math.min(TELEPORT_RANGE, companion.getPatrolRadius()));
        for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
            int dx = randomBetween(-radius, radius);
            int dz = randomBetween(-radius, radius);
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            BlockPos targetPos = ownerPos.offset(dx, 0, dz);
            if (isTeleportFriendly(targetPos)) {
                companion.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                companion.getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    private boolean isTeleportFriendly(BlockPos pos) {
        return companion.level().isEmptyBlock(pos)
                && companion.level().isEmptyBlock(pos.above())
                && companion.level().noCollision(companion, companion.getBoundingBox().move(
                pos.getX() - companion.getX(),
                pos.getY() - companion.getY(),
                pos.getZ() - companion.getZ()));
    }

    private int randomBetween(int min, int max) {
        return companion.getRandom().nextInt(max - min + 1) + min;
    }
}
