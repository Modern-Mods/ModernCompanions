package com.majorbonghits.moderncompanions.client;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Exposes the registered ModConfigSpec values through NeoForge's Mod List Config button. */
public final class ModConfigScreens {
    private ModConfigScreens() {}

    public static void register() {
        // Jobs remain experimental; keep their developer settings out of the player-facing screen.
        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ConfigurationScreen(container, parent,
                        (screen, key, element) -> "jobs".equals(key) || "creeperDefaultMigrated".equals(key) ? null : element));
    }
}
