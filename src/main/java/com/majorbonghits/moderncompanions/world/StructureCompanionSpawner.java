package com.majorbonghits.moderncompanions.world;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Spawns a matching companion when one of our structures is generated.
 * This avoids relying on NBT-embedded entities and prevents duplicates via a SavedData guard.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class StructureCompanionSpawner {
    private StructureCompanionSpawner() {}

    // One full companion initialization per tick keeps pregeneration from blocking the server loop.
    private static final int MAX_SPAWNS_PER_TICK = 1;
    // Rotate a few unavailable targets so one unloaded center cannot block later structures.
    private static final int MAX_QUEUE_SCANS_PER_TICK = 8;
    private static final ConcurrentLinkedQueue<SpawnRequest> PENDING_SPAWNS = new ConcurrentLinkedQueue<>();
    private static final Set<String> QUEUED_SPAWNS = ConcurrentHashMap.newKeySet();

    private static final float FIREARM_SPECIALIST_CHANCE = 0.08F;
    private static final Set<ResourceLocation> FIREARM_STRUCTURE_POOL = Set.of(
            Constants.id("berserker_house"), Constants.id("scout_house"), Constants.id("stormcaller_house"),
            Constants.id("vanguard_house"), Constants.id("smith"), Constants.id("house"),
            Constants.id("largehouse"), Constants.id("largehouse2"), Constants.id("largehouse3"),
            Constants.id("lumber"), Constants.id("windmill"), Constants.id("oak_house"),
            Constants.id("oak_birch_house"), Constants.id("birch_house"), Constants.id("acacia_house"),
            Constants.id("dark_oak_house"), Constants.id("sandstone_house"), Constants.id("terracotta_house"));

    /** Map structure id -> companion entity choices (supports multiple per structure). */
    private static final Map<ResourceLocation, List<Supplier<? extends EntityType<? extends PathfinderMob>>>> STRUCTURE_TO_ENTITIES = Map.ofEntries(
            Map.entry(Constants.id("alchemist_house"), choices(ModEntityTypes.ALCHEMIST, ModEntityTypes.WITCH, ModEntityTypes.DRUID)),
            Map.entry(Constants.id("beastmaster_house"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("berserker_house"), List.of(ModEntityTypes.BERSERKER)),
            Map.entry(Constants.id("cleric_house"), choices(ModEntityTypes.CLERIC)),
            Map.entry(Constants.id("scout_house"), List.of(ModEntityTypes.SCOUT)),
            Map.entry(Constants.id("stormcaller_house"), List.of(ModEntityTypes.STORMCALLER)),
            Map.entry(Constants.id("vanguard_house"), List.of(ModEntityTypes.VANGUARD)),
            Map.entry(Constants.id("smith"), List.of(ModEntityTypes.VANGUARD)),
            Map.entry(Constants.id("house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("largehouse"), List.of(ModEntityTypes.ARCHER)),
            Map.entry(Constants.id("largehouse2"), List.of(ModEntityTypes.AXEGUARD)),
            Map.entry(Constants.id("largehouse3"), List.of(ModEntityTypes.BERSERKER)),
            Map.entry(Constants.id("lumber"), List.of(ModEntityTypes.ARBALIST)),
            // Towers can roll different mage variants
            Map.entry(Constants.id("tower1"), choices(ModEntityTypes.FIRE_MAGE, ModEntityTypes.LIGHTNING_MAGE, ModEntityTypes.WIZARD, ModEntityTypes.SORCERER, ModEntityTypes.CRYOMANCER, ModEntityTypes.ILLUSIONIST, ModEntityTypes.BATTLEMAGE)),
            Map.entry(Constants.id("tower2"), choices(ModEntityTypes.NECROMANCER, ModEntityTypes.WARLOCK, ModEntityTypes.HAG)),
            Map.entry(Constants.id("watermill"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("windmill"), List.of(ModEntityTypes.STORMCALLER)),
            Map.entry(Constants.id("church"), choices(ModEntityTypes.CLERIC)),
            // Biome-themed house variants (default to Knight so every house gets a resident)
            Map.entry(Constants.id("oak_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("oak_birch_house"), List.of(ModEntityTypes.SCOUT)),
            Map.entry(Constants.id("birch_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("acacia_house"), List.of(ModEntityTypes.ARCHER)),
            Map.entry(Constants.id("spruce_house"), List.of(ModEntityTypes.BEASTMASTER)),
            Map.entry(Constants.id("dark_oak_house"), List.of(ModEntityTypes.AXEGUARD)),
            Map.entry(Constants.id("sandstone_house"), List.of(ModEntityTypes.KNIGHT)),
            Map.entry(Constants.id("terracotta_house"), List.of(ModEntityTypes.ARBALIST))
    );

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        ChunkAccess chunk = event.getChunk();

        // Chunk load may be off-thread; only collect lightweight immutable requests here.
        List<SpawnRequest> pending = new ArrayList<>();

        chunk.getAllStarts().forEach((structure, start) -> {
            if (!start.isValid()) return;
            ResourceLocation id = serverLevel.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .getKey(structure);
            if (id == null || !STRUCTURE_TO_ENTITIES.containsKey(id)) return;
            List<Supplier<? extends EntityType<? extends PathfinderMob>>> choices = STRUCTURE_TO_ENTITIES.get(id);
            if (choices.isEmpty()) return; // Do not service structures whose gated companion is absent.

            BoundingBox bounds = start.getBoundingBox();
            BlockPos center = bounds.getCenter();
            String key = id + "|" + center.getX() + "," + center.getY() + "," + center.getZ();
            // Include this live server instance so a static queue cannot suppress a new
            // server's request with a key left behind by a previous world session.
            String queueKey = Integer.toHexString(System.identityHashCode(serverLevel.getServer()))
                    + "|" + serverLevel.dimension().location() + "|" + key;
            pending.add(new SpawnRequest(serverLevel, id, center, bounds, key, queueKey, choices));
        });

        for (SpawnRequest req : pending) {
            // Coalesce repeated ChunkEvent.Load notifications until the request is handled.
            if (QUEUED_SPAWNS.add(req.queueKey())) PENDING_SPAWNS.add(req);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int spawned = 0;
        int scanned = 0;
        Set<String> scannedKeys = new HashSet<>();
        while (spawned < MAX_SPAWNS_PER_TICK && scanned++ < MAX_QUEUE_SCANS_PER_TICK) {
            SpawnRequest req = PENDING_SPAWNS.poll();
            if (req == null) return;
            // A failed request is requeued for a later tick; do not rescan that same request repeatedly here.
            if (!scannedKeys.add(req.queueKey())) {
                PENDING_SPAWNS.add(req);
                return;
            }
            if (req.level().getServer() != event.getServer()) {
                // Never retain a request or dedup key belonging to a stopped server.
                QUEUED_SPAWNS.remove(req.queueKey());
                continue;
            }

            boolean completed = false;
            try {
                StructureSpawnTracker tracker = StructureSpawnTracker.get(req.level());
                boolean alreadySpawned = tracker.hasSeen(req.key());
                if (alreadySpawned) {
                    completed = true;
                    continue;
                }
                if (tracker.isLegacy(req.key())) {
                    // Old versions recorded the key before insertion. Wait until the complete
                    // structure area is loaded so an old resident cannot be mistaken for a
                    // missing one simply because its entity chunk is still pending.
                    if (!areStructureChunksLoaded(req.level(), req.bounds())) continue;
                    if (hasResidentInStructure(req.level(), req.bounds())) {
                        tracker.confirmLegacy(req.key());
                        completed = true;
                        continue;
                    }
                    tracker.forgetLegacy(req.key());
                } else if (hasResidentInStructure(req.level(), req.bounds())) {
                    // Covers a crash between entity acceptance and SavedData persistence.
                    tracker.markSpawned(req.key());
                    completed = true;
                    continue;
                }
                EntityType<? extends PathfinderMob> type = pickEntityFor(req.level().random, req.structureId(), req.typeSuppliers());
                BlockPos spawnPos = findSafeSpawnPosition(req.level(), req.bounds(), type);
                boolean spawnSucceeded = spawnPos != null && spawnCompanion(req.level(), type, spawnPos);
                if (StructureSpawnRules.shouldRetry(alreadySpawned, spawnSucceeded)) continue;
                if (spawnSucceeded) {
                    tracker.markSpawned(req.key());
                    spawned++;
                }
                completed = true;
            } catch (RuntimeException ex) {
                // Keep transient registry/event failures retryable instead of losing the resident request.
                Constants.LOG.debug("Deferred companion structure spawn for {} at {}", req.structureId(), req.center(), ex);
            } finally {
                if (completed) {
                    QUEUED_SPAWNS.remove(req.queueKey());
                } else {
                    PENDING_SPAWNS.add(req);
                }
            }
        }
    }

    private static boolean areStructureChunksLoaded(ServerLevel level, BoundingBox bounds) {
        int minChunkX = bounds.minX() >> 4;
        int maxChunkX = bounds.maxX() >> 4;
        int minChunkZ = bounds.minZ() >> 4;
        int maxChunkZ = bounds.maxZ() >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunkAt(new BlockPos(chunkX << 4, bounds.minY(), chunkZ << 4))) return false;
            }
        }
        return true;
    }

    private static boolean hasResidentInStructure(ServerLevel level, BoundingBox bounds) {
        AABB area = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(),
                (double) bounds.maxX() + 1.0D, (double) bounds.maxY() + 1.0D, (double) bounds.maxZ() + 1.0D);
        return level.getEntitiesOfClass(AbstractHumanCompanionEntity.class, area,
                entity -> entity.isAlive() && !entity.isRemoved()).stream().findAny().isPresent();
    }

    /** Find a loaded, non-fluid, two-block-high position on a collision floor inside the structure. */
    private static BlockPos findSafeSpawnPosition(ServerLevel level, BoundingBox bounds,
                                                   EntityType<? extends PathfinderMob> type) {
        int[] xPositions = centeredRange(bounds.minX(), bounds.maxX(), bounds.getCenter().getX());
        int[] yPositions = centeredRange(bounds.minY(), bounds.maxY() - 1, bounds.getCenter().getY());
        int[] zPositions = centeredRange(bounds.minZ(), bounds.maxZ(), bounds.getCenter().getZ());
        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();

        for (int x : xPositions) {
            for (int y : yPositions) {
                candidate.set(x, y, 0);
                if (level.isOutsideBuildHeight(y)) continue;
                for (int z : zPositions) {
                    candidate.setZ(z);
                    BlockPos head = candidate.above();
                    BlockPos floor = candidate.below();
                    if (level.isOutsideBuildHeight(head) || level.isOutsideBuildHeight(floor)
                            || !level.getWorldBorder().isWithinBounds(candidate)
                            || !level.hasChunkAt(candidate) || !level.hasChunkAt(head) || !level.hasChunkAt(floor)) {
                        continue;
                    }

                    BlockState feetState = level.getBlockState(candidate);
                    BlockState headState = level.getBlockState(head);
                    BlockState floorState = level.getBlockState(floor);
                    if (!level.getFluidState(candidate).isEmpty() || !level.getFluidState(head).isEmpty()
                            || !level.getFluidState(floor).isEmpty()
                            || !feetState.getCollisionShape(level, candidate).isEmpty()
                            || !headState.getCollisionShape(level, head).isEmpty()
                            || floorState.getCollisionShape(level, floor).isEmpty()
                            || type.isBlockDangerous(feetState) || type.isBlockDangerous(headState)
                            || type.isBlockDangerous(floorState)) {
                        continue;
                    }

                    AABB spawnBox = type.getSpawnAABB(x + 0.5D, y, z + 0.5D);
                    if (level.noCollision(spawnBox)) return candidate.immutable();
                }
            }
        }
        return null;
    }

    private static int[] centeredRange(int min, int max, int center) {
        if (max < min) return new int[0];
        int[] values = new int[max - min + 1];
        int index = 0;
        for (int offset = 0; index < values.length; offset++) {
            int lower = center - offset;
            if (lower >= min && lower <= max) values[index++] = lower;
            if (offset == 0) continue;
            int upper = center + offset;
            if (upper >= min && upper <= max) values[index++] = upper;
        }
        return values;
    }

    private static boolean spawnCompanion(ServerLevel level, EntityType<? extends PathfinderMob> type, BlockPos position) {
        Entity entity = type.create(level, null, position, MobSpawnType.STRUCTURE, false, false);
        if (entity == null || !level.noCollision(entity)) {
            if (entity != null) entity.discard();
            return false;
        }

        // EntityType#spawn discards the add result; keep the request pending when NeoForge cancels the join event.
        if (!level.addFreshEntity(entity)) {
            entity.discard();
            return false;
        }
        return entity.isAddedToLevel() && !entity.isRemoved();
    }

    private static EntityType<? extends PathfinderMob> pickEntityFor(RandomSource random, ResourceLocation structureId,
                                                                       List<Supplier<? extends EntityType<? extends PathfinderMob>>> choices) {
        if (choices.isEmpty()) throw new IllegalStateException("No entity choices for structure spawn");
        if (ModEntityTypes.FIREARM_SPECIALIST != null
                && FIREARM_STRUCTURE_POOL.contains(structureId)
                && random.nextFloat() < FIREARM_SPECIALIST_CHANCE) {
            return ModEntityTypes.FIREARM_SPECIALIST.get();
        }
        Supplier<? extends EntityType<? extends PathfinderMob>> supplier = choices.size() == 1
                ? choices.getFirst()
                : choices.get(random.nextInt(choices.size()));
        return supplier.get();
    }

    @SafeVarargs
    private static List<Supplier<? extends EntityType<? extends PathfinderMob>>> choices(Supplier<? extends EntityType<? extends PathfinderMob>>... entries) {
        return Arrays.stream(entries).filter(Objects::nonNull).toList();
    }

    private record SpawnRequest(ServerLevel level, ResourceLocation structureId, BlockPos center, BoundingBox bounds,
                                String key, String queueKey,
                                List<Supplier<? extends EntityType<? extends PathfinderMob>>> typeSuppliers) {}

    /**
     * SavedData to remember which structure placements already spawned a companion.
     */
    private static final class StructureSpawnTracker extends SavedData {
        private static final String DATA_NAME = Constants.MOD_ID + "_structure_spawns";
        private static final int DATA_VERSION = 2;
        private final Set<String> successfulKeys = new HashSet<>();
        private final Set<String> legacyKeys = new HashSet<>();

        StructureSpawnTracker() {}

        static StructureSpawnTracker get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(StructureSpawnTracker::new, StructureSpawnTracker::load),
                    DATA_NAME
            );
        }

        boolean hasSeen(String key) {
            return successfulKeys.contains(key);
        }

        boolean isLegacy(String key) {
            return legacyKeys.contains(key);
        }

        void confirmLegacy(String key) {
            if (legacyKeys.remove(key)) {
                successfulKeys.add(key);
                setDirty();
            }
        }

        void forgetLegacy(String key) {
            if (legacyKeys.remove(key)) setDirty();
        }

        void markSpawned(String key) {
            boolean changed = legacyKeys.remove(key);
            changed |= successfulKeys.add(key);
            if (changed) setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            ListTag list = new ListTag();
            for (String key : successfulKeys) {
                list.add(StringTag.valueOf(key));
            }
            ListTag legacy = new ListTag();
            for (String key : legacyKeys) {
                legacy.add(StringTag.valueOf(key));
            }
            tag.putInt("version", DATA_VERSION);
            tag.put("keys", list);
            tag.put("legacy_keys", legacy);
            return tag;
        }

        private static StructureSpawnTracker load(CompoundTag tag, HolderLookup.Provider provider) {
            StructureSpawnTracker tracker = new StructureSpawnTracker();
            ListTag list = tag.getList("keys", ListTag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                (tag.getInt("version") >= DATA_VERSION ? tracker.successfulKeys : tracker.legacyKeys)
                        .add(list.getString(i));
            }
            ListTag legacy = tag.getList("legacy_keys", ListTag.TAG_STRING);
            for (int i = 0; i < legacy.size(); i++) {
                tracker.legacyKeys.add(legacy.getString(i));
            }
            return tracker;
        }
    }
}
