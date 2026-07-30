package com.majorbonghits.moderncompanions.compat.jei;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.item.CompanionBrewing;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
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
    }
}
