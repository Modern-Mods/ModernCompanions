package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Only wander when the companion is set to follow its owner.
 */
public class CustomWaterAvoidingRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final AbstractHumanCompanionEntity companion;

    public CustomWaterAvoidingRandomStrollGoal(AbstractHumanCompanionEntity mob, double speed) {
        super(mob, speed);
        this.companion = mob;
    }

    @Override
    public boolean canUse() {
        if (!companion.isFollowing()) {
            return false;
        }
        return super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        LivingEntity owner = companion.getOwner();
        if (owner == null) {
            return null;
        }

        double radius = companion.getPatrolRadius();
        double radiusSquared = radius * radius;
        // Keep native water-safe wandering while rejecting destinations outside the saved owner radius.
        for (int attempt = 0; attempt < 8; attempt++) {
            Vec3 candidate = super.getPosition();
            if (candidate != null && candidate.distanceToSqr(owner.position()) <= radiusSquared) {
                return candidate;
            }
        }
        return null;
    }
}
