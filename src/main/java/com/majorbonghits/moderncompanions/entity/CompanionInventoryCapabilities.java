package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** Makes the existing companion inventory available to integrations that use NeoForge's entity inventory capability. */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CompanionInventoryCapabilities {
    private CompanionInventoryCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        register(event, ModEntityTypes.KNIGHT.get());
        register(event, ModEntityTypes.ARCHER.get());
        register(event, ModEntityTypes.ARBALIST.get());
        register(event, ModEntityTypes.AXEGUARD.get());
        register(event, ModEntityTypes.VANGUARD.get());
        register(event, ModEntityTypes.BERSERKER.get());
        register(event, ModEntityTypes.BEASTMASTER.get());
        register(event, ModEntityTypes.CLERIC.get());
        register(event, ModEntityTypes.ALCHEMIST.get());
        register(event, ModEntityTypes.SCOUT.get());
        register(event, ModEntityTypes.STORMCALLER.get());
        register(event, ModEntityTypes.FIRE_MAGE.get());
        register(event, ModEntityTypes.LIGHTNING_MAGE.get());
        register(event, ModEntityTypes.NECROMANCER.get());
    }

    private static <T extends AbstractHumanCompanionEntity> void register(RegisterCapabilitiesEvent event, EntityType<T> type) {
        // TacZ uses this standard capability for both reload checks and ammo consumption.
        event.registerEntity(Capabilities.ItemHandler.ENTITY, type, (companion, context) -> new InvWrapper(companion.getInventory()));
    }
}
