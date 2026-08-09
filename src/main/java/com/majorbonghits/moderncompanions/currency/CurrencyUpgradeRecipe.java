package com.majorbonghits.moderncompanions.currency;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

/** Dynamic, value-preserving adjacent-denomination crafting recipe. */
public final class CurrencyUpgradeRecipe implements CraftingRecipe {
    private final ResourceLocation fromId;
    private final ResourceLocation toId;
    private final Item from;
    private final Item to;
    private final CraftingBookCategory category;

    public CurrencyUpgradeRecipe(ResourceLocation fromId, ResourceLocation toId, CraftingBookCategory category) {
        this.fromId = fromId;
        this.toId = toId;
        this.from = BuiltInRegistries.ITEM.getOptional(fromId).orElse(Items.AIR);
        this.to = BuiltInRegistries.ITEM.getOptional(toId).orElse(Items.AIR);
        this.category = category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        CurrencyService.Conversion conversion = CurrencyService.conversion(from, to);
        if (!CurrencyService.enabled() || !craftable(conversion)
                || input.ingredientCount() != conversion.fromCount()) return false;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (!stack.isEmpty() && stack.getItem() != from) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        CurrencyService.Conversion conversion = CurrencyService.conversion(from, to);
        return conversion == null ? ItemStack.EMPTY : new ItemStack(to, conversion.toCount());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        CurrencyService.Conversion conversion = CurrencyService.conversion(from, to);
        return craftable(conversion) && conversion.fromCount() <= width * height;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(to);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        CurrencyService.Conversion conversion = CurrencyService.conversion(from, to);
        if (!craftable(conversion)) return NonNullList.create();
        return NonNullList.withSize(conversion.fromCount(), Ingredient.of(from));
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return com.majorbonghits.moderncompanions.core.ModRecipeSerializers.CURRENCY_UPGRADE.get();
    }

    private static boolean craftable(CurrencyService.Conversion conversion) {
        return conversion != null && conversion.fromCount() <= 9 && conversion.toCount() <= 64;
    }

    public static final class Serializer implements RecipeSerializer<CurrencyUpgradeRecipe> {
        private static final MapCodec<CurrencyUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("from").forGetter(recipe -> recipe.fromId),
                ResourceLocation.CODEC.fieldOf("to").forGetter(recipe -> recipe.toId),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(recipe -> recipe.category)
        ).apply(instance, CurrencyUpgradeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CurrencyUpgradeRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, recipe -> recipe.fromId,
                        ResourceLocation.STREAM_CODEC, recipe -> recipe.toId,
                        CraftingBookCategory.STREAM_CODEC, recipe -> recipe.category,
                        CurrencyUpgradeRecipe::new);

        @Override
        public MapCodec<CurrencyUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CurrencyUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
