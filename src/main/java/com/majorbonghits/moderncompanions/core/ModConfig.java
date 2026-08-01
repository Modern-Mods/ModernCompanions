package com.majorbonghits.moderncompanions.core;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Port of the original Human Companions common configuration.
 * TODO: reconnect the values to gameplay once entity logic is fully ported.
 */
public final class ModConfig {
    private ModConfig() {}

    private static final List<String> DEFAULT_ALL_FOODS = List.of(
            "minecraft:cookie", "minecraft:bread", "minecraft:melon_slice", "minecraft:apple", "minecraft:sweet_berries",
            "minecraft:carrot", "minecraft:baked_potato", "minecraft:cooked_salmon", "minecraft:cooked_cod",
            "minecraft:cooked_mutton", "minecraft:cooked_porkchop", "minecraft:cooked_beef", "minecraft:cooked_chicken",
            "minecraft:pumpkin_pie", "minecraft:glow_berries", "minecraft:potato", "minecraft:beetroot",
            "minecraft:dried_kelp", "minecraft:cooked_rabbit");
    private static final List<String> DEFAULT_EXTRA_HEAL_CONSUMABLES = List.of(
            "minecraft:golden_apple", "minecraft:enchanted_golden_apple", "minecraft:golden_carrot", "minecraft:honey_bottle",
            "minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew");
    private static final List<String> DEFAULT_COMMON_RESOURCE_ITEMS = List.of(
            "minecraft:coal", "minecraft:charcoal", "minecraft:copper_ingot", "minecraft:iron_ingot", "minecraft:redstone",
            "minecraft:lapis_lazuli", "minecraft:flint", "minecraft:clay_ball", "minecraft:string", "minecraft:leather",
            "minecraft:bone", "minecraft:feather");
    private static final List<String> DEFAULT_UNCOMMON_RESOURCE_ITEMS = List.of(
            "minecraft:gold_ingot", "minecraft:amethyst_shard", "minecraft:slime_ball", "minecraft:gunpowder",
            "minecraft:glowstone_dust", "minecraft:prismarine_shard", "minecraft:prismarine_crystals",
            "minecraft:ender_pearl", "minecraft:obsidian");
    private static final List<String> DEFAULT_RARE_RESOURCE_ITEMS = List.of(
            "minecraft:diamond", "minecraft:emerald", "minecraft:blaze_rod", "minecraft:magma_cream");
    private static final List<String> DEFAULT_HUNT_MOBS = List.of(
            "minecraft:chicken", "minecraft:cow", "minecraft:pig", "minecraft:rabbit", "minecraft:sheep", "minecraft:goat");

