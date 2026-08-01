package com.majorbonghits.moderncompanions.compat.epicfight.client;

import com.majorbonghits.moderncompanions.client.renderer.EpicFightCompanionRenderer;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Registers the companion-aware Epic Fight renderer only on clients with Epic Fight installed. */
public final class EpicFightClientCompat {
    private EpicFightClientCompat() {}

    public static void register() {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(EpicFightClientCompat::registerRenderers);
        if (ModList.get().isLoaded("epicfight_curios_compat")) {
            EpicFightClientEventHooks.Registry.MODIFY_PATCHED_ENTITY.registerEvent(EpicFightClientCompat::registerCuriosLayers);
        }
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

    /** Epic Fight x Curios Compat only patches the player; attach its layer to our patched bodies too. */
    private static void registerCuriosLayers(RegisterPatchedRenderersEvent.ModifyEntity event) {
        try {
            Class<?> curiosLayer = Class.forName("top.theillusivec4.curios.client.render.CuriosLayer");
            Class<?> patchedCuriosLayer = Class.forName(
                    "com.oneworldstudio.epicfightcurioscompat.ClientCuriosCompat$PatchedCuriosLayerRenderer");
            Constructor<?> constructor = patchedCuriosLayer.getConstructor();
            for (EntityType<?> type : companionTypes()) {
                Object renderer = event.get(type);
                if (renderer == null) continue;
                for (Method method : renderer.getClass().getMethods()) {
                    if (method.getName().equals("addPatchedLayerAlways") && method.getParameterCount() == 2) {
                        method.invoke(renderer, curiosLayer, constructor.newInstance());
                        break;
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // The optional compatibility mod can change its private renderer class without breaking base Epic Fight support.
        }
    }

    private static List<EntityType<?>> companionTypes() {
        List<EntityType<?>> types = new ArrayList<>();
        addType(types, ModEntityTypes.KNIGHT); addType(types, ModEntityTypes.ARCHER); addType(types, ModEntityTypes.ARBALIST);
        addType(types, ModEntityTypes.AXEGUARD); addType(types, ModEntityTypes.VANGUARD); addType(types, ModEntityTypes.BERSERKER);
        addType(types, ModEntityTypes.BEASTMASTER); addType(types, ModEntityTypes.CLERIC); addType(types, ModEntityTypes.ALCHEMIST);
        addType(types, ModEntityTypes.SCOUT); addType(types, ModEntityTypes.STORMCALLER); addType(types, ModEntityTypes.FIREARM_SPECIALIST);
        addType(types, ModEntityTypes.FIRE_MAGE); addType(types, ModEntityTypes.LIGHTNING_MAGE); addType(types, ModEntityTypes.NECROMANCER);
        addType(types, ModEntityTypes.WIZARD); addType(types, ModEntityTypes.SORCERER); addType(types, ModEntityTypes.WARLOCK);
        addType(types, ModEntityTypes.WITCH); addType(types, ModEntityTypes.HAG); addType(types, ModEntityTypes.CRYOMANCER);
        addType(types, ModEntityTypes.DRUID); addType(types, ModEntityTypes.ILLUSIONIST); addType(types, ModEntityTypes.BATTLEMAGE);
        return types;
    }

    private static <T extends AbstractHumanCompanionEntity> void addType(List<EntityType<?>> types,
                                                                          DeferredHolder<EntityType<?>, EntityType<T>> type) {
        if (type != null) types.add(type.get());
    }
}
