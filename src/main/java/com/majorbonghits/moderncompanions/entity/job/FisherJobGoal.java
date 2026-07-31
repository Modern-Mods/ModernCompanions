package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Fisher job: stand near a water block and periodically generate simple fishing
 * loot (cod/salmon) with a short delay. Keeps the cadence low to avoid item
 * spam.
 */
public class FisherJobGoal extends ResumableJobGoal {
    private static final int SEARCH_COOLDOWN = 10; // quicker reacquire when nearby water exists
    private static final int RESCAN_STUCK_TICKS = 80;
    private static final int MAX_RINGS_PER_SCAN = 8;
    private static final int MIN_WATER_ADJACENT = 2;
    private static final int RECAST_DELAY = 20;
    private static final int CAST_ATTEMPTS = 12;
    private static final int CAST_MIN_DIST = 5;
    private static final int CAST_MAX_DIST = 7;
    private static final int CAST_SIDE_SPREAD = 2;

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Random random = new Random();
    private BlockPos waterSpot;
    private BlockPos standPos;
    private int searchCooldown;
    private int idleNavTicks;
    private BlockPos scanOrigin;
    private int scanRing;
    private int recastCooldown;
    private CompanionFishingHook activeHook;
    private final Map<BlockPos, Integer> rejectedWater = new HashMap<>();

