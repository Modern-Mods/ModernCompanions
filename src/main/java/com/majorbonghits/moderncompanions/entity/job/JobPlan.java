package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Server-owned durable facts for a job. Navigation paths, scan cursors, and
 * retry clocks deliberately stay in the goals and are rebuilt after reload.
 */
public final class JobPlan {
    public static final int SCHEMA_VERSION = 1;

    private CompanionJob job = CompanionJob.NONE;
    private JobPhase phase = JobPhase.SEARCHING;
    private JobPhase preDeliveryPhase = JobPhase.SEARCHING;
    private String waitingReason = "";
    private String identity = "";
    @Nullable private BlockPos target;
    @Nullable private BlockPos stand;
    @Nullable private BlockPos returnPosition;
    @Nullable private BlockPos preDeliveryTarget;
    @Nullable private BlockPos preDeliveryStand;
    private String preDeliveryIdentity = "";
    private boolean deliveryActive;
    private boolean deliveryReturnPending;
    private CompoundTag payload = new CompoundTag();

    public CompanionJob job() {
        return job;
    }

    public void setJob(CompanionJob job) {
        this.job = job == null ? CompanionJob.NONE : job;
    }

    public JobPhase phase() {
        return phase;
    }

    public JobPhase preDeliveryPhase() {
        return preDeliveryPhase;
    }

    public String waitingReason() {
        return waitingReason;
    }

    public String identity() {
        return identity;
    }

    @Nullable
    public BlockPos target() {
        return target;
    }

    @Nullable
    public BlockPos stand() {
        return stand;
    }

    @Nullable
    public BlockPos returnPosition() {
        return returnPosition;
    }

    @Nullable
    public BlockPos preDeliveryTarget() {
        return preDeliveryTarget;
    }

    @Nullable
    public BlockPos preDeliveryStand() {
        return preDeliveryStand;
    }

    public String preDeliveryIdentity() {
        return preDeliveryIdentity;
    }

    public boolean deliveryActive() {
        return deliveryActive;
    }

    public boolean deliveryReturnPending() {
        return deliveryReturnPending;
    }

    /** Keeps a recovered physical return stand durable across goal preemption or reload. */
    public void setReturnPosition(@Nullable BlockPos position) {
        this.returnPosition = position == null ? null : position.immutable();
    }

    public CompoundTag payload() {
        return payload.copy();
    }

    public void setPayload(@Nullable CompoundTag payload) {
        this.payload = payload == null ? new CompoundTag() : payload.copy();
    }

    /**
     * Updates a phase without moving the saved return point. A new target gets
     * the current position as its return point exactly once.
     */
    public void checkpoint(JobPhase phase, @Nullable BlockPos target, @Nullable BlockPos stand,
                           @Nullable String identity, @Nullable BlockPos currentPosition) {
        JobPhase safePhase = phase == null ? JobPhase.SEARCHING : phase;
        if (safePhase == JobPhase.DELIVERING && !deliveryActive && !deliveryReturnPending) {
            beginDelivery(this.phase, this.target, this.stand, this.identity, currentPosition);
        }
        if (safePhase == JobPhase.SEARCHING && target == null) {
            clearWorkUnit();
        } else if (target != null && this.target == null && currentPosition != null) {
            this.returnPosition = currentPosition.immutable();
        }
        this.phase = safePhase;
        this.target = target == null ? this.target : target.immutable();
        this.stand = stand == null ? this.stand : stand.immutable();
        if (identity != null) this.identity = identity;
        if (safePhase != JobPhase.PAUSED && safePhase != JobPhase.WAITING) {
            this.waitingReason = "";
        }
    }

    public void setWaitingReason(@Nullable String reason) {
        this.waitingReason = reason == null ? "" : reason;
    }

    public void beginDelivery(JobPhase previousPhase, @Nullable BlockPos previousTarget,
                              @Nullable BlockPos previousStand, @Nullable String previousIdentity,
                              @Nullable BlockPos currentPosition) {
        if (!deliveryActive && !deliveryReturnPending) {
            this.preDeliveryPhase = previousPhase == null ? JobPhase.SEARCHING : previousPhase;
            this.preDeliveryTarget = previousTarget == null ? null : previousTarget.immutable();
            this.preDeliveryStand = previousStand == null ? null : previousStand.immutable();
            this.preDeliveryIdentity = previousIdentity == null ? "" : previousIdentity;
            if (currentPosition != null) this.returnPosition = currentPosition.immutable();
            this.deliveryActive = true;
        }
        this.phase = JobPhase.DELIVERING;
    }

