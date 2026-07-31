package com.majorbonghits.moderncompanions.entity.job;

/** Pure rules kept separate so route safety stays cheap to test without a game world. */
public final class WorkerSafetyPredicates {
    private WorkerSafetyPredicates() {}

    static boolean stepHeightIsSafe(int fromY, int toY) {
        return Math.abs(toY - fromY) <= 1;
    }

    static boolean hasTwoBlockHeadroom(boolean feetClear, boolean headClear) {
        return feetClear && headClear;
    }

    static boolean excavationHeadFirst(int fromY, int toY) {
        return toY < fromY;
    }

    public static boolean bulkDeliveryDue(long gameTime, long lastDelivery, long interval) {
        long latestDusk = Math.floorDiv(gameTime - 12000L, 24000L) * 24000L + 12000L;
        return gameTime - lastDelivery >= interval || latestDusk > lastDelivery;
    }

    static long spiralOffset(int index) {
        if (index <= 0) return 0L;
        long n = (long) index + 1L;
        int ring = (int) Math.ceil((Math.sqrt(n) - 1.0D) / 2.0D);
        long side = ring * 2L;
        long max = (ring * 2L + 1L) * (ring * 2L + 1L);
        long offset = max - n;
        int x;
        int z;
        if (offset < side) {
            x = ring - (int) offset;
            z = -ring;
        } else if (offset < side * 2L) {
            x = -ring;
            z = -ring + (int) (offset - side);
        } else if (offset < side * 3L) {
            x = -ring + (int) (offset - side * 2L);
            z = ring;
        } else {
            x = ring;
            z = ring - (int) (offset - side * 3L);
        }
        return (long) x << 32 | z & 0xffffffffL;
    }
}
