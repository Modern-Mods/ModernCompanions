package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Deterministic checks for the durable worker contract that need no game world. */
public final class JobContractTest {
    private JobContractTest() {}

    public static void main(String[] args) {
        lifecycleRetainsPausedWork();
        planRoundTripsDeliveryAndPayload();
        reservationsRespectOwnerExpiryAndRelease();
        failedActionsNeverAdvanceAPlan();
    }

    private static void lifecycleRetainsPausedWork() {
        JobLifecycle lifecycle = new JobLifecycle();
        lifecycle.advance(JobPhase.WORKING);
        lifecycle.pause("combat");
        assert lifecycle.phase() == JobPhase.PAUSED;
        assert "combat".equals(lifecycle.reason());
        lifecycle.resume();
        assert lifecycle.phase() == JobPhase.WORKING;
        assert lifecycle.retry("blocked", 1);
        assert !lifecycle.retryReady();
    }

    private static void planRoundTripsDeliveryAndPayload() {
        BlockPos target = new BlockPos(8, 64, -3);
        BlockPos stand = new BlockPos(7, 64, -3);
        JobPlan plan = new JobPlan();
        plan.setJob(CompanionJob.FISHER);
        plan.checkpoint(JobPhase.WORKING, target, stand, "shore", new BlockPos(0, 64, 0));
        CompoundTag payload = new CompoundTag();
        payload.putLong("PendingCatch", 42L);
        plan.setPayload(payload);
        plan.beginDelivery(JobPhase.WORKING, target, stand, "shore", new BlockPos(4, 64, 4));

        JobPlan loaded = JobPlan.load(plan.save(new CompoundTag()));
        assert loaded.job() == CompanionJob.FISHER;
        assert loaded.phase() == JobPhase.DELIVERING;
        assert loaded.preDeliveryPhase() == JobPhase.WORKING;
        assert target.equals(loaded.preDeliveryTarget());
        assert stand.equals(loaded.preDeliveryStand());
        assert "shore".equals(loaded.preDeliveryIdentity());
        assert loaded.returnPosition().equals(new BlockPos(4, 64, 4));
        assert loaded.payload().getLong("PendingCatch") == 42L;
        assert loaded.deliveryActive();
        assert !loaded.deliveryReturnPending();

        loaded.restoreAfterDelivery();
        assert loaded.phase() == JobPhase.RETURNING;
        assert target.equals(loaded.target());
        assert stand.equals(loaded.stand());
        assert !loaded.deliveryActive();
        assert loaded.deliveryReturnPending();
        assert loaded.completeDeliveryReturn() == JobPhase.WORKING;
        assert loaded.phase() == JobPhase.WORKING;
        assert !loaded.deliveryReturnPending();
        assert loaded.returnPosition() == null;

        JobPlan debtPlan = new JobPlan();
        debtPlan.setJob(CompanionJob.LUMBERJACK);
        CompoundTag debt = new CompoundTag();
        debt.putString("ReplantDebt", "oak");
        debtPlan.setPayload(debt);
        debtPlan.beginDelivery(JobPhase.SEARCHING, null, null, "", new BlockPos(2, 64, 2));
        debtPlan.restoreAfterDelivery();
        assert debtPlan.completeDeliveryReturn() == JobPhase.SEARCHING;
        assert "oak".equals(debtPlan.payload().getString("ReplantDebt"));
    }

    private static void reservationsRespectOwnerExpiryAndRelease() {
        JobReservations.clear(null);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        assert JobReservations.claim(null, ReservationType.ENTITY, "animal:1", first, "hunter", 10L, 5L);
        assert !JobReservations.claim(null, ReservationType.ENTITY, "animal:1", second, "hunter", 11L, 5L)
                : "reservation unexpectedly replaced; size=" + JobReservations.size(null);
        assert JobReservations.claim(null, ReservationType.ENTITY, "animal:1", second, "hunter", 15L, 5L);
        assert JobReservations.size(null) == 1;
        JobReservations.release(null, second);
        assert JobReservations.size(null) == 0;
    }

    private static void failedActionsNeverAdvanceAPlan() {
        assert WorkerActionResult.SUCCESS.advancesPlan();
        for (WorkerActionResult result : WorkerActionResult.values()) {
            if (result != WorkerActionResult.SUCCESS) assert !result.advancesPlan();
        }
    }
}
