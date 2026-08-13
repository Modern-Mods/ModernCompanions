package com.majorbonghits.moderncompanions.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Persistent recipe payload carried by a configured Alchemist recipe item. */
public record AlchemistRecipeData(ResourceLocation potionId, List<ResourceLocation> ingredients) {
    public static final Codec<AlchemistRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("potion").forGetter(AlchemistRecipeData::potionId),
            ResourceLocation.CODEC.listOf().fieldOf("ingredients").forGetter(AlchemistRecipeData::ingredients)
    ).apply(instance, AlchemistRecipeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlchemistRecipeData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AlchemistRecipeData::potionId,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), AlchemistRecipeData::ingredients,
            AlchemistRecipeData::new);

    public AlchemistRecipeData {
        ingredients = List.copyOf(ingredients);
    }
}
