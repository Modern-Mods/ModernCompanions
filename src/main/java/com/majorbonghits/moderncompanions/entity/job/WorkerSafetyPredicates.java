package com.majorbonghits.moderncompanions.entity.job;

/** Pure rules kept separate so route safety stays cheap to test without a game world. */
final class WorkerSafetyPredicates {
    private WorkerSafetyPredicates() {}

    static boolean stepHeightIsSafe(int fromY, int toY) {
        return Math.abs(toY - fromY) <= 1;
    }

    static boolean hasTwoBlockHeadroom(boolean feetClear, boolean headClear) {
        return feetClear && headClear;
    }
}
