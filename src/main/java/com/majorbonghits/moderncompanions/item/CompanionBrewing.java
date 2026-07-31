package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import java.util.List;

/** Narrow recipes preserve vessel shape through water, awkward, and reagent steps. */
public final class CompanionBrewing {
    private CompanionBrewing() {}

    public static void register(RegisterBrewingRecipesEvent event) {
        for (BrewingStep step : steps()) {
            if (isWaterBottle(step.ingredient())) water(event, step.input(), step.output());
            else recipe(event, step.input(), step.ingredient(), step.output());
        }
    }

    /** Shared with JEI so the displayed and functional brewing paths cannot diverge. */
    public static List<BrewingStep> steps() {
        ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        return List.of(
                new BrewingStep("water_round", ModItems.EMPTY_ROUND_VESSEL.get(), waterBottle, ModItems.WATER_ROUND_VESSEL.get()),
                new BrewingStep("water_rectangle", ModItems.EMPTY_RECTANGLE_VESSEL.get(), waterBottle, ModItems.WATER_RECTANGLE_VESSEL.get()),
                new BrewingStep("water_pyramid", ModItems.EMPTY_PYRAMID_VESSEL.get(), waterBottle, ModItems.WATER_PYRAMID_VESSEL.get()),
                new BrewingStep("water_hexagon", ModItems.EMPTY_HEXAGON_VESSEL.get(), waterBottle, ModItems.WATER_HEXAGON_VESSEL.get()),
                new BrewingStep("water_droplet", ModItems.EMPTY_DROPLET_VESSEL.get(), waterBottle, ModItems.WATER_DROPLET_VESSEL.get()),
                new BrewingStep("awkward_round", ModItems.WATER_ROUND_VESSEL.get(), new ItemStack(Items.NETHER_WART), ModItems.AWKWARD_ROUND_VESSEL.get()),
                new BrewingStep("awkward_rectangle", ModItems.WATER_RECTANGLE_VESSEL.get(), new ItemStack(Items.NETHER_WART), ModItems.AWKWARD_RECTANGLE_VESSEL.get()),
                new BrewingStep("awkward_pyramid", ModItems.WATER_PYRAMID_VESSEL.get(), new ItemStack(Items.NETHER_WART), ModItems.AWKWARD_PYRAMID_VESSEL.get()),
                new BrewingStep("awkward_hexagon", ModItems.WATER_HEXAGON_VESSEL.get(), new ItemStack(Items.NETHER_WART), ModItems.AWKWARD_HEXAGON_VESSEL.get()),
                new BrewingStep("awkward_droplet", ModItems.WATER_DROPLET_VESSEL.get(), new ItemStack(Items.NETHER_WART), ModItems.AWKWARD_DROPLET_VESSEL.get()),
                new BrewingStep("health", ModItems.AWKWARD_ROUND_VESSEL.get(), new ItemStack(Items.GLISTERING_MELON_SLICE), ModItems.HEALTH_POTION.get()),
                new BrewingStep("regeneration", ModItems.AWKWARD_ROUND_VESSEL.get(), new ItemStack(Items.GHAST_TEAR), ModItems.REGENERATION_POTION.get()),
                new BrewingStep("stamina_base", ModItems.AWKWARD_RECTANGLE_VESSEL.get(), new ItemStack(Items.SUGAR), ModItems.STAMINA_BASE.get()),
                new BrewingStep("stamina", ModItems.STAMINA_BASE.get(), new ItemStack(Items.RABBIT_FOOT), ModItems.STAMINA_POTION.get()),
                new BrewingStep("mana_base", ModItems.AWKWARD_PYRAMID_VESSEL.get(), new ItemStack(Items.AMETHYST_SHARD), ModItems.MANA_BASE.get()),
                new BrewingStep("mana", ModItems.MANA_BASE.get(), new ItemStack(Items.LAPIS_LAZULI), ModItems.MANA_POTION.get()),
                new BrewingStep("rejuvenation_base", ModItems.AWKWARD_HEXAGON_VESSEL.get(), new ItemStack(Items.GHAST_TEAR), ModItems.REJUVENATION_BASE.get()),
                new BrewingStep("rejuvenation", ModItems.REJUVENATION_BASE.get(), new ItemStack(Items.AMETHYST_SHARD), ModItems.REJUVENATION_POTION.get()),
                new BrewingStep("shield_base", ModItems.AWKWARD_DROPLET_VESSEL.get(), new ItemStack(Items.TURTLE_SCUTE), ModItems.SHIELD_BASE.get()),
                new BrewingStep("shield", ModItems.SHIELD_BASE.get(), new ItemStack(Items.IRON_INGOT), ModItems.SHIELD_POTION.get())
        );
    }

    public record BrewingStep(String id, Item input, ItemStack ingredient, Item output) {}

    private static void recipe(RegisterBrewingRecipesEvent event, Item input, ItemStack ingredient, Item output) {
        event.getBuilder().addRecipe(Ingredient.of(input), Ingredient.of(ingredient), new ItemStack(output));
    }

    private static void water(RegisterBrewingRecipesEvent event, Item input, Item output) {
        event.getBuilder().addRecipe(new IBrewingRecipe() {
            @Override public boolean isInput(ItemStack stack) { return stack.is(input); }
            @Override public boolean isIngredient(ItemStack stack) { return isWaterBottle(stack); }
            @Override public ItemStack getOutput(ItemStack vessel, ItemStack ingredient) {
                return isInput(vessel) && isIngredient(ingredient) ? new ItemStack(output) : ItemStack.EMPTY;
            }
        });
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
    }
}
