package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ModernCompanions.MOD_ID)
public final class CompanionEvents {
    private static final double DIMENSION_FOLLOW_RADIUS = 35.0D;
    private static final Map<UUID, PendingDimensionFollow> pendingDimensionFollows = new HashMap<>();

    private CompanionEvents() {}

    /** Capture eligible companions before the player leaves the source level. */
    @SubscribeEvent
    public static void captureDimensionFollowers(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel source)
                || source.dimension().equals(event.getDimension())) {
            return;
        }

        List<UUID> companions = source.getEntitiesOfClass(AbstractHumanCompanionEntity.class,
                        player.getBoundingBox().inflate(DIMENSION_FOLLOW_RADIUS),
                        companion -> companion.isAlive()
                                && companion.isTame()
                                && player.getUUID().equals(companion.getOwnerUUID())
                                && companion.isFollowing()
                                && !companion.isPatrolling()
                                && !companion.isGuarding()
                                && !companion.isWorkEnabled()
                                && !companion.isOrderedToSit()
                                && companion.distanceToSqr(player) <= DIMENSION_FOLLOW_RADIUS * DIMENSION_FOLLOW_RADIUS)
                .stream()
                .map(Entity::getUUID)
                .toList();

        if (companions.isEmpty()) {
            pendingDimensionFollows.remove(player.getUUID());
        } else {
            pendingDimensionFollows.put(player.getUUID(),
                    new PendingDimensionFollow(source.dimension(), event.getDimension(), companions));
        }
    }

    /** Transfer only the companions captured immediately before the player arrived. */
    @SubscribeEvent
    public static void moveDimensionFollowers(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PendingDimensionFollow pending = pendingDimensionFollows.remove(player.getUUID());
        if (pending == null || !pending.from().equals(event.getFrom()) || !pending.to().equals(event.getTo())) return;

        if (!(player.level() instanceof ServerLevel destination) || player.getServer() == null) return;
        ServerLevel source = player.getServer().getLevel(pending.from());
        if (source == null) return;

        for (UUID companionId : pending.companions()) {
            Entity entity = source.getEntity(companionId);
            if (!(entity instanceof AbstractHumanCompanionEntity companion)
                    || !companion.isAlive()
                    || !companion.isTame()
                    || !player.getUUID().equals(companion.getOwnerUUID())
                    || !companion.isFollowing()
                    || companion.isPatrolling()
                    || companion.isGuarding()
                    || companion.isWorkEnabled()
                    || companion.isOrderedToSit()
                    || !companion.canChangeDimensions(source, destination)) {
                continue;
            }

            companion.getNavigation().stop();
            Vec3 target = findSafeSpot(destination, player.position(), companion).orElse(player.position());
            companion.teleportTo(destination, target.x(), target.y(), target.z(),
                    java.util.Set.of(), companion.getYRot(), companion.getXRot());
        }
    }

    private static java.util.Optional<Vec3> findSafeSpot(ServerLevel level, Vec3 center, Entity entity) {
        BlockPos base = BlockPos.containing(center);
        for (int attempt = 0; attempt < 12; attempt++) {
            int dx = level.random.nextInt(5) - 2;
            int dz = level.random.nextInt(5) - 2;
            BlockPos candidate = base.offset(dx, 0, dz);
            if (level.isEmptyBlock(candidate)
                    && level.isEmptyBlock(candidate.above())
                    && level.noCollision(entity, entity.getBoundingBox().move(
                    candidate.getX() + 0.5D - entity.getX(),
                    candidate.getY() - entity.getY(),
                    candidate.getZ() + 0.5D - entity.getZ()))) {
                return java.util.Optional.of(new Vec3(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D));
            }
        }
        return java.util.Optional.empty();
    }

    private record PendingDimensionFollow(ResourceKey<Level> from, ResourceKey<Level> to, List<UUID> companions) {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            CompanionData.updateResourceProgress(event.getEntity());
        }
    }

    /** Preserve restored companion passengers while a mounted player's vehicle link is rebuilt. */
    @SubscribeEvent
    public static void reconcileMountsAfterLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.isPassenger()
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        for (AbstractHumanCompanionEntity companion : level.getEntitiesOfClass(
                AbstractHumanCompanionEntity.class, player.getBoundingBox().inflate(64.0D),
                candidate -> candidate.isAlive()
                        && candidate.isTame()
                        && player.getUUID().equals(candidate.getOwnerUUID()))) {
            companion.scheduleMountReconciliation();
        }
    }

    @SubscribeEvent
    public static void giveExperience(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        AbstractHumanCompanionEntity companion = CompanionProtectionEvents.companionAttacker(event.getSource().getDirectEntity());
        if (companion == null) companion = CompanionProtectionEvents.companionAttacker(event.getSource().getEntity());
        if (companion == null) return;
        companion.recordKill(event.getEntity());
        companion.giveExperiencePoints(event.getEntity().getExperienceReward(serverLevel, companion));
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

    /** Forget a temporary fence before a player break/place can turn it into player-owned terrain. */
    @SubscribeEvent
    public static void invalidateTemporaryMountFenceOnBreak(BlockEvent.BreakEvent event) {
        invalidateTemporaryMountFence(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void invalidateTemporaryMountFenceOnPlace(BlockEvent.EntityPlaceEvent event) {
        invalidateTemporaryMountFence(event.getLevel(), event.getPos());
    }

    private static void invalidateTemporaryMountFence(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (AbstractHumanCompanionEntity companion : serverLevel.getEntitiesOfClass(
                AbstractHumanCompanionEntity.class, new AABB(pos).inflate(16.0D))) {
            companion.invalidateTemporaryMountFence(pos);
        }
    }
}