    private static ModConfigSpec COMMON_SPEC;
    public static ModConfigSpec.IntValue AVERAGE_HOUSE_SEPARATION;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_COMPANIONS;
    public static ModConfigSpec.BooleanValue FRIENDLY_FIRE_PLAYER;
    public static ModConfigSpec.BooleanValue FALL_DAMAGE;
    public static ModConfigSpec.BooleanValue SPAWN_ARMOR;
    public static ModConfigSpec.BooleanValue SPAWN_WEAPON;
    public static ModConfigSpec.BooleanValue AUTO_EQUIP;
    public static ModConfigSpec.BooleanValue TELEPORT_LEASH;
    public static ModConfigSpec.IntValue BASE_HEALTH;
    public static ModConfigSpec.BooleanValue LOW_HEALTH_FOOD;
    public static ModConfigSpec.DoubleValue LOW_HEALTH_FOOD_THRESHOLD;
    public static ModConfigSpec.BooleanValue STAMINA_ENABLED;
    public static ModConfigSpec.IntValue STAMINA_SPRINT_COST;
    public static ModConfigSpec.IntValue STAMINA_MELEE_COST;
    public static ModConfigSpec.BooleanValue CREEPER_WARNING;
    public static ModConfigSpec.EnumValue<CompanionVoiceMode> COMPANION_VOICE_MODE;
    public static ModConfigSpec.IntValue COMPANION_VOICE_VOLUME;
    public static ModConfigSpec.ConfigValue<List<? extends String>> ALL_FOODS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> EXTRA_HEAL_CONSUMABLES;
    public static ModConfigSpec.ConfigValue<List<? extends String>> COMMON_RESOURCE_ITEMS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> UNCOMMON_RESOURCE_ITEMS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> RARE_RESOURCE_ITEMS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> HUNT_MOBS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> ALERT_EXCLUDED_MOBS;
    private static ModConfigSpec.BooleanValue ALERT_CREEPER_DEFAULT_MIGRATED;
    public static ModConfigSpec.BooleanValue TRAITS_ENABLED;
    public static ModConfigSpec.IntValue SECONDARY_TRAIT_CHANCE;
    public static ModConfigSpec.BooleanValue BOND_ENABLED;
    public static ModConfigSpec.BooleanValue MORALE_ENABLED;
    public static ModConfigSpec.IntValue BOND_TICK_INTERVAL;
    public static ModConfigSpec.IntValue BOND_TIME_XP;
    public static ModConfigSpec.IntValue BOND_FEED_XP;
    public static ModConfigSpec.IntValue BOND_RESURRECT_XP;
    public static ModConfigSpec.DoubleValue MORALE_FEED_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_NEAR_DEATH_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_RESURRECT_DELTA;
    public static ModConfigSpec.DoubleValue MORALE_BOND_LEVEL_DELTA;
    public static ModConfigSpec.DoubleValue LUCKY_EXTRA_DROP_CHANCE;
    public static ModConfigSpec.BooleanValue JOB_LUMBERJACK_ENABLED;
    public static ModConfigSpec.IntValue JOB_LUMBERJACK_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_HUNTER_ENABLED;
    public static ModConfigSpec.IntValue JOB_HUNTER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_MINER_ENABLED;
    public static ModConfigSpec.IntValue JOB_MINER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_FISHER_ENABLED;
    public static ModConfigSpec.IntValue JOB_FISHER_RADIUS;
    public static ModConfigSpec.BooleanValue JOB_CHEF_ENABLED;
    public static ModConfigSpec.IntValue JOB_CHEF_RADIUS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> JOB_MINER_ALLOW_BLOCKS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> JOB_MINER_DENY_BLOCKS;
    public static ModConfigSpec.BooleanValue JOB_ASSIGNED_CHESTS_CHUNKLOAD;
    public static ModConfigSpec.BooleanValue SHOW_JOBS_BUTTON;

