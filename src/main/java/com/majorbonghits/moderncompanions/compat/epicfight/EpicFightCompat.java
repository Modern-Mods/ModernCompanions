package com.majorbonghits.moderncompanions.compat.epicfight;

import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.EntityPatchRegistryEvent;
import yesman.epicfight.gameasset.Armatures;

/** Registers the native Epic Fight patch only when Epic Fight itself is present. */
public final class EpicFightCompat {
    private EpicFightCompat() {}

    public static void register() {
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(EpicFightCompat::registerPatches);
    }

    private static void registerPatches(EntityPatchRegistryEvent event) {
        register(event, ModEntityTypes.KNIGHT);
        register(event, ModEntityTypes.ARCHER);
        register(event, ModEntityTypes.ARBALIST);
        register(event, ModEntityTypes.AXEGUARD);
        register(event, ModEntityTypes.VANGUARD);
        register(event, ModEntityTypes.BERSERKER);
        register(event, ModEntityTypes.BEASTMASTER);
        register(event, ModEntityTypes.CLERIC);
        register(event, ModEntityTypes.ALCHEMIST);
        register(event, ModEntityTypes.SCOUT);
        register(event, ModEntityTypes.STORMCALLER);
        register(event, ModEntityTypes.FIREARM_SPECIALIST);
        register(event, ModEntityTypes.FIRE_MAGE);
        register(event, ModEntityTypes.LIGHTNING_MAGE);
        register(event, ModEntityTypes.NECROMANCER);
        register(event, ModEntityTypes.WIZARD);
        register(event, ModEntityTypes.SORCERER);
        register(event, ModEntityTypes.WARLOCK);
        register(event, ModEntityTypes.WITCH);
        register(event, ModEntityTypes.HAG);
        register(event, ModEntityTypes.CRYOMANCER);
        register(event, ModEntityTypes.DRUID);
        register(event, ModEntityTypes.ILLUSIONIST);
        register(event, ModEntityTypes.BATTLEMAGE);
    }

    private static <T extends AbstractHumanCompanionEntity> void register(EntityPatchRegistryEvent event,
                                                                            DeferredHolder<EntityType<?>, EntityType<T>> type) {
        if (type == null) return;
        event.registerEntityPatch(type.get(), companion -> new CompanionEpicFightPatch<>(companion));
        // Companions use the same wide-arm biped skeleton as Epic Fight's humanoid animations.
        Armatures.registerEntityTypeArmature(type.get(), Armatures.BIPED);
    }
}
