package com.majorbonghits.moderncompanions.world;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEntityTypes;
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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;
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

            BlockPos center = start.getBoundingBox().getCenter();
            String key = id + "|" + center.getX() + "," + center.getY() + "," + center.getZ();
            String queueKey = serverLevel.dimension().location() + "|" + key;
            pending.add(new SpawnRequest(serverLevel, id, center, key, queueKey, choices));
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
        while (spawned < MAX_SPAWNS_PER_TICK && scanned++ < MAX_QUEUE_SCANS_PER_TICK) {
            SpawnRequest req = PENDING_SPAWNS.poll();
            if (req == null) return;
            if (req.level().getServer() != event.getServer()) {
                PENDING_SPAWNS.add(req);
                continue;
            }
            // Never let entity insertion synchronously request another chunk while generation is active.
            if (!StructureSpawnRules.canInsert(req.level().hasChunkAt(req.center()), false)) {
                PENDING_SPAWNS.add(req);
                continue;
            }

            try {
                StructureSpawnTracker tracker = StructureSpawnTracker.get(req.level());
                if (!StructureSpawnRules.canInsert(true, tracker.hasSeen(req.key()))) continue;
                EntityType<? extends PathfinderMob> type = pickEntityFor(req.level().random, req.structureId(), req.typeSuppliers());
                Entity entity = type.spawn(req.level(), req.center(), MobSpawnType.STRUCTURE);
                // Do not consume the one-resident record if a spawn event cancels this insertion.
                if (entity != null) tracker.markSpawned(req.key());
                spawned++;
            } finally {
                QUEUED_SPAWNS.remove(req.queueKey());
            }
        }
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

    private record SpawnRequest(ServerLevel level, ResourceLocation structureId, BlockPos center, String key, String queueKey,
                                List<Supplier<? extends EntityType<? extends PathfinderMob>>> typeSuppliers) {}

    /**
     * SavedData to remember which structure placements already spawned a companion.
     */
    private static final class StructureSpawnTracker extends SavedData {
        private static final String DATA_NAME = Constants.MOD_ID + "_structure_spawns";
        private final Set<String> seenKeys = new HashSet<>();

        StructureSpawnTracker() {}

        static StructureSpawnTracker get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(StructureSpawnTracker::new, StructureSpawnTracker::load),
                    DATA_NAME
            );
        }

        boolean hasSeen(String key) {
            return seenKeys.contains(key);
        }

        void markSpawned(String key) {
            if (seenKeys.add(key)) setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            ListTag list = new ListTag();
            for (String key : seenKeys) {
                list.add(StringTag.valueOf(key));
            }
            tag.put("keys", list);
            return tag;
        }

        private static StructureSpawnTracker load(CompoundTag tag, HolderLookup.Provider provider) {
            StructureSpawnTracker tracker = new StructureSpawnTracker();
            ListTag list = tag.getList("keys", ListTag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                tracker.seenKeys.add(list.getString(i));
            }
            return tracker;
        }
    }
}
