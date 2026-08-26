package com.majorbonghits.moderncompanions.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Verifies the shipped recipe uses the 1.21.1 Ingredient object schema. */
public final class CompanionTableRecipeTest {
    public static void main(String[] args) throws Exception {
        try (var stream = CompanionTableRecipeTest.class
                .getResourceAsStream("/data/modern_companions/recipe/companion_table.json")) {
            assert stream != null;
            JsonObject key = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("key");
            for (Map.Entry<String, String> entry : Map.of(
                    "D", "minecraft:diamond",
                    "B", "minecraft:book",
                    "O", "minecraft:obsidian",
                    "E", "minecraft:echo_shard").entrySet()) {
                JsonObject ingredient = key.getAsJsonObject(entry.getKey());
                assert ingredient.get("item").getAsString().equals(entry.getValue());
            }
        }
    }
}
