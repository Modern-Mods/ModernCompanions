package com.majorbonghits.moderncompanions.core;

import java.util.ArrayList;
import java.util.List;

/** Dependency-free default handling for Alert exclusions and its regression check. */
final class AlertExclusionDefaults {
    static final String CREEPER_ID = "minecraft:creeper";

    private AlertExclusionDefaults() {}

    /** Adds the original safety default while retaining every player-configured exclusion. */
    static List<String> withDefaultCreeper(List<? extends String> excludedMobs) {
        if (excludedMobs.contains(CREEPER_ID)) return List.copyOf(excludedMobs);

        List<String> migrated = new ArrayList<>(excludedMobs);
        migrated.add(CREEPER_ID);
        return List.copyOf(migrated);
    }
}
