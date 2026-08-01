package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client request to toggle one of a companion's six main equipment renders. */
public record CompanionToggleEquipmentRenderPayload(int entityId, int slotIndex) implements CustomPacketPayload {
    public static final Type<CompanionToggleEquipmentRenderPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "toggle_companion_equipment_render"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompanionToggleEquipmentRenderPayload> CODEC =
            StreamCodec.of(CompanionToggleEquipmentRenderPayload::encode, CompanionToggleEquipmentRenderPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CompanionToggleEquipmentRenderPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeVarInt(payload.slotIndex);
    }

    private static CompanionToggleEquipmentRenderPayload decode(RegistryFriendlyByteBuf buf) {
        return new CompanionToggleEquipmentRenderPayload(buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
