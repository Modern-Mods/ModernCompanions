package com.majorbonghits.moderncompanions.entity.job;

/** Focused executable checks for route height and two-block work-site headroom. */
public final class WorkerSafetyPredicatesTest {
    public static void main(String[] args) {
        assert WorkerSafetyPredicates.stepHeightIsSafe(64, 65);
        assert WorkerSafetyPredicates.stepHeightIsSafe(65, 64);
        assert !WorkerSafetyPredicates.stepHeightIsSafe(64, 66);
        assert WorkerSafetyPredicates.hasTwoBlockHeadroom(true, true);
        assert !WorkerSafetyPredicates.hasTwoBlockHeadroom(true, false);
    }
}
