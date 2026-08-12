package com.majorbonghits.moderncompanions.entity.job;

import java.util.HashSet;
import java.util.Set;

/** Focused executable checks for route height and two-block work-site headroom. */
public final class WorkerSafetyPredicatesTest {
    public static void main(String[] args) {
        assert WorkerSafetyPredicates.stepHeightIsSafe(64, 65);
        assert WorkerSafetyPredicates.stepHeightIsSafe(65, 64);
        assert !WorkerSafetyPredicates.stepHeightIsSafe(64, 66);
        assert WorkerSafetyPredicates.hasTwoBlockHeadroom(true, true);
        assert !WorkerSafetyPredicates.hasTwoBlockHeadroom(true, false);
        assert WorkerSafetyPredicates.excavationHeadFirst(64, 63);
        assert !WorkerSafetyPredicates.excavationHeadFirst(64, 64);
        assert !WorkerSafetyPredicates.excavationHeadFirst(64, 65);
        assert !WorkerSafetyPredicates.bulkDeliveryDue(11000L, 10000L, 2400L);
        assert WorkerSafetyPredicates.bulkDeliveryDue(12400L, 10000L, 2400L);
        assert WorkerSafetyPredicates.bulkDeliveryDue(12000L, 11999L, 2400L);
        Set<Long> columns = new HashSet<>();
        for (int i = 0; i < 25; i++) columns.add(WorkerSafetyPredicates.spiralOffset(i));
        assert columns.size() == 25;
        assert WorkerSafetyPredicates.spiralOffset(0) == 0L;
        long first = WorkerSafetyPredicates.spiralOffset(1);
        assert Math.abs((int) (first >> 32)) + Math.abs((int) first) == 1;
        JobLifecycle lifecycle = new JobLifecycle();
        lifecycle.advance(JobPhase.WORKING);
        lifecycle.pause("combat");
        assert lifecycle.phase() == JobPhase.PAUSED;
        lifecycle.resume();
        assert lifecycle.phase() == JobPhase.WORKING;
        assert lifecycle.retry("blocked", 1);
        assert !lifecycle.retry("blocked", 1);
        JobLifecycle backoff = new JobLifecycle();
        assert backoff.retry("route", 3, 10, 200);
        assert !backoff.retryReady();
        for (int i = 0; i < 10; i++) backoff.tick();
        assert backoff.retryReady();
    }
}
