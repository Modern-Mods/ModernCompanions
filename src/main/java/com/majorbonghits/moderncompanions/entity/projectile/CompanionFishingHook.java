package com.majorbonghits.moderncompanions.entity.projectile;

import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Lightweight fishing bobber that renders a line from a companion without using player-only hooks.
 */
public class CompanionFishingHook extends Projectile {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID =
            SynchedEntityData.defineId(CompanionFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING =
            SynchedEntityData.defineId(CompanionFishingHook.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_LIFETIME = 20 * 60;
    private static final int CAST_TICKS = 12;

    private int lifeTicks;
    private int biteTicks;
    private BlockPos waterSpot;

    public CompanionFishingHook(EntityType<? extends CompanionFishingHook> type, Level level) {
        super(type, level);
    }

    public CompanionFishingHook(Level level, AbstractHumanCompanionEntity owner, BlockPos waterSpot) {
        this(ModEntityTypes.COMPANION_FISHING_HOOK.get(), level);
        this.waterSpot = waterSpot.immutable();
        this.setOwner(owner);
        this.entityData.set(DATA_OWNER_ID, owner.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_ID, 0);
        builder.define(DATA_BITING, false);
    }

    @Override
    public void tick() {
        super.tick();
        AbstractHumanCompanionEntity owner = getOwnerCompanion();
        // Hooks are transient work state. Discard one restored after an unload,
        // job switch, Work-off toggle, death, or dimension change instead of
        // letting it become an orphan that competes with a new cast.
        if (owner == null || !owner.isAlive() || owner.getJob() != CompanionJob.FISHER || !owner.isWorkEnabled()) {
            discard();
            return;
        }
        if (lifeTicks++ > MAX_LIFETIME) {
            discard();
            return;
        }
        if (lifeTicks >= CAST_TICKS && waterSpot != null) {
            // Keep the bobber anchored at the target water surface.
            Vec3 bobberPos = Vec3.atCenterOf(waterSpot).add(0.0D, 0.1D, 0.0D);
            this.setPos(bobberPos.x, bobberPos.y, bobberPos.z);
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (!this.level().isClientSide) {
            // A bite lasts long enough for the server goal to react instead of flickering for one tick.
            if (biteTicks > 0) biteTicks--;
            if (isSettled() && isLineInWater() && biteTicks == 0 && lifeTicks > 80 && random.nextInt(100) == 0) {
                biteTicks = 20;
            }
            this.entityData.set(DATA_BITING, biteTicks > 0);
        }
    }

    public boolean isSettled() {
        return lifeTicks >= CAST_TICKS;
    }

    public boolean isLineInWater() {
        return this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER);
    }

    public boolean isBiting() {
        return this.entityData.get(DATA_BITING);
    }

    @Nullable
    public AbstractHumanCompanionEntity getOwnerCompanion() {
        Entity owner = this.level().getEntity(this.entityData.get(DATA_OWNER_ID));
        return owner instanceof AbstractHumanCompanionEntity companion ? companion : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (waterSpot != null) {
            tag.putLong("WaterSpot", waterSpot.asLong());
        }
        tag.putInt("OwnerId", this.entityData.get(DATA_OWNER_ID));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WaterSpot")) {
            waterSpot = BlockPos.of(tag.getLong("WaterSpot"));
        }
        if (tag.contains("OwnerId")) {
            this.entityData.set(DATA_OWNER_ID, tag.getInt("OwnerId"));
        }
    }
}
