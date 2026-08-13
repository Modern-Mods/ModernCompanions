package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hunter loop that softly mirrors the legacy hunt toggle: periodically finds a
 * nearby valid target and lets existing target goals handle combat. Kept light
 * to avoid double-pathing with built-in attack goals.
 */
public class HunterJobGoal extends ResumableJobGoal {
    private static final int CHECK_INTERVAL = 20;
    private static final int POST_KILL_WAIT_TICKS = 40;
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> DENIED_ANIMALS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "hunter_denied"));
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> ALLOWED_ANIMALS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "hunter_allowed"));
    private final double searchRadius;
    private final boolean enabled;

    private final AbstractHumanCompanionEntity companion;
    private int tickDown;
    private LivingEntity jobTarget;
    private UUID jobTargetId;
    private BlockPos lastTargetPos;
    private ItemEntity ownedDrop;
    private final List<UUID> ownedDropIds = new ArrayList<>();
    private int postKillWaitTicks;
    private boolean restoredPlan;

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
        if (!retryReady()) return;
        if (ownedDrop != null) {
            tickOwnedDrop();
            return;
        }
        LivingEntity current = companion.getTarget();
        if (current != null && current != jobTarget && !validTarget(current)) {
            // Defensive/owner-protection targets preempt the job; never clear them
            // just because they are not legal hunting prey.
            return;
        }
        if (jobTarget != null && !jobTarget.isAlive()) {
            if (postKillWaitTicks <= 0) postKillWaitTicks = POST_KILL_WAIT_TICKS;
            if (confirmKill()) {
                postKillWaitTicks = 0;
                return;
            }
            if (--postKillWaitTicks > 0) {
                phase(JobPhase.COLLECTING, "job_status.modern_companions.collecting", lastTargetPos);
                return;
            }
            abandonTarget();
            return;
        }
        if (jobTarget != null && !validTarget(jobTarget)) {
            if (companion.getTarget() == jobTarget) companion.setTarget(null);
            abandonTarget();
            return;
        }
        if (jobTarget != null) {
            lastTargetPos = jobTarget.blockPosition().immutable();
            if (companion.getTarget() == null) companion.setTarget(jobTarget);
            if (companion.getTarget() == jobTarget) {
                phase(JobPhase.WORKING, "job_status.modern_companions.hunting", jobTarget.blockPosition());
            }
            return;
        }
        if (tickDown-- > 0) return;
        tickDown = CHECK_INTERVAL;
        LivingEntity target = findTarget();
        if (target != null && reserve("animal:" + target.getUUID())) {
            jobTarget = target;
            jobTargetId = target.getUUID();
            lastTargetPos = target.blockPosition().immutable();
            postKillWaitTicks = 0;
            companion.setTarget(target);
            savePlan();
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.hunting", target.blockPosition());
        } else if (target != null) {
            waiting("job_status.modern_companions.animal_reserved");
        } else {
            phase(JobPhase.SEARCHING, "job_status.modern_companions.no_prey");
        }
    }

    private LivingEntity findTarget() {
        net.minecraft.core.BlockPos center = companion.getWorkCenter().orElse(companion.blockPosition());
        double radius = Math.min(128.0D, Math.max(searchRadius, companion.getPatrolRadius()));
        AABB box = new AABB(center).inflate(radius);
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
        if (entity == companion || !entity.isAlive() || !companion.isInWorkArea(entity.blockPosition())) return false;
        boolean animal = entity instanceof Animal;
        boolean baby = animal && ((Animal) entity).isBaby();
        boolean tame = entity instanceof TamableAnimal tameable && tameable.isTame();
        if (!passesEligibility(animal, baby, entity.isAlliedTo(companion), tame,
                entity.getType().is(DENIED_ANIMALS), entity.getType().is(ALLOWED_ANIMALS))) return false;
        // Default every adult wild Animal is eligible; pack tags can add animal-like mod entities.
        var path = companion.getNavigation().createPath(entity, 0);
        return path != null && path.canReach();
    }

    /** Pure contract predicate: tags may add entity types, but never override safety exclusions. */
    static boolean passesEligibility(boolean animal, boolean baby, boolean allied, boolean tame,
                                     boolean denied, boolean explicitlyAllowed) {
        return !baby && !allied && !tame && !denied && (animal || explicitlyAllowed);
    }

    private boolean confirmKill() {
        lastTargetPos = lastTargetPos == null ? companion.blockPosition() : lastTargetPos;
        if (ownedDrop == null) ownedDrop = nextOwnedDrop();
        if (ownedDrop == null) {
            for (ItemEntity drop : findOwnedDrops()) {
                if (!ownedDropIds.contains(drop.getUUID()) && reserve("drop:" + drop.getUUID())) {
                    ownedDropIds.add(drop.getUUID());
                }
            }
            ownedDrop = nextOwnedDrop();
        }
        if (ownedDrop != null) {
            phase(JobPhase.COLLECTING, "job_status.modern_companions.collecting", ownedDrop.blockPosition());
            savePlan();
            return true;
        }
        return false;
    }

    private List<ItemEntity> findOwnedDrops() {
        BlockPos center = lastTargetPos == null ? companion.blockPosition() : lastTargetPos;
        AABB box = new AABB(center).inflate(16.0D);
        List<ItemEntity> drops = new ArrayList<>(companion.level().getEntitiesOfClass(ItemEntity.class, box,
                candidate -> candidate.isAlive() && JobDropClaims.isOwnedBy(candidate, companion.getUUID())));
        drops.sort(java.util.Comparator.comparingDouble(companion::distanceToSqr));
        return drops;
    }

    private ItemEntity nextOwnedDrop() {
        if (!(companion.level() instanceof ServerLevel server)) return null;
        for (int index = 0; index < ownedDropIds.size(); index++) {
            UUID id = ownedDropIds.get(index);
            if (server.getEntity(id) instanceof ItemEntity item && item.isAlive()
                    && JobDropClaims.isOwnedBy(item, companion.getUUID())) {
                return item;
            }
            release("drop:" + id);
            ownedDropIds.remove(index--);
        }
        return null;
    }

    private void tickOwnedDrop() {
        if (ownedDrop == null) ownedDrop = nextOwnedDrop();
        if (ownedDrop == null) {
            // A generic pickup goal may have collected one claimed drop before
            // this goal ticked; refresh the complete owned set before ending.
            for (ItemEntity drop : findOwnedDrops()) {
                if (!ownedDropIds.contains(drop.getUUID()) && reserve("drop:" + drop.getUUID())) {
                    ownedDropIds.add(drop.getUUID());
                }
            }
            ownedDrop = nextOwnedDrop();
        }
        if (ownedDrop == null) {
            if (!ownedDropIds.isEmpty()) {
                savePlan();
                return;
            }
            abandonTarget();
            return;
        }
        double distance = companion.distanceToSqr(ownedDrop);
        if (distance > 2.25D) {
            phase(JobPhase.COLLECTING, "job_status.modern_companions.collecting", ownedDrop.blockPosition());
            if (companion.getNavigation().isDone()) {
                companion.getNavigation().moveTo(ownedDrop.getX(), ownedDrop.getY(), ownedDrop.getZ(), 1.1D);
            }
            return;
        }
        if (!companion.collectOwnedJobDrop(ownedDrop)) {
            companion.setJobStatus("job_status.modern_companions.inventory_full");
            companion.requestImmediateDelivery(null);
            return;
        }
        UUID collectedId = ownedDrop.getUUID();
        release("drop:" + collectedId);
        ownedDropIds.remove(collectedId);
        ownedDrop = nextOwnedDrop();
        if (ownedDrop == null) {
            for (ItemEntity drop : findOwnedDrops()) {
                if (!ownedDropIds.contains(drop.getUUID()) && reserve("drop:" + drop.getUUID())) {
                    ownedDropIds.add(drop.getUUID());
                }
            }
            ownedDrop = nextOwnedDrop();
        }
        if (ownedDrop == null) abandonTarget();
        else savePlan();
    }

    private void abandonTarget() {
        if (companion.level() instanceof ServerLevel server && jobTargetId != null) {
            JobReservations.release(server, ReservationType.ENTITY, "animal:" + jobTargetId, companion.getUUID());
        }
        jobTarget = null;
        jobTargetId = null;
        lastTargetPos = null;
        if (ownedDrop != null) release("drop:" + ownedDrop.getUUID());
        for (UUID dropId : ownedDropIds) release("drop:" + dropId);
        ownedDrop = null;
        ownedDropIds.clear();
        postKillWaitTicks = 0;
        savePlan();
    }

    private void savePlan() {
        CompoundTag payload = companion.getJobPlanPayload();
        payload.remove("HunterTarget");
        payload.remove("HunterTargetPos");
        payload.remove("HunterDrop");
        payload.remove("HunterDrops");
        if (jobTargetId != null) payload.putUUID("HunterTarget", jobTargetId);
        if (lastTargetPos != null) payload.putLong("HunterTargetPos", lastTargetPos.asLong());
        if (ownedDrop != null) payload.putUUID("HunterDrop", ownedDrop.getUUID());
        if (!ownedDropIds.isEmpty()) {
            ListTag drops = new ListTag();
            for (UUID dropId : ownedDropIds) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("Id", dropId);
                drops.add(entry);
            }
            payload.put("HunterDrops", drops);
        }
        companion.setJobPlanPayload(payload);
    }

    private void restorePlan() {
        if (restoredPlan) return;
        restoredPlan = true;
        CompoundTag payload = companion.getJobPlanPayload();
        if (payload.hasUUID("HunterTarget")) {
            jobTargetId = payload.getUUID("HunterTarget");
            if (companion.level() instanceof ServerLevel server
                    && server.getEntity(jobTargetId) instanceof LivingEntity living) {
                jobTarget = living;
            }
        }
        if (payload.contains("HunterTargetPos")) lastTargetPos = BlockPos.of(payload.getLong("HunterTargetPos"));
        ownedDropIds.clear();
        ListTag drops = payload.getList("HunterDrops", Tag.TAG_COMPOUND);
        for (int index = 0; index < drops.size(); index++) {
            CompoundTag entry = drops.getCompound(index);
            if (entry.hasUUID("Id")) ownedDropIds.add(entry.getUUID("Id"));
        }
        if (payload.hasUUID("HunterDrop") && companion.level() instanceof ServerLevel server
                && server.getEntity(payload.getUUID("HunterDrop")) instanceof ItemEntity item) {
            ownedDrop = item;
            if (!ownedDropIds.contains(item.getUUID())) ownedDropIds.add(item.getUUID());
        }
    }

    private boolean isActiveJob() {
        restorePlan();
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.HUNTER) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        companion.ensureJobToolEquipped();
        if (!JobToolPolicy.matches(CompanionJob.HUNTER, companion.getMainHandItem())) return false;
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    private boolean hasWeapon() {
        return JobToolPolicy.has(companion, CompanionJob.HUNTER);
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
