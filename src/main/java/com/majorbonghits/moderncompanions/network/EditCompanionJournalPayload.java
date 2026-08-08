package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Owner-only update request from the companion journal's editable identity fields. */
public record EditCompanionJournalPayload(int entityId, String field, String value) implements CustomPacketPayload {
    public static final Type<EditCompanionJournalPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "edit_companion_journal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EditCompanionJournalPayload> CODEC =
            StreamCodec.of(EditCompanionJournalPayload::encode, EditCompanionJournalPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, EditCompanionJournalPayload payload) {
        buf.writeVarInt(payload.entityId);
        buf.writeUtf(payload.field, 16);
        buf.writeUtf(payload.value, 240);
    }

    private static EditCompanionJournalPayload decode(RegistryFriendlyByteBuf buf) {
        return new EditCompanionJournalPayload(buf.readVarInt(), buf.readUtf(16), buf.readUtf(240));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
