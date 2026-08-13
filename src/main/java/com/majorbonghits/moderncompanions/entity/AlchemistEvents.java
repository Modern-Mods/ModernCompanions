package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Keeps Alchemist splash effects and the minions from Oozing/Infested on the owner's side. */
@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class AlchemistEvents {
    private static final String OOZING_SOURCE_TAG = "ModernCompanionsAlchemistOozing";
    private static final String INFESTED_SOURCE_TAG = "ModernCompanionsAlchemistInfested";
    private static final String MINION_OWNER_TAG = "ModernCompanionsAlchemistMinion";
    private static final String MINION_PLAYER_OWNER_TAG = "ModernCompanionsAlchemistPlayerOwner";

    private AlchemistEvents() {
    }

    /** Reject harmful effects before vanilla applies them to the thrower or its protected party. */
    @SubscribeEvent
    public static void protectNegativeEffects(MobEffectEvent.Applicable event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (effect == null || effect.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) return;
        if (isProtectedFrom(event.getEntity(), event.getEffectSource())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    /** Remember which Alchemist created the two effects that spawn independent hostile mobs. */
    @SubscribeEvent
    public static void rememberCreatedMinions(MobEffectEvent.Added event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (!(event.getEffectSource() instanceof Alchemist alchemist) || effect == null) return;

        if (effect.getEffect().is(MobEffects.OOZING)) {
            event.getEntity().getPersistentData().putUUID(OOZING_SOURCE_TAG, alchemist.getUUID());
        } else if (effect.getEffect().is(MobEffects.INFESTED)) {
            event.getEntity().getPersistentData().putUUID(INFESTED_SOURCE_TAG, alchemist.getUUID());
        }
    }

    @SubscribeEvent
    public static void clearCreatedMinionMarker(MobEffectEvent.Remove event) {
        if (event.getEffect().is(MobEffects.OOZING)) {
            event.getEntity().getPersistentData().remove(OOZING_SOURCE_TAG);
        } else if (event.getEffect().is(MobEffects.INFESTED)) {
            event.getEntity().getPersistentData().remove(INFESTED_SOURCE_TAG);
        }
    }

    /** Harming is instantaneous, so it bypasses Applicable and is stopped at its damage source instead. */
    @SubscribeEvent
    public static void protectInstantPotionDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getDirectEntity() instanceof ThrownPotion potion)) return;
        if (isProtectedFrom(event.getEntity(), potion.getOwner())) {
            event.setNewDamage(0.0F);
        }
    }

    /** Attribute newly spawned Oozing slimes and Infested silverfish to the effect's Alchemist. */
    @SubscribeEvent
    public static void markCreatedMinion(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Slime) && !(entity instanceof Silverfish)) return;
        if (!(event.getLevel() instanceof ServerLevel server)) return;

        String sourceTag = entity instanceof Slime ? OOZING_SOURCE_TAG : INFESTED_SOURCE_TAG;
        for (LivingEntity host : server.getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(3.0D), candidate -> candidate.getPersistentData().hasUUID(sourceTag))) {
            UUID alchemistId = host.getPersistentData().getUUID(sourceTag);
            Entity source = server.getEntity(alchemistId);
            if (source instanceof Alchemist alchemist && alchemist.isAlive()) {
                entity.getPersistentData().putUUID(MINION_OWNER_TAG, alchemist.getUUID());
                if (alchemist.getOwnerUUID() != null) {
                    entity.getPersistentData().putUUID(MINION_PLAYER_OWNER_TAG, alchemist.getOwnerUUID());
                }
                return;
            }
        }
    }

    /** Stop a tagged minion from retaining a stale target after its owner or party moves nearby. */
    @SubscribeEvent
    public static void protectMinionTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null || !(event.getEntity() instanceof Mob minion)) return;
        Alchemist alchemist = ownerOf(minion);
        if (isProtectedMinionTarget(minion, alchemist, target)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void clearMinionTarget(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob minion) || minion.level().isClientSide()) return;
        Alchemist alchemist = ownerOf(minion);
        LivingEntity target = minion.getTarget();
        if (target != null && isProtectedMinionTarget(minion, alchemist, target)) {
            minion.setTarget(null);
            minion.getNavigation().stop();
        }
    }

    /** Prevent the generated silverfish/slimes from damaging the Alchemist's party. */
    @SubscribeEvent
    public static void protectMinionDamage(LivingIncomingDamageEvent event) {
        Alchemist alchemist = ownerOf(event.getSource().getDirectEntity());
        if (alchemist == null) alchemist = ownerOf(event.getSource().getEntity());
        Entity source = event.getSource().getDirectEntity() != null
                ? event.getSource().getDirectEntity() : event.getSource().getEntity();
        if (source != null && isProtectedMinionTarget(source, alchemist, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean isProtectedFrom(LivingEntity target, Entity source) {
        if (source instanceof Alchemist alchemist) {
            return isProtectedTarget(alchemist, target);
        }
        if (!(target instanceof AbstractHumanCompanionEntity companion)) return false;
        if (source == companion.getOwner()) return true;
        if (source instanceof Player player && player.getUUID().equals(companion.getOwnerUUID())) return true;
        if (source instanceof AbstractHumanCompanionEntity other
                && other.getOwnerUUID() != null
                && other.getOwnerUUID().equals(companion.getOwnerUUID())) return true;
        return false;
    }

    private static boolean isProtectedTarget(Alchemist alchemist, Entity target) {
        if (target == alchemist || target == alchemist.getOwner()) return true;
        return isProtectedByOwner(alchemist.getOwnerUUID(), target);
    }

    private static boolean isProtectedByOwner(@Nullable UUID ownerId, Entity target) {
        if (ownerId == null) return false;

        if (target instanceof Player player) {
            return ownerId.equals(player.getUUID());
        }
        if (target instanceof AbstractHumanCompanionEntity companion) {
            return ownerId.equals(companion.getOwnerUUID());
        }
        if (target instanceof TamableAnimal tame && tame.isTame()) {
            return ownerId.equals(tame.getOwnerUUID());
        }
        if (Beastmaster.isBeastmasterPet(target) && target.level() instanceof ServerLevel server) {
            UUID beastmasterId = target.getPersistentData().getUUID(Beastmaster.BEASTMASTER_OWNER_TAG);
            Entity beastmaster = server.getEntity(beastmasterId);
            return beastmaster instanceof Beastmaster owner && ownerId.equals(owner.getOwnerUUID());
        }
        return false;
    }

    private static boolean isProtectedMinionTarget(Entity minion, @Nullable Alchemist alchemist, Entity target) {
        if (alchemist != null && isProtectedTarget(alchemist, target)) return true;
        if (!minion.getPersistentData().hasUUID(MINION_PLAYER_OWNER_TAG)) return false;
        return isProtectedByOwner(minion.getPersistentData().getUUID(MINION_PLAYER_OWNER_TAG), target);
    }

    private static Alchemist ownerOf(Entity entity) {
        if (entity == null || !entity.getPersistentData().hasUUID(MINION_OWNER_TAG)) return null;
        if (!(entity.level() instanceof ServerLevel server)) return null;
        Entity owner = server.getEntity(entity.getPersistentData().getUUID(MINION_OWNER_TAG));
        return owner instanceof Alchemist alchemist && alchemist.isAlive() ? alchemist : null;
    }
}
