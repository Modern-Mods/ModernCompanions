package com.majorbonghits.moderncompanions.client;

import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.client.renderer.CompanionFishingHookRenderer;
import com.majorbonghits.moderncompanions.client.renderer.HolySparkProjectileRenderer;
import com.majorbonghits.moderncompanions.client.screen.CompanionScreen;
import com.majorbonghits.moderncompanions.client.screen.CompanionTableScreen;
import com.majorbonghits.moderncompanions.client.screen.TraitReforgingScreen;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.core.ModEffects;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import static com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {}

    @SubscribeEvent
    public static void onRegisterMenus(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.COMPANION_MENU.get(), CompanionScreen::new);
        event.register(ModMenuTypes.TRAIT_REFORGING_MENU.get(), TraitReforgingScreen::new);
        event.register(ModMenuTypes.COMPANION_TABLE_MENU.get(), CompanionTableScreen::new);
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
        magic(event, ModEntityTypes.FIREARM_SPECIALIST);
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
        event.registerEntityRenderer(ModEntityTypes.HOLY_SPARK.get(), HolySparkProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterEffectIcons(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(new EffectIcon("regeneration"), ModEffects.COMPANION_REGENERATION.get());
        event.registerMobEffect(new EffectIcon("stamina"), ModEffects.COMPANION_STAMINA.get());
        event.registerMobEffect(new EffectIcon("mana"), ModEffects.COMPANION_MANA.get());
        event.registerMobEffect(new EffectIcon("rejuvination"), ModEffects.COMPANION_REJUVENATION.get());
        event.registerMobEffect(new EffectIcon("shield"), ModEffects.COMPANION_SHIELD.get());
        event.registerMobEffect(HIDDEN_EFFECT, MobEffects.REGENERATION.value(), MobEffects.MOVEMENT_SPEED.value(), MobEffects.DAMAGE_BOOST.value());
    }

    private static final IClientMobEffectExtensions HIDDEN_EFFECT = new IClientMobEffectExtensions() {
        @Override
        public boolean isVisibleInInventory(MobEffectInstance instance) {
            return instance.showIcon();
        }
    };

    private record EffectIcon(ResourceLocation inventoryTexture, ResourceLocation hudTexture) implements IClientMobEffectExtensions {
        private EffectIcon(String name) {
            this(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/" + name + "32.png"),
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/" + name + "18.png"));
        }

        @Override
        public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, net.minecraft.client.gui.GuiGraphics graphics, int x, int y, int offset) {
            graphics.blit(inventoryTexture, x - 7, y, 0, 0, 32, 32, 32, 32);
            return true;
        }

        @Override
        public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, net.minecraft.client.gui.GuiGraphics graphics, int x, int y, float z, float alpha) {
            graphics.blit(hudTexture, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
            return true;
        }
    }

    private static <T extends com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity> void magic(EntityRenderersEvent.RegisterRenderers event, net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<T>> type) {
        if (type != null) event.registerEntityRenderer(type.get(), CompanionRenderer::new);
    }

}
