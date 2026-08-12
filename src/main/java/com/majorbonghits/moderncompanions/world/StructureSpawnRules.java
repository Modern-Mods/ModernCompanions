package com.majorbonghits.moderncompanions.world;

/** Pure retry rule kept separate so it can be checked without bootstrapping Minecraft registries. */
final class StructureSpawnRules {
    private StructureSpawnRules() {}

    static boolean shouldRetry(boolean alreadySpawned, boolean spawnSucceeded) {
        return !alreadySpawned && !spawnSucceeded;
    }
}
