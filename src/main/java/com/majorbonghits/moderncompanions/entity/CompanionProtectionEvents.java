package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.lang.reflect.Method;

/** Stops companion-caused splash, projectile, fire, and explosion damage that bypasses AI targeting. */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionProtectionEvents {
    private CompanionProtectionEvents() {}

    @SubscribeEvent
    public static void preventProtectedDamage(LivingDamageEvent.Pre event) {
        AbstractHumanCompanionEntity companion = companionAttacker(event.getSource().getDirectEntity());
        if (companion == null) companion = companionAttacker(event.getSource().getEntity());
        LivingEntity victim = event.getEntity();
        if (companion != null && !canHarm(companion, victim)) {
            event.setNewDamage(0.0F);
        } else if (companion instanceof AbstractMageCompanion mage) {
            event.setNewDamage(mage.magicDamage(event.getNewDamage()));
        }
    }

    /** Keep upstream summons from acquiring targets their caster is forbidden to harm. */
    @SubscribeEvent
    public static void preventProtectedTarget(LivingChangeTargetEvent event) {
        AbstractHumanCompanionEntity companion = companionAttacker(event.getEntity());
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (companion != null && target != null && !canHarm(companion, target)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    static boolean canHarm(AbstractHumanCompanionEntity companion, Entity victim) {
        if (!companion.canHarm(victim)) return false;
        Entity victimOwner = ownerOf(victim);
        if (victimOwner == companion || victimOwner == companion.getOwner()) return false;
        if (victimOwner instanceof AbstractHumanCompanionEntity other) {
            return other.getOwnerUUID() == null || !other.getOwnerUUID().equals(companion.getOwnerUUID())
                    ? companion.canHarmPlayers() : false;
        }
        return !(victimOwner instanceof Player) || companion.canHarmPlayers();
    }

    static AbstractHumanCompanionEntity companionAttacker(Entity entity) {
        for (int depth = 0; entity != null && depth < 4; depth++) {
            if (entity instanceof AbstractHumanCompanionEntity companion) return companion;
            Entity owner = ownerOf(entity);
            if (owner == entity) break;
            entity = owner;
        }
        return null;
    }

    private static Entity ownerOf(Entity entity) {
        if (entity instanceof Projectile projectile) return projectile.getOwner();
        if (entity instanceof OwnableEntity ownable) return ownable.getOwner();
        try {
            Method getSummoner = entity.getClass().getMethod("getSummoner");
            Object owner = getSummoner.invoke(entity);
            return owner instanceof Entity result ? result : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
