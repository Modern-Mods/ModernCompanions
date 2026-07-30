package com.majorbonghits.moderncompanions.client;

import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.client.renderer.CompanionFishingHookRenderer;
import com.majorbonghits.moderncompanions.client.screen.CompanionScreen;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void onRegisterMenus(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPANION_MENU.get(), CompanionScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.KNIGHT.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARCHER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARBALIST.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AXEGUARD.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VANGUARD.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BERSERKER.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BEASTMASTER.get(), CompanionRenderer::new);
        magic(event, ModEntityTypes.CLERIC);
        event.registerEntityRenderer(ModEntityTypes.ALCHEMIST.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SCOUT.get(), CompanionRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.STORMCALLER.get(), CompanionRenderer::new);
        magic(event, ModEntityTypes.FIRE_MAGE);
        magic(event, ModEntityTypes.LIGHTNING_MAGE);
        magic(event, ModEntityTypes.NECROMANCER);
        magic(event, ModEntityTypes.WIZARD);
        magic(event, ModEntityTypes.SORCERER);
        magic(event, ModEntityTypes.WARLOCK);
        magic(event, ModEntityTypes.WITCH);
        magic(event, ModEntityTypes.HAG);
        magic(event, ModEntityTypes.CRYOMANCER);
        magic(event, ModEntityTypes.DRUID);
        magic(event, ModEntityTypes.ILLUSIONIST);
        magic(event, ModEntityTypes.BATTLEMAGE);
        event.registerEntityRenderer(ModEntityTypes.COMPANION_FISHING_HOOK.get(), CompanionFishingHookRenderer::new);
    }

    private static <T extends com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity> void magic(EntityRenderersEvent.RegisterRenderers event, net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<T>> type) {
        if (type != null) event.registerEntityRenderer(type.get(), CompanionRenderer::new);
    }
}
