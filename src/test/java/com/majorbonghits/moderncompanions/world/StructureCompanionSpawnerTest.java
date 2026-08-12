package com.majorbonghits.moderncompanions.world;

/** No-world regression checks for the structure-resident retry gate. */
public final class StructureCompanionSpawnerTest {
    public static void main(String[] args) {
        assert StructureSpawnRules.shouldRetry(false, false);
        assert !StructureSpawnRules.shouldRetry(false, true);
        assert !StructureSpawnRules.shouldRetry(true, false);
    }
}
