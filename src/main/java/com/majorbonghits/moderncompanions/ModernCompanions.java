package com.majorbonghits.moderncompanions;

import com.majorbonghits.moderncompanions.registry.ModCreativeTabHandler;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.core.ModEntityAttributes;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModEffects;
import com.majorbonghits.moderncompanions.item.CompanionBrewing;
import com.majorbonghits.moderncompanions.registry.ModCreativeTabs;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public final class ModernCompanions {
    /** Kept for legacy references across the codebase. */
    public static final String MOD_ID = Constants.MOD_ID;

    public ModernCompanions(IEventBus modBus) {
        // Ensure config values are registered before any entities/spawn eggs are constructed.
        ModConfig.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // Register NeoForge's native editor so common settings, including Alert exclusions, are player-visible.
            com.majorbonghits.moderncompanions.client.ModConfigScreens.register();
        }

        com.majorbonghits.moderncompanions.registry.ModItems.register(modBus); // weapons
        com.majorbonghits.moderncompanions.core.ModItems.ITEMS.register(modBus); // spawn eggs
        ModEffects.EFFECTS.register(modBus);
        com.majorbonghits.moderncompanions.core.ModRecipeSerializers.SERIALIZERS.register(modBus);
        ModCreativeTabs.register(modBus); // dedicated creative tab
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        modBus.addListener(ModConfig::migrateAlertExclusions);
        modBus.addListener(ModEntityAttributes::registerAttributes);
        modBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(CompanionBrewing::register);
        ModCreativeTabHandler.register(modBus);

        // Only load Curios hooks when the mod is present to avoid classloading crashes.
        if (ModList.get().isLoaded("curios")) {
            com.majorbonghits.moderncompanions.compat.curios.CuriosCompat.register(modBus, FMLEnvironment.dist == Dist.CLIENT);
        }
        if (ModList.get().isLoaded("epicfight")) {
            // Keep Epic Fight combat classes out of the base-mod load path.
            com.majorbonghits.moderncompanions.compat.epicfight.EpicFightCompat.register();
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.majorbonghits.moderncompanions.compat.epicfight.client.EpicFightClientCompat.register();
            }
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // Currently nothing to execute; kept for symmetry with upstream weapon setup hooks.
    }
}
