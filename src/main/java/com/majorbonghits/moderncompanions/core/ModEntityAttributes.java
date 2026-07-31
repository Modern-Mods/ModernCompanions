package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * Registers entity attribute sets.
 */
public final class ModEntityAttributes {
    private ModEntityAttributes() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        var attrs = AbstractHumanCompanionEntity.createAttributes().build();
        event.put(ModEntityTypes.KNIGHT.get(), attrs);
        event.put(ModEntityTypes.ARCHER.get(), attrs);
        event.put(ModEntityTypes.ARBALIST.get(), attrs);
        event.put(ModEntityTypes.AXEGUARD.get(), attrs);
        event.put(ModEntityTypes.VANGUARD.get(), attrs);
        event.put(ModEntityTypes.BERSERKER.get(), attrs);
        event.put(ModEntityTypes.BEASTMASTER.get(), attrs);
        magic(event, ModEntityTypes.CLERIC, attrs);
        event.put(ModEntityTypes.ALCHEMIST.get(), attrs);
        event.put(ModEntityTypes.SCOUT.get(), attrs);
        event.put(ModEntityTypes.STORMCALLER.get(), attrs);
        magic(event, ModEntityTypes.FIREARM_SPECIALIST, attrs);
        magic(event, ModEntityTypes.FIRE_MAGE, attrs);
        magic(event, ModEntityTypes.LIGHTNING_MAGE, attrs);
        magic(event, ModEntityTypes.NECROMANCER, attrs);
        magic(event, ModEntityTypes.WIZARD, attrs);
        magic(event, ModEntityTypes.SORCERER, attrs);
        magic(event, ModEntityTypes.WARLOCK, attrs);
        magic(event, ModEntityTypes.WITCH, attrs);
        magic(event, ModEntityTypes.HAG, attrs);
        magic(event, ModEntityTypes.CRYOMANCER, attrs);
        magic(event, ModEntityTypes.DRUID, attrs);
        magic(event, ModEntityTypes.ILLUSIONIST, attrs);
        magic(event, ModEntityTypes.BATTLEMAGE, attrs);
        // Projectile entities have no attributes
    }

    private static <T extends AbstractHumanCompanionEntity> void magic(EntityAttributeCreationEvent event, net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<T>> type, net.minecraft.world.entity.ai.attributes.AttributeSupplier attrs) {
        if (type != null) event.put(type.get(), attrs);
    }

}
