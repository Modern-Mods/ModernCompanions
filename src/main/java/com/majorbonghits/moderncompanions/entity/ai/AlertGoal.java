package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * Targets modded and vanilla monsters while honoring shared and player-configured safety exclusions.
 */
public class AlertGoal extends NearestAttackableTargetGoal<LivingEntity> {
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> UNSAFE_ALERT_TARGETS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "alert_unsafe"));
    private final AbstractHumanCompanionEntity companion;

    public AlertGoal(AbstractHumanCompanionEntity companion) {
        super(companion, LivingEntity.class, true, AlertGoal::isAlertTarget);
        this.companion = companion;
    }

    private static boolean isAlertTarget(LivingEntity entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return AlertTargetRules.shouldTarget(entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER,
                entity.getType().is(UNSAFE_ALERT_TARGETS), ModConfig.safeGet(ModConfig.ALERT_EXCLUDED_MOBS).contains(id));
    }

    @Override
    public boolean canUse() {
        return companion.isAlert() && super.canUse();
    }
}
