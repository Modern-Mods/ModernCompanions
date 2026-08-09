package com.majorbonghits.moderncompanions.currency;

import java.util.List;

/** Pure money rules kept independent of Minecraft so the invariant check stays runnable. */
final class CurrencyRules {
    private CurrencyRules() {
    }

    static long safeAdd(long first, long second) {
        if (first < 0L || second < 0L || first > Long.MAX_VALUE - second) return -1L;
        return first + second;
    }

    static int highestBalanceIndex(List<Long> balances) {
        int best = -1;
        for (int index = 0; index < balances.size(); index++) {
            if (best < 0 || balances.get(index) > balances.get(best)) best = index;
        }
        return best;
    }

    static int lowestSufficientIndex(List<Long> balances, long cost) {
        if (cost <= 0L) return -1;
        int best = -1;
        for (int index = 0; index < balances.size(); index++) {
            long balance = balances.get(index);
            if (balance >= cost && (best < 0 || balance < balances.get(best))) best = index;
        }
        return best;
    }

    static Conversion conversion(long fromValue, long toValue) {
        if (fromValue <= 0L || toValue <= 0L) return null;
        long gcd = gcd(fromValue, toValue);
        long fromCount = toValue / gcd;
        long toCount = fromValue / gcd;
        if (fromCount > Integer.MAX_VALUE || toCount > Integer.MAX_VALUE) return null;
        return new Conversion((int) fromCount, (int) toCount);
    }

    private static long gcd(long first, long second) {
        while (second != 0L) {
            long remainder = first % second;
            first = second;
            second = remainder;
        }
        return first;
    }

    record Conversion(int fromCount, int toCount) {
    }
}
