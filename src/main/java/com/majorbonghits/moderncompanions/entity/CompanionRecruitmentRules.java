package com.majorbonghits.moderncompanions.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Parses the small, registry-ID based format used by the recruitment config. */
public final class CompanionRecruitmentRules {
    private CompanionRecruitmentRules() {}

    public record Requirement(String companionId, String itemId, int count) {}

    public static Optional<Requirement> parseEntry(String raw) {
        if (raw == null) return Optional.empty();
        String[] parts = raw.split("\\|", -1);
        if (parts.length != 3) return Optional.empty();

        String companionId = parts[0].trim();
        String itemId = parts[1].trim();
        if (companionId.isEmpty() || itemId.isEmpty()) return Optional.empty();
        try {
            int count = Integer.parseInt(parts[2].trim());
            return count > 0 && count <= 100_000
                    ? Optional.of(new Requirement(companionId, itemId, count))
                    : Optional.empty();
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    /** Combines repeated rows so a pack can split a requirement across config-list entries. */
    public static Map<String, Map<String, Integer>> parse(List<? extends String> entries) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        for (String raw : entries) {
            parseEntry(raw).ifPresent(rule -> result
                    .computeIfAbsent(rule.companionId(), ignored -> new LinkedHashMap<>())
                    .merge(rule.itemId(), rule.count(), Integer::sum));
        }
        return result;
    }
}
