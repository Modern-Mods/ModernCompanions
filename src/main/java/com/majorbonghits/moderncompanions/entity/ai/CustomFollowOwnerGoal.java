package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Lightweight follow-owner goal that respects the companion's follow flag.
 */
public class CustomFollowOwnerGoal extends Goal {
    private static final int TELEPORT_ATTEMPTS = 10;
    private static final int TELEPORT_RANGE = 3;
    private static final int PATH_RECALCULATION_INTERVAL = 5;
    private static final int POST_TELEPORT_FOLLOW_TICKS = 20;
    private static final int MAX_AIRBORNE_OWNER_GROUND_GAP = 4;
    private static final double PROGRESS_EPSILON_SQUARED = 0.04D;

    private final AbstractHumanCompanionEntity companion;
    private final double speedModifier;
    private final boolean teleport;
    private LivingEntity owner;
    private int timeToRecalc;
    private int postTeleportFollowTicks;
    private int noProgressTicks;
    private double lastDistanceSq = Double.MAX_VALUE;
    private long teleportGraceUntil;
    private long teleportCooldownUntil;

    public CustomFollowOwnerGoal(AbstractHumanCompanionEntity companion, double speed, boolean teleport) {
        this.companion = companion;
        this.speedModifier = speed;
        this.teleport = teleport;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (companion.isJobReturnPending()
                || !companion.isFollowing() || companion.isOrderedToSit() || companion.isPassenger()) {
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
                && !companion.isJobReturnPending()
                && companion.isFollowing()
                && !companion.isOrderedToSit()
                && !companion.isPassenger()
                && owner.level() == companion.level()
                && (postTeleportFollowTicks > 0 || companion.distanceToSqr(owner) > returnDistanceSquared());
    }

    @Override
    public void start() {
        timeToRecalc = 0;
        postTeleportFollowTicks = 0;
        noProgressTicks = 0;
        lastDistanceSq = companion.distanceToSqr(owner);
        // Let navigation try to catch up before an emergency teleport is allowed.
        teleportGraceUntil = companion.level().getGameTime() + teleportDelayTicks();
    }

    @Override
    public void stop() {
        this.owner = null;
        this.companion.getNavigation().stop();
        this.postTeleportFollowTicks = 0;
        this.noProgressTicks = 0;
        this.lastDistanceSq = Double.MAX_VALUE;
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }

        if (owner.level() != companion.level()) {
            return;
        }

        if (postTeleportFollowTicks > 0) {
            postTeleportFollowTicks--;
        }

        companion.getLookControl().setLookAt(owner, 10.0F, companion.getMaxHeadXRot());

        if (--timeToRecalc <= 0) {
            timeToRecalc = PATH_RECALCULATION_INTERVAL;
            long gameTime = companion.level().getGameTime();
            double distanceSq = companion.distanceToSqr(owner);
            if (distanceSq <= lastDistanceSq - PROGRESS_EPSILON_SQUARED) {
                noProgressTicks = 0;
            } else {
                noProgressTicks += PATH_RECALCULATION_INTERVAL;
            }
            lastDistanceSq = distanceSq;

            if (distanceSq >= FollowLeashRules.teleportDistanceSquared(companion.getPatrolRadius())
                    && teleport
                    && ModConfig.safeGet(ModConfig.TELEPORT_LEASH)
                    && gameTime >= teleportGraceUntil
                    && gameTime >= teleportCooldownUntil
                    && noProgressTicks >= teleportDelayTicks()) {
                if (!tryTeleportCloseToOwner()) {
                    moveTowardOwner(); // Fallback if no safe spot is found.
                } else {
                    // Keep the follow goal active after recall so the companion resumes walking.
                    postTeleportFollowTicks = POST_TELEPORT_FOLLOW_TICKS;
                    teleportCooldownUntil = gameTime + teleportCooldownTicks();
                    noProgressTicks = 0;
                    lastDistanceSq = companion.distanceToSqr(owner);
                    moveTowardOwner();
                }
            } else {
                moveTowardOwner();
            }
        }
    }

    private int teleportDelayTicks() {
        return ModConfig.safeGet(ModConfig.TELEPORT_DELAY_TICKS);
    }

    private int teleportCooldownTicks() {
        return ModConfig.safeGet(ModConfig.TELEPORT_COOLDOWN_TICKS);
    }

    private void moveTowardOwner() {
        // Return to the companion's selected radius, not directly onto the owner.
        Vec3 direction = companion.position().subtract(owner.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            companion.getNavigation().moveTo(owner, speedModifier);
            return;
        }
        Vec3 returnPoint = owner.position().add(direction.normalize().scale(returnDistance()));
        companion.getNavigation().moveTo(returnPoint.x, returnPoint.y, returnPoint.z, speedModifier);
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
        if (owner.isFallFlying() || !owner.onGround()) {
            return false;
        }
        int groundY = companion.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                owner.getBlockX(), owner.getBlockZ());
        if (owner.getY() - groundY > MAX_AIRBORNE_OWNER_GROUND_GAP) {
            return false;
        }

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
                && companion.level().getBlockState(pos.below()).isFaceSturdy(companion.level(), pos.below(), Direction.UP)
                && companion.level().noCollision(companion, companion.getBoundingBox().move(
                pos.getX() - companion.getX(),
                pos.getY() - companion.getY(),
                pos.getZ() - companion.getZ()));
    }

    private int randomBetween(int min, int max) {
        return companion.getRandom().nextInt(max - min + 1) + min;
    }
}
