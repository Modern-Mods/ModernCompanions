package com.majorbonghits.moderncompanions.compat.epicfight.client;

import com.majorbonghits.moderncompanions.client.renderer.EpicFightCompanionRenderer;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;

/** Registers the companion-aware Epic Fight renderer only on clients with Epic Fight installed. */
public final class EpicFightClientCompat {
    private EpicFightClientCompat() {}

    public static void register() {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(EpicFightClientCompat::registerRenderers);
    }

    private static void registerRenderers(RegisterPatchedRenderersEvent.AddEntity event) {
        register(event, ModEntityTypes.KNIGHT); register(event, ModEntityTypes.ARCHER); register(event, ModEntityTypes.ARBALIST);
        register(event, ModEntityTypes.AXEGUARD); register(event, ModEntityTypes.VANGUARD); register(event, ModEntityTypes.BERSERKER);
        register(event, ModEntityTypes.BEASTMASTER); register(event, ModEntityTypes.CLERIC); register(event, ModEntityTypes.ALCHEMIST);
        register(event, ModEntityTypes.SCOUT); register(event, ModEntityTypes.STORMCALLER); register(event, ModEntityTypes.FIREARM_SPECIALIST);
        register(event, ModEntityTypes.FIRE_MAGE); register(event, ModEntityTypes.LIGHTNING_MAGE); register(event, ModEntityTypes.NECROMANCER);
        register(event, ModEntityTypes.WIZARD); register(event, ModEntityTypes.SORCERER); register(event, ModEntityTypes.WARLOCK);
        register(event, ModEntityTypes.WITCH); register(event, ModEntityTypes.HAG); register(event, ModEntityTypes.CRYOMANCER);
        register(event, ModEntityTypes.DRUID); register(event, ModEntityTypes.ILLUSIONIST); register(event, ModEntityTypes.BATTLEMAGE);
    }

    private static <T extends AbstractHumanCompanionEntity> void register(RegisterPatchedRenderersEvent.AddEntity event,
                                                                            DeferredHolder<EntityType<?>, EntityType<T>> type) {
        if (type != null) event.addPatchedEntityRenderer(type.get(), entityType -> new EpicFightCompanionRenderer(event.getContext(), entityType));
    }
}
