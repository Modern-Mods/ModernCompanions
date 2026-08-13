package com.majorbonghits.moderncompanions.recipe;

import com.majorbonghits.moderncompanions.core.ModDataComponents;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.item.AlchemistBrewing;
import com.majorbonghits.moderncompanions.item.AlchemistRecipeData;
import com.majorbonghits.moderncompanions.item.AlchemistRecipeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

/** Dynamic shapeless recipe: blank recipe paper plus a known potion creates its Alchemist recipe. */
public final class AlchemistRecipe implements CraftingRecipe {
    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != 2) return false;
        ItemStack blank = ItemStack.EMPTY;
        ItemStack potion = ItemStack.EMPTY;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) continue;
            if (AlchemistRecipeItem.isBlank(stack)) {
                if (!blank.isEmpty()) return false;
                blank = stack;
            } else if (stack.getItem() instanceof PotionItem) {
                if (!potion.isEmpty()) return false;
                potion = stack;
            } else {
                return false;
            }
        }
        return !blank.isEmpty() && !potion.isEmpty() && potionId(potion) != null
                && AlchemistBrewing.recipeFor(potionId(potion)).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack potion = findPotion(input);
        var potionId = potionId(potion);
        if (potion.isEmpty() || potionId == null) return ItemStack.EMPTY;
        List<net.minecraft.resources.ResourceLocation> ingredients =
                AlchemistBrewing.recipeFor(potionId).orElse(List.of());
        if (ingredients.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(ModItems.RECIPE.get());
        result.set(ModDataComponents.ALCHEMIST_RECIPE.get(), new AlchemistRecipeData(potionId, ingredients));
        result.set(DataComponents.POTION_CONTENTS,
                potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.RECIPE.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ModItems.RECIPE.get()),
                Ingredient.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION));
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return com.majorbonghits.moderncompanions.core.ModRecipeSerializers.ALCHEMIST_RECIPE.get();
    }

    private static ItemStack findPotion(CraftingInput input) {
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.getItem() instanceof PotionItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static net.minecraft.resources.ResourceLocation potionId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().flatMap(holder -> holder.unwrapKey().map(key -> key.location())).orElse(null);
    }

    public static final class Serializer implements RecipeSerializer<AlchemistRecipe> {
        private static final MapCodec<AlchemistRecipe> CODEC = MapCodec.unit(new AlchemistRecipe());
        // Recipe sync has no per-instance payload; decode a fresh recipe and accept every server instance.
        private static final StreamCodec<RegistryFriendlyByteBuf, AlchemistRecipe> STREAM_CODEC =
                StreamCodec.of((buffer, recipe) -> {}, buffer -> new AlchemistRecipe());

        @Override
        public MapCodec<AlchemistRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlchemistRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
