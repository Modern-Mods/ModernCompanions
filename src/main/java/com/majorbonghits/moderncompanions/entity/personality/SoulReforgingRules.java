package com.majorbonghits.moderncompanions.entity.personality;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Pure selection rule shared by the Soul Gem item and its regression check. */
public final class SoulReforgingRules {
    private SoulReforgingRules() {}

    public static List<String> rollOptions(List<String> preferred, String primary, String secondary, Random random) {
        List<String> options = new ArrayList<>();
        Set<String> excluded = new HashSet<>();
        if (primary != null && !primary.isEmpty()) excluded.add(primary);
        if (secondary != null && !secondary.isEmpty()) excluded.add(secondary);
        addRandomOptions(options, preferred, excluded, random);
        addRandomOptions(options, CompanionPersonality.TRAITS, excluded, random);
        return options;
    }

    private static void addRandomOptions(List<String> options, List<String> pool, Set<String> excluded,
            Random random) {
        List<String> remaining = new ArrayList<>(pool);
        while (!remaining.isEmpty() && options.size() < 3) {
            String trait = remaining.remove(random.nextInt(remaining.size()));
            if (!excluded.contains(trait) && !options.contains(trait)) options.add(trait);
        }
    }
}
