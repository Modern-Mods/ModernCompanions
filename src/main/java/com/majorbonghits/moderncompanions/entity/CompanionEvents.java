package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionEvents {
    private CompanionEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            CompanionData.updateResourceProgress(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void giveExperience(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion && event.getEntity().level() instanceof ServerLevel serverLevel) {
            companion.recordKill(event.getEntity());
            companion.giveExperiencePoints(event.getEntity().getExperienceReward(serverLevel, companion));
        }
    }

    @SubscribeEvent
    public static void friendlyFire(LivingIncomingDamageEvent event) {
        var source = event.getSource();
        AbstractHumanCompanionEntity companion = CompanionProtectionEvents.companionAttacker(source.getDirectEntity());
        if (companion == null) companion = CompanionProtectionEvents.companionAttacker(source.getEntity());
        if (companion != null && !CompanionProtectionEvents.canHarm(companion, event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof AbstractHumanCompanionEntity companion)) return;
        if (!companion.isTame()) return;
        if (!companion.hasTrait("trait_lucky")) return;
        double chance = ModConfig.safeGet(ModConfig.LUCKY_EXTRA_DROP_CHANCE);
        if (companion.getRandom().nextDouble() >= chance) return;
        var drops = event.getDrops();
        if (drops.isEmpty()) return;
        var list = drops.stream().toList();
        var pick = list.get(companion.getRandom().nextInt(list.size()));
        if (pick.getItem().isEmpty()) return;
        var copy = pick.getItem().copy();
        copy.setCount(Math.max(1, copy.getCount()));
        var extra = new net.minecraft.world.entity.item.ItemEntity(event.getEntity().level(), pick.getX(), pick.getY(), pick.getZ(), copy);
        drops.add(extra);
    }
}
