package com.majorbonghits.moderncompanions.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.currency.CardData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Mod-owned persistent components used by ItemStacks. */
public final class ModDataComponents {
    private ModDataComponents() {
    }

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModernCompanions.MOD_ID);

    private static final Codec<CardData> CARD_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("card_id").forGetter(CardData::cardId),
            Codec.LONG.fieldOf("balance").forGetter(CardData::balance)
    ).apply(instance, CardData::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, CardData> CARD_DATA_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CardData::cardId,
            ByteBufCodecs.VAR_LONG, CardData::balance,
            CardData::new);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardData>> CREDIT_CARD =
            COMPONENTS.register("credit_card", () -> DataComponentType.<CardData>builder()
                    .persistent(CARD_DATA_CODEC)
                    .networkSynchronized(CARD_DATA_STREAM_CODEC)
                    .build());

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
