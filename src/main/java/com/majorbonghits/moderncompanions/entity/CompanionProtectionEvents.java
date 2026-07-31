package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.reflect.Method;

/** Stops companion-caused splash, projectile, fire, and explosion damage that bypasses AI targeting. */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionProtectionEvents {
    private static final int COMBAT_ASSIST_MEMORY_TICKS = 200;

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

    /** Keep upstream summons limited to safe hostile targets with a visible attack path. */
    @SubscribeEvent
    public static void preventProtectedTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) return;

        AbstractHumanCompanionEntity summonOwner = summonOwner(event.getEntity());
        if (summonOwner != null && !canSummonTarget(event.getEntity(), summonOwner, target)) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }

        AbstractHumanCompanionEntity companion = companionAttacker(event.getEntity());
        if (companion != null && !canHarm(companion, target)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    /** Revalidate retained upstream targets before their native AI can keep pathing to stale threats. */
    @SubscribeEvent
    public static void enforceSummonTarget(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob summon) || summon.level().isClientSide()) return;

        LivingEntity target = summon.getTarget();
        // ponytail: poll idle summon ownership every 20 ticks; add event-driven summon tracking only if scale requires it.
        if (target == null && summon.tickCount % 20 != 0) return;
        AbstractHumanCompanionEntity companion = summonOwner(summon);
        if (companion == null) return;

        if (target != null && !canSummonTarget(summon, companion, target)) {
            summon.setTarget(null);
            summon.getNavigation().stop();
            target = null;
        }

        // Native summon AI handles ordinary hostile mobs; this adds the owner's explicit combat assist path.
        if (target == null) {
            LivingEntity assistTarget = combatAssistTarget(companion);
            if (assistTarget != null && canSummonTarget(summon, companion, assistTarget)) {
                summon.setTarget(assistTarget);
            }
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

    private static AbstractHumanCompanionEntity summonOwner(Entity entity) {
        Entity summoner = summonerOf(entity);
        return summoner == null ? null : companionAttacker(summoner);
    }

    private static boolean canSummonTarget(LivingEntity summon, AbstractHumanCompanionEntity companion, LivingEntity target) {
        if (!target.isAlive() || !canHarm(companion, target)) return false;
        if (summon instanceof Mob mob && !mob.getSensing().hasLineOfSight(target)) return false;
        return target.getType().getCategory() == MobCategory.MONSTER || isCombatAssistTarget(companion, target);
    }

    private static boolean isCombatAssistTarget(AbstractHumanCompanionEntity companion, LivingEntity target) {
        if (target == companion.getTarget()) return true;
        if (recentCombatTarget(companion, companion.getLastHurtByMob(), companion.getLastHurtByMobTimestamp()) == target
                || recentCombatTarget(companion, companion.getLastHurtMob(), companion.getLastHurtMobTimestamp()) == target) {
            return true;
        }
        if (target instanceof Mob mob && (mob.getTarget() == companion || mob.getKillCredit() == companion)) return true;
        if (companion.getOwner() instanceof LivingEntity owner) {
            if (recentCombatTarget(owner, owner.getLastHurtByMob(), owner.getLastHurtByMobTimestamp()) == target
                    || recentCombatTarget(owner, owner.getLastHurtMob(), owner.getLastHurtMobTimestamp()) == target) {
                return true;
            }
            if (target instanceof Mob mob) {
                return mob.getTarget() == companion || mob.getKillCredit() == companion
                        || mob.getTarget() == owner || mob.getKillCredit() == owner;
            }
        }
        return false;
    }

    private static LivingEntity combatAssistTarget(AbstractHumanCompanionEntity companion) {
        LivingEntity target = companion.getTarget();
        if (target != null && target.isAlive()) return target;
        target = recentCombatTarget(companion, companion.getLastHurtByMob(), companion.getLastHurtByMobTimestamp());
        if (target != null) return target;
        target = recentCombatTarget(companion, companion.getLastHurtMob(), companion.getLastHurtMobTimestamp());
        if (target != null) return target;
        if (companion.getOwner() instanceof LivingEntity owner) {
            target = recentCombatTarget(owner, owner.getLastHurtByMob(), owner.getLastHurtByMobTimestamp());
            if (target != null) return target;
            return recentCombatTarget(owner, owner.getLastHurtMob(), owner.getLastHurtMobTimestamp());
        }
        return null;
    }

    private static LivingEntity recentCombatTarget(LivingEntity source, LivingEntity target, int timestamp) {
        int age = source.tickCount - timestamp;
        return target != null && target.isAlive() && age >= 0 && age <= COMBAT_ASSIST_MEMORY_TICKS ? target : null;
    }

    private static Entity summonerOf(Entity entity) {
        try {
            Method getSummoner = entity.getClass().getMethod("getSummoner");
            Object owner = getSummoner.invoke(entity);
            return owner instanceof Entity result ? result : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Entity ownerOf(Entity entity) {
        if (entity instanceof Projectile projectile) return projectile.getOwner();
        if (entity instanceof OwnableEntity ownable) return ownable.getOwner();
        return summonerOf(entity);
    }
}