    /** Moves the saved work unit into the explicit return phase after delivery. */
    public void restoreAfterDelivery() {
        this.deliveryActive = false;
        this.deliveryReturnPending = true;
        this.phase = JobPhase.RETURNING;
        this.target = preDeliveryTarget == null ? null : preDeliveryTarget.immutable();
        this.stand = preDeliveryStand == null ? null : preDeliveryStand.immutable();
        this.identity = preDeliveryIdentity;
        this.waitingReason = "";
    }

    /** Completes the physical return and restores the profession phase. */
    public JobPhase completeDeliveryReturn() {
        JobPhase restoredPhase = preDeliveryPhase;
        this.deliveryReturnPending = false;
        this.phase = restoredPhase;
        this.returnPosition = null;
        this.preDeliveryTarget = null;
        this.preDeliveryStand = null;
        this.preDeliveryIdentity = "";
        this.preDeliveryPhase = JobPhase.SEARCHING;
        this.waitingReason = "";
        return restoredPhase;
    }

    public void clearWorkUnit() {
        this.target = null;
        this.stand = null;
        this.returnPosition = null;
        this.identity = "";
        this.preDeliveryTarget = null;
        this.preDeliveryStand = null;
        this.preDeliveryIdentity = "";
        this.preDeliveryPhase = JobPhase.SEARCHING;
        this.deliveryActive = false;
        this.deliveryReturnPending = false;
        this.payload = new CompoundTag();
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Schema", SCHEMA_VERSION);
        tag.putString("Job", job.id());
        tag.putString("Phase", phase.name());
        tag.putString("PreDeliveryPhase", preDeliveryPhase.name());
        tag.putString("WaitingReason", waitingReason);
        tag.putString("Identity", identity);
        putPos(tag, "Target", target);
        putPos(tag, "Stand", stand);
        putPos(tag, "Return", returnPosition);
        putPos(tag, "PreDeliveryTarget", preDeliveryTarget);
        putPos(tag, "PreDeliveryStand", preDeliveryStand);
        tag.putString("PreDeliveryIdentity", preDeliveryIdentity);
        tag.putBoolean("DeliveryActive", deliveryActive);
        tag.putBoolean("DeliveryReturnPending", deliveryReturnPending);
        tag.put("Payload", payload.copy());
        return tag;
    }

    public static JobPlan load(CompoundTag tag) {
        JobPlan plan = new JobPlan();
        plan.job = CompanionJob.fromId(tag.getString("Job"));
        plan.phase = phase(tag.getString("Phase"), JobPhase.SEARCHING);
        plan.preDeliveryPhase = phase(tag.getString("PreDeliveryPhase"), JobPhase.SEARCHING);
        plan.waitingReason = tag.getString("WaitingReason");
        plan.identity = tag.getString("Identity");
        plan.target = readPos(tag, "Target");
        plan.stand = readPos(tag, "Stand");
        plan.returnPosition = readPos(tag, "Return");
        plan.preDeliveryTarget = readPos(tag, "PreDeliveryTarget");
        plan.preDeliveryStand = readPos(tag, "PreDeliveryStand");
        plan.preDeliveryIdentity = tag.getString("PreDeliveryIdentity");
        plan.deliveryActive = tag.contains("DeliveryActive")
                ? tag.getBoolean("DeliveryActive")
                : plan.phase == JobPhase.DELIVERING;
        plan.deliveryReturnPending = tag.getBoolean("DeliveryReturnPending");
        if (tag.contains("Payload", 10)) plan.payload = tag.getCompound("Payload").copy();
        return plan;
    }

    public void copyFrom(JobPlan other) {
        this.job = other.job;
        this.phase = other.phase;
        this.preDeliveryPhase = other.preDeliveryPhase;
        this.waitingReason = other.waitingReason;
        this.identity = other.identity;
        this.target = other.target == null ? null : other.target.immutable();
        this.stand = other.stand == null ? null : other.stand.immutable();
        this.returnPosition = other.returnPosition == null ? null : other.returnPosition.immutable();
        this.preDeliveryTarget = other.preDeliveryTarget == null ? null : other.preDeliveryTarget.immutable();
        this.preDeliveryStand = other.preDeliveryStand == null ? null : other.preDeliveryStand.immutable();
        this.preDeliveryIdentity = other.preDeliveryIdentity;
        this.deliveryActive = other.deliveryActive;
        this.deliveryReturnPending = other.deliveryReturnPending;
        this.payload = other.payload.copy();
    }

    private static JobPhase phase(String raw, JobPhase fallback) {
        try {
            return JobPhase.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static void putPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) tag.putLong(key, pos.asLong());
    }

    @Nullable
    private static BlockPos readPos(CompoundTag tag, String key) {
        return tag.contains(key) ? BlockPos.of(tag.getLong(key)) : null;
    }
}
