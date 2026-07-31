package com.majorbonghits.moderncompanions.world;

/** Pure insertion gate kept separate so it can be checked without bootstrapping Minecraft registries. */
final class StructureSpawnRules {
    private StructureSpawnRules() {}

    static boolean canInsert(boolean targetChunkLoaded, boolean alreadySpawned) {
        return targetChunkLoaded && !alreadySpawned;
    }
}