    public FisherJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        super(companion, CompanionJob.FISHER);
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (waterSpot != null && standPos != null && isFishableWater(waterSpot) && isStandValid(standPos)
                && !isRejected(waterSpot)) {
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling_to_shore", waterSpot);
            return reserve("shore:" + waterSpot.asLong());
        }
        if (searchCooldown-- > 0) return false;
        boolean found = findWaterAndStand();
        searchCooldown = SEARCH_COOLDOWN;
        if (found && !reserve("shore:" + waterSpot.asLong())) {
            waiting("job_status.modern_companions.shore_reserved");
            return false;
        }
        if (found) {
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling_to_shore", waterSpot);
            moveToStand();
        }
        return found;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && waterSpot != null && standPos != null && isFishableWater(waterSpot) && isStandValid(standPos)
                && !isRejected(waterSpot);
    }

    @Override
    public void start() {
        moveToStand();
    }

    @Override
    public void stop() {
        // The shore is a resumable checkpoint; only the transient hook is discarded.
        clearLine();
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (waterSpot == null || standPos == null) return;
        if (!isFishableWater(waterSpot) || !isStandValid(standPos)) {
            rejectWater();
            if (!findWaterAndStand()) {
                clearLine();
                return;
            }
            clearLine();
            moveToStand();
            return;
        }
        double dist = companion.distanceToSqr(Vec3.atCenterOf(standPos));
        if (dist > 9.0D) {
            clearLine();
            moveToStand();
            return;
        }
        if (companion.getNavigation().isDone() && dist > 1.5D) {
            idleNavTicks++;
            if (idleNavTicks > RESCAN_STUCK_TICKS) {
                if (findWaterAndStand()) {
                    clearLine();
                    moveToStand();
                    idleNavTicks = 0;
                    return;
                }
                idleNavTicks = 0;
            } else {
                clearLine();
                moveToStand();
            }
        } else {
            idleNavTicks = 0;
        }
        if (dist <= 2.25D && !hasLineCast()) {
            // Only cast once we are close enough to the shoreline stand position.
            if (recastCooldown-- <= 0) {
                phase(JobPhase.WORKING, "job_status.modern_companions.fishing", waterSpot);
                BlockPos castTarget = selectCastTarget();
                faceWater(castTarget);
                castLine(castTarget);
                // A removed/rejected hook cannot cause a cast loop faster than once per second.
                recastCooldown = RECAST_DELAY;
            }
        } else if (recastCooldown > 0) {
            recastCooldown--;
        }
        if (!hasLineCast()) return;
        if (!activeHook.isSettled()) return;
        if (!activeHook.isLineInWater()) {
            // Do not reel in unless the line is actually in water.
            rejectWater();
            clearLine();
            recastCooldown = RECAST_DELAY;
            return;
        }
        if (!activeHook.isBiting()) return;
        phase(JobPhase.COLLECTING, "job_status.modern_companions.reeling_in", waterSpot);
        faceWater();
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        reelIn();
        companion.getMainHandItem().hurtAndBreak(1, companion, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        clearLine();
        recastCooldown = RECAST_DELAY;
    }

    private void faceWater() {
        faceWater(waterSpot);
    }

    private void faceWater(BlockPos target) {
        if (target == null) return;
        Vec3 look = Vec3.atCenterOf(target);
        Vec3 delta = look.subtract(companion.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (Math.atan2(delta.z, delta.x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(delta.y, horizontal) * 180.0D / Math.PI);
        companion.setYRot(yaw);
        companion.setYHeadRot(yaw);
        companion.setXRot(pitch);
        companion.getLookControl().setLookAt(look.x, look.y, look.z);
    }

    private void reelIn() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        ItemStack catchStack = rollFishingLoot();
        if (!catchStack.isEmpty()) {
            ItemStack leftover = companion.getInventory().addItem(catchStack);
            if (!leftover.isEmpty()) {
                companion.spawnAtLocation(leftover);
            }
            companion.incrementFishCaughtSession();
        }
    }

    private ItemStack rollFishingLoot() {
        if (!(companion.level() instanceof ServerLevel server)) {
            return ItemStack.EMPTY;
        }
        LootTable lootTable = server.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
        if (lootTable == null) {
            return new ItemStack(Items.COD);
        }
        double luck = companion.getAttributes().hasAttribute(Attributes.LUCK)
                ? companion.getAttributeValue(Attributes.LUCK)
                : 0.0D;
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, companion.position())
                .withParameter(LootContextParams.TOOL, companion.getMainHandItem())
                .withLuck((float) luck)
                .create(LootContextParamSets.FISHING);
        var list = lootTable.getRandomItems(params);
        if (!list.isEmpty()) {
            return list.get(random.nextInt(list.size())).copy();
        }
        return new ItemStack(Items.COD);
    }

    private boolean findWaterAndStand() {
        BlockPos origin = companion.getWorkCenter().orElse(companion.blockPosition());
        BlockPos patrolCenter = companion.getWorkCenter().orElse(origin);
        Level level = companion.level();
        int radius = Math.max(4, Math.min(searchRadius, companion.getPatrolRadius()));
        int radiusSq = radius * radius;
        if (waterSpot == null) {
            BlockPos saved = companion.getJobCheckpointTarget().orElse(null);
            if (saved != null && patrolCenter.distSqr(saved) <= radiusSq && isFishableWater(level, saved)) {
                BlockPos stand = WorkerSite.findApproachStand(companion, saved, 2);
                if (stand != null) {
                    waterSpot = saved.immutable();
                    standPos = stand;
                    return true;
                }
            }
        }
        if (scanOrigin == null || !scanOrigin.equals(origin)) {
            // Reset the progressive scan when the companion moves.
            scanOrigin = origin.immutable();
            scanRing = 0;
        }
        int ringsScanned = 0;

        for (int r = scanRing; r <= radius; r++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // perimeter only
                        BlockPos candidate = origin.offset(dx, dy, dz);
                        if (patrolCenter.distSqr(candidate) > radiusSq || isRejected(candidate)) continue;
                        BlockPos stand = candidate.above();
                        if (!isStandValid(stand)) continue;
                        BlockPos water = adjacentFishableWater(level, candidate);
                        if (water == null) continue;
                        // Path to the stand air block so navigation targets the actual feet position.
                        var path = companion.getNavigation().createPath(stand, 0);
                        if (path == null || !path.canReach()) continue;
                        standPos = stand.immutable();
                        waterSpot = water.immutable();
                        scanRing = 0;
                        return true;
                    }
                }
            }
            ringsScanned++;
            if (ringsScanned >= MAX_RINGS_PER_SCAN) {
                // Continue the outward search next time so we eventually reach farther water.
                scanRing = r + 1;
                return false;
            }
        }
        scanRing = 0;
        waterSpot = null;
        standPos = null;
        return false;
    }

    private boolean hasLineCast() {
        return activeHook != null && !activeHook.isRemoved();
    }

    private void castLine(BlockPos target) {
        if (!(companion.level() instanceof ServerLevel server)) return;
        if (target == null) return;
        clearLine();
        companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        // Spawn at rod height then let the server simulate a short visible cast arc.
        CompanionFishingHook hook = new CompanionFishingHook(server, companion, target);
        // Spawn at validated surface water: Projectile collision otherwise deletes shore casts before they settle.
        Vec3 water = Vec3.atCenterOf(target).add(0.0D, 0.1D, 0.0D);
        hook.setPos(water.x, water.y, water.z);
        hook.setDeltaMovement(Vec3.ZERO);
        hook.setNoGravity(true);
        server.addFreshEntity(hook);
        activeHook = hook;
        server.playSound(null, companion.blockPosition(), SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.PLAYERS, 0.6F, 1.0F);
    }

    private BlockPos selectCastTarget() {
        if (waterSpot == null) return null;
        Vec3 shore = standPos == null ? companion.position() : Vec3.atCenterOf(standPos);
        Vec3 flatLook = Vec3.atCenterOf(waterSpot).subtract(shore);
        flatLook = new Vec3(flatLook.x, 0.0D, flatLook.z);
        if (flatLook.lengthSqr() < 1.0E-4D) {
            flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            flatLook = flatLook.normalize();
        }
        Vec3 right = new Vec3(-flatLook.z, 0.0D, flatLook.x);
        Vec3 origin = Vec3.atCenterOf(waterSpot);
        Level level = companion.level();

        for (int i = 0; i < CAST_ATTEMPTS; i++) {
            int dist = CAST_MIN_DIST + random.nextInt(CAST_MAX_DIST - CAST_MIN_DIST + 1);
            int side = random.nextInt(CAST_SIDE_SPREAD * 2 + 1) - CAST_SIDE_SPREAD;
            Vec3 target = origin.add(flatLook.scale(dist)).add(right.scale(side));
            BlockPos base = BlockPos.containing(target.x, waterSpot.getY(), target.z);
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos candidate = base.offset(0, dy, 0);
                if (isFishableWater(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }
        return isFishableWater(level, waterSpot) ? waterSpot : null;
    }

    private void clearLine() {
        if (activeHook != null && !activeHook.isRemoved()) {
            activeHook.discard();
        }
        activeHook = null;
    }

    private boolean isWater(BlockPos pos) {
        return isWater(companion.level(), pos);
    }

    private boolean isWater(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.WATER) || state.getFluidState().isSource() && state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
    }

    private boolean isFishableWater(BlockPos pos) {
        return isFishableWater(companion.level(), pos);
    }

    private boolean isFishableWater(Level level, BlockPos pos) {
        if (!isWater(level, pos)) return false;
        // Require surface water (air above) so the bobber sits at the water surface.
        var above = level.getBlockState(pos.above());
        if (!above.getFluidState().isEmpty() || !above.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        // Require nearby water neighbors so companions avoid 1-block puddles.
        int adjacent = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isWater(level, pos.relative(dir))) {
                adjacent++;
                if (adjacent >= MIN_WATER_ADJACENT) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos adjacentFishableWater(Level level, BlockPos standFloor) {
        // Favor horizontal adjacency first, then vertical within reach.
        for (BlockPos side : BlockPos.betweenClosed(standFloor.offset(-1, 0, -1), standFloor.offset(1, 0, 1))) {
            if (isFishableWater(level, side)) return side.immutable();
        }
        for (BlockPos side : BlockPos.betweenClosed(standFloor.offset(-1, -1, -1), standFloor.offset(1, 1, 1))) {
            if (isFishableWater(level, side)) return side.immutable();
        }
        return null;
    }

    private boolean isStandValid(BlockPos pos) {
        Level level = companion.level();
        BlockPos floor = pos.below();
        var floorState = level.getBlockState(floor);
        var feet = level.getBlockState(pos);
        // Need a solid floor with an open stand space for navigation.
        return WorkerSite.isSafeStand(level, pos);
    }

    private void moveToStand() {
        if (standPos == null) return;
        companion.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY(), standPos.getZ() + 0.5D, 1.0D);
    }

    private boolean isRejected(BlockPos water) {
        return rejectedWater.getOrDefault(water, 0) > companion.tickCount;
    }

    private void rejectWater() {
        if (waterSpot != null) rejectedWater.put(waterSpot.immutable(), companion.tickCount + 20 * 30);
    }

    private boolean isActiveJob() {
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.FISHER) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (!hasRod()) { companion.setJobStatus("job_status.modern_companions.no_rod"); return false; }
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    private boolean hasRod() {
        return companion.getMainHandItem().getItem() instanceof FishingRodItem;
    }

    private boolean hasTool(java.util.function.Predicate<ItemStack> matcher) {
        if (matcher.test(companion.getMainHandItem())) return true;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            if (matcher.test(companion.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinWorkArea(double ownerMax) {
        LivingEntity owner = companion.getOwner();
        if (owner != null && companion.distanceToSqr(owner) <= ownerMax * ownerMax) {
            return true;
        }
        return false;
    }

    private boolean isWithinPatrolArea() {
        return companion.isPatrolling() && companion.getPatrolPos().isPresent()
                && companion.getPatrolPos().get().distSqr(companion.blockPosition()) <= Math.pow(Math.max(8.0D, companion.getPatrolRadius() + 4), 2);
    }
}
