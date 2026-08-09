package com.majorbonghits.moderncompanions.currency;

import java.util.Optional;

/** Parses the compact config format used for display-only JEI trade recipes. */
public record CurrencyTrade(String firstItem, int firstCount, String secondItem, int secondCount,
                            String outputItem, int outputCount) {
    public static Optional<CurrencyTrade> parse(String raw) {
        if (raw == null) return Optional.empty();
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 6) return Optional.empty();

        int firstCount = parseCount(parts[1]);
        int secondCount = "-".equals(parts[2]) ? 0 : parseCount(parts[3]);
        int outputCount = parseCount(parts[5]);
        // Keep the documented "-|0" sentinel strict so malformed no-input trades are rejected.
        if (firstCount < 1 || outputCount < 1
                || ("-".equals(parts[2]) ? !"0".equals(parts[3]) : secondCount < 1)) {
            return Optional.empty();
        }
        if (!validItemId(parts[0]) || (!"-".equals(parts[2]) && !validItemId(parts[2])) || !validItemId(parts[4])) {
            return Optional.empty();
        }
        return Optional.of(new CurrencyTrade(parts[0], firstCount, parts[2], secondCount, parts[4], outputCount));
    }

    private static int parseCount(String raw) {
        try {
            int count = Integer.parseInt(raw);
            return count > 0 && count <= 64 ? count : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static boolean validItemId(String raw) {
        return raw.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+");
    }

}
