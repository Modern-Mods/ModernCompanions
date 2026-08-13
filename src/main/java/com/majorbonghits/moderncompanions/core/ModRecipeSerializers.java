package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.currency.CurrencyUpgradeRecipe;
import com.majorbonghits.moderncompanions.recipe.AlchemistRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom recipe serializers owned by Modern Companions. */
public final class ModRecipeSerializers {
    private ModRecipeSerializers() {
    }

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ModernCompanions.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<RecipeSerializer<?>, CurrencyUpgradeRecipe.Serializer> CURRENCY_UPGRADE =
            SERIALIZERS.register("currency_upgrade", CurrencyUpgradeRecipe.Serializer::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<RecipeSerializer<?>, AlchemistRecipe.Serializer> ALCHEMIST_RECIPE =
            SERIALIZERS.register("alchemist_recipe", AlchemistRecipe.Serializer::new);
}
