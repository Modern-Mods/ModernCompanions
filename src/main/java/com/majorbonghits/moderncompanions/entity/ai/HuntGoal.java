package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Hunts passive mobs when hunting flag is enabled.
 */
public class HuntGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private final AbstractHumanCompanionEntity companion;

    public HuntGoal(AbstractHumanCompanionEntity companion) {
        super(companion, LivingEntity.class, true, HuntGoal::isHuntTarget);
        this.companion = companion;
    }

    private static boolean isHuntTarget(LivingEntity entity) {
        return ModConfig.safeGet(ModConfig.HUNT_MOBS)
                .contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }

    @Override
    public boolean canUse() {
        // HunterJobGoal owns profession target selection; this remains manual hunting only.
        return companion.getJob() != com.majorbonghits.moderncompanions.entity.job.CompanionJob.HUNTER
                && companion.isHunting() && super.canUse();
    }
}
