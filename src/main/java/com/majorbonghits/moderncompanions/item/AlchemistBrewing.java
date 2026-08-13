package com.majorbonghits.moderncompanions.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Vanilla potion ingredient paths used by the Alchemist's inventory brewer; glass bottles are never required. */
public final class AlchemistBrewing {
    private static final Map<ResourceLocation, List<ResourceLocation>> RECIPES = createRecipes();

    private AlchemistBrewing() {
    }

    public static Optional<List<ResourceLocation>> recipeFor(ResourceLocation potionId) {
        return Optional.ofNullable(RECIPES.get(potionId));
    }

    /** Creates the configured recipe's one-use splash potion after registry validation. */
    public static ItemStack createSplash(AlchemistRecipeData data) {
        ResourceKey<Potion> potionKey = ResourceKey.create(Registries.POTION, data.potionId());
        return BuiltInRegistries.POTION.getHolder(potionKey)
                .map(holder -> PotionContents.createItemStack(Items.SPLASH_POTION, holder))
                .orElse(ItemStack.EMPTY);
    }

    private static Map<ResourceLocation, List<ResourceLocation>> createRecipes() {
        Map<ResourceLocation, List<ResourceLocation>> recipes = new HashMap<>();

        // The ordinary paths mirror PotionBrewing: nether wart makes the awkward base,
        // the effect reagent selects the potion, and gunpowder makes the result splash.
        add(recipes, "healing", "nether_wart", "glistering_melon_slice");
        add(recipes, "strong_healing", "nether_wart", "glistering_melon_slice", "glowstone_dust");
        add(recipes, "harming", "nether_wart", "glistering_melon_slice", "fermented_spider_eye");
        add(recipes, "strong_harming", "nether_wart", "glistering_melon_slice", "glowstone_dust", "fermented_spider_eye");

        add(recipes, "regeneration", "nether_wart", "ghast_tear");
        add(recipes, "long_regeneration", "nether_wart", "ghast_tear", "redstone");
        add(recipes, "strong_regeneration", "nether_wart", "ghast_tear", "glowstone_dust");
        add(recipes, "poison", "nether_wart", "spider_eye");
        add(recipes, "long_poison", "nether_wart", "spider_eye", "redstone");
        add(recipes, "strong_poison", "nether_wart", "spider_eye", "glowstone_dust");
        add(recipes, "strength", "nether_wart", "blaze_powder");
        add(recipes, "long_strength", "nether_wart", "blaze_powder", "redstone");
        add(recipes, "strong_strength", "nether_wart", "blaze_powder", "glowstone_dust");
        add(recipes, "swiftness", "nether_wart", "sugar");
        add(recipes, "long_swiftness", "nether_wart", "sugar", "redstone");
        add(recipes, "strong_swiftness", "nether_wart", "sugar", "glowstone_dust");
        add(recipes, "leaping", "nether_wart", "rabbit_foot");
        add(recipes, "long_leaping", "nether_wart", "rabbit_foot", "redstone");
        add(recipes, "strong_leaping", "nether_wart", "rabbit_foot", "glowstone_dust");
        add(recipes, "fire_resistance", "nether_wart", "magma_cream");
        add(recipes, "long_fire_resistance", "nether_wart", "magma_cream", "redstone");
        add(recipes, "water_breathing", "nether_wart", "pufferfish");
        add(recipes, "long_water_breathing", "nether_wart", "pufferfish", "redstone");
        add(recipes, "night_vision", "nether_wart", "golden_carrot");
        add(recipes, "long_night_vision", "nether_wart", "golden_carrot", "redstone");
        add(recipes, "invisibility", "nether_wart", "golden_carrot", "fermented_spider_eye");
        add(recipes, "long_invisibility", "nether_wart", "golden_carrot", "redstone", "fermented_spider_eye");
        add(recipes, "slow_falling", "nether_wart", "phantom_membrane");
        add(recipes, "long_slow_falling", "nether_wart", "phantom_membrane", "redstone");
        add(recipes, "turtle_master", "nether_wart", "turtle_helmet");
        add(recipes, "long_turtle_master", "nether_wart", "turtle_helmet", "redstone");
        add(recipes, "strong_turtle_master", "nether_wart", "turtle_helmet", "glowstone_dust");

        // Weakness is the one vanilla path that starts from a water potion directly.
        add(recipes, "weakness", "fermented_spider_eye");
        add(recipes, "long_weakness", "fermented_spider_eye", "redstone");
        add(recipes, "slowness", "nether_wart", "rabbit_foot", "fermented_spider_eye");
        add(recipes, "long_slowness", "nether_wart", "rabbit_foot", "redstone", "fermented_spider_eye");
        add(recipes, "strong_slowness", "nether_wart", "rabbit_foot", "glowstone_dust", "fermented_spider_eye");

        // The 1.21 effect potions are awkward-base start mixes in vanilla.
        add(recipes, "wind_charged", "nether_wart", "breeze_rod");
        add(recipes, "weaving", "nether_wart", "cobweb");
        add(recipes, "oozing", "nether_wart", "slime_block");
        add(recipes, "infested", "nether_wart", "stone");
        return Map.copyOf(recipes);
    }

    private static void add(Map<ResourceLocation, List<ResourceLocation>> recipes, String potion, String... ingredients) {
        List<ResourceLocation> ids = new ArrayList<>(ingredients.length + 1);
        for (String ingredient : ingredients) {
            // The Alchemist creates the splash output directly, so a glass bottle is not a consumed ingredient.
            if ("glass_bottle".equals(ingredient)) continue;
            ids.add(ResourceLocation.withDefaultNamespace(ingredient));
        }
        ids.add(ResourceLocation.withDefaultNamespace("gunpowder"));
        recipes.put(ResourceLocation.withDefaultNamespace(potion), List.copyOf(ids));
    }

    public static Item resolveIngredient(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
    }
}
