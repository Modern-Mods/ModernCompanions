package com.majorbonghits.moderncompanions.compat.jei;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.currency.CurrencyTrade;
import com.majorbonghits.moderncompanions.currency.CurrencyTradeResolver;
import com.majorbonghits.moderncompanions.item.CompanionBrewing;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
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
    private static final RecipeType<CurrencyTradeResolver.Resolved> CURRENCY_TRADES = RecipeType.create(
            ModernCompanions.MOD_ID, "currency_trade", CurrencyTradeResolver.Resolved.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (ModConfig.safeGet(ModConfig.CURRENCIES_ENABLED)) {
            registration.addRecipeCategories(new CurrencyTradeCategory(registration.getJeiHelpers().getGuiHelper()));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ModConfig.safeGet(ModConfig.CURRENCIES_ENABLED)) {
            List<CurrencyTradeResolver.Resolved> trades = ModConfig.safeGet(ModConfig.CURRENCY_TRADE_RECIPES).stream()
                    .map(CurrencyTrade::parse)
                    .flatMap(java.util.Optional::stream)
                    .map(CurrencyTradeResolver::resolve)
                    .flatMap(java.util.Optional::stream)
                    .toList();
            registration.addRecipes(CURRENCY_TRADES, trades);
        }

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
        ShapedRecipe placementWandRecipe = new ShapedRecipe("", CraftingBookCategory.MISC,
                ShapedRecipePattern.of(java.util.Map.of(
                        'R', Ingredient.of(Items.REDSTONE),
                        'S', Ingredient.of(Items.STICK)), "  R", " S ", " S "),
                new ItemStack(ModItems.PLACEMENT_WAND.get()));
        RecipeHolder<CraftingRecipe> placementWandHolder = new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "placement_wand"), placementWandRecipe);
        // Imported armor recipes use JEI's built-in minecraft crafting and smithing
        // recipe types, so datapack discovery exposes every armor result here without
        // maintaining a second, potentially divergent recipe list.
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(holder, animalWandHolder, placementWandHolder));
    }

    private static final class CurrencyTradeCategory extends AbstractRecipeCategory<CurrencyTradeResolver.Resolved> {
        private CurrencyTradeCategory(IGuiHelper helper) {
            super(CURRENCY_TRADES, Component.translatable("jei.modern_companions.currency_trade"),
                    helper.createBlankDrawable(116, 54), 116, 54);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder layout, CurrencyTradeResolver.Resolved trade, IFocusGroup focuses) {
            layout.addSlot(RecipeIngredientRole.INPUT, 5, 19).addItemStack(trade.firstInput());
            if (!trade.secondInput().isEmpty()) {
                layout.addSlot(RecipeIngredientRole.INPUT, 29, 19).addItemStack(trade.secondInput());
            }
            layout.addSlot(RecipeIngredientRole.OUTPUT, 92, 19).addItemStack(trade.output());
        }
    }
}
