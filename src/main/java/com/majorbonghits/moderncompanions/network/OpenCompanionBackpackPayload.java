package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Requests the equipped Sophisticated Backpack inventory for an owned companion. */
public record OpenCompanionBackpackPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenCompanionBackpackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "open_companion_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCompanionBackpackPayload> CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, OpenCompanionBackpackPayload::entityId,
            OpenCompanionBackpackPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
