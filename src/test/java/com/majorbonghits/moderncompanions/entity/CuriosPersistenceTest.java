package com.majorbonghits.moderncompanions.entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Regression check for Curios' deferred attachment lifecycle during entity NBT loads. */
public final class CuriosPersistenceTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/com/majorbonghits/moderncompanions/entity/AbstractHumanCompanionEntity.java");

    private CuriosPersistenceTest() {}

    public static void main(String[] args) throws IOException {
        String source = Files.readString(SOURCE);
        int loadStart = source.indexOf("public void load(CompoundTag tag)");
        int readStart = source.indexOf("public void readAdditionalSaveData(CompoundTag tag)");
        int superReadCall = source.indexOf("super.readAdditionalSaveData(tag);", readStart);
        int postLoadLookup = source.indexOf("CuriosApi.getCuriosInventory(this);", superReadCall);
        int explicitReset = source.indexOf("handler.reset()", readStart);

        assert loadStart < 0 : "Curios must not be queried before Entity.load deserializes attachments";
        assert readStart >= 0;
        assert superReadCall > readStart;
        assert postLoadLookup > superReadCall;
        assert explicitReset < 0 : "Curios must consume deferred data through its capability constructor exactly once";
    }
}
