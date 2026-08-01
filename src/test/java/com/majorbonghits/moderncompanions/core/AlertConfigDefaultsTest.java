package com.majorbonghits.moderncompanions.core;

import java.util.List;

/** Regression check for preserving player exclusions while adding the Creeper default. */
public final class AlertConfigDefaultsTest {
    public static void main(String[] args) {
        assert AlertExclusionDefaults.withDefaultCreeper(List.of()).equals(List.of("minecraft:creeper"));
        assert AlertExclusionDefaults.withDefaultCreeper(List.of("minecraft:ender_dragon"))
                .equals(List.of("minecraft:ender_dragon", "minecraft:creeper"));
        assert AlertExclusionDefaults.withDefaultCreeper(List.of("minecraft:creeper")).equals(List.of("minecraft:creeper"));
    }
}
