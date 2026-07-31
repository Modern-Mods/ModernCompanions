package com.majorbonghits.moderncompanions.world;

/** No-world regression checks for the structure-resident insertion gate. */
public final class StructureCompanionSpawnerTest {
    public static void main(String[] args) {
        assert !StructureSpawnRules.canInsert(false, false);
        assert !StructureSpawnRules.canInsert(true, true);
        assert StructureSpawnRules.canInsert(true, false);
    }
}
