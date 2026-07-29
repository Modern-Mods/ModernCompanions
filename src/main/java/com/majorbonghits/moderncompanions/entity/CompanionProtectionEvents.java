package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Stops companion-caused splash, projectile, fire, and explosion damage that bypasses AI targeting. */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionProtectionEvents {
    private CompanionProtectionEvents() {}

    @SubscribeEvent
    public static void preventProtectedDamage(LivingDamageEvent.Pre event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity victim = event.getEntity();
        AbstractHumanCompanionEntity companion = attacker instanceof AbstractHumanCompanionEntity human ? human : null;
        if (companion != null && !companion.canHarm(victim)) {
            event.setNewDamage(0.0F);
        }
    }
}
