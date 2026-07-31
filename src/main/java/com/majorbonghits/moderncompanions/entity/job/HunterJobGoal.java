package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.EnumSet;

/**
 * Hunter loop that softly mirrors the legacy hunt toggle: periodically finds a
 * nearby valid target and lets existing target goals handle combat. Kept light
 * to avoid double-pathing with built-in attack goals.
 */
public class HunterJobGoal extends ResumableJobGoal {
    private static final int CHECK_INTERVAL = 20;
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> DENIED_ANIMALS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "hunter_denied"));
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> ALLOWED_ANIMALS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "hunter_allowed"));
    private final double searchRadius;
    private final boolean enabled;

    private final AbstractHumanCompanionEntity companion;
    private int tickDown;

    public HunterJobGoal(AbstractHumanCompanionEntity companion, double searchRadius, boolean enabled) {
        super(companion, CompanionJob.HUNTER);
        this.companion = companion;
        this.searchRadius = Math.max(6.0D, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isActiveJob();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob();
    }

    @Override
    public void tick() {
        if (tickDown-- > 0) return;
        tickDown = CHECK_INTERVAL;
        if (companion.getTarget() != null && validTarget(companion.getTarget())) {
            phase(JobPhase.WORKING, "job_status.modern_companions.hunting", companion.getTarget().blockPosition());
            return;
        }
        companion.setTarget(null);
        LivingEntity target = findTarget();
        if (target != null && reserve("animal:" + target.getUUID())) {
            companion.setTarget(target);
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.hunting", target.blockPosition());
        } else if (target != null) {
            waiting("job_status.modern_companions.animal_reserved");
        } else {
            phase(JobPhase.SEARCHING, "job_status.modern_companions.no_prey");
        }
    }

    private LivingEntity findTarget() {
        net.minecraft.core.BlockPos center = companion.getWorkCenter().orElse(companion.blockPosition());
        AABB box = new AABB(center).inflate(Math.min(searchRadius, companion.getPatrolRadius()));
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : companion.level().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!validTarget(entity)) continue;
            double distance = companion.distanceToSqr(entity);
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        return best;
    }

    private boolean validTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isAlliedTo(companion) || !companion.isInWorkArea(entity.blockPosition())) return false;
        if (entity instanceof Animal animal && animal.isBaby()) return false;
        if (!(entity instanceof Animal) && !entity.getType().is(ALLOWED_ANIMALS)) return false;
        if (entity instanceof TamableAnimal tameable && tameable.isTame()) return false;
        if (entity.getType().is(DENIED_ANIMALS)) return false;
        // Default every adult wild Animal is eligible; pack tags can add animal-like mod entities.
        var path = companion.getNavigation().createPath(entity, 0);
        return path != null && path.canReach();
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.HUNTER) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasWeapon()) return false;
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    private boolean hasWeapon() {
        ItemStack stack = companion.getMainHandItem();
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
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

}
