package com.majorbonghits.moderncompanions.network;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.CompanionVoice;
import com.majorbonghits.moderncompanions.core.ModSounds;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.network.OpenCompanionInventoryPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.SimpleMenuProvider;

import java.net.URI;
import java.net.URISyntaxException;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = ModernCompanions.MOD_ID)
public final class ModNetwork {
    private ModNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ModernCompanions.MOD_ID)
                .playToServer(ToggleFlagPayload.TYPE, ToggleFlagPayload.CODEC, ModNetwork::handleToggleFlag)
                .playToServer(CompanionActionPayload.TYPE, CompanionActionPayload.CODEC, ModNetwork::handleAction)
                .playToServer(SetPatrolRadiusPayload.TYPE, SetPatrolRadiusPayload.CODEC, ModNetwork::handlePatrolRadius)
                .playToServer(SetCompanionJobPayload.TYPE, SetCompanionJobPayload.CODEC, ModNetwork::handleSetJob)
                .playToServer(EditCompanionJournalPayload.TYPE, EditCompanionJournalPayload.CODEC, ModNetwork::handleJournalEdit)
                .playToServer(OpenCompanionInventoryPayload.TYPE, OpenCompanionInventoryPayload.CODEC, ModNetwork::handleOpenInventory)
                .playToServer(CompanionToggleEquipmentRenderPayload.TYPE, CompanionToggleEquipmentRenderPayload.CODEC,
                        ModNetwork::handleToggleEquipmentRender);
    }

    private static void handleToggleFlag(ToggleFlagPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.applyFlag(payload.flag(), payload.value());
                switch (payload.flag()) {
                    case "alert" -> companion.setAlert(payload.value());
                    case "hunt" -> companion.setHunting(payload.value());
                    case "sprint" -> companion.setSprintEnabled(payload.value());
                    case "patrol" -> {
                        if (payload.value()) companion.resumeMovementOrder();
                        companion.setPatrolPos(companion.blockPosition());
                        companion.setPatrolling(payload.value());
                        if (payload.value()) {
                            companion.setFollowing(false);
                            companion.setGuarding(false);
                        }
                    }
                    case "guard" -> {
                        if (payload.value()) companion.resumeMovementOrder();
                        companion.setGuarding(payload.value());
                        companion.setPatrolPos(companion.blockPosition());
                        if (payload.value()) {
                            companion.setFollowing(false);
                            companion.setPatrolling(false);
                        }
                    }
                    case "work" -> {
                        companion.setWorkEnabled(payload.value());
                        CompanionVoice.play(companion, payload.value() ? ModSounds.Cue.CONFIRMATION : ModSounds.Cue.FAREWELL);
                    }
                    case "follow" -> {
                        if (payload.value()) {
                            companion.resumeMovementOrder();
                            companion.setPatrolling(false);
                            companion.setGuarding(false);
                        }
                        companion.setFollowing(payload.value());
                    }
                    case "pickup" -> companion.setPickupEnabled(payload.value());
                    default -> {}
                }
            }
        });
    }

    private static void handleAction(CompanionActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                switch (payload.action()) {
                    case "cycle_orders" -> companion.cycleOrders();
                    case "clear_target" -> companion.setTarget(null);
                    case "deliver_now" -> companion.requestImmediateDelivery(serverPlayer);
                    case "release" -> {
                        companion.release();
                        serverPlayer.sendSystemMessage(Component.translatable("message.modern_companions.companion_released", companion.getDisplayName()));
                    }
                    default -> {}
                }
            }
        });
    }

    private static void handlePatrolRadius(SetPatrolRadiusPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.setPatrolRadius(payload.radius());
            }
        });
    }

    private static void handleSetJob(SetCompanionJobPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                companion.setJob(CompanionJob.fromId(payload.jobId()));
                companion.onJobChanged();
                CompanionVoice.play(companion, companion.getJob() == CompanionJob.NONE
                        ? ModSounds.Cue.FAREWELL : ModSounds.Cue.CONFIRMATION);
            }
        });
    }

    private static void handleJournalEdit(EditCompanionJournalPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (!(entity instanceof AbstractHumanCompanionEntity companion) || !companion.isOwnedBy(serverPlayer)) return;

            String value = payload.value().trim();
            switch (payload.field()) {
                case "name" -> {
                    if (!value.isEmpty() && value.length() <= 64) companion.setCustomName(Component.literal(value));
                }
                case "age" -> setAge(companion, value);
                case "bio" -> {
                    if (value.length() <= 240) companion.setCustomBio(value);
                }
                case "skin" -> {
                    if (isHttpUrl(value)) companion.setCustomSkinUrl(value);
                }
                case "model" -> {
                    if (value.equalsIgnoreCase("alex")) companion.setUsesAlexModel(true);
                    else if (value.equalsIgnoreCase("steve")) companion.setUsesAlexModel(false);
                }
                default -> { }
            }
        });
    }

    /** Keep editable human companion ages sensible and prevent malformed client payloads. */
    private static void setAge(AbstractHumanCompanionEntity companion, String value) {
        try {
            int age = Integer.parseInt(value);
            if (age >= 1 && age <= 120) companion.setAgeYears(age);
        } catch (NumberFormatException ignored) {
            // Invalid text leaves the existing age unchanged.
        }
    }

    // Matches /companionskin: custom textures are loaded only from explicit web URLs.
    private static boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static void handleOpenInventory(OpenCompanionInventoryPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, player) -> new CompanionMenu(id, inv, companion), companion.getDisplayName()),
                        buf -> buf.writeVarInt(companion.getId()));
            }
        });
    }

    private static void handleToggleEquipmentRender(CompanionToggleEquipmentRenderPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
            Entity entity = serverPlayer.level().getEntity(payload.entityId());
            if (entity instanceof AbstractHumanCompanionEntity companion && companion.isOwnedBy(serverPlayer)) {
                var slot = AbstractHumanCompanionEntity.equipmentSlotFromIndex(payload.slotIndex());
                if (slot != null) companion.toggleEquipmentRender(slot);
            }
        });
    }
}
