package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Patrolling random stroll constrained to a patrol radius around the patrol position.
 */
public class PatrolGoal extends RandomStrollGoal {
    public Vec3 patrolVec;
    public AbstractHumanCompanionEntity companion;
    public int radius;

    public PatrolGoal(AbstractHumanCompanionEntity mob, int interval, int radius) {
        this(mob, 1.0D, 0.001F, interval, radius);
    }

    public PatrolGoal(AbstractHumanCompanionEntity mob, double speed, float probability, int interval, int radius) {
        super(mob, speed);
        this.companion = mob;
        this.interval = interval;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        if (companion.getPatrolPos().isEmpty() || !companion.isPatrolling()) {
            return false;
        }
        // If the companion has an active job, let the job goals drive movement instead of patrol strolling.
        if (companion.getJob() != CompanionJob.NONE) {
            return false;
        }
        this.patrolVec = Vec3.atBottomCenterOf(companion.getPatrolPos().orElse(companion.blockPosition()));
        return super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        if (patrolVec == null) return null;
        double maxDistanceSqr = (double) radius * radius;
        for (int attempt = 0; attempt < 8; attempt++) {
            Vec3 candidate = getRandomAroundPatrol();
            if (candidate != null && candidate.distanceToSqr(patrolVec) <= maxDistanceSqr) {
                return candidate;
            }
        }
        // Never fall back to RandomStrollGoal's unbounded world position: returning to the
        // patrol center lets MoveBackToPatrolGoal recover the companion on the next pass.
        return patrolVec;
    }

    private Vec3 getRandomAroundPatrol() {
        if (patrolVec == null) return null;
        return LandRandomPos.getPosTowards(this.mob, Math.max(1, radius), 7, patrolVec);
    }
}