    /**
     * Safely read a config value even during very early lifecycle (e.g., attribute construction) by
     * falling back to its default when the config file has not been loaded yet.
     */
    public static <T> T safeGet(ModConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException ex) {
            return value.getDefault();
        }
    }

    public static void register() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation("modern_companions.configuration.worldgen")
                .comment("Settings for world gen (data packs recommended in 1.21.1)").push("worldgen");
        AVERAGE_HOUSE_SEPARATION = builder.translation("modern_companions.configuration.worldgen.average_house_separation")
                .comment("Average chunk separation between companion houses")
                .defineInRange("averageHouseSeparation", 20, 11, Integer.MAX_VALUE);
        builder.pop();

        builder.translation("modern_companions.configuration.companion").push("companion");
        FRIENDLY_FIRE_COMPANIONS = builder.translation("modern_companions.configuration.companion.friendly_fire_companions")
                .comment("Whether companions can hurt each other")
                .define("friendlyFireCompanions", false);
        FRIENDLY_FIRE_PLAYER = builder.translation("modern_companions.configuration.companion.friendly_fire_player")
                .comment("Whether companion can damage the owning player")
                .define("friendlyFirePlayer", true);
        FALL_DAMAGE = builder.translation("modern_companions.configuration.companion.fall_damage")
                .comment("Whether companions take fall damage")
                .define("fallDamage", true);
        SPAWN_ARMOR = builder.translation("modern_companions.configuration.companion.spawn_armor")
                .comment("Whether companions spawn with random armor")
                .define("spawnArmor", true);
        SPAWN_WEAPON = builder.translation("modern_companions.configuration.companion.spawn_weapon")
                .comment("Whether companions spawn with a weapon")
                .define("spawnWeapon", true);
        AUTO_EQUIP = builder.translation("modern_companions.configuration.companion.auto_equip")
                .comment("Whether companions automatically equip suitable gear from their inventory")
                .define("autoEquip", false);
        TELEPORT_LEASH = builder.translation("modern_companions.configuration.companion.teleport_leash")
                .comment("When enabled, following companions teleport to a safe spot near the owner after exceeding the selected Radius by 5 blocks.")
                .define("teleportLeash", false);
        BASE_HEALTH = builder.translation("modern_companions.configuration.companion.base_health")
                .comment("Base health for companions; a small random variance is applied on spawn")
                .defineInRange("baseHealth", 20, 5, Integer.MAX_VALUE);
        LOW_HEALTH_FOOD = builder.translation("modern_companions.configuration.companion.low_health_food")
                .comment("If true, companions eat and ask for food when they reach the configured health threshold")
                .define("lowHealthFood", true);
        LOW_HEALTH_FOOD_THRESHOLD = builder.translation("modern_companions.configuration.companion.low_health_food_threshold")
                .comment("Health fraction at or below which companions eat or ask for food; 0.5 means half health")
                .defineInRange("lowHealthFoodThreshold", 0.5D, 0.0D, 1.0D);
        STAMINA_ENABLED = builder.translation("modern_companions.configuration.companion.stamina_enabled")
                .comment("Enable the companion Stamina system")
                .define("staminaEnabled", true);
        STAMINA_SPRINT_COST = builder.translation("modern_companions.configuration.companion.stamina_sprint_cost")
                .comment("Stamina spent per game tick while sprinting; 0 disables sprint drain")
                .defineInRange("sprintStaminaCost", 1, 0, 100);
        STAMINA_MELEE_COST = builder.translation("modern_companions.configuration.companion.stamina_melee_cost")
                .comment("Stamina spent after a successful melee attack; 0 disables melee drain")
                .defineInRange("meleeStaminaCost", 8, 0, 100);
        CREEPER_WARNING = builder.translation("modern_companions.configuration.companion.creeper_warning")
                .comment("If true, companions warn the player and avoid nearby creepers")
                .define("creeperWarning", true);
        COMPANION_VOICE_MODE = builder.translation("modern_companions.configuration.companion.voice_mode")
                .comment("Full plays all companion voice lines; Limited keeps pain, death, and ambient noises; Off disables custom companion sounds.")
                .defineEnum("voiceMode", CompanionVoiceMode.FULL);
        COMPANION_VOICE_VOLUME = builder.translation("modern_companions.configuration.companion.voice_volume")
                .comment("Volume of custom companion sounds as a percentage.")
                .defineInRange("voiceVolume", 80, 0, 100);
        builder.pop();

        builder.translation("modern_companions.configuration.taming").push("taming");
        ALL_FOODS = builder.translation("modern_companions.configuration.taming.all_foods")
                .comment("Item registry ids companions may request, choose as favorites, and eat for healing.")
                .defineList("allFoods", DEFAULT_ALL_FOODS, () -> "minecraft:bread", ModConfig::isKnownItemId);
        EXTRA_HEAL_CONSUMABLES = builder.translation("modern_companions.configuration.taming.extra_heal_consumables")
                .comment("Additional item registry ids companions may consume for healing but never request while taming.")
                .defineListAllowEmpty("extraHealConsumables", DEFAULT_EXTRA_HEAL_CONSUMABLES, () -> "minecraft:golden_apple", ModConfig::isKnownItemId);
        COMMON_RESOURCE_ITEMS = builder.translation("modern_companions.configuration.taming.common_resource_items")
                .comment("Item registry ids used for common companion taming-resource requests.")
                .defineList("commonResourceItems", DEFAULT_COMMON_RESOURCE_ITEMS, () -> "minecraft:iron_ingot", ModConfig::isKnownItemId);
        UNCOMMON_RESOURCE_ITEMS = builder.translation("modern_companions.configuration.taming.uncommon_resource_items")
                .comment("Item registry ids used for uncommon companion taming-resource requests.")
                .defineList("uncommonResourceItems", DEFAULT_UNCOMMON_RESOURCE_ITEMS, () -> "minecraft:gold_ingot", ModConfig::isKnownItemId);
        RARE_RESOURCE_ITEMS = builder.translation("modern_companions.configuration.taming.rare_resource_items")
                .comment("Item registry ids used for rare companion taming-resource requests.")
                .defineList("rareResourceItems", DEFAULT_RARE_RESOURCE_ITEMS, () -> "minecraft:diamond", ModConfig::isKnownItemId);
        builder.pop();

        builder.translation("modern_companions.configuration.hunting").push("hunting");
        HUNT_MOBS = builder.translation("modern_companions.configuration.hunting.hunt_mobs")
                .comment("Entity registry ids companions target when the manual Hunt toggle is enabled.")
                .defineListAllowEmpty("huntMobs", DEFAULT_HUNT_MOBS, () -> "minecraft:chicken", ModConfig::isKnownEntityId);
        builder.pop();

        builder.translation("modern_companions.configuration.alert").push("alert");
        ALERT_EXCLUDED_MOBS = builder.translation("modern_companions.configuration.alert.excluded_mobs")
                .comment("Use registry ids such as minecraft:ender_dragon, not Java class names. Creeper is the one editable default.")
                .defineListAllowEmpty("excludedMobs", () -> List.of(AlertExclusionDefaults.CREEPER_ID), () -> "example:dangerous_mob", ModConfig::isKnownEntityId);
        // Hidden migration marker: distinguishes old empty configs from a player's intentional removal.
        ALERT_CREEPER_DEFAULT_MIGRATED = builder.comment("Internal migration marker for the Creeper Alert default.")
                .define("creeperDefaultMigrated", false);
        builder.pop();

        builder.translation("modern_companions.configuration.personality").push("personality");
        TRAITS_ENABLED = builder.translation("modern_companions.configuration.personality.traits_enabled")
                .comment("Enable birth traits for companions (Primary/Secondary).")
                .define("traitsEnabled", true);
        SECONDARY_TRAIT_CHANCE = builder.translation("modern_companions.configuration.personality.secondary_trait_chance")
                .comment("Chance (percent) for a companion to roll a secondary trait at spawn.")
                .defineInRange("secondaryTraitChance", 40, 0, 100);
        BOND_ENABLED = builder.translation("modern_companions.configuration.personality.bond_enabled")
                .comment("Enable the Bond/Loyalty track.")
                .define("bondEnabled", true);
        MORALE_ENABLED = builder.translation("modern_companions.configuration.personality.morale_enabled")
                .comment("Enable morale tracking and small performance nudges.")
                .define("moraleEnabled", true);
        BOND_TICK_INTERVAL = builder.translation("modern_companions.configuration.personality.bond_tick_interval")
                .comment("Ticks between passive bond XP awards while near the owner (20 ticks = 1 second).")
                .defineInRange("bondTickInterval", 1200, 20, Integer.MAX_VALUE);
        BOND_TIME_XP = builder.translation("modern_companions.configuration.personality.bond_time_xp")
                .comment("Bond XP granted each interval when alive near the owner.")
                .defineInRange("bondTimeXp", 5, 0, 10000);
        BOND_FEED_XP = builder.translation("modern_companions.configuration.personality.bond_feed_xp")
                .comment("Bond XP granted when the owner feeds the companion.")
                .defineInRange("bondFeedXp", 15, 0, 10000);
        BOND_RESURRECT_XP = builder.translation("modern_companions.configuration.personality.bond_resurrect_xp")
                .comment("Bond XP granted when resurrecting a companion.")
                .defineInRange("bondResurrectXp", 80, 0, 100000);
        MORALE_FEED_DELTA = builder.translation("modern_companions.configuration.personality.morale_feed_delta")
                .comment("Morale change applied when the companion is fed by the owner.")
                .defineInRange("moraleFeedDelta", 0.05D, -1.0D, 1.0D);
        MORALE_NEAR_DEATH_DELTA = builder.translation("modern_companions.configuration.personality.morale_near_death_delta")
                .comment("Morale change applied when the companion nearly dies.")
                .defineInRange("moraleNearDeathDelta", -0.07D, -1.0D, 1.0D);
        MORALE_RESURRECT_DELTA = builder.translation("modern_companions.configuration.personality.morale_resurrect_delta")
                .comment("Morale change applied when resurrected.")
                .defineInRange("moraleResurrectDelta", -0.1D, -1.0D, 1.0D);
        MORALE_BOND_LEVEL_DELTA = builder.translation("modern_companions.configuration.personality.morale_bond_level_delta")
                .comment("Morale change applied on bond level up.")
                .defineInRange("moraleBondLevelDelta", 0.05D, -1.0D, 1.0D);
        LUCKY_EXTRA_DROP_CHANCE = builder.translation("modern_companions.configuration.personality.lucky_extra_drop_chance")
                .comment("Chance for Lucky trait companions to duplicate one dropped item on a kill (0.0-1.0).")
                .defineInRange("luckyExtraDropChance", 0.05D, 0.0D, 1.0D);
        builder.pop();

        builder.translation("modern_companions.configuration.jobs").push("jobs");
        JOB_LUMBERJACK_ENABLED = builder.translation("modern_companions.configuration.jobs.lumberjack_enabled")
                .comment("Enable the Lumberjack job behaviors.")
                .define("lumberjackEnabled", true);
        JOB_LUMBERJACK_RADIUS = builder.translation("modern_companions.configuration.jobs.lumberjack_radius")
                .comment("Minimum Lumberjack search radius; the companion Radius can expand work up to 128 blocks.")
                .defineInRange("lumberjackRadius", 10, 4, 64);
        JOB_HUNTER_ENABLED = builder.translation("modern_companions.configuration.jobs.hunter_enabled")
                .comment("Enable the Hunter job behaviors.")
                .define("hunterEnabled", true);
        JOB_HUNTER_RADIUS = builder.translation("modern_companions.configuration.jobs.hunter_radius")
                .comment("Search radius for Hunter target scans.")
                .defineInRange("hunterRadius", 20, 6, 64);
        JOB_MINER_ENABLED = builder.translation("modern_companions.configuration.jobs.miner_enabled")
                .comment("Enable the Miner job behaviors.")
                .define("minerEnabled", true);
        JOB_MINER_RADIUS = builder.translation("modern_companions.configuration.jobs.miner_radius")
                .comment("Minimum Miner search radius; the companion Radius can expand work up to 128 blocks.")
                .defineInRange("minerRadius", 8, 4, 32);
        JOB_MINER_ALLOW_BLOCKS = builder.translation("modern_companions.configuration.jobs.miner_allow_blocks")
                .comment("Optional whitelist of block ids the Miner may break (empty uses default tags).")
                .defineList("minerAllowBlocks", List::of, o -> o instanceof String);
        JOB_MINER_DENY_BLOCKS = builder.translation("modern_companions.configuration.jobs.miner_deny_blocks")
                .comment("Blacklist of block ids the Miner should never break.")
                .defineList("minerDenyBlocks", () -> List.of("minecraft:chest", "minecraft:spawner"), o -> o instanceof String);
        JOB_FISHER_ENABLED = builder.translation("modern_companions.configuration.jobs.fisher_enabled")
                .comment("Enable the Fisher job behaviors.")
                .define("fisherEnabled", true);
        JOB_FISHER_RADIUS = builder.translation("modern_companions.configuration.jobs.fisher_radius")
                .comment("Search radius for Fisher water spot scans.")
                .defineInRange("fisherRadius", 10, 4, 32);
        JOB_CHEF_ENABLED = builder.translation("modern_companions.configuration.jobs.chef_enabled")
                .comment("Enable the Chef job behaviors.")
                .define("chefEnabled", true);
        JOB_CHEF_RADIUS = builder.translation("modern_companions.configuration.jobs.chef_radius")
                .comment("Search radius for Chef heat source scans.")
                .defineInRange("chefRadius", 8, 3, 24);
        JOB_ASSIGNED_CHESTS_CHUNKLOAD = builder.translation("modern_companions.configuration.jobs.assigned_chests_chunkload")
                .comment("If true, companions keep their assigned drop-off chests chunk-loaded to prevent courier failures.")
                .define("assignedChestsChunkload", false);
        SHOW_JOBS_BUTTON = builder.translation("modern_companions.configuration.jobs.show_jobs_button")
                .comment("Show the Jobs button in the companion inventory. Disabled by default while Jobs are experimental.")
                .define("showJobsButton", false);
        builder.pop();

        COMMON_SPEC = builder.build();
        ModLoadingContext.get().getActiveContainer()
                .registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, COMMON_SPEC);
    }

    /** Upgrades pre-Creeper-default configs once without overwriting later player choices. */
    public static void migrateAlertExclusions(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != COMMON_SPEC || safeGet(ALERT_CREEPER_DEFAULT_MIGRATED)) return;

        ALERT_EXCLUDED_MOBS.set(AlertExclusionDefaults.withDefaultCreeper(safeGet(ALERT_EXCLUDED_MOBS)));
        ALERT_CREEPER_DEFAULT_MIGRATED.set(true);
        COMMON_SPEC.save();
    }

    /** Reject malformed or unloaded entity ids in the native config editor. */
    private static boolean isKnownEntityId(Object value) {
        if (!(value instanceof String raw)) return false;
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id);
    }

    /** Reject malformed or unloaded item ids in the native config editor. */
    private static boolean isKnownItemId(Object value) {
        if (!(value instanceof String raw)) return false;
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }
}
