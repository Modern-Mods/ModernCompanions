package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import com.majorbonghits.moderncompanions.entity.ai.HunterCombatGoal;

import java.util.UUID;

/** Deterministic checks for the durable worker contract that need no game world. */
public final class JobContractTest {
    private JobContractTest() {}

    public static void main(String[] args) {
        lifecycleRetainsPausedWork();
        planRoundTripsDeliveryAndPayload();
        reservationsRespectOwnerExpiryAndRelease();
        failedActionsNeverAdvanceAPlan();
        minerRouteRulesPreferSafeCavesAndRejectStraightDrops();
        treeMetadataPreservesTwoByTwoFootprints();
        hunterContractRejectsUnsafeTargetsAndUnsupportedWeapons();
        chefContractRequiresTaggedRecipeInput();
        dropClaimsKeepOwnershipTyped();
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
        assert JobReservations.renew(null, first, 12L, 10L) == 1;
        assert !JobReservations.claim(null, ReservationType.ENTITY, "animal:1", second, "hunter", 20L, 5L);
        assert JobReservations.claim(null, ReservationType.ENTITY, "animal:1", second, "hunter", 23L, 5L);
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

    private static void minerRouteRulesPreferSafeCavesAndRejectStraightDrops() {
        BlockPos start = new BlockPos(0, 64, 0);
        assert MinerRouteRules.isStairStep(start, new BlockPos(1, 63, 0));
        assert !MinerRouteRules.isStairStep(start, new BlockPos(0, 63, 0));
        assert MinerRouteRules.stepCost(true, false, 0.0F, 0)
                < MinerRouteRules.stepCost(false, false, 1.5F, 0);
        assert MinerRouteRules.bridgeBudgetAvailable(0, 4);
        assert !MinerRouteRules.bridgeBudgetAvailable(4, 4);

        JobPlan plan = new JobPlan();
        plan.setJob(CompanionJob.MINER);
        BlockPos ore = new BlockPos(9, 28, 3);
        BlockPos returnPoint = new BlockPos(0, 64, 0);
        plan.checkpoint(JobPhase.WORKING, ore, new BlockPos(8, 28, 3), "ore", returnPoint);
        plan.beginDelivery(JobPhase.WORKING, ore, new BlockPos(8, 28, 3), "ore", returnPoint);
        plan.restoreAfterDelivery();
        assert plan.phase() == JobPhase.RETURNING;
        assert ore.equals(plan.target());
        assert returnPoint.equals(plan.returnPosition());
        assert plan.completeDeliveryReturn() == JobPhase.WORKING;
    }

    private static void treeMetadataPreservesTwoByTwoFootprints() {
        assert LumberjackJobGoal.isTwoByTwoFootprint(java.util.List.of(
                new BlockPos(0, 64, 0), new BlockPos(1, 64, 0),
                new BlockPos(0, 64, 1), new BlockPos(1, 64, 1)));
        assert !LumberjackJobGoal.isTwoByTwoFootprint(java.util.List.of(
                new BlockPos(0, 64, 0), new BlockPos(1, 64, 0),
                new BlockPos(0, 64, 1)));
        assert !LumberjackJobGoal.isTwoByTwoFootprint(java.util.List.of(
                new BlockPos(0, 64, 0), new BlockPos(2, 64, 0),
                new BlockPos(0, 64, 1), new BlockPos(2, 64, 1)));
    }

    private static void hunterContractRejectsUnsafeTargetsAndUnsupportedWeapons() {
        assert HunterJobGoal.passesEligibility(true, false, false, false, false, false);
        assert HunterJobGoal.passesEligibility(false, false, false, false, false, true);
        assert !HunterJobGoal.passesEligibility(true, true, false, false, false, false);
        assert !HunterJobGoal.passesEligibility(true, false, true, false, false, false);
        assert !HunterJobGoal.passesEligibility(true, false, false, true, false, false);
        assert !HunterJobGoal.passesEligibility(true, false, false, false, true, false);
        assert !HunterJobGoal.passesEligibility(false, false, false, false, false, false);

        assert JobToolPolicy.isRequired(CompanionJob.HUNTER);
        assert HunterCombatGoal.supportsWeaponMode(true, false, false, false);
        assert HunterCombatGoal.supportsWeaponMode(false, true, false, false);
        assert HunterCombatGoal.supportsWeaponMode(false, false, true, true);
        assert !HunterCombatGoal.supportsWeaponMode(false, false, true, false);
        assert !HunterCombatGoal.supportsWeaponMode(false, false, false, true);
    }

    private static void chefContractRequiresTaggedRecipeInput() {
        assert ChefJobGoal.acceptsTaggedRecipe(true, true);
        assert !ChefJobGoal.acceptsTaggedRecipe(false, true);
        assert !ChefJobGoal.acceptsTaggedRecipe(true, false);
    }

    private static void dropClaimsKeepOwnershipTyped() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        assert JobDropClaims.ownerMatches(owner, owner);
        assert !JobDropClaims.ownerMatches(owner, other);
        assert !JobDropClaims.ownerMatches(null, owner);
    }
}
