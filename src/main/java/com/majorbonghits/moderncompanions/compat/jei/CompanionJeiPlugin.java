package com.majorbonghits.moderncompanions.compat.jei;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.item.CompanionBrewing;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Exposes the runtime custom-vessel brewing transitions to JEI's vanilla brewing category. */
@JeiPlugin
public final class CompanionJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<IJeiBrewingRecipe> recipes = CompanionBrewing.steps().stream()
                .map(step -> registration.getVanillaRecipeFactory().createBrewingRecipe(
                        List.of(step.ingredient().copy()), new ItemStack(step.input()), new ItemStack(step.output()),
                        ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "brewing/" + step.id())))
                .toList();
        registration.addRecipes(RecipeTypes.BREWING, recipes);

        // Keep the table visible in JEI even when a pack's recipe filtering hides datapack recipes.
        ShapedRecipe companionTableRecipe = new ShapedRecipe("", CraftingBookCategory.MISC,
                ShapedRecipePattern.of(java.util.Map.of(
                        'D', Ingredient.of(Items.DIAMOND),
                        'B', Ingredient.of(Items.BOOK),
                        'O', Ingredient.of(Items.OBSIDIAN),
                        'E', Ingredient.of(Items.ECHO_SHARD)), "DBD", "OEO", "OOO"),
                new ItemStack(ModItems.COMPANION_TABLE.get()));
        RecipeHolder<CraftingRecipe> holder = new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "companion_table"), companionTableRecipe);
        ShapedRecipe animalWandRecipe = new ShapedRecipe("", CraftingBookCategory.MISC,
                ShapedRecipePattern.of(java.util.Map.of(
                        'E', Ingredient.of(Items.ECHO_SHARD),
                        'A', Ingredient.of(Items.AMETHYST_SHARD),
                        'S', Ingredient.of(Items.STICK)), "  E", " A ", " S "),
                new ItemStack(ModItems.ANIMAL_WAND.get()));
        RecipeHolder<CraftingRecipe> animalWandHolder = new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "animal_wand"), animalWandRecipe);
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(holder, animalWandHolder));
    }
}
