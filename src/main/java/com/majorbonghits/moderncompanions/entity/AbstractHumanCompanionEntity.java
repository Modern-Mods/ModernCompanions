package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModSounds;
import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.entity.ai.*;
import com.majorbonghits.moderncompanions.entity.personality.CompanionPersonality;
import com.majorbonghits.moderncompanions.entity.job.CompanionJob;
import com.majorbonghits.moderncompanions.menu.CompanionMenu;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.core.ModEnchantments;
import com.majorbonghits.moderncompanions.item.ResurrectionScrollItem;
import com.majorbonghits.moderncompanions.item.CompanionPotionItem;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import com.majorbonghits.moderncompanions.core.TagsInit;
import com.majorbonghits.moderncompanions.entity.job.LumberjackJobGoal;
import com.majorbonghits.moderncompanions.entity.job.HunterJobGoal;
import com.majorbonghits.moderncompanions.entity.job.MinerJobGoal;
import com.majorbonghits.moderncompanions.entity.job.FisherJobGoal;
import com.majorbonghits.moderncompanions.entity.job.ChefJobGoal;
import com.majorbonghits.moderncompanions.entity.job.FarmerJobGoal;
import com.majorbonghits.moderncompanions.entity.job.JobReservations;
import com.majorbonghits.moderncompanions.entity.job.JobPhase;
import com.majorbonghits.moderncompanions.entity.job.JobPlan;
import com.majorbonghits.moderncompanions.entity.job.JobDropClaims;
import com.majorbonghits.moderncompanions.entity.job.JobToolPolicy;
import com.majorbonghits.moderncompanions.entity.ai.HunterCombatGoal;

/**
 * Port of the original AbstractHumanCompanionEntity with taming, leveling,
 * patrol/guard logic, and inventory handling.
 */
public abstract class AbstractHumanCompanionEntity extends TamableAnimal {
    private static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SEX = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> VOICE_ACTOR = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BASE_HEALTH = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXP_LVL = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final String HEALTH_WAS_FULL_TAG = "CompanionWasFullHealth";
    private static final EntityDataAccessor<String> CUSTOM_SKIN_URL = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> CUSTOM_BIO = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ALEX_MODEL = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STR = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DEX = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTL = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> END = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KILL_COUNT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> EATING = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LAST_SWING_TICK = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ALERT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PATROLLING = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GUARDING = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SPRINT_ENABLED = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PICKUP_ITEMS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALLOW_VILLAGER_HARM = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALLOW_PLAYER_HARM = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> PATROL_POS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> PATROL_RADIUS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DELIVERY_CHEST = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<String> DELIVERY_DIMENSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FOOD1 = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FOOD2 = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FAVORITE_FOOD = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FOOD1_AMT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD2_AMT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> RECRUITMENT_REQUIREMENTS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> EXP_PROGRESS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SPECIALIST = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> PRIMARY_TRAIT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SECONDARY_TRAIT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BOND_LEVEL = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOND_XP = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> BACKSTORY_ID = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> MORALE = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RESURRECT_COUNT = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> FIRST_TAMED_TIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> DIST_TRAVELED = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> MAJOR_KILLS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE_YEARS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> JOB_ID = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> WORK_ENABLED = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> JOB_STATUS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> MINER_ORES_COUNTED = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MINER_ORES_MINED = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MINER_ORES_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LUMBER_LOGS_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LUMBER_LOGS_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FISH_CAUGHT_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FISH_CAUGHT_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FARMER_HARVESTED_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FARMER_HARVESTED_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FARMER_PLANTED_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FARMER_PLANTED_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    // Job counters are synced for the Jobs screen and persisted with the companion.
    private static final EntityDataAccessor<Integer> HUNTER_KILLS_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNTER_KILLS_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHEF_COOKED_SESSION = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHEF_COOKED_LIFETIME = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAMINA = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAMINA_MAX = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MANA = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MANA_MAX = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    // A spellbook is a separate magical-only equipment view, not a vanilla hand slot.
    private static final EntityDataAccessor<ItemStack> SPELLBOOK = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> EQUIPMENT_RENDER_MASK = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> COSMETIC_HEAD = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> COSMETIC_CHEST = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> COSMETIC_LEGS = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> COSMETIC_FEET = SynchedEntityData
            .defineId(AbstractHumanCompanionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final int ALL_EQUIPMENT_RENDER_MASK = (1 << 6) - 1;
    private static final ResourceLocation MOD_MORALE_DAMAGE = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "morale_damage");
    private static final ResourceLocation MOD_MORALE_ARMOR = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "morale_armor");
    private static final ResourceLocation MOD_TRAIT_QUICKSTEP = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_quickstep_speed");
    private static final ResourceLocation MOD_TRAIT_STALWART = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_stalwart_kb");
    private static final ResourceLocation MOD_TRAIT_RECKLESS = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_reckless_speed");
    private static final ResourceLocation MOD_TRAIT_BRAVE = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_brave_damage");
    private static final ResourceLocation MOD_TRAIT_GUARDIAN = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_guardian_armor");
    private static final ResourceLocation MOD_TRAIT_DEVOTED = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_devoted_armor");
    private static final ResourceLocation MOD_TRAIT_NIGHT_OWL_DAMAGE = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_night_owl_damage");
    private static final ResourceLocation MOD_TRAIT_NIGHT_OWL_SPEED = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_night_owl_speed");
    private static final ResourceLocation MOD_TRAIT_SUN_DAMAGE = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_sun_damage");
    private static final ResourceLocation MOD_TRAIT_SUN_SPEED = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_sun_speed");
    private static final ResourceLocation MOD_TRAIT_MELANCHOLIC = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "trait_melancholic_penalty");
    private static final int FOOD_REQUEST_COOLDOWN_TICKS = 600; // ~30s between requests
    private static final int STAMINA_MAX_DEFAULT = 100;
    private static final int MANA_MAX_DEFAULT = 100;
    private static final int SPRINT_RESUME_STAMINA = 15;

    // Seven visible rows in the companion menu; saved inventories keep their existing slot indices.
    protected final SimpleContainer inventory = new SimpleContainer(63) {
        @Override
        public int getMaxStackSize(ItemStack stack) {
            return AbstractHumanCompanionEntity.this.getInventoryStackLimit(stack, super.getMaxStackSize(stack));
        }
    };
    // Vanilla LivingEntity equipment is the single source of truth; only manual locks need extra state.
    private final boolean[] manuallyEquipped = new boolean[6];
    private ItemStack savedOffhand = ItemStack.EMPTY;
    /** Position of the temporary invisible light block used while a torch or lantern is held. */
    @Nullable
    private BlockPos heldLightPos;
    protected final Map<Item, Integer> foodRequirements = new LinkedHashMap<>();
    private boolean resourceRequirementResolved;
    private boolean untamedGreetingPlayed;
    private boolean renderingEquipment;
    protected final Random rand = new Random();

    public PatrolGoal patrolGoal;
    public MoveBackToPatrolGoal moveBackGoal;
    public com.majorbonghits.moderncompanions.entity.job.LumberjackJobGoal lumberjackGoal;
    public com.majorbonghits.moderncompanions.entity.job.ChefJobGoal chefGoal;
    private int lastFoodRequestTick = -200;
    private int specialistAttr = -1; // 0=STR,1=DEX,2=INT,3=END; -1 none

    private int totalExperience;
    private float experienceProgress;
    private int lastLevelUpTime;

    private final CompanionPersonality personality = new CompanionPersonality();
    private final EnumMap<ModSounds.Cue, Integer> voiceCooldowns = new EnumMap<>(ModSounds.Cue.class);
    // Cross-cue lock keeps independent events from starting overlapping voice playback.
    private int voiceLockUntilTick = Integer.MIN_VALUE;
    private int bondTickCounter = 0;
    private int lastNearDeathTick = -200;
    private int personalityRefreshTicker = 0;
    private double lastTrackX;
    private double lastTrackY;
    private double lastTrackZ;
    private double distanceAccumulator;
    private static final long AGE_INTERVAL_TICKS = 90L * 24000L; // 90 in-game days (~3 months) per year

    private int equipmentStrengthBonus;
    private int equipmentDexterityBonus;
    private ItemStack cachedPatrolWeapon = ItemStack.EMPTY; // weapon the companion held before swapping to job tool
    private ItemStack cachedPatrolTool = ItemStack.EMPTY;   // tool currently borrowed from inventory while patrolling
    private int cachedPatrolToolSlot = -1;                  // original slot of the borrowed tool so we can return it
    private int equipmentIntelligenceBonus;
    private int equipmentEnduranceBonus;
    // Miner persistent memory: ore positions catalogued during patrol session
    private java.util.List<BlockPos> minerOreMemory = new java.util.ArrayList<>();
    private int minerOreIndex = 0;
    private BlockPos minerPlanCenter = BlockPos.ZERO;
    private int minerPlanRadius = 0;
    private int minerPlanUp = 0;
    private int minerPlanDown = 0;

    private net.minecraft.world.level.ChunkPos forcedChestChunk;
    private ResourceKey<Level> forcedChestDimension;
    private long lastCourierMessageTick = -200L;
    private boolean forceDeliverRequest;
    private long deliveryRetryAt;
    private long lastDeliveryGameTime = -24000L;
    // Only durable job facts survive saves; goals rebuild paths and scan cursors on resume.
    private final JobPlan jobPlan = new JobPlan();
    private int committedSwimTicks;
    private net.minecraft.world.phys.Vec3 committedSwimDir = net.minecraft.world.phys.Vec3.ZERO;

    // The assigned mount is durable; the owner vehicle UUID only tracks the current travel pair.
    @Nullable private UUID assignedMountId;
    @Nullable private UUID ridingWithOwnerMountId;
    // Player vehicle/passenger links can be restored over several server ticks after login.
    private int mountReconciliationTicks;
    // Only fences placed by this companion are eligible for automatic no-drop cleanup.
    @Nullable private BlockPos temporaryMountFencePos;
    @Nullable private BlockState temporaryMountFenceState;

    // Client-side tracking of the last swing tick we already applied locally.
    private int lastAppliedSwingTick = -1;
    private int combatGraceTicks;
    private int lastExhaustedMeleeTick = -100;

    private static final ResourceLocation PREFERRED_WEAPON_MOD = ResourceLocation.fromNamespaceAndPath(
            com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, "preferred_weapon_bonus");

    protected AbstractHumanCompanionEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setTame(false, false);
        if (this.getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
            nav.setCanFloat(true);
        }
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new CompanionGroundPathNavigation(this, level);
    }

    /* ---------- Registration ---------- */

    public static AttributeSupplier.Builder createAttributes() {
        double baseHealth = ModConfig.BASE_HEALTH != null ? ModConfig.safeGet(ModConfig.BASE_HEALTH).doubleValue()
                : 20.0D;
        // Give every loaded attribute an instance so equipment from optional mods can modify companions.
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        for (var entry : BuiltInRegistries.ATTRIBUTE.entrySet()) {
            ResourceKey<net.minecraft.world.entity.ai.attributes.Attribute> key = entry.getKey();
            BuiltInRegistries.ATTRIBUTE.getHolder(key)
                    .ifPresent(holder -> builder.add(holder, entry.getValue().getDefaultValue()));
        }
        return builder
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MAX_HEALTH, baseHealth)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_SPEED, 1.6D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_VARIANT, 0);
        builder.define(SEX, 0);
        builder.define(VOICE_ACTOR, "");
        builder.define(BASE_HEALTH, ModConfig.safeGet(ModConfig.BASE_HEALTH));
        builder.define(EXP_LVL, 0);
        builder.define(EATING, false);
        builder.define(ALERT, false);
        builder.define(HUNTING, false);
        builder.define(PATROLLING, false);
        builder.define(FOLLOWING, false);
        builder.define(GUARDING, false);
        builder.define(SPRINT_ENABLED, false);
        builder.define(PICKUP_ITEMS, true);
        builder.define(ALLOW_VILLAGER_HARM, false);
        builder.define(ALLOW_PLAYER_HARM, false);
        builder.define(PATROL_POS, Optional.empty());
        builder.define(PATROL_RADIUS, 10);
        builder.define(DELIVERY_CHEST, Optional.empty());
        builder.define(DELIVERY_DIMENSION, "");
        if (minerOreMemory == null) minerOreMemory = new java.util.ArrayList<>();
        minerOreMemory.clear();
        minerOreIndex = 0;
        builder.define(FOOD1, "");
        builder.define(FOOD2, "");
        builder.define(FAVORITE_FOOD, "");
        builder.define(FOOD1_AMT, 0);
        builder.define(FOOD2_AMT, 0);
        builder.define(RECRUITMENT_REQUIREMENTS, "");
        builder.define(EXP_PROGRESS, 0.0F);
        builder.define(STR, 4);
        builder.define(DEX, 4);
        builder.define(INTL, 4);
        builder.define(END, 4);
        builder.define(SPECIALIST, -1);
        builder.define(KILL_COUNT, 0);
        builder.define(PRIMARY_TRAIT, "");
        builder.define(SECONDARY_TRAIT, "");
        builder.define(BOND_LEVEL, 0);
        builder.define(BOND_XP, 0);
        builder.define(BACKSTORY_ID, "");
        builder.define(MORALE, 0.0F);
        builder.define(RESURRECT_COUNT, 0);
        builder.define(FIRST_TAMED_TIME, -1L);
        builder.define(DIST_TRAVELED, 0L);
        builder.define(MAJOR_KILLS, 0);
        builder.define(AGE_YEARS, 0);
        builder.define(CUSTOM_SKIN_URL, "");
        builder.define(CUSTOM_BIO, "");
        builder.define(ALEX_MODEL, false);
        builder.define(LAST_SWING_TICK, 0);
        builder.define(JOB_ID, CompanionJob.NONE.id());
        builder.define(WORK_ENABLED, false);
        builder.define(JOB_STATUS, "job_status.modern_companions.idle");
        builder.define(MINER_ORES_COUNTED, 0);
        builder.define(MINER_ORES_MINED, 0);
        builder.define(MINER_ORES_LIFETIME, 0);
        builder.define(LUMBER_LOGS_SESSION, 0);
        builder.define(LUMBER_LOGS_LIFETIME, 0);
        builder.define(FISH_CAUGHT_SESSION, 0);
        builder.define(FISH_CAUGHT_LIFETIME, 0);
        builder.define(FARMER_HARVESTED_SESSION, 0);
        builder.define(FARMER_HARVESTED_LIFETIME, 0);
        builder.define(FARMER_PLANTED_SESSION, 0);
        builder.define(FARMER_PLANTED_LIFETIME, 0);
        builder.define(HUNTER_KILLS_SESSION, 0);
        builder.define(HUNTER_KILLS_LIFETIME, 0);
        builder.define(CHEF_COOKED_SESSION, 0);
        builder.define(CHEF_COOKED_LIFETIME, 0);
        builder.define(STAMINA_MAX, STAMINA_MAX_DEFAULT);
        builder.define(STAMINA, STAMINA_MAX_DEFAULT);
        builder.define(MANA_MAX, MANA_MAX_DEFAULT);
        builder.define(MANA, MANA_MAX_DEFAULT);
        builder.define(SPELLBOOK, ItemStack.EMPTY);
        builder.define(EQUIPMENT_RENDER_MASK, ALL_EQUIPMENT_RENDER_MASK);
        builder.define(COSMETIC_HEAD, ItemStack.EMPTY);
        builder.define(COSMETIC_CHEST, ItemStack.EMPTY);
        builder.define(COSMETIC_LEGS, ItemStack.EMPTY);
        builder.define(COSMETIC_FEET, ItemStack.EMPTY);
    }

    @Override
    public void swing(InteractionHand hand) {
        swing(hand, true);
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        // Bypass the vanilla guard that ignores swings if an earlier one hasn't reached
        // mid-animation.
        // Reset state first so rapid consecutive hits always restart the swing
        // animation locally and in packets.
        this.swinging = false;
        this.swingTime = -1;
        super.swing(hand, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new EatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new HunterCombatGoal(this));
        this.goalSelector.addGoal(2, new FirearmAttackGoal(this));
        this.goalSelector.addGoal(2, new AvoidCreeperGoal(this, 1.5D, 1.5D));
        this.goalSelector.addGoal(3, new MoveBackToGuardGoal(this));
        this.goalSelector.addGoal(3, new CustomFollowOwnerGoal(this, followSpeed(), true));
        this.goalSelector.addGoal(4, new DeliverToChestGoal(this, 1.1D));
        if (ModConfig.safeGet(ModConfig.JOB_LUMBERJACK_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_LUMBERJACK_RADIUS);
            this.lumberjackGoal = new LumberjackJobGoal(this, radius, true);
            this.goalSelector.addGoal(5, lumberjackGoal);
        }
        if (ModConfig.safeGet(ModConfig.JOB_MINER_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_MINER_RADIUS);
            this.goalSelector.addGoal(6, new MinerJobGoal(this, radius, true));
        }
        if (ModConfig.safeGet(ModConfig.JOB_FISHER_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_FISHER_RADIUS);
            this.goalSelector.addGoal(7, new FisherJobGoal(this, radius, true));
        }
        if (ModConfig.safeGet(ModConfig.JOB_CHEF_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_CHEF_RADIUS);
            this.chefGoal = new ChefJobGoal(this, radius, true);
            this.goalSelector.addGoal(8, chefGoal);
        }
        if (ModConfig.safeGet(ModConfig.JOB_FARMER_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_FARMER_RADIUS);
            this.goalSelector.addGoal(9, new FarmerJobGoal(this, radius, true));
        }
        if (ModConfig.safeGet(ModConfig.JOB_HUNTER_ENABLED)) {
            int radius = ModConfig.safeGet(ModConfig.JOB_HUNTER_RADIUS);
            this.goalSelector.addGoal(10, new HunterJobGoal(this, radius, true));
        }
        this.goalSelector.addGoal(11, new CustomWaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(13, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(14, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(15, new LowHealthGoal(this));
        patrolGoal = new PatrolGoal(this, 60, getPatrolRadius());
        moveBackGoal = new MoveBackToPatrolGoal(this, getPatrolRadius());
        this.goalSelector.addGoal(3, moveBackGoal);
        this.goalSelector.addGoal(3, patrolGoal);

        this.targetSelector.addGoal(1, new CustomOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CustomOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new CustomHurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new HuntGoal(this));
        this.targetSelector.addGoal(5, new AlertGoal(this));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (level().isClientSide) {
            if (key.equals(PRIMARY_TRAIT) || key.equals(SECONDARY_TRAIT) || key.equals(BOND_XP)
                    || key.equals(BOND_LEVEL) || key.equals(BACKSTORY_ID) || key.equals(MORALE)
                    || key.equals(RESURRECT_COUNT) || key.equals(FIRST_TAMED_TIME) || key.equals(DIST_TRAVELED)
                    || key.equals(MAJOR_KILLS) || key.equals(AGE_YEARS)) {
                personality.setPrimaryTrait(this.entityData.get(PRIMARY_TRAIT));
                personality.setSecondaryTrait(this.entityData.get(SECONDARY_TRAIT));
                personality.setBondXp(this.entityData.get(BOND_XP));
                personality.setBondLevel(this.entityData.get(BOND_LEVEL));
                personality.setBackstoryId(this.entityData.get(BACKSTORY_ID));
                personality.setMorale(this.entityData.get(MORALE));
                personality.setFirstTamedGameTime(this.entityData.get(FIRST_TAMED_TIME));
                personality.addDistanceTraveled(this.entityData.get(DIST_TRAVELED) - personality.getDistanceTraveledWithOwner());
                personality.setMajorKills(this.entityData.get(MAJOR_KILLS));
                personality.setAgeYears(this.entityData.get(AGE_YEARS));
                // sync resurrection count (monotonic)
                int target = this.entityData.get(RESURRECT_COUNT);
                while (personality.getTimesResurrected() < target) {
                    personality.noteResurrection();
                }
            }
        }
    }

    /* ---------- Flags & helpers ---------- */

    /**
     * Current job assignment. Job enum lives in a dedicated package so UI, NBT,
     * and AI goals can share the same identifiers. Set via SetCompanionJobPayload
     * from the CompanionScreen cycle button.
     */
    public CompanionJob getJob() {
        return CompanionJob.fromId(this.entityData.get(JOB_ID));
    }

    public void setJob(CompanionJob job) {
        CompanionJob safeJob = job == null ? CompanionJob.NONE : job;
        if (safeJob != getJob()) {
            net.minecraft.nbt.Tag preservedReplantDebt = null;
            CompoundTag existingPayload = jobPlan.payload();
            if (existingPayload.contains("ReplantDebt", 9)) {
                preservedReplantDebt = existingPayload.get("ReplantDebt").copy();
            }
            if (this.level() instanceof ServerLevel level) {
                JobReservations.release(level, this.getUUID());
            }
            this.entityData.set(WORK_ENABLED, false);
            restoreCachedWeapon();
            jobPlan.setJob(safeJob);
            clearJobCheckpoint();
            // Job changes clear incompatible active work, but a Lumberjack's
            // confirmed replant debt remains a durable settlement obligation.
            if (preservedReplantDebt != null) {
                CompoundTag payload = jobPlan.payload();
                payload.put("ReplantDebt", preservedReplantDebt);
                jobPlan.setPayload(payload);
            }
        }
        this.entityData.set(JOB_ID, safeJob.id());
        jobPlan.setJob(safeJob);
        if (safeJob == CompanionJob.NONE) {
            this.entityData.set(WORK_ENABLED, false);
            this.entityData.set(JOB_STATUS, "job_status.modern_companions.idle");
        }
    }

    /** Server-synchronized profession gate; paused work retains each goal's checkpoint. */
    public boolean isWorkEnabled() {
        return getJob() != CompanionJob.NONE && this.entityData.get(WORK_ENABLED);
    }

    public void setWorkEnabled(boolean enabled) {
        boolean active = enabled && getJob() != CompanionJob.NONE;
        this.entityData.set(WORK_ENABLED, active);
        if (active) {
            // Work owns movement; player Follow/Patrol orders must not compete with job goals.
            setFollowing(false);
            setPatrolling(false);
            setGuarding(false);
            cacheCurrentJobWeapon();
            equipJobToolIfNeeded();
            setJobStatus(getWorkCenter().isPresent() ? "job_status.modern_companions.searching" : "job_status.modern_companions.assign_chest");
        } else if (getJob() != CompanionJob.NONE) {
            this.getNavigation().stop();
            if (getJob() == CompanionJob.HUNTER) setHunting(false);
            // Claims are transient execution locks; Work OFF preserves the plan but releases them.
            if (this.level() instanceof ServerLevel level) JobReservations.release(level, this.getUUID());
            checkpointJob(JobPhase.PAUSED, jobPlan.target());
            restoreCachedWeapon();
            setJobStatus("job_status.modern_companions.paused");
        }
    }

    /** Jobs use their Assignment-Wand chest as center; patrol radius remains Work radius. */
    public Optional<BlockPos> getWorkCenter() {
        Optional<ResourceKey<Level>> dimension = getAssignedChestDimension();
        if (this.level() == null || dimension.isEmpty() || !this.level().dimension().equals(dimension.get())) return Optional.empty();
        return getAssignedChest();
    }

    public boolean isInWorkArea(BlockPos pos) {
        return getWorkCenter().filter(center -> center.distSqr(pos) <= (long) getPatrolRadius() * getPatrolRadius()).isPresent();
    }

    public JobPhase getJobCheckpointPhase() {
        return jobPlan.phase();
    }

    public Optional<BlockPos> getJobCheckpointTarget() {
        return Optional.ofNullable(jobPlan.target());
    }

    public void checkpointJob(JobPhase phase, @Nullable BlockPos target) {
        jobPlan.checkpoint(phase, target, null, null, this.blockPosition());
    }

    /** Updates the durable plan while retaining the caller's real return point. */
    public void checkpointJob(JobPhase phase, @Nullable BlockPos target, @Nullable BlockPos stand,
                              @Nullable String identity) {
        jobPlan.checkpoint(phase, target, stand, identity, this.blockPosition());
    }

    public Optional<BlockPos> getJobCheckpointStand() {
        return Optional.ofNullable(jobPlan.stand());
    }

    public Optional<BlockPos> getJobReturnPosition() {
        return Optional.ofNullable(jobPlan.returnPosition());
    }

    /** Persists a nearby safe return stand selected after the original feet cell changes. */
    public void setJobReturnPosition(@Nullable BlockPos position) {
        jobPlan.setReturnPosition(position);
    }

    public JobPhase getJobPreDeliveryPhase() {
        return jobPlan.preDeliveryPhase();
    }

    public Optional<BlockPos> getJobPreDeliveryTarget() {
        return Optional.ofNullable(jobPlan.preDeliveryTarget());
    }

    public Optional<BlockPos> getJobPreDeliveryStand() {
        return Optional.ofNullable(jobPlan.preDeliveryStand());
    }

    public boolean isJobReturnPending() {
        return jobPlan.deliveryReturnPending();
    }

    public CompoundTag getJobPlanPayload() {
        return jobPlan.payload();
    }

    public void setJobPlanPayload(CompoundTag payload) {
        jobPlan.setPayload(payload);
    }

    /** Saves the pre-delivery work unit before the courier takes control. */
    public void beginJobDelivery() {
        jobPlan.beginDelivery(jobPlan.phase(), jobPlan.target(), jobPlan.stand(), jobPlan.identity(), this.blockPosition());
    }

    /** Moves the saved profession target into the explicit post-delivery return phase. */
    public void finishJobDelivery() {
        jobPlan.restoreAfterDelivery();
        checkpointJob(JobPhase.RETURNING, jobPlan.target(), jobPlan.stand(), jobPlan.identity());
    }

    /** Marks the saved checkpoint reached so the profession can resume its prior phase. */
    public void finishJobReturn() {
        JobPhase restoredPhase = jobPlan.completeDeliveryReturn();
        if (restoredPhase == JobPhase.SEARCHING && jobPlan.target() == null) {
            // Do not route this through checkpointJob(SEARCHING, null): that
            // terminal helper clears profession payloads such as replant debt.
            return;
        }
        checkpointJob(restoredPhase, jobPlan.target(), jobPlan.stand(), jobPlan.identity());
    }

    private void clearJobCheckpoint() {
        jobPlan.clearWorkUnit();
        jobPlan.checkpoint(JobPhase.SEARCHING, null, null, null, null);
    }

    public String getJobStatus() {
        return this.entityData.get(JOB_STATUS);
    }

    public void setJobStatus(String status) {
        String safeStatus = status == null || status.isBlank()
                ? "job_status.modern_companions.idle"
                : status.substring(0, Math.min(128, status.length()));
        this.entityData.set(JOB_STATUS, safeStatus);
        if (jobPlan.phase() == JobPhase.PAUSED || jobPlan.phase() == JobPhase.WAITING) {
            jobPlan.setWaitingReason(safeStatus);
        }
    }

    public Component getJobStatusComponent() {
        String status = getJobStatus();
        return status.startsWith("job_status.") ? Component.translatable(status) : Component.translatable("job_status.modern_companions.idle");
    }

    /**
     * Re-sync any secondary flags that depend on job selection. This keeps legacy
     * hunt/alert toggles aligned with the new job pipeline until the dedicated job
     * goals take over more behavior.
     */
    public void onJobChanged() {
        onJobChanged(true);
    }

    /** Reconcile a loaded job without treating the saved assignment as a new selection. */
    private void onJobChanged(boolean resetSessionStats) {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        CompanionJob job = getJob();
        if (job == CompanionJob.NONE) {
            // NONE is an explicit return to regular companion behavior.
            setPatrolling(false);
            setGuarding(false);
            setWorkEnabled(false);
        } else {
            // Job territory is bound later by Assignment Wand, never by an independent Patrol order.
            setPatrolling(false);
            setFollowing(false);
            equipJobToolIfNeeded();
            if (!isWorkEnabled()) setJobStatus("job_status.modern_companions.paused");
        }
        if (job != CompanionJob.HUNTER && isHunting()) {
            setHunting(false);
        }

        if (!resetSessionStats) return;

        // Reset per-session stats when a player selects a new job. Save loading
        // uses the non-resetting path so a relog cannot erase the session view.
        switch (job) {
            case MINER -> {
                setMinerOresCounted(0);
                setMinerOresMined(0);
            }
            case LUMBERJACK -> setLumberLogsSession(0);
            case FISHER -> setFishCaughtSession(0);
            case FARMER -> {
                setFarmerHarvestedSession(0);
                setFarmerPlantedSession(0);
            }
            case HUNTER -> setHunterKillsSession(0);
            case CHEF -> setChefCookedSession(0);
            default -> {
            }
        }
    }

    /* ---------- Courier / chest assignment ---------- */

    public Optional<BlockPos> getAssignedChest() {
        return this.entityData.get(DELIVERY_CHEST);
    }

    public Optional<ResourceKey<Level>> getAssignedChestDimension() {
        String raw = this.entityData.get(DELIVERY_DIMENSION);
        if (raw == null || raw.isBlank()) return Optional.empty();
        try {
            return Optional.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(raw)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public void assignDeliveryChest(ServerLevel level, BlockPos pos) {
        releaseDeliveryChunkTicket(level);
        this.entityData.set(DELIVERY_CHEST, Optional.of(pos.immutable()));
        this.entityData.set(DELIVERY_DIMENSION, level.dimension().location().toString());
        refreshDeliveryChunkTicket(level);
    }

    public void clearDeliveryChest(ServerLevel level) {
        releaseDeliveryChunkTicket(level);
        this.entityData.set(DELIVERY_CHEST, Optional.empty());
        this.entityData.set(DELIVERY_DIMENSION, "");
    }

    public boolean hasDeliverableCargo() {
        List<ItemStack> reservedEquipment = collectEquippedStacks();
        int foodRation = getJob().isWorker() ? 2 : Integer.MAX_VALUE;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (reserveForEquippedCopy(stack, reservedEquipment)) continue;
            if (CompanionData.isFood(stack)) {
                if (stack.getCount() > foodRation) return true;
                foodRation -= stack.getCount();
                continue;
            }
            if (shouldRetainForUse(stack) || getJob() == CompanionJob.LUMBERJACK && isSaplingItem(stack)) continue;
            return true;
        }
        return false;
    }

    public boolean isInventoryFull() {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            if (this.inventory.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean requestImmediateDelivery(@Nullable ServerPlayer requester) {
        if (getAssignedChest().isEmpty()) {
            if (requester != null) {
                requester.sendSystemMessage(Component.translatable("message.modern_companions.courier.no_chest"));
            }
            return false;
        }
        this.forceDeliverRequest = true;
        this.setTarget(null);
        if (this.getNavigation() != null) {
            this.getNavigation().stop();
        }
        return true;
    }

    public DeliveryResult deliverInventoryToChest(ServerLevel level, BlockPos chestPos) {
        Container container = resolveChestContainer(level, chestPos);
        List<net.neoforged.neoforge.items.IItemHandler> handlers = itemHandlers(level, chestPos);
        if (container == null && handlers.isEmpty()) {
            return DeliveryResult.MISSING;
        }

        List<ItemStack> reservedEquipment = collectEquippedStacks();
        boolean movedAny = false;

        int foodRation = getJob().isWorker() ? 2 : Integer.MAX_VALUE;
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (reserveForEquippedCopy(stack, reservedEquipment)) continue;

            int keep = retainedFoodCount(stack, foodRation);
            if (keep > 0) foodRation -= keep;
            if (keep >= stack.getCount() || shouldRetainForUse(stack)) continue;
            if (getJob() == CompanionJob.LUMBERJACK && isSaplingItem(stack)) continue;

            ItemStack toMove = stack.copyWithCount(stack.getCount() - keep);
            ItemStack remainder = toMove;
            for (net.neoforged.neoforge.items.IItemHandler handler : handlers) {
                remainder = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handler, remainder, false);
                if (remainder.isEmpty()) break;
            }
            if (!handlers.isEmpty() && container != null && !remainder.isEmpty()) {
                remainder = insertIntoContainer(container, remainder);
            } else if (handlers.isEmpty()) {
                remainder = insertIntoContainer(container, remainder);
            }
            if (remainder.getCount() != toMove.getCount()) {
                movedAny = true;
            }
            this.inventory.setItem(i, remainder.isEmpty() ? ItemStack.EMPTY : remainder.copyWithCount(remainder.getCount() + keep));
        }

        if (container instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
        this.inventory.setChanged();

        this.lastDeliveryGameTime = level.getGameTime();
        DeliveryResult result = movedAny
                ? (hasDeliverableCargo() ? DeliveryResult.PARTIAL : DeliveryResult.SUCCESS)
                : DeliveryResult.FULL;
        onDeliveryFinished(result);

        return result;
    }

    /** Caller must already be at an approved chest stand; this only performs one safe inventory transfer. */
    public ItemStack withdrawOneFromChest(ServerLevel level, BlockPos chestPos, java.util.function.Predicate<ItemStack> accepted) {
        if (!level.isLoaded(chestPos)) return ItemStack.EMPTY;
        List<net.neoforged.neoforge.items.IItemHandler> handlers = itemHandlers(level, chestPos);
        if (!handlers.isEmpty()) {
            for (net.neoforged.neoforge.items.IItemHandler sidedHandler : handlers) {
                for (int slot = 0; slot < sidedHandler.getSlots(); slot++) {
                    ItemStack stack = sidedHandler.getStackInSlot(slot);
                    if (!stack.isEmpty() && accepted.test(stack) && canStoreInInventory(stack)) {
                        ItemStack extracted = sidedHandler.extractItem(slot, 1, false);
                        if (!extracted.isEmpty()) return extracted;
                    }
                }
            }
            return ItemStack.EMPTY;
        }
        Container container = resolveChestContainer(level, chestPos);
        if (container == null) return ItemStack.EMPTY;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty() && accepted.test(stack) && canStoreInInventory(stack)) {
                return container.removeItem(slot, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    /** Prefer the canonical handler view and preserve sided rules when no unsided view exists. */
    private List<net.neoforged.neoforge.items.IItemHandler> itemHandlers(ServerLevel level, BlockPos pos) {
        List<net.neoforged.neoforge.items.IItemHandler> handlers = new ArrayList<>();
        net.neoforged.neoforge.items.IItemHandler unsided = level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, pos, null);
        // Adding every sided view alongside an unsided view can expose the
        // same slot multiple times on modded storage.
        if (unsided != null) return List.of(unsided);
        for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
            net.neoforged.neoforge.items.IItemHandler sided = level.getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, pos, side);
            if (sided != null && !handlers.contains(sided)) handlers.add(sided);
        }
        return handlers;
    }

    private Container resolveChestContainer(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            Container chest = ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, pos, true);
            if (chest != null) return chest;
        }
        var be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            return container;
        }
        return null;
    }

    private void onDeliveryFinished(DeliveryResult result) {
        if (result == DeliveryResult.SUCCESS) {
            clearForceDeliverRequest();
            CompanionVoice.play(this, ModSounds.Cue.COMPLETION);
        }
    }

    public void refreshDeliveryChunkTicket(ServerLevel level) {
        if (!ModConfig.safeGet(ModConfig.JOB_ASSIGNED_CHESTS_CHUNKLOAD)) {
            releaseDeliveryChunkTicket(level);
            return;
        }
        Optional<BlockPos> pos = getAssignedChest();
        Optional<ResourceKey<Level>> dim = getAssignedChestDimension();
        if (pos.isEmpty() || dim.isEmpty()) return;
        if (!level.dimension().equals(dim.get())) return;

        ChunkPos chunkPos = new ChunkPos(pos.get());
        releaseDeliveryChunkTicket(level);
        level.getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
        forcedChestChunk = chunkPos;
        forcedChestDimension = dim.get();
    }

    private void releaseDeliveryChunkTicket(@Nullable ServerLevel level) {
        if (forcedChestChunk == null || level == null) return;
        if (forcedChestDimension != null && !level.dimension().equals(forcedChestDimension)) return;
        level.getChunkSource().removeRegionTicket(TicketType.FORCED, forcedChestChunk, 1, forcedChestChunk);
        forcedChestChunk = null;
        forcedChestDimension = null;
    }

    public void alertChestUnloaded() {
        notifyCourierOwnerText(Component.translatable("message.modern_companions.courier.unloaded"));
    }

    public void notifyCourierOwnerText(Component message) {
        notifyCourierOwner(message, 80);
    }

    public boolean isForceDeliverRequested() {
        return this.forceDeliverRequest;
    }

    public long getDeliveryRetryAt() {
        return deliveryRetryAt;
    }

    public void deferDelivery(long gameTime) {
        deliveryRetryAt = Math.max(deliveryRetryAt, gameTime);
    }

    public void clearForceDeliverRequest() {
        this.forceDeliverRequest = false;
        this.deliveryRetryAt = 0L;
    }

    private void notifyCourierOwner(Component message, int cooldownTicks) {
        if (!(this.level() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        if (now - lastCourierMessageTick < cooldownTicks) return;
        lastCourierMessageTick = now;
        if (this.getOwner() instanceof ServerPlayer owner) {
            owner.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), message));
        }
    }

    private List<ItemStack> collectEquippedStacks() {
        List<ItemStack> equipped = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = this.getFunctionalEquipmentItem(slot);
            if (!stack.isEmpty()) {
                equipped.add(stack.copy());
            }
        }
        return equipped;
    }

    private boolean reserveForEquippedCopy(ItemStack stack, List<ItemStack> reserved) {
        for (int i = 0; i < reserved.size(); i++) {
            ItemStack equipped = reserved.get(i);
            if (ItemStack.isSameItemSameComponents(stack, equipped)) {
                reserved.remove(i);
                return true;
            }
        }
        return false;
    }

    private ItemStack insertIntoContainer(Container container, ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack target = container.getItem(slot);
            if (target.isEmpty()) {
                int move = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), container.getMaxStackSize()));
                ItemStack placed = remaining.copyWithCount(move);
                container.setItem(slot, placed);
                remaining.shrink(move);
                continue;
            }
            if (ItemStack.isSameItemSameComponents(target, remaining)) {
                int max = Math.min(target.getMaxStackSize(), container.getMaxStackSize());
                int space = max - target.getCount();
                if (space > 0) {
                    int move = Math.min(space, remaining.getCount());
                    target.grow(move);
                    remaining.shrink(move);
                }
            }
        }
        return remaining;
    }

    public enum DeliveryResult {
        SUCCESS,
        PARTIAL,
        FULL,
        MISSING
    }

    public long getLastDeliveryGameTime() {
        return lastDeliveryGameTime;
    }

    private boolean shouldRetainForUse(ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (CompanionData.isFood(stack)) return getJob() == CompanionJob.NONE;
        if (stack.has(net.minecraft.core.component.DataComponents.POTION_CONTENTS)) return true;
        if (isMainHandWeapon(stack) || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            return true; // keep primary weapons
        }
        return false;
    }

    private boolean isSaplingItem(ItemStack stack) {
        var block = net.minecraft.world.level.block.Block.byItem(stack.getItem());
        if (block == net.minecraft.world.level.block.Blocks.AIR) return false;
        BlockState state = block.defaultBlockState();
        return state.is(BlockTags.SAPLINGS);
    }

    private void boostWaterMovement() {
        if (!this.isInWater()) {
            if (this.isSwimming()) this.setSwimming(false);
            committedSwimTicks = 0;
            return;
        }
        this.setSwimming(true);
        var grace = this.getEffect(MobEffects.DOLPHINS_GRACE);
        if (grace == null || grace.getDuration() <= 20) {
            this.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, true, false, false));
        }

        if (committedSwimTicks <= 0) {
            committedSwimTicks = 200; // ~10 seconds before reconsidering
            committedSwimDir = currentSwimVector();
        }

        if (committedSwimTicks > 0) {
            double push = 1.25D;
            this.getMoveControl().setWantedPosition(
                    this.getX() + committedSwimDir.x * 1.8D,
                    this.getY() + committedSwimDir.y * 0.15D,
                    this.getZ() + committedSwimDir.z * 1.8D,
                    push);
        }
    }

    private void tickCommittedSwim() {
        if (committedSwimTicks > 0) {
            committedSwimTicks--;
            if (this.getNavigation() instanceof GroundPathNavigation nav && !nav.isDone()) {
                nav.setSpeedModifier(1.25D);
            }
        }
    }

    private net.minecraft.world.phys.Vec3 currentSwimVector() {
        if (this.getNavigation() != null && this.getNavigation().getPath() != null) {
            var path = this.getNavigation().getPath();
            if (!path.isDone()) {
                var next = path.getNextNodePos();
                if (next != null) {
                    net.minecraft.world.phys.Vec3 dir = net.minecraft.world.phys.Vec3.atCenterOf(next).subtract(this.position());
                    if (dir.lengthSqr() > 0.0001D) {
                        return dir.normalize();
                    }
                }
            }
        }
        net.minecraft.world.phys.Vec3 look = this.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return new net.minecraft.world.phys.Vec3(0, 0, 1);
        }
        return look.normalize();
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setFollowing(boolean value) {
        this.entityData.set(FOLLOWING, value);
    }

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    public void setPatrolling(boolean value) {
        boolean was = this.entityData.get(PATROLLING);
        this.entityData.set(PATROLLING, value);
        if (!was && value) {
            cachePatrolWeapon();
            equipJobToolIfNeeded();
        } else if (was && !value) {
            restoreCachedWeapon();
        }
    }

    public boolean isGuarding() {
        return this.entityData.get(GUARDING);
    }

    public void setGuarding(boolean value) {
        this.entityData.set(GUARDING, value);
    }

    private void cachePatrolWeapon() {
        ItemStack main = this.getMainHandItem();
        cachedPatrolWeapon = main.isEmpty() ? ItemStack.EMPTY : main.copy();
        if (!main.isEmpty()) {
            // Hold onto a copy while we visually equip tools; clear the hand.
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    private void restoreCachedWeapon() {
        // Tool stayed in inventory; just clear cached refs.
        cachedPatrolTool = ItemStack.EMPTY;
        cachedPatrolToolSlot = -1;

        if (!cachedPatrolWeapon.isEmpty()) {
            // Bypass the active job's tool filter while restoring the pre-work weapon.
            super.setItemSlot(EquipmentSlot.MAINHAND, cachedPatrolWeapon);
            cachedPatrolWeapon = ItemStack.EMPTY;
        }
    }

    private void cacheCurrentJobWeapon() {
        if (!cachedPatrolWeapon.isEmpty()) return;
        ItemStack current = this.getMainHandItem();
        if (!current.isEmpty() && !JobToolPolicy.matches(getJob(), current)) {
            cachedPatrolWeapon = current.copy();
            super.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    private void equipJobToolIfNeeded() {
        CompanionJob job = getJob();
        if (job == CompanionJob.NONE || !(isPatrolling() || isWorkEnabled())) return;
        if (job == CompanionJob.HUNTER && !cachedPatrolWeapon.isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, cachedPatrolWeapon);
            return;
        }
        ItemStack current = this.getMainHandItem();
        if (JobToolPolicy.matches(job, current)) {
            // Already holding a tool; remember it so we can return it later if we missed caching.
            if (cachedPatrolTool.isEmpty()) {
                cachedPatrolTool = current;
            }
            return;
        }
        int slot = findJobToolSlot(job);
        if (slot < 0) return;

        ItemStack tool = this.getInventory().getItem(slot);
        if (tool.isEmpty()) return;

        // Place the same stack in hand without removing it from the inventory slot.
        // Sharing the instance keeps durability updates visible while leaving the tool visible in the GUI.
        this.setItemSlot(EquipmentSlot.MAINHAND, tool);
        cachedPatrolToolSlot = slot;
        cachedPatrolTool = tool;
    }

    /** Reconciles the live hand after inventory/NBT loading without changing the work checkpoint. */
    public void ensureJobToolEquipped() {
        if (getJob().isWorker() && (isPatrolling() || isWorkEnabled())) {
            cacheCurrentJobWeapon();
            equipJobToolIfNeeded();
        }
    }

    private boolean isJobTool(ItemStack stack, CompanionJob job) {
        return JobToolPolicy.matches(job, stack);
    }

    /**
     * Remove a single matching stack from the companion inventory so we avoid duplicating
     * weapons/tools when swapping in and out of patrol mode.
     */
    private boolean removeOneMatchingFromInventory(ItemStack match) {
        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, match)) {
                this.getInventory().removeItem(i, 1);
                return true;
            }
        }
        return false;
    }

    private int findJobToolSlot(CompanionJob job) {
        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (isJobTool(stack, job)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isPickupEnabled() {
        return this.entityData.get(PICKUP_ITEMS);
    }

    public void setPickupEnabled(boolean value) {
        this.entityData.set(PICKUP_ITEMS, value);
    }

    public boolean canHarmVillagers() {
        return this.entityData.get(ALLOW_VILLAGER_HARM);
    }

    public void setCanHarmVillagers(boolean value) {
        this.entityData.set(ALLOW_VILLAGER_HARM, value);
    }

    public boolean canHarmPlayers() {
        return this.entityData.get(ALLOW_PLAYER_HARM);
    }

    public void setCanHarmPlayers(boolean value) {
        this.entityData.set(ALLOW_PLAYER_HARM, value);
    }

    public boolean isSprintEnabled() {
        return this.entityData.get(SPRINT_ENABLED);
    }

    public void setSprintEnabled(boolean value) {
        this.entityData.set(SPRINT_ENABLED, value);
    }

    public boolean isAlert() {
        return this.entityData.get(ALERT);
    }

    public void setAlert(boolean value) {
        this.entityData.set(ALERT, value);
        // Disabling combat must also stop any target selected before the toggle changed.
        if (!value) this.setTarget(null);
    }

    public boolean isHunting() {
        return this.entityData.get(HUNTING);
    }

    public void setHunting(boolean value) {
        this.entityData.set(HUNTING, value);
    }

    public Optional<BlockPos> getPatrolPos() {
        return this.entityData.get(PATROL_POS);
    }

    public void setPatrolPos(@Nullable BlockPos pos) {
        this.entityData.set(PATROL_POS, Optional.ofNullable(pos));
    }

    public int getPatrolRadius() {
        return this.entityData.get(PATROL_RADIUS);
    }

    /**
     * Human-readable class label derived from the entity registry name.
     */
    public Component getClassDisplayName() {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        if (key == null)
            return Component.translatable("entity.modern_companions.companion");
        return Component.translatable("entity." + key.getNamespace() + "." + key.getPath());
    }

    public void setPatrolRadius(int radius) {
        int clamped = Mth.clamp(radius, 1, 128);
        this.entityData.set(PATROL_RADIUS, clamped);
        if (patrolGoal != null)
            patrolGoal.radius = clamped;
        if (moveBackGoal != null)
            moveBackGoal.radius = clamped;
    }

    /* ---------- Companion mount assignment ---------- */

    public Optional<UUID> getAssignedMountId() {
        return Optional.ofNullable(assignedMountId);
    }

    /** Keep a restored passenger relationship alive while the owner's vehicle is reattached. */
    public void scheduleMountReconciliation() {
        mountReconciliationTicks = Math.max(mountReconciliationTicks, 40);
    }

    /** Match the player's native horse seating point; the generic mob fallback is at the feet. */
    @Override
    public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        return vehicle instanceof AbstractHorse ? Player.DEFAULT_VEHICLE_ATTACHMENT
                : super.getVehicleAttachmentPoint(vehicle);
    }

    /** The Assignment Wand uses the same ownership boundary as companion interactions. */
    public boolean canAssignMount(Entity target, Player owner) {
        if (!(target instanceof Mob mount) || !isOwnedSaddledMount(mount, owner) || target == this) {
            return false;
        }
        if (!(mount instanceof Leashable leash) || !leash.canBeLeashed()) return false;
        if (mount.getPassengers().stream().anyMatch(passenger -> passenger instanceof AbstractHumanCompanionEntity
                && passenger != this)) {
            return false;
        }
        if (leash.getLeashHolder() instanceof AbstractHumanCompanionEntity other
                && other != this) {
            return false;
        }
        if (mount.level() instanceof ServerLevel level && isAssignedToOtherCompanion(level, mount)) return false;
        return true;
    }

    public boolean assignMount(ServerLevel level, Mob mount, ServerPlayer requester) {
        if (!canAssignMount(mount, requester)) return false;
        if (Objects.equals(assignedMountId, mount.getUUID())) return false;

        clearAssignedMount(level);
        assignedMountId = mount.getUUID();
        ridingWithOwnerMountId = null;
        if (isOrderedToSit()) {
            if (!anchorMountAtFence(level, requester, mount)) attachMountToCompanion(mount);
        } else {
            attachMountToCompanion(mount);
        }
        return true;
    }

    public boolean clearAssignedMount(ServerLevel level) {
        if (assignedMountId == null && temporaryMountFencePos == null) return false;
        Entity mount = assignedMountId == null ? null : level.getEntity(assignedMountId);
        if (mount != null && this.getVehicle() == mount) {
            restoreMountedMountAi(mount);
            this.stopRiding();
        }
        if (mount instanceof Leashable leash) {
            Entity holder = leash.getLeashHolder();
            if (holder == this || holder instanceof LeashFenceKnotEntity) {
                // The Assignment Wand owns the relationship, not a physical lead item.
                leash.dropLeash(true, false);
            }
        }
        releaseTemporaryMountFence(level, mount instanceof Mob mob ? mob : null);
        assignedMountId = null;
        ridingWithOwnerMountId = null;
        return true;
    }

    /** Movement orders release the sit pose and restore the companion-held leash. */
    public void resumeMovementOrder() {
        if (this.level() instanceof ServerLevel level) {
            Mob mount = resolveAssignedMount(level);
            releaseTemporaryMountFence(level, mount);
            if (isOrderedToSit()) this.setOrderedToSit(false);
            if (mount != null) attachMountToCompanion(mount);
        } else if (isOrderedToSit()) {
            this.setOrderedToSit(false);
        }
    }

    private static boolean isOwnedSaddledMount(Entity target, LivingEntity owner) {
        if (!(target instanceof Mob)
                || !(target instanceof Saddleable saddle)
                || !saddle.isSaddled()
                || !(target instanceof OwnableEntity ownable)
                || !owner.getUUID().equals(ownable.getOwnerUUID())) {
            return false;
        }
        return target.isAlive() && !target.isRemoved();
    }

    @Nullable
    private Mob resolveAssignedMount(ServerLevel level) {
        if (assignedMountId == null) return null;
        LivingEntity owner = getOwner();
        if (owner == null) return null;
        Entity entity = level.getEntity(assignedMountId);
        if (entity instanceof Mob mount && isOwnedSaddledMount(mount, owner)) return mount;
        if (entity != null && !entity.isAlive()) assignedMountId = null;
        return null;
    }

    private void tickCompanionMount() {
        if (!(this.level() instanceof ServerLevel level)
                || !this.isTame()
                || !(this.getOwner() instanceof ServerPlayer owner)
                || owner.level() != this.level()) {
            return;
        }

        boolean reconciliationPending = mountReconciliationTicks > 0;
        if (reconciliationPending) mountReconciliationTicks--;
        Entity ownerVehicle = owner.getVehicle();
        Mob ownerMount = ownerVehicle instanceof Mob mount && isOwnedSaddledMount(mount, owner) ? mount : null;
        boolean ownerMounted = ownerMount != null;
        Entity companionVehicle = this.getVehicle();
        if (companionVehicle != null) {
            if (!(companionVehicle instanceof Mob mount) || !isOwnedSaddledMount(mount, owner)) {
                if (!reconciliationPending) {
                    restoreMountedMountAi(companionVehicle);
                    this.stopRiding();
                    ridingWithOwnerMountId = null;
                }
                return;
            }

            boolean ownerVehicleStillRestoring = reconciliationPending && ridingWithOwnerMountId == null;
            boolean ownerChangedVehicles = ridingWithOwnerMountId != null
                    && (!ownerMounted || !ridingWithOwnerMountId.equals(ownerVehicle.getUUID()));
            if (CompanionMountRules.shouldDismount(true, ownerMounted, ownerVehicleStillRestoring)
                    || ownerChangedVehicles
                    || !isFollowing()
                    || isOrderedToSit()) {
                restoreMountedMountAi(mount);
                this.stopRiding();
                ridingWithOwnerMountId = null;
                if (assignedMountId != null && assignedMountId.equals(mount.getUUID())) {
                    attachMountToCompanion(mount);
                }
            } else if (ownerMounted) {
                if (ridingWithOwnerMountId == null) ridingWithOwnerMountId = ownerVehicle.getUUID();
                if (this.tickCount % 5 == 0) guideMountedMount(mount, ownerVehicle);
            }
            return;
        }

        Mob assignedMount = resolveAssignedMount(level);
        if (isOrderedToSit()) {
            if (assignedMount != null && this.tickCount % 20 == 0) {
                anchorMountAtFence(level, owner, assignedMount);
            }
            return;
        }

        if (!CompanionMountRules.shouldAutoMount(isFollowing(), isOrderedToSit(), ownerMounted,
                false, this.getTarget() != null, owner.level() == this.level())) {
            if (assignedMount != null) attachMountToCompanion(assignedMount);
            return;
        }

        if (this.tickCount % 5 != 0) {
            return;
        }

        // Automatic selection uses a separate horse. An explicitly assigned active horse may
        // share its seat; force-add keeps the player first, preserving native horse control.
        Mob mount = assignedMount != null ? assignedMount : findOwnedMount(level, owner, ownerMount);
        if (mount == null || !hasOnlyOwnerPassengers(mount, owner)
                || mount.distanceToSqr(this) > 256.0D) {
            if (assignedMount != null) attachMountToCompanion(assignedMount);
            return;
        }
        boolean sameOwnerMount = ownerMount != null && ownerMount.getUUID().equals(mount.getUUID());
        boolean boarded = this.startRiding(mount);
        if (!boarded && sameOwnerMount && mount instanceof AbstractHorse) {
            boarded = this.startRiding(mount, true);
        }
        if (boarded) {
            if (assignedMount != null) detachMountLeadForRide(mount);
            this.getNavigation().stop();
            ridingWithOwnerMountId = ownerVehicle.getUUID();
            guideMountedMount(mount, ownerVehicle);
        } else if (assignedMount != null
                && (ownerMount == null || !ownerMount.getUUID().equals(mount.getUUID()))) {
            // Keep the lead if a transient passenger/boarding state made this attempt fail.
            attachMountToCompanion(mount);
        }
    }

    @Nullable
    private Mob findOwnedMount(ServerLevel level, ServerPlayer owner, @Nullable Mob excludedMount) {
        List<Mob> candidates = level.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(16.0D),
                mount -> isOwnedSaddledMount(mount, owner) && mount != this
                        && (excludedMount == null || !excludedMount.getUUID().equals(mount.getUUID()))
                        && hasOnlyOwnerPassengers(mount, owner));
        candidates.sort(Comparator.comparingDouble(owner::distanceToSqr));
        for (Mob mount : candidates) {
            if (!isAssignedToOtherCompanion(level, mount)) return mount;
        }
        return null;
    }

    private boolean isAssignedToOtherCompanion(ServerLevel level, Mob mount) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof AbstractHumanCompanionEntity other
                    && other != this
                    && other.assignedMountId != null
                    && other.assignedMountId.equals(mount.getUUID())) {
                return true;
            }
        }
        return false;
    }

    /** Workers retain only a tiny emergency ration; profession output is deliverable. */
    private int retainedFoodCount(ItemStack stack, int remainingRation) {
        if (!getJob().isWorker() || !CompanionData.isFood(stack)) return 0;
        return Math.min(stack.getCount(), Math.max(0, remainingRation));
    }

    private boolean hasOnlyOwnerPassengers(Mob mount, Player owner) {
        return mount.getPassengers().stream().allMatch(passenger -> passenger == owner || passenger == this);
    }

    /** Drive a separate mount without letting a low-stat horse crawl behind the owner. */
    private void guideMountedMount(Mob mount, Entity ownerVehicle) {
        if (mount.getUUID().equals(ownerVehicle.getUUID())) return;

        if (mount.distanceToSqr(ownerVehicle) > 9.0D) {
            double mountSpeed = mount.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double ownerMountSpeed = ownerVehicle instanceof LivingEntity living
                    ? living.getAttributeValue(Attributes.MOVEMENT_SPEED)
                    : mountSpeed;
            double speedModifier = CompanionMountRules.guidedMountSpeedModifier(mountSpeed, ownerMountSpeed);
            // MoveControl still uses the mount's movement and jump attributes; the bounded
            // multiplier only prevents a slow separate horse from falling permanently behind.
            mount.getNavigation().moveTo(ownerVehicle, speedModifier);
        } else {
            mount.getNavigation().stop();
        }
    }

    private void restoreMountedMountAi(Entity mount) {
        if (mount instanceof Mob mob) {
            mob.getNavigation().stop();
        }
    }

    private void attachMountToCompanion(@Nullable Mob mount) {
        if (mount == null || this.isPassenger() || mount.distanceToSqr(this) > 100.0D
                || !(mount instanceof Leashable leash) || !leash.canBeLeashed()) {
            return;
        }
        Entity holder = leash.getLeashHolder();
        if (holder == this) return;
        if (holder != null) leash.dropLeash(true, false);
        leash.setLeashedTo(this, true);
    }

    private void detachMountLeadForRide(Mob mount) {
        if (!(mount instanceof Leashable leash)) return;
        Entity holder = leash.getLeashHolder();
        if (holder == this || holder instanceof LeashFenceKnotEntity) {
            leash.dropLeash(true, false);
        }
    }

    /** Called by block events when a player changes a tracked temporary fence. */
    public void invalidateTemporaryMountFence(BlockPos changedPos) {
        if (temporaryMountFencePos == null || !temporaryMountFencePos.equals(changedPos)) return;
        if (this.level() instanceof ServerLevel level) {
            Mob mount = resolveAssignedMount(level);
            detachMountFromFence(level, changedPos, mount);
        }
        temporaryMountFencePos = null;
        temporaryMountFenceState = null;
    }

    @Nullable
    private BlockPos getValidTemporaryMountFence(ServerLevel level) {
        if (temporaryMountFencePos == null || temporaryMountFenceState == null) return null;
        if (level.isLoaded(temporaryMountFencePos)
                && !level.getBlockState(temporaryMountFencePos).equals(temporaryMountFenceState)) {
            Mob mount = resolveAssignedMount(level);
            detachMountFromFence(level, temporaryMountFencePos, mount);
            temporaryMountFencePos = null;
            temporaryMountFenceState = null;
            return null;
        }
        return temporaryMountFencePos;
    }

    /** Remove only the companion-created fence, and only while its exact placed state remains. */
    private void releaseTemporaryMountFence(ServerLevel level, @Nullable Mob mount) {
        BlockPos fencePos = temporaryMountFencePos;
        BlockState placedState = temporaryMountFenceState;
        if (fencePos == null) return;

        detachMountFromFence(level, fencePos, mount);
        if (placedState != null && level.isLoaded(fencePos)
                && level.getBlockState(fencePos).equals(placedState)) {
            level.destroyBlock(fencePos, false);
        }
        temporaryMountFencePos = null;
        temporaryMountFenceState = null;
    }

    private void detachMountFromFence(ServerLevel level, BlockPos fencePos, @Nullable Mob mount) {
        if (mount instanceof Leashable leash
                && leash.getLeashHolder() instanceof LeashFenceKnotEntity knot
                && knot.getPos().equals(fencePos)) {
            leash.dropLeash(true, false);
        }
        for (LeashFenceKnotEntity knot : level.getEntitiesOfClass(LeashFenceKnotEntity.class,
                new AABB(fencePos))) {
            if (knot.getPos().equals(fencePos)) knot.discard();
        }
    }

    private boolean anchorMountAtFence(ServerLevel level, ServerPlayer player, Mob mount) {
        if (!isOwnedSaddledMount(mount, player) || mount.distanceToSqr(this) > 100.0D
                || !(mount instanceof Leashable leash) || !leash.canBeLeashed()) {
            return false;
        }
        BlockPos fencePos = getValidTemporaryMountFence(level);
        if (fencePos == null) fencePos = findExistingFence(level, player, mount);
        if (fencePos == null) {
            fencePos = findOrPlaceFence(level, player, mount);
            if (fencePos != null) {
                temporaryMountFencePos = fencePos.immutable();
                temporaryMountFenceState = level.getBlockState(fencePos);
            }
        }
        if (fencePos == null) {
            attachMountToCompanion(mount);
            return false;
        }

        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(level, fencePos);
        if (leash.getLeashHolder() != knot) {
            if (leash.getLeashHolder() != null) leash.dropLeash(true, false);
            leash.setLeashedTo(knot, true);
        }
        return true;
    }

    @Nullable
    private BlockPos findExistingFence(ServerLevel level, ServerPlayer player, Mob mount) {
        BlockPos origin = this.blockPosition();
        BlockPos found = null;
        double closest = Double.MAX_VALUE;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!level.isLoaded(candidate) || !level.getBlockState(candidate).is(BlockTags.FENCES)
                            || !player.mayInteract(level, candidate)
                            || mount.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(candidate)) > 64.0D) {
                        continue;
                    }
                    double distance = mount.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(candidate));
                    if (distance < closest) {
                        closest = distance;
                        found = candidate;
                    }
                }
            }
        }
        return found;
    }

    @Nullable
    private BlockPos findOrPlaceFence(ServerLevel level, ServerPlayer player, Mob mount) {
        if (!(Items.OAK_FENCE instanceof BlockItem fenceItem)) return null;
        BlockPos origin = this.blockPosition();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!level.isLoaded(candidate) || !level.getBlockState(candidate).isAir()
                            || !level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)
                            || mount.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(candidate)) > 64.0D
                            || this.getBoundingBox().intersects(new AABB(candidate))
                            || mount.getBoundingBox().intersects(new AABB(candidate))
                            || !player.mayInteract(level, candidate)) {
                        continue;
                    }
                    ItemStack fenceStack = new ItemStack(Items.OAK_FENCE);
                    if (!player.mayUseItemAt(candidate, Direction.UP, fenceStack)) continue;
                    BlockHitResult hit = new BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(candidate),
                            Direction.UP, candidate, false);
                    BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, fenceStack, hit);
                    if (fenceItem.place(context).consumesAction()
                            && level.getBlockState(candidate).is(BlockTags.FENCES)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    public void clearPatrol() {
        setPatrolPos(null);
        setPatrolling(false);
        setPatrolRadius(4);
    }

    public Component getFoodStatus() {
        List<Component> requirements = foodRequirements.entrySet().stream()
                .map(entry -> foodRequirementComponent(
                        BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue()))
                .toList();
        if (requirements.isEmpty()) return Component.translatable("food.modern_companions.none");
        if (requirements.size() == 1) {
            return Component.translatable("food.modern_companions.wants_single", requirements.get(0));
        }
        MutableComponent result = Component.translatable("food.modern_companions.wants", requirements.get(0), requirements.get(1));
        for (int i = 2; i < requirements.size(); i++) {
            result.append(Component.literal(", ")).append(requirements.get(i));
        }
        return result;
    }

    public Component getFoodStatusForGui() {
        if (!this.isTame()) {
            return getWantedFoodsCompact();
        }
        if (this.getHealth() < this.getMaxHealth() - 0.5F) {
            return hasFoodInInventory()
                    ? Component.translatable("gui.modern_companions.food.healing")
                    : Component.translatable("gui.modern_companions.food.needs_heal");
        }
        return Component.empty();
    }

    public Component getFavoriteFoodName() {
        String id = this.entityData.get(FAVORITE_FOOD);
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? Component.translatable("gui.modern_companions.memory.unknown") : BuiltInRegistries.ITEM.get(key).getDescription();
    }

    public boolean isFavoriteFood(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(this.entityData.get(FAVORITE_FOOD));
    }

    private void assignFavoriteFood() {
        Item favorite = CompanionData.pickConfiguredFood(this.random);
        this.entityData.set(FAVORITE_FOOD, BuiltInRegistries.ITEM.getKey(favorite).toString());
    }

    public Component getWantedFoodsCompact() {
        List<Component> active = syncedFoodRequirements().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> foodRequirementComponent(entry.getKey(), entry.getValue()))
                .toList();
        if (active.isEmpty()) return Component.empty();
        if (active.size() == 2) {
            return Component.translatable("food.modern_companions.compact.both", active.get(0), active.get(1));
        }
        MutableComponent result = Component.empty();
        for (int i = 0; i < active.size(); i++) {
            if (i > 0) result.append(Component.literal(", "));
            result.append(active.get(i));
        }
        return result;
    }

    /** Read the arbitrary-length synced list, with a fallback for pre-3.53 entity data. */
    private Map<String, Integer> syncedFoodRequirements() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String serialized = entityData.get(RECRUITMENT_REQUIREMENTS);
        if (serialized != null && !serialized.isBlank()) {
            for (String raw : serialized.split(";")) {
                String[] parts = raw.split("=", 2);
                if (parts.length != 2) continue;
                try {
                    result.put(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed client data and keep rendering valid requirements.
                }
            }
        }
        if (result.isEmpty()) {
            addSyncedRequirement(result, entityData.get(FOOD1), entityData.get(FOOD1_AMT));
            addSyncedRequirement(result, entityData.get(FOOD2), entityData.get(FOOD2_AMT));
        }
        return result;
    }

    private void addSyncedRequirement(Map<String, Integer> result, String id, int amount) {
        if (id != null && !id.isBlank()) result.put(id, amount);
    }

    private Component foodRequirementComponent(String id, int amount) {
        if (amount <= 0)
            return Component.translatable("food.modern_companions.done");

        ResourceLocation resource = ResourceLocation.tryParse(id);
        Item item = resource == null ? Items.AIR : BuiltInRegistries.ITEM.get(resource);
        Component itemName = item == Items.AIR
                ? Component.literal(id)
                : Component.translatableWithFallback(
                        item.getDescriptionId(), resource.getPath().replace('_', ' '));
        // Keep names client-resolved; the fallback covers missing item translations without hard-coding addons.
        return Component.translatable("food.modern_companions.item_amount", amount)
                .append(Component.literal(" "))
                .append(itemName);
    }

    /** Allows a class to raise its own cargo limit without changing the global item stack limit. */
    protected int getInventoryStackLimit(ItemStack stack, int vanillaLimit) {
        return vanillaLimit;
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public ItemStack getSpellbookItem() {
        return hasMana() ? entityData.get(SPELLBOOK) : ItemStack.EMPTY;
    }

    public boolean canEquipSpellbook(ItemStack stack) {
        return hasMana() && (stack.isEmpty() || MagicCastingCompat.isSpellbook(stack));
    }

    /** Keep the spellbook synchronized for the menu while persisting its full native components. */
    public boolean setSpellbookItem(ItemStack stack) {
        if (!canEquipSpellbook(stack)) return false;
        entityData.set(SPELLBOOK, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        return true;
    }

    public ItemStack removeSpellbook(int amount) {
        ItemStack current = getSpellbookItem();
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack removed = current.copyWithCount(Math.min(amount, current.getCount()));
        setSpellbookItem(ItemStack.EMPTY);
        return removed;
    }

    private boolean hasDedicatedEquipment(EquipmentSlot slot) {
        int index = dedicatedEquipmentIndex(slot);
        return manuallyEquipped[index] && !super.getItemBySlot(slot).isEmpty();
    }

    private int dedicatedEquipmentIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            case MAINHAND -> 4;
            case OFFHAND -> 5;
            default -> throw new IllegalArgumentException("Unsupported companion equipment slot: " + slot);
        };
    }

    public static EquipmentSlot equipmentSlotFromIndex(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            case 4 -> EquipmentSlot.MAINHAND;
            case 5 -> EquipmentSlot.OFFHAND;
            default -> null;
        };
    }

    public boolean isEquipmentRenderVisible(EquipmentSlot slot) {
        int index = equipmentRenderIndex(slot);
        return index < 0 || (this.entityData.get(EQUIPMENT_RENDER_MASK) & (1 << index)) != 0;
    }

    public void toggleEquipmentRender(EquipmentSlot slot) {
        int index = equipmentRenderIndex(slot);
        if (index < 0) return;
        int bit = 1 << index;
        this.entityData.set(EQUIPMENT_RENDER_MASK, this.entityData.get(EQUIPMENT_RENDER_MASK) ^ bit);
    }

    private static int equipmentRenderIndex(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            case MAINHAND -> 4;
            case OFFHAND -> 5;
            default -> -1;
        };
    }

    /** Lets the client renderer hide selected slots without changing live equipment or AI state. */
    public void setEquipmentRenderContext(boolean rendering) {
        this.renderingEquipment = rendering;
    }

    /**
     * Renderer view: cosmetic armor may replace functional armor only while a renderer owns the context.
     * Menu and gameplay code must use {@link #getFunctionalEquipmentItem(EquipmentSlot)} instead.
     */
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if (renderingEquipment) {
            if (!isEquipmentRenderVisible(slot)) return ItemStack.EMPTY;
            ItemStack cosmetic = getCosmeticArmorItem(slot);
            if (!cosmetic.isEmpty()) return cosmetic;
        }
        return super.getItemBySlot(slot);
    }

    /** Functional equipment view used by inventory slots; cosmetic gear is renderer-only. */
    public ItemStack getFunctionalEquipmentItem(EquipmentSlot slot) {
        return super.getItemBySlot(slot);
    }

    private static EntityDataAccessor<ItemStack> cosmeticAccessor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> COSMETIC_HEAD;
            case CHEST -> COSMETIC_CHEST;
            case LEGS -> COSMETIC_LEGS;
            case FEET -> COSMETIC_FEET;
            default -> null;
        };
    }

    public ItemStack getCosmeticArmorItem(EquipmentSlot slot) {
        EntityDataAccessor<ItemStack> accessor = cosmeticAccessor(slot);
        return accessor == null ? ItemStack.EMPTY : this.entityData.get(accessor);
    }

    public boolean canEquipCosmeticArmor(EquipmentSlot slot, ItemStack stack) {
        return stack.isEmpty() || (cosmeticAccessor(slot) != null && stack.canEquip(slot, this));
    }

    public boolean setCosmeticArmorItem(EquipmentSlot slot, ItemStack stack) {
        EntityDataAccessor<ItemStack> accessor = cosmeticAccessor(slot);
        if (accessor == null || !canEquipCosmeticArmor(slot, stack)) return false;
        this.entityData.set(accessor, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        return true;
    }

    /** Restore saved cosmetic data verbatim; load paths must not reject a previously accepted stack. */
    private void restoreCosmeticArmorItem(EquipmentSlot slot, ItemStack stack) {
        EntityDataAccessor<ItemStack> accessor = cosmeticAccessor(slot);
        if (accessor != null) {
            this.entityData.set(accessor, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
    }

    /** Reads the pre-indexed format and repairs sparse armor by matching each stack to its armor slot. */
    private void restoreLegacyCosmeticArmor(ListTag savedItems) {
        SimpleContainer compactArmor = new SimpleContainer(4);
        compactArmor.fromTag(savedItems, this.registryAccess());
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        boolean[] restored = new boolean[slots.length];
        for (int itemIndex = 0; itemIndex < compactArmor.getContainerSize(); itemIndex++) {
            ItemStack stack = compactArmor.getItem(itemIndex);
            if (stack.isEmpty()) continue;

            int targetIndex = -1;
            for (int slotIndex = 0; slotIndex < slots.length; slotIndex++) {
                if (!restored[slotIndex] && stack.canEquip(slots[slotIndex], this)) {
                    targetIndex = slotIndex;
                    break;
                }
            }
            if (targetIndex < 0 && itemIndex < slots.length && !restored[itemIndex]) {
                targetIndex = itemIndex;
            }
            if (targetIndex >= 0) {
                restoreCosmeticArmorItem(slots[targetIndex], stack);
                restored[targetIndex] = true;
            }
        }
    }

    public ItemStack removeCosmeticArmor(EquipmentSlot slot, int amount) {
        ItemStack current = getCosmeticArmorItem(slot);
        if (current.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        setCosmeticArmorItem(slot, ItemStack.EMPTY);
        return current.copyWithCount(Math.min(amount, current.getCount()));
    }

    /** Manual slots are locks; automatic equipment continues to use the live entity slot. */
    public void setManualEquipment(EquipmentSlot slot, ItemStack stack) {
        if (!canEquipInSlot(slot, stack)) return;
        int index = dedicatedEquipmentIndex(slot);
        manuallyEquipped[index] = !stack.isEmpty();
        super.setItemSlot(slot, stack.copy());
    }

    /** Removes gear through the same live slot used by rendering and vanilla NBT. */
    public ItemStack removeEquipment(EquipmentSlot slot, int amount) {
        ItemStack current = super.getItemBySlot(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = current.split(amount);
        super.setItemSlot(slot, current);
        if (current.isEmpty()) manuallyEquipped[dedicatedEquipmentIndex(slot)] = false;
        return removed;
    }

    /** Food is held only for the existing eat animation, then the saved offhand returns. */
    public void setTemporaryOffhandItem(ItemStack stack) {
        if (stack.isEmpty()) {
            super.setItemSlot(EquipmentSlot.OFFHAND, savedOffhand);
            savedOffhand = ItemStack.EMPTY;
        } else {
            if (savedOffhand.isEmpty()) savedOffhand = super.getItemBySlot(EquipmentSlot.OFFHAND).copy();
            super.setItemSlot(EquipmentSlot.OFFHAND, stack);
        }
    }

    /** Hand slots only accept usable gear; cargo and consumables stay in the companion inventory. */
    public boolean canEquipInSlot(EquipmentSlot slot, ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slot;
        }
        return switch (slot) {
            case MAINHAND -> isMainHandEquipment(stack) && (!FirearmSupport.isFirearm(stack) || isFirearmAllowed(stack));
            case OFFHAND -> isOffhandEquipment(stack);
            default -> false;
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
            super.setItemSlot(slot, stack);
            return;
        }
        if (!canEquipInSlot(slot, stack)) {
            if (slot != EquipmentSlot.MAINHAND) return;
            stack = ItemStack.EMPTY;
        }
        if (slot == EquipmentSlot.MAINHAND && getJob() != CompanionJob.NONE && getJob() != CompanionJob.HUNTER
                && !stack.isEmpty() && !isJobTool(stack, getJob())) return;
        if (slot == EquipmentSlot.MAINHAND && getJob() == CompanionJob.NONE && !isMainHandWeapon(stack)) {
            ItemStack fallback = findInventoryWeaponOrTool();
            stack = fallback.isEmpty() ? (isMainHandEquipment(stack) ? stack : ItemStack.EMPTY) : fallback;
        }
        if (!ModConfig.safeGet(ModConfig.AUTO_EQUIP) && findInventorySlot(stack) >= 0
                && !(slot == EquipmentSlot.MAINHAND && getJob().isWorker() && (isPatrolling() || isWorkEnabled())
                && isJobTool(stack, getJob()))) return;
        int index = dedicatedEquipmentIndex(slot);
        ItemStack manual = super.getItemBySlot(slot);
        if (manuallyEquipped[index] && !manual.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(manual, stack)) return;
        } else {
            // Keep a player-upgraded sword when a class scans cargo and finds a weaker one.
            if (slot == EquipmentSlot.MAINHAND && manual.getItem() instanceof SwordItem
                    && stack.getItem() instanceof SwordItem && !isBetterEquipment(stack, manual, slot)) return;
            manuallyEquipped[index] = false;
            if (!setAutomaticEquipment(slot, stack)) return;
            return;
        }
        super.setItemSlot(slot, stack);
    }

    private boolean isMainHandEquipment(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof ArmorItem) && (isMagicEquipment(stack)
                || stack.getItem() instanceof TieredItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof MaceItem
                || stack.getItem() instanceof FishingRodItem
                || FirearmSupport.isFirearm(stack)
                || stack.is(Items.STICK) || stack.is(Items.BLAZE_ROD)
                || stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.SWORDS)
                || stack.is(TagsInit.Items.SWORDS));
    }

    /** Caster implements are equipment only for companions that actually own the mage AI. */
    private boolean isMagicEquipment(ItemStack stack) {
        return hasMana() && MagicCastingCompat.isMagicItem(stack);
    }

    /** Move caster items loaded through Mob's raw HandItems list out of non-mage hands. */
    private void migrateStaleCasterEquipment() {
        if (hasMana()) return;
        ItemStack staleMainHand = getFunctionalEquipmentItem(EquipmentSlot.MAINHAND);
        if (staleMainHand.isEmpty() || !MagicCastingCompat.isMagicItem(staleMainHand)) return;

        manuallyEquipped[dedicatedEquipmentIndex(EquipmentSlot.MAINHAND)] = false;
        super.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        storeOrDrop(staleMainHand);
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    private boolean isMainHandWeapon(ItemStack stack) {
        return isMainHandEquipment(stack) && !(stack.getItem() instanceof DiggerItem)
                && !(stack.getItem() instanceof FishingRodItem);
    }

    /** Keeps a valid class weapon equipped instead of swapping it with an older cargo item every tick. */
    protected ItemStack retainPreferredMainHand(Predicate<ItemStack> preferredWeapon) {
        ItemStack mainHand = getMainHandItem();
        return !mainHand.isEmpty() && preferredWeapon.test(mainHand) ? mainHand : ItemStack.EMPTY;
    }

    /** Weapons win for companions without jobs; tools remain a valid fallback. */
    private ItemStack findInventoryWeaponOrTool() {
        ItemStack tool = ItemStack.EMPTY;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack candidate = inventory.getItem(i);
            if (!isAutomaticMainHandCandidate(candidate)) continue;
            if (isMainHandWeapon(candidate)) return candidate;
            if (tool.isEmpty() && isMainHandEquipment(candidate)) tool = candidate;
        }
        return tool;
    }

    /** Equips one valid defensive/light offhand item when no offhand is already occupied. */
    private void equipOffhandFromInventory() {
        if (!ModConfig.safeGet(ModConfig.AUTO_EQUIP)
                || !getFunctionalEquipmentItem(EquipmentSlot.OFFHAND).isEmpty()) return;
        ItemStack fallback = ItemStack.EMPTY;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack candidate = inventory.getItem(i);
            if (!isOffhandEquipment(candidate)) continue;
            // A totem is the only offhand item that can prevent a lethal hit, so it wins.
            if (candidate.is(Items.TOTEM_OF_UNDYING)) {
                fallback = candidate;
                break;
            }
            if (fallback.isEmpty()) fallback = candidate;
        }
        if (!fallback.isEmpty()) setItemSlot(EquipmentSlot.OFFHAND, fallback);
    }

    /** Shift-click equips one better gear piece or fills the magical spellbook slot without overriding a manual slot. */
    public boolean equipBetterFromPlayer(ItemStack stack) {
        if (!ModConfig.safeGet(ModConfig.AUTO_EQUIP)) return false;
        if (hasMana() && MagicCastingCompat.isSpellbook(stack)) {
            if (!getSpellbookItem().isEmpty()) return false;
            setSpellbookItem(stack.copyWithCount(1));
            stack.shrink(1);
            return true;
        }
        EquipmentSlot slot = equipmentSlotFor(stack);
        if (slot == null || hasDedicatedEquipment(slot)) return false;
        ItemStack current = getFunctionalEquipmentItem(slot);
        if (!isBetterEquipment(stack, current, slot)) return false;
        ItemStack equipped = stack.copyWithCount(1);
        if (!setAutomaticEquipment(slot, equipped)) return false;
        stack.shrink(1);
        return true;
    }

    /** Moves auto-equipped cargo into its dedicated slot and returns replaced gear to cargo. */
    private boolean setAutomaticEquipment(EquipmentSlot slot, ItemStack stack) {
        ItemStack current = super.getItemBySlot(slot);
        int sourceSlot = findInventorySlot(stack);
        boolean unchanged = ItemStack.isSameItemSameComponents(current, stack);
        if (!unchanged && !current.isEmpty() && sourceSlot < 0 && !canStoreInInventory(current)) return false;

        ItemStack equipped = sourceSlot < 0 ? stack : inventory.removeItem(sourceSlot, 1);
        if (equipped.isEmpty() && !stack.isEmpty()) return false;
        if (!unchanged && !current.isEmpty() && !insertIntoContainer(inventory, current.copy()).isEmpty()) return false;
        super.setItemSlot(slot, equipped);
        return true;
    }

    @Nullable
    private EquipmentSlot equipmentSlotFor(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) return armor.getEquipmentSlot();
        if (stack.getItem() instanceof SwordItem || isMagicEquipment(stack)) return EquipmentSlot.MAINHAND;
        return isOffhandEquipment(stack) ? EquipmentSlot.OFFHAND : null;
    }

    private boolean isBetterEquipment(ItemStack candidate, ItemStack current, EquipmentSlot slot) {
        if (current.isEmpty()) return true;
        if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) return CompanionData.isBetterArmor(candidate, current);
        if (slot == EquipmentSlot.MAINHAND && isMagicEquipment(candidate)) {
            return current.isEmpty() || !isMagicEquipment(current);
        }
        if (slot == EquipmentSlot.MAINHAND && candidate.getItem() instanceof SwordItem sword
                && current.getItem() instanceof SwordItem equippedSword) return sword.getDamage(candidate) > equippedSword.getDamage(current);
        return false;
    }

    private boolean inventoryContains(ItemStack stack) {
        return findInventorySlot(stack) >= 0;
    }

    private int findInventorySlot(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(i), stack)) return i;
        }
        return -1;
    }

    private boolean canStoreInInventory(ItemStack stack) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stored = inventory.getItem(i);
            if (stored.isEmpty() || ItemStack.isSameItemSameComponents(stored, stack)
                    && stored.getCount() < stored.getMaxStackSize()) return true;
        }
        return false;
    }

    /** Firearms take precedence over class weapons so per-tick selectors cannot unequip them. */
    protected ItemStack getEquippedOrInventoryFirearm() {
        if (FirearmSupport.isAllowedFirearm(this, getMainHandItem())) return getMainHandItem();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (FirearmSupport.isAllowedFirearm(this, stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Normal companions accept every TacZ firearm; specialists narrow this hook. */
    public boolean isFirearmAllowed(ItemStack stack) {
        return true;
    }

    /** Controls automatic main-hand selection without changing manual inventory storage. */
    protected boolean isAutomaticMainHandCandidate(ItemStack stack) {
        return true;
    }

    public Map<Item, Integer> getFoodRequirements() {
        return foodRequirements;
    }

    public int getSkinIndex() {
        return this.entityData.get(SKIN_VARIANT);
    }

    public void setSkinIndex(int index) {
        int sex = getSex();
        int max = CompanionData.skins[sex].length;
        this.entityData.set(SKIN_VARIANT, Mth.clamp(index, 0, Math.max(0, max - 1)));
    }

    public ResourceLocation getDefaultSkinTexture() {
        int sex = Mth.clamp(getSex(), 0, CompanionData.skins.length - 1);
        ResourceLocation[] variants = CompanionData.skins[sex];
        int idx = Mth.clamp(getSkinIndex(), 0, variants.length - 1);
        return variants[idx];
    }

    public Optional<String> getCustomSkinUrl() {
        String raw = this.entityData.get(CUSTOM_SKIN_URL);
        return raw == null || raw.isBlank() ? Optional.empty() : Optional.of(raw);
    }

    public void setCustomSkinUrl(@Nullable String url) {
        // Store URL as a synced string so clients can fetch/download the texture on
        // demand.
        this.entityData.set(CUSTOM_SKIN_URL, url == null ? "" : url.trim());
    }

    /** Player-authored journal text, synchronized so the journal updates for every viewer. */
    public String getCustomBio() {
        return this.entityData.get(CUSTOM_BIO);
    }

    public void setCustomBio(@Nullable String bio) {
        this.entityData.set(CUSTOM_BIO, bio == null ? "" : bio.trim());
    }

    /** Selects Minecraft's slim-armed Alex model; false preserves the default Steve model. */
    public boolean usesAlexModel() {
        return this.entityData.get(ALEX_MODEL);
    }

    public void setUsesAlexModel(boolean alex) {
        this.entityData.set(ALEX_MODEL, alex);
    }

    public int getSex() {
        return this.entityData.get(SEX);
    }

    public void setSex(int value) {
        this.entityData.set(SEX, Mth.clamp(value, 0, CompanionData.skins.length - 1));
    }

    public String getVoiceActor() {
        return this.entityData.get(VOICE_ACTOR);
    }

    public void setVoiceActor(String actor) {
        this.entityData.set(VOICE_ACTOR, actor == null ? "" : actor.toLowerCase(Locale.ROOT));
    }

    EnumMap<ModSounds.Cue, Integer> voiceCooldowns() {
        return voiceCooldowns;
    }

    int voiceLockUntilTick() {
        return voiceLockUntilTick;
    }

    void setVoiceLockUntilTick(int tick) {
        voiceLockUntilTick = tick;
    }

    public int getBaseHealth() {
        return this.entityData.get(BASE_HEALTH);
    }

    public void setBaseHealth(int health) {
        this.entityData.set(BASE_HEALTH, health);
    }

    public boolean isEating() {
        return this.entityData.get(EATING);
    }

    public void setEating(boolean eating) {
        this.entityData.set(EATING, eating);
    }

    public int getExpLvl() {
        return this.entityData.get(EXP_LVL);
    }

    public void setExpLvl(int lvl) {
        this.entityData.set(EXP_LVL, Math.max(lvl, 0));
    }

    public float getExperienceProgress() {
        return this.level().isClientSide ? this.entityData.get(EXP_PROGRESS) : this.experienceProgress;
    }

    public int getTotalExperience() {
        return this.totalExperience;
    }

    public int getKillCount() {
        return this.entityData.get(KILL_COUNT);
    }

    public java.util.List<BlockPos> getMinerOreMemory() {
        return minerOreMemory;
    }

    public int getMinerOreIndex() {
        return minerOreIndex;
    }

    public void setMinerOreIndex(int idx) {
        this.minerOreIndex = Math.max(0, idx);
    }

    public void overwriteMinerOreMemory(java.util.List<BlockPos> newMemory) {
        minerOreMemory.clear();
        minerOreMemory.addAll(newMemory);
    }

    public void resetMinerOreMemory() {
        minerOreMemory.clear();
        minerOreIndex = 0;
    }

    public int getMinerOresCounted() {
        return this.entityData.get(MINER_ORES_COUNTED);
    }

    public void setMinerOresCounted(int counted) {
        this.entityData.set(MINER_ORES_COUNTED, Math.max(0, counted));
    }

    public int getMinerOresMined() {
        return this.entityData.get(MINER_ORES_MINED);
    }

    public void setMinerOresMined(int mined) {
        this.entityData.set(MINER_ORES_MINED, Math.max(0, mined));
    }

    public void incrementMinerOresMined() {
        setMinerOresMined(getMinerOresMined() + 1);
        setMinerOresLifetime(getMinerOresLifetime() + 1);
    }

    public int getMinerOresLifetime() {
        return this.entityData.get(MINER_ORES_LIFETIME);
    }

    public void setMinerOresLifetime(int lifetime) {
        this.entityData.set(MINER_ORES_LIFETIME, Math.max(0, lifetime));
    }

    public int getLumberLogsSession() {
        return this.entityData.get(LUMBER_LOGS_SESSION);
    }

    public void setLumberLogsSession(int logs) {
        this.entityData.set(LUMBER_LOGS_SESSION, Math.max(0, logs));
    }

    public void incrementLumberLogsSession() {
        setLumberLogsSession(getLumberLogsSession() + 1);
        setLumberLogsLifetime(getLumberLogsLifetime() + 1);
    }

    public int getLumberLogsLifetime() {
        return this.entityData.get(LUMBER_LOGS_LIFETIME);
    }

    public void setLumberLogsLifetime(int logs) {
        this.entityData.set(LUMBER_LOGS_LIFETIME, Math.max(0, logs));
    }

    public int getFishCaughtSession() {
        return this.entityData.get(FISH_CAUGHT_SESSION);
    }

    public void setFishCaughtSession(int fish) {
        this.entityData.set(FISH_CAUGHT_SESSION, Math.max(0, fish));
    }

    public void incrementFishCaughtSession() {
        setFishCaughtSession(getFishCaughtSession() + 1);
        setFishCaughtLifetime(getFishCaughtLifetime() + 1);
    }

    public int getFishCaughtLifetime() {
        return this.entityData.get(FISH_CAUGHT_LIFETIME);
    }

    public void setFishCaughtLifetime(int fish) {
        this.entityData.set(FISH_CAUGHT_LIFETIME, Math.max(0, fish));
    }

    public int getFarmerHarvestedSession() {
        return this.entityData.get(FARMER_HARVESTED_SESSION);
    }

    public void setFarmerHarvestedSession(int count) {
        this.entityData.set(FARMER_HARVESTED_SESSION, Math.max(0, count));
    }

    public void incrementFarmerHarvestedSession() {
        setFarmerHarvestedSession(getFarmerHarvestedSession() + 1);
        setFarmerHarvestedLifetime(getFarmerHarvestedLifetime() + 1);
    }

    public int getFarmerHarvestedLifetime() {
        return this.entityData.get(FARMER_HARVESTED_LIFETIME);
    }

    public void setFarmerHarvestedLifetime(int count) {
        this.entityData.set(FARMER_HARVESTED_LIFETIME, Math.max(0, count));
    }

    public int getFarmerPlantedSession() {
        return this.entityData.get(FARMER_PLANTED_SESSION);
    }

    public void setFarmerPlantedSession(int count) {
        this.entityData.set(FARMER_PLANTED_SESSION, Math.max(0, count));
    }

    public void incrementFarmerPlantedSession() {
        setFarmerPlantedSession(getFarmerPlantedSession() + 1);
        setFarmerPlantedLifetime(getFarmerPlantedLifetime() + 1);
    }

    public int getFarmerPlantedLifetime() {
        return this.entityData.get(FARMER_PLANTED_LIFETIME);
    }

    public void setFarmerPlantedLifetime(int count) {
        this.entityData.set(FARMER_PLANTED_LIFETIME, Math.max(0, count));
    }

    public int getHunterKillsSession() {
        return this.entityData.get(HUNTER_KILLS_SESSION);
    }

    public void setHunterKillsSession(int count) {
        this.entityData.set(HUNTER_KILLS_SESSION, Math.max(0, count));
    }

    public void incrementHunterKills() {
        setHunterKillsSession(getHunterKillsSession() + 1);
        setHunterKillsLifetime(getHunterKillsLifetime() + 1);
    }

    public int getHunterKillsLifetime() {
        return this.entityData.get(HUNTER_KILLS_LIFETIME);
    }

    public void setHunterKillsLifetime(int count) {
        this.entityData.set(HUNTER_KILLS_LIFETIME, Math.max(0, count));
    }

    public int getChefCookedSession() {
        return this.entityData.get(CHEF_COOKED_SESSION);
    }

    public void setChefCookedSession(int count) {
        this.entityData.set(CHEF_COOKED_SESSION, Math.max(0, count));
    }

    public void incrementChefCooked(int count) {
        int safeCount = Math.max(0, count);
        setChefCookedSession(getChefCookedSession() + safeCount);
        setChefCookedLifetime(getChefCookedLifetime() + safeCount);
    }

    public int getChefCookedLifetime() {
        return this.entityData.get(CHEF_COOKED_LIFETIME);
    }

    public void setChefCookedLifetime(int count) {
        this.entityData.set(CHEF_COOKED_LIFETIME, Math.max(0, count));
    }

    public BlockPos getMinerPlanCenter() {
        return minerPlanCenter == null ? BlockPos.ZERO : minerPlanCenter;
    }

    public void setMinerPlanCenter(BlockPos center) {
        this.minerPlanCenter = center == null ? BlockPos.ZERO : center;
    }

    public int getMinerPlanRadius() {
        return minerPlanRadius;
    }

    public void setMinerPlanRadius(int radius) {
        this.minerPlanRadius = Math.max(0, radius);
    }

    public int getMinerPlanUp() {
        return minerPlanUp;
    }

    public void setMinerPlanUp(int up) {
        this.minerPlanUp = Math.max(0, up);
    }

    public int getMinerPlanDown() {
        return minerPlanDown;
    }

    public void setMinerPlanDown(int down) {
        this.minerPlanDown = Math.max(0, down);
    }

    public void setKillCount(int kills) {
        this.entityData.set(KILL_COUNT, Math.max(0, kills));
    }

    public void incrementKillCount() {
        if (!this.level().isClientSide) {
            setKillCount(getKillCount() + 1);
            personality.incrementTotalKills(false);
        }
    }

    public void recordKill(LivingEntity victim) {
        boolean major = isMajorKill(victim);
        incrementKillCount();
        if (getJob() == CompanionJob.HUNTER) incrementHunterKills();
        personality.incrementTotalKills(major);
        if (major) {
            this.entityData.set(MAJOR_KILLS, personality.getMajorKills());
        }
        syncPersonalityToData();
    }

    /** Keeps delayed fire, fall, and environmental deaths tied to a live combat engagement. */
    public boolean isEngagedWith(LivingEntity victim) {
        return victim != null && (getTarget() == victim || getLastHurtMob() == victim || getLastHurtByMob() == victim);
    }

    private boolean isMajorKill(LivingEntity victim) {
        var type = victim.getType();
        return type == EntityType.ENDER_DRAGON
                || type == EntityType.WITHER
                || type == EntityType.WARDEN
                || type == EntityType.ELDER_GUARDIAN;
    }

    public CompanionPersonality getPersonality() {
        return personality;
    }

    public String getPrimaryTraitId() {
        return this.entityData.get(PRIMARY_TRAIT);
    }

    public String getSecondaryTraitId() {
        return this.entityData.get(SECONDARY_TRAIT);
    }

    public String getBackstoryId() {
        return this.entityData.get(BACKSTORY_ID);
    }

    public int getBondLevel() {
        return this.entityData.get(BOND_LEVEL);
    }

    public int getBondXp() {
        return this.entityData.get(BOND_XP);
    }

    public float getMorale() {
        return this.entityData.get(MORALE);
    }

    public String getMoraleDescriptorKey() {
        return personality.moraleDescriptorKey();
    }

    public int getTimesResurrected() {
        return this.entityData.get(RESURRECT_COUNT);
    }

    public long getFirstTamedGameTime() {
        return this.entityData.get(FIRST_TAMED_TIME);
    }

    public long getDistanceTraveledWithOwner() {
        return this.entityData.get(DIST_TRAVELED);
    }

    public int getMajorKills() {
        return this.entityData.get(MAJOR_KILLS);
    }

    public int getAgeYears() {
        return this.entityData.get(AGE_YEARS);
    }

    public void setPrimaryTraitId(String trait) {
        personality.setPrimaryTrait(trait);
        this.entityData.set(PRIMARY_TRAIT, trait == null ? "" : trait);
    }

    public void setSecondaryTraitId(String trait) {
        personality.setSecondaryTrait(trait);
        this.entityData.set(SECONDARY_TRAIT, trait == null ? "" : trait);
    }

    public void setBackstoryId(String id) {
        personality.setBackstoryId(id);
        this.entityData.set(BACKSTORY_ID, id == null ? "" : id);
    }

    public void setBondXp(int xp) {
        personality.setBondXp(xp);
        this.entityData.set(BOND_XP, personality.getBondXp());
        this.entityData.set(BOND_LEVEL, personality.getBondLevel());
    }

    public void awardBondXp(int amount) {
        if (!ModConfig.safeGet(ModConfig.BOND_ENABLED) || amount <= 0) return;
        int before = personality.getBondLevel();
        personality.awardBondXp(amount);
        this.entityData.set(BOND_XP, personality.getBondXp());
        this.entityData.set(BOND_LEVEL, personality.getBondLevel());
        if (personality.getBondLevel() > before && ModConfig.safeGet(ModConfig.MORALE_ENABLED)) {
            adjustMorale(ModConfig.safeGet(ModConfig.MORALE_BOND_LEVEL_DELTA).floatValue());
        }
    }

    public void setMorale(float morale) {
        personality.setMorale(morale);
        this.entityData.set(MORALE, personality.getMorale());
    }

    public void adjustMorale(float delta) {
        float adjusted = delta;
        if (delta < 0) {
            if (hasTrait("trait_disciplined")) adjusted *= 0.7F;
            if (hasTrait("trait_jokester")) adjusted *= 0.7F;
        }
        personality.adjustMorale(adjusted);
        float floor = getMoraleFloor();
        if (personality.getMorale() < floor) {
            personality.setMorale(floor);
        }
        this.entityData.set(MORALE, personality.getMorale());
    }

    private float getMoraleFloor() {
        if (!ModConfig.safeGet(ModConfig.BOND_ENABLED)) return -1.0F;
        float floor = -0.5F + (getBondLevel() * 0.05F);
        return Mth.clamp(floor, -0.2F, 0.2F);
    }

    public void incrementResurrections() {
        personality.noteResurrection();
        this.entityData.set(RESURRECT_COUNT, personality.getTimesResurrected());
    }

    public void setFirstTamedGameTime(long gameTime) {
        personality.setFirstTamedGameTime(gameTime);
        this.entityData.set(FIRST_TAMED_TIME, personality.getFirstTamedGameTime());
    }

    public void addDistanceTraveled(long delta) {
        personality.addDistanceTraveled(delta);
        this.entityData.set(DIST_TRAVELED, personality.getDistanceTraveledWithOwner());
    }

    public void setAgeYears(int years) {
        personality.setAgeYears(years);
        this.entityData.set(AGE_YEARS, personality.getAgeYears());
    }

    public void setMajorKills(int value) {
        personality.setMajorKills(value);
        this.entityData.set(MAJOR_KILLS, personality.getMajorKills());
    }

    private void syncPersonalityToData() {
        this.entityData.set(PRIMARY_TRAIT, personality.getPrimaryTrait());
        this.entityData.set(SECONDARY_TRAIT, personality.getSecondaryTrait());
        this.entityData.set(BOND_XP, personality.getBondXp());
        this.entityData.set(BOND_LEVEL, personality.getBondLevel());
        this.entityData.set(BACKSTORY_ID, personality.getBackstoryId());
        this.entityData.set(MORALE, personality.getMorale());
        this.entityData.set(RESURRECT_COUNT, personality.getTimesResurrected());
        this.entityData.set(FIRST_TAMED_TIME, personality.getFirstTamedGameTime());
        this.entityData.set(DIST_TRAVELED, personality.getDistanceTraveledWithOwner());
        this.entityData.set(MAJOR_KILLS, personality.getMajorKills());
        this.entityData.set(AGE_YEARS, personality.getAgeYears());
    }

    public void noteResurrection() {
        incrementResurrections();
    }

    public void onResurrectedEvent() {
        noteResurrection();
        if (ModConfig.safeGet(ModConfig.BOND_ENABLED)) {
            int resXp = applyBondTraitMultiplier(ModConfig.safeGet(ModConfig.BOND_RESURRECT_XP), false, false, true);
            awardBondXp(resXp);
        }
        if (ModConfig.safeGet(ModConfig.MORALE_ENABLED)) {
            adjustMorale(ModConfig.safeGet(ModConfig.MORALE_RESURRECT_DELTA).floatValue());
        }
    }

    public int getStrength() {
        return getBaseStrength() + equipmentStrengthBonus;
    }

    public int getDexterity() {
        return getBaseDexterity() + equipmentDexterityBonus;
    }

    public int getIntelligence() {
        return getBaseIntelligence() + equipmentIntelligenceBonus;
    }

    public int getEndurance() {
        return getBaseEndurance() + equipmentEnduranceBonus;
    }

    public int getBaseStrength() {
        return this.entityData.get(STR);
    }

    public int getBaseDexterity() {
        return this.entityData.get(DEX);
    }

    public int getBaseIntelligence() {
        return this.entityData.get(INTL);
    }

    public int getBaseEndurance() {
        return this.entityData.get(END);
    }

    public int getSpecialistAttributeIndex() {
        return this.entityData.get(SPECIALIST);
    }

    public void setStrength(int value) {
        this.entityData.set(STR, Math.max(1, value));
    }

    public void setDexterity(int value) {
        this.entityData.set(DEX, Math.max(1, value));
    }

    public void setIntelligence(int value) {
        this.entityData.set(INTL, Math.max(1, value));
    }

    public void setEndurance(int value) {
        this.entityData.set(END, Math.max(1, value));
    }

    public void setSpecialistAttributeIndex(int idx) {
        this.entityData.set(SPECIALIST, idx);
        this.specialistAttr = idx;
    }

    public ResourceLocation getSkinTexture() {
        int sex = Mth.clamp(getSex(), 0, CompanionData.skins.length - 1);
        ResourceLocation[] variants = CompanionData.skins[sex];
        int idx = Mth.clamp(getSkinIndex(), 0, variants.length - 1);
        return variants[idx];
    }

    public boolean hasFoodInInventory() {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (CompanionData.isFood(inventory.getItem(i)))
                return true;
        }
        return false;
    }

    public ItemStack checkFood() {
        int missing = (int) Math.ceil(this.getMaxHealth() - this.getHealth());
        ItemStack best = ItemStack.EMPTY;
        float bestOverflow = Float.MAX_VALUE;
        float bestUnder = -1;

        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (!CompanionData.isFood(stack))
                continue;

            float healValue = estimateHealingPotential(stack, missing);
            if (healValue <= 0)
                continue;

            if (healValue >= missing) {
                float overflow = healValue - missing;
                if (overflow < bestOverflow) {
                    bestOverflow = overflow;
                    best = stack;
                }
            } else if (bestOverflow == Float.MAX_VALUE && healValue > bestUnder) {
                bestUnder = healValue;
                best = stack;
            }
        }
        return best;
    }

    public boolean healFromFoodStack(ItemStack stack) {
        if (stack.isEmpty() || !CompanionData.isFood(stack))
            return false;
        float missing = this.getMaxHealth() - this.getHealth();
        if (missing <= 0.01f)
            return false;

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            int healAmount = Math.max(1, Math.min(food.nutrition(), (int) Math.ceil(missing)));
            playConsumptionEffects(stack);
            stack.shrink(1);
            this.heal(healAmount);
            applyFoodEffects(food);
            food.usingConvertsTo().ifPresent(this::storeOrDrop);
            return true;
        }

        if (CompanionData.isHealingPotion(stack)) {
            ItemStack potionCopy = stack.copyWithCount(1); // preserve effects before shrinking
            playConsumptionEffects(potionCopy);
            stack.shrink(1);
            applyPotionEffects(potionCopy);
            storeOrDrop(new ItemStack(Items.GLASS_BOTTLE));
            return true;
        }

        return false;
    }

    private float estimateHealingPotential(ItemStack stack, float missingHealth) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            return Math.min(food.nutrition(), missingHealth);
        }
        if (!CompanionData.isHealingPotion(stack))
            return 0;

        float healAmount = 0f;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().is(MobEffects.HEAL)) {
                healAmount += 4f * (effect.getAmplifier() + 1);
            } else if (effect.getEffect().is(MobEffects.REGENERATION)) {
                healAmount += (effect.getDuration() * (effect.getAmplifier() + 1)) / 50f;
            } else if (effect.getEffect().is(MobEffects.ABSORPTION)) {
                healAmount += 4f * (effect.getAmplifier() + 1) * 0.5f; // weight shields lightly
            }
        }
        return Math.min(healAmount, missingHealth + 8); // let regen/absorb count a bit above missing
    }

    private void applyFoodEffects(FoodProperties food) {
        if (this.level().isClientSide())
            return;
        for (FoodProperties.PossibleEffect possible : food.effects()) {
            if (this.random.nextFloat() <= possible.probability()) {
                this.addEffect(possible.effect());
            }
        }
    }

    private void applyPotionEffects(ItemStack stack) {
        if (this.level().isClientSide())
            return;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(null, null, this, effect.getAmplifier(), 1.0D);
            } else {
                this.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private void playConsumptionEffects(ItemStack stack) {
        if (this.level().isClientSide())
            return;
        var sound = stack.getUseAnimation() == UseAnim.DRINK ? stack.getItem().getDrinkingSound()
                : stack.getItem().getEatingSound();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), 0.7F,
                1.0F + (this.getRandom().nextFloat() - 0.5F) * 0.2F);
        for (int j = 0; j < 5; ++j) {
            double dx = this.getRandom().nextGaussian() * 0.02D;
            double dy = this.getRandom().nextGaussian() * 0.02D;
            double dz = this.getRandom().nextGaussian() * 0.02D;
            this.level().addParticle(
                    new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,
                            stack.copyWithCount(1)),
                    this.getX() + (double) (this.getRandom().nextFloat() * 0.4F - 0.2F),
                    this.getY() + this.getBbHeight() * 0.8D,
                    this.getZ() + (double) (this.getRandom().nextFloat() * 0.4F - 0.2F),
                    dx, dy, dz);
        }
    }

    private void storeOrDrop(ItemStack stack) {
        if (stack.isEmpty())
            return;
        ItemStack remainder = this.inventory.addItem(stack);
        if (!remainder.isEmpty()) {
            this.spawnAtLocation(remainder);
        }
    }

    /* ---------- Interaction ---------- */

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        // Health packs must reach their item interaction before the companion GUI/food rules.
        if (held.is(ModItems.HEALTH_PACK.get())) {
            return held.interactLivingEntity(player, this, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.isTame() && !this.level().isClientSide()) {
                if (!untamedGreetingPlayed) {
                    untamedGreetingPlayed = true;
                    CompanionVoice.play(this, ModSounds.Cue.GREETING);
                }
                if (foodRequirements.isEmpty() || !resourceRequirementResolved) {
                    assignFoodRequirements(player);
                }
                if (held.isEmpty()) {
                    // Empty-hand conversations use the dedicated pre-taming dialogue pool.
                    player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                            CompanionData.notTamed[this.random.nextInt(CompanionData.notTamed.length)]));
                    player.sendSystemMessage(getFoodStatus());
                } else if (foodRequirements.containsKey(held.getItem())) {
                    Item fedItem = held.getItem();
                    int remaining = foodRequirements.get(fedItem);
                    if (remaining > 0) {
                        held.shrink(1);
                        foodRequirements.put(fedItem, remaining - 1);
                        syncFoodRequirements();
                        if (foodRequirements.values().stream().allMatch(v -> v <= 0)) {
                            this.tame(player);
                            CompanionVoice.play(this, ModSounds.Cue.CONFIRMATION);
                            setFirstTamedGameTime(this.level().getGameTime());
                            syncPersonalityToData();
                            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                    Component.translatable("dialogue.modern_companions.tamed.thanks")));
                            player.sendSystemMessage(Component.translatable("message.modern_companions.companion_added"));
                            setPatrolPos(null);
                            setPatrolling(false);
                            setFollowing(true);
                            setPatrolRadius(4);
                            if (patrolGoal != null)
                                patrolGoal.radius = 4;
                            if (moveBackGoal != null)
                                moveBackGoal.radius = 4;
                        } else if (foodRequirements.get(fedItem) == 0) {
                            CompanionVoice.play(this, ModSounds.Cue.CONFIRMATION);
                            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                    CompanionData.ENOUGH_FOOD[this.random
                                            .nextInt(CompanionData.ENOUGH_FOOD.length)]));
                        } else {
                            CompanionVoice.play(this, ModSounds.Cue.CONFIRMATION);
                            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                    CompanionData.tameFail[this.random.nextInt(CompanionData.tameFail.length)]));
                        }
                    } else {
                        CompanionVoice.play(this, ModSounds.Cue.REFUSAL);
                        player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                                CompanionData.ENOUGH_FOOD[this.random.nextInt(CompanionData.ENOUGH_FOOD.length)]));
                    }
                } else {
                    CompanionVoice.play(this, ModSounds.Cue.REFUSAL);
                    player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                            CompanionData.WRONG_FOOD[this.random.nextInt(CompanionData.WRONG_FOOD.length)]));
                    player.sendSystemMessage(getFoodStatus());
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                if (this.isAlliedTo(player)) {
                    if (!this.level().isClientSide() && CompanionData.isFood(held) && this.getHealth() < this.getMaxHealth() - 0.1F) {
                        ItemStack single = held.copyWithCount(1);
                        if (healFromFoodStack(single)) {
                            boolean favorite = isFavoriteFood(held);
                            held.shrink(1);
                            int feedXp = applyBondTraitMultiplier(ModConfig.safeGet(ModConfig.BOND_FEED_XP), true, false, false)
                                    * (favorite ? 2 : 1);
                            awardBondXp(feedXp);
                            CompanionVoice.play(this, ModSounds.Cue.CONFIRMATION);
                            if (ModConfig.safeGet(ModConfig.MORALE_ENABLED)) {
                                float morale = ModConfig.safeGet(ModConfig.MORALE_FEED_DELTA).floatValue();
                                adjustMorale(favorite ? morale * 2.0F : morale);
                            }
                            return InteractionResult.CONSUME;
                        }
                    }
                    // Let the Companion Mover handle interaction (even when sneaking) to avoid
                    // triggering sit/GUI.
                    if (held.is(ModItems.COMPANION_MOVER.get())) {
                        return InteractionResult.PASS;
                    }
                    if (held.is(ModItems.ASSIGNMENT_WAND.get())) {
                        // Entity interaction runs before Item#interactLivingEntity; delegate explicitly.
                        return held.interactLivingEntity(player, this, hand);
                    }
                    if (held.is(ModItems.SOUL_ORB.get())) {
                        // Soul Orb swaps must run before the normal companion inventory interaction.
                        return held.interactLivingEntity(player, this, hand);
                    }
                    if (player.isShiftKeyDown()) {
                        if (!this.level().isClientSide()) {
                            toggleSit((ServerPlayer) player);
                        }
                    } else {
                        if (!this.level().isClientSide()) {
                            CompanionVoice.play(this, ModSounds.Cue.GREETING);
                            openGui((ServerPlayer) player);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    private void toggleSit(ServerPlayer player) {
        CompanionVoice.play(this, ModSounds.Cue.CONFIRMATION);
        if (!this.isOrderedToSit()) {
            this.setOrderedToSit(true);
            if (this.isPassenger()) {
                restoreMountedMountAi(this.getVehicle());
                this.stopRiding();
            }
            if (this.getAssignedMountId().isPresent() && this.level() instanceof ServerLevel level) {
                Mob mount = resolveAssignedMount(level);
                if (mount != null) anchorMountAtFence(level, player, mount);
            }
            Component text = Component.translatable("message.modern_companions.sit.stand_here");
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), text));
        } else {
            resumeMovementOrder();
            Component text = Component.translatable("message.modern_companions.sit.move_around");
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), text));
        }
    }

    public void openGui(ServerPlayer player) {
        MenuProvider provider = new SimpleMenuProvider(
                (id, inv, p) -> new CompanionMenu(id, inv, this),
                getDisplayName());
        player.openMenu(provider, buf -> buf.writeVarInt(getId()));
    }

    private void assignFoodRequirements() {
        assignFoodRequirements(null);
    }

    private void assignFoodRequirements(Player player) {
        Optional<Map<Item, Integer>> configured = CompanionData.getConfiguredRecruitmentRequirements(
                BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()));
        Map<Item, Integer> newReq = configured.orElseGet(() -> player == null
                ? CompanionData.getRandomFoodRequirement(rand)
                : CompanionData.getRandomFoodRequirement(rand, player));
        foodRequirements.clear();
        foodRequirements.putAll(newReq);
        resourceRequirementResolved = configured.isPresent() || player != null;
        syncFoodRequirements();
    }

    private void syncFoodRequirements() {
        String serialized = foodRequirements.entrySet().stream()
                .map(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()) + "=" + entry.getValue())
                .collect(java.util.stream.Collectors.joining(";"));
        entityData.set(RECRUITMENT_REQUIREMENTS, serialized);
        List<Map.Entry<Item, Integer>> entries = foodRequirements.entrySet().stream().toList();
        setLegacyRequirement(FOOD1, FOOD1_AMT, entries, 0);
        setLegacyRequirement(FOOD2, FOOD2_AMT, entries, 1);
    }

    private void setLegacyRequirement(EntityDataAccessor<String> idAccessor, EntityDataAccessor<Integer> amountAccessor,
                                      List<Map.Entry<Item, Integer>> entries, int index) {
        if (index >= entries.size()) {
            entityData.set(idAccessor, "");
            entityData.set(amountAccessor, 0);
            return;
        }
        Map.Entry<Item, Integer> entry = entries.get(index);
        entityData.set(idAccessor, BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
        entityData.set(amountAccessor, entry.getValue());
    }

    private void loadSerializedFoodRequirements(String serialized) {
        if (serialized == null || serialized.isBlank()) return;
        for (String raw : serialized.split(";")) {
            String[] parts = raw.split("=", 2);
            if (parts.length != 2) continue;
            ResourceLocation id = ResourceLocation.tryParse(parts[0]);
            if (id == null) continue;
            try {
                int amount = Integer.parseInt(parts[1]);
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR && amount >= 0) foodRequirements.put(item, amount);
            } catch (NumberFormatException ignored) {
                // Ignore malformed saved data instead of preventing the companion from loading.
            }
        }
    }

    private void loadLegacyFoodRequirements() {
        loadLegacyFoodRequirement(entityData.get(FOOD1), entityData.get(FOOD1_AMT));
        loadLegacyFoodRequirement(entityData.get(FOOD2), entityData.get(FOOD2_AMT));
    }

    private void loadLegacyFoodRequirement(String idText, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(idText);
        if (id == null) return;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != Items.AIR && amount >= 0) foodRequirements.put(item, amount);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return canHarm(target) && super.wantsToAttack(target, owner);
    }

    public int getStamina() { return this.entityData.get(STAMINA); }
    public int getStaminaMax() { return this.entityData.get(STAMINA_MAX); }
    public boolean isStaminaEnabled() { return ModConfig.safeGet(ModConfig.STAMINA_ENABLED); }
    private int sprintStaminaCost() { return ModConfig.safeGet(ModConfig.STAMINA_SPRINT_COST); }
    private int meleeStaminaCost() { return ModConfig.safeGet(ModConfig.STAMINA_MELEE_COST); }
    public int getMana() { return this.entityData.get(MANA); }
    public int getManaMax() {
        int fallback = CompanionResourceRules.manaMaxAtLeastDefault(this.entityData.get(MANA_MAX), MANA_MAX_DEFAULT);
        // Optional-provider attributes and their negative modifiers cannot remove the intrinsic pool.
        return hasMana()
                ? CompanionResourceRules.manaMaxAtLeastDefault(MagicCastingCompat.maxMana(this, fallback), MANA_MAX_DEFAULT)
                : fallback;
    }
    public boolean hasMana() { return this instanceof AbstractMageCompanion; }
    public boolean canSpendMana(int amount) { return hasMana() && getMana() >= amount; }
    public void restoreStamina(int amount) {
        if (!isStaminaEnabled()) {
            this.entityData.set(STAMINA, getStaminaMax());
            return;
        }
        this.entityData.set(STAMINA, bounded(getStamina() + amount, getStaminaMax()));
    }
    public void restoreMana(int amount) { if (hasMana()) this.entityData.set(MANA, bounded(getMana() + amount, getManaMax())); }
    public boolean spendMana(int amount) {
        if (!canSpendMana(amount)) return false;
        this.entityData.set(MANA, getMana() - amount);
        return true;
    }
    public static int bounded(int value, int max) { return CompanionResourceRules.bounded(value, max); }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other);
    }

    /* ---------- Breeding / persistence ---------- */

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // Max-health modifiers can be rebuilt after vanilla loads Health; preserve whether the
        // saved value represented a genuinely full companion so a larger max is refilled once.
        tag.putBoolean(HEALTH_WAS_FULL_TAG, this.getHealth() >= this.getMaxHealth() - 0.01F);
        tag.put("Inventory", this.inventory.createTag(this.registryAccess()));
        byte[] manualSlots = new byte[manuallyEquipped.length];
        for (int i = 0; i < manualSlots.length; i++) manualSlots[i] = (byte) (manuallyEquipped[i] ? 1 : 0);
        tag.putByteArray("DedicatedEquipmentManual", manualSlots);
        tag.putInt("skin", this.getSkinIndex());
        tag.putString("CustomSkinUrl", this.entityData.get(CUSTOM_SKIN_URL));
        tag.putString("CustomBio", this.entityData.get(CUSTOM_BIO));
        tag.putBoolean("AlexModel", usesAlexModel());
        tag.putBoolean("Eating", this.isEating());
        tag.putBoolean("UntamedGreetingPlayed", untamedGreetingPlayed);
        tag.putBoolean("Alert", this.isAlert());
        tag.putBoolean("Hunting", this.isHunting());
        tag.putBoolean("Patrolling", this.isPatrolling());
        tag.putBoolean("Following", this.isFollowing());
        tag.putBoolean("Guarding", this.isGuarding());
        tag.putBoolean("SprintEnabled", this.isSprintEnabled());
        tag.putBoolean("Pickup", this.isPickupEnabled());
        tag.putBoolean("AllowVillagerHarm", this.canHarmVillagers());
        tag.putBoolean("AllowPlayerHarm", this.canHarmPlayers());
        tag.putInt("radius", this.getPatrolRadius());
        tag.putInt("sex", this.getSex());
        tag.putString("VoiceActor", this.getVoiceActor());
        tag.putString("JobId", this.getJob().id());
        tag.putBoolean("WorkEnabled", this.isWorkEnabled());
        tag.putString("JobStatus", this.getJobStatus());
        CompoundTag checkpoint = new CompoundTag();
        checkpoint.putString("Phase", jobPlan.phase().name());
        if (jobPlan.target() != null) checkpoint.putLong("Target", jobPlan.target().asLong());
        if (jobPlan.returnPosition() != null) checkpoint.putLong("Return", jobPlan.returnPosition().asLong());
        tag.put("JobCheckpoint", checkpoint);
        tag.put("JobPlan", jobPlan.save(new CompoundTag()));
        tag.putInt("baseHealth", this.getBaseHealth());
        tag.putFloat("XpP", this.experienceProgress);
        tag.putInt("XpLevel", this.getExpLvl());
        tag.putInt("XpTotal", this.totalExperience);
        tag.putInt("KillCount", this.getKillCount());
        tag.putString("food1", entityData.get(FOOD1));
        tag.putString("food2", entityData.get(FOOD2));
        tag.putString("FavoriteFood", entityData.get(FAVORITE_FOOD));
        tag.putInt("food1_amt", entityData.get(FOOD1_AMT));
        tag.putInt("food2_amt", entityData.get(FOOD2_AMT));
        tag.putString("RecruitmentRequirements", entityData.get(RECRUITMENT_REQUIREMENTS));
        tag.putBoolean("ResourceRequirementResolved", resourceRequirementResolved);
        tag.putInt("Strength", getBaseStrength());
        tag.putInt("Dexterity", getBaseDexterity());
        tag.putInt("Intelligence", getBaseIntelligence());
        tag.putInt("Endurance", getBaseEndurance());
        tag.putInt("SpecialistAttr", getSpecialistAttributeIndex());
        long[] oreArr = minerOreMemory.stream().mapToLong(BlockPos::asLong).toArray();
        tag.putLongArray("MinerOreMemory", oreArr);
        tag.putInt("MinerOreIndex", minerOreIndex);
        tag.putInt("MinerOresCounted", getMinerOresCounted());
        tag.putInt("MinerOresMined", getMinerOresMined());
        tag.putInt("MinerOresLifetime", getMinerOresLifetime());
        tag.putInt("LumberLogsSession", getLumberLogsSession());
        tag.putInt("LumberLogsLifetime", getLumberLogsLifetime());
        tag.putInt("FishCaughtSession", getFishCaughtSession());
        tag.putInt("FishCaughtLifetime", getFishCaughtLifetime());
        tag.putInt("FarmerHarvestedSession", getFarmerHarvestedSession());
        tag.putInt("FarmerHarvestedLifetime", getFarmerHarvestedLifetime());
        tag.putInt("FarmerPlantedSession", getFarmerPlantedSession());
        tag.putInt("FarmerPlantedLifetime", getFarmerPlantedLifetime());
        tag.putInt("HunterKillsSession", getHunterKillsSession());
        tag.putInt("HunterKillsLifetime", getHunterKillsLifetime());
        tag.putInt("ChefCookedSession", getChefCookedSession());
        tag.putInt("ChefCookedLifetime", getChefCookedLifetime());
        tag.putIntArray("MinerPlanCenter", new int[] { getMinerPlanCenter().getX(), getMinerPlanCenter().getY(), getMinerPlanCenter().getZ() });
        tag.putInt("MinerPlanRadius", getMinerPlanRadius());
        tag.putInt("MinerPlanUp", getMinerPlanUp());
        tag.putInt("MinerPlanDown", getMinerPlanDown());
        tag.putLong("LastDeliveryTime", lastDeliveryGameTime);
        if (assignedMountId != null) tag.putUUID("AssignedMount", assignedMountId);
        if (temporaryMountFencePos != null && temporaryMountFenceState != null) {
            tag.putLong("TemporaryMountFencePos", temporaryMountFencePos.asLong());
            tag.put("TemporaryMountFenceState", NbtUtils.writeBlockState(temporaryMountFenceState));
        }
        if (heldLightPos != null) tag.putLong("HeldLightPos", heldLightPos.asLong());
        getAssignedChest().ifPresent(chest -> tag.putIntArray("AssignedChest", new int[] { chest.getX(), chest.getY(), chest.getZ() }));
        getAssignedChestDimension().ifPresent(dim -> tag.putString("AssignedChestDim", dim.location().toString()));
        if (this.getPatrolPos().isPresent()) {
            int[] patrolPos = { this.getPatrolPos().get().getX(), this.getPatrolPos().get().getY(),
                    this.getPatrolPos().get().getZ() };
            tag.putIntArray("patrol_pos", patrolPos);
        }
        CompoundTag personalityTag = new CompoundTag();
        personality.saveTo(personalityTag);
        tag.put("Personality", personalityTag);
        tag.putInt("AgeYears", personality.getAgeYears());
        tag.putLong("AgeLastCheck", personality.getLastAgeCheckGameTime());
        tag.putInt("Stamina", getStamina());
        tag.putInt("StaminaMax", getStaminaMax());
        tag.putInt("Mana", getMana());
        // Curios/armor bonuses are live attributes; persist only the intrinsic pool or
        // an Ars max-mana bonus would be applied again after a relog.
        tag.putInt("ManaMax", CompanionResourceRules.manaMaxAtLeastDefault(this.entityData.get(MANA_MAX), MANA_MAX_DEFAULT));
        SimpleContainer spellbook = new SimpleContainer(1);
        spellbook.setItem(0, getSpellbookItem().copy());
        tag.put("Spellbook", spellbook.createTag(this.registryAccess()));
        tag.putInt("EquipmentRenderMask", entityData.get(EQUIPMENT_RENDER_MASK));
        // Keep each cosmetic slot's index; a SimpleContainer list compacts around empty slots.
        NonNullList<ItemStack> cosmeticArmor = NonNullList.withSize(4, ItemStack.EMPTY);
        cosmeticArmor.set(0, getCosmeticArmorItem(EquipmentSlot.HEAD).copy());
        cosmeticArmor.set(1, getCosmeticArmorItem(EquipmentSlot.CHEST).copy());
        cosmeticArmor.set(2, getCosmeticArmorItem(EquipmentSlot.LEGS).copy());
        cosmeticArmor.set(3, getCosmeticArmorItem(EquipmentSlot.FEET).copy());
        tag.put("CosmeticArmor", ContainerHelper.saveAllItems(new CompoundTag(), cosmeticArmor, this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Entity.load has already deserialized attachments; Curios' capability constructor
        // consumes its deferred inventory exactly once on this first lookup.
        if (net.neoforged.fml.ModList.get().isLoaded("curios")) {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(this);
        }
        heldLightPos = null;
        if (tag.contains("HeldLightPos") && this.level() instanceof ServerLevel server) {
            BlockPos savedLight = BlockPos.of(tag.getLong("HeldLightPos"));
            if (server.hasChunkAt(savedLight) && isTemporaryHeldLight(server.getBlockState(savedLight))) {
                server.setBlock(savedLight, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        float savedHealth = this.getHealth();
        boolean hasFullHealthMarker = tag.contains(HEALTH_WAS_FULL_TAG);
        boolean wasFullAtSave = hasFullHealthMarker && tag.getBoolean(HEALTH_WAS_FULL_TAG);
        this.setSkinIndex(tag.getInt("skin"));
        if (tag.contains("CustomSkinUrl")) {
            this.setCustomSkinUrl(tag.getString("CustomSkinUrl"));
        }
        if (tag.contains("CustomBio")) {
            this.setCustomBio(tag.getString("CustomBio"));
        }
        if (tag.contains("AlexModel")) {
            this.setUsesAlexModel(tag.getBoolean("AlexModel"));
        }
        this.setEating(tag.getBoolean("Eating"));
        untamedGreetingPlayed = tag.getBoolean("UntamedGreetingPlayed");
        entityData.set(EQUIPMENT_RENDER_MASK, tag.contains("EquipmentRenderMask")
                ? tag.getInt("EquipmentRenderMask") : ALL_EQUIPMENT_RENDER_MASK);
        this.setAlert(tag.getBoolean("Alert"));
        this.setHunting(tag.getBoolean("Hunting"));
        this.setPatrolling(tag.getBoolean("Patrolling"));
        this.setFollowing(tag.getBoolean("Following"));
        this.setGuarding(tag.getBoolean("Guarding"));
        if (tag.contains("SprintEnabled")) {
            this.setSprintEnabled(tag.getBoolean("SprintEnabled"));
        } else if (tag.contains("Stationery")) {
            // Backward compatibility: old saves used Stationery flag; treat "not
            // stationary" as sprint off.
            this.setSprintEnabled(false);
        }
        this.setPickupEnabled(tag.contains("Pickup") ? tag.getBoolean("Pickup") : true);
        this.setCanHarmVillagers(tag.getBoolean("AllowVillagerHarm"));
        this.setCanHarmPlayers(tag.getBoolean("AllowPlayerHarm"));
        this.setPatrolRadius(tag.getInt("radius"));
        assignedMountId = tag.hasUUID("AssignedMount") ? tag.getUUID("AssignedMount") : null;
        ridingWithOwnerMountId = null;
        mountReconciliationTicks = 0;
        temporaryMountFencePos = null;
        temporaryMountFenceState = null;
        if (tag.contains("TemporaryMountFencePos") && tag.contains("TemporaryMountFenceState", 10)) {
            BlockState state = NbtUtils.readBlockState(this.registryAccess().lookupOrThrow(Registries.BLOCK),
                    tag.getCompound("TemporaryMountFenceState"));
            if (state.is(Blocks.OAK_FENCE)) {
                temporaryMountFencePos = BlockPos.of(tag.getLong("TemporaryMountFencePos"));
                temporaryMountFenceState = state;
            }
        }
        this.setSex(tag.getInt("sex"));
        if (tag.contains("VoiceActor")) this.setVoiceActor(tag.getString("VoiceActor"));
        if (!level().isClientSide()) CompanionVoice.ensureActor(this);
        this.setJob(CompanionJob.fromId(tag.getString("JobId")));
        this.setWorkEnabled(tag.getBoolean("WorkEnabled"));
        if (tag.contains("JobStatus")) this.setJobStatus(tag.getString("JobStatus"));
        if (tag.contains("JobPlan", 10)) {
            JobPlan loadedPlan = JobPlan.load(tag.getCompound("JobPlan"));
            jobPlan.copyFrom(loadedPlan);
        } else if (tag.contains("JobCheckpoint", 10)) {
            CompoundTag checkpoint = tag.getCompound("JobCheckpoint");
            JobPhase phase;
            try {
                phase = JobPhase.valueOf(checkpoint.getString("Phase"));
            } catch (IllegalArgumentException ignored) {
                phase = JobPhase.SEARCHING;
            }
            BlockPos target = checkpoint.contains("Target") ? BlockPos.of(checkpoint.getLong("Target")) : null;
            jobPlan.checkpoint(phase, target, null, null,
                    checkpoint.contains("Return") ? BlockPos.of(checkpoint.getLong("Return")) : null);
        }
        this.onJobChanged(false);
        this.experienceProgress = tag.getFloat("XpP");
        this.totalExperience = tag.getInt("XpTotal");
        this.setExpLvl(tag.getInt("XpLevel"));
        setKillCount(tag.contains("KillCount") ? tag.getInt("KillCount") : 0);
        syncExpProgress();
        entityData.set(FOOD1, tag.getString("food1"));
        entityData.set(FOOD2, tag.getString("food2"));
        entityData.set(FAVORITE_FOOD, tag.getString("FavoriteFood"));
        entityData.set(FOOD1_AMT, tag.getInt("food1_amt"));
        entityData.set(FOOD2_AMT, tag.getInt("food2_amt"));
        boolean hasSerializedRequirements = tag.contains("RecruitmentRequirements");
        String serializedRequirements = hasSerializedRequirements ? tag.getString("RecruitmentRequirements") : "";
        entityData.set(RECRUITMENT_REQUIREMENTS, serializedRequirements);
        resourceRequirementResolved = tag.contains("ResourceRequirementResolved")
                ? tag.getBoolean("ResourceRequirementResolved") : this.isTame();
        foodRequirements.clear();
        if (hasSerializedRequirements) {
            loadSerializedFoodRequirements(serializedRequirements);
        } else {
            loadLegacyFoodRequirements();
            syncFoodRequirements();
        }
        if (tag.getInt("baseHealth") == 0) {
            this.setBaseHealth(ModConfig.safeGet(ModConfig.BASE_HEALTH));
        } else {
            this.setBaseHealth(tag.getInt("baseHealth"));
        }
        setSpecialistAttributeIndex(tag.contains("SpecialistAttr") ? tag.getInt("SpecialistAttr") : -1);
        minerOreMemory.clear();
        if (tag.contains("MinerOreMemory")) {
            long[] arr = tag.getLongArray("MinerOreMemory");
            for (long l : arr) {
                minerOreMemory.add(BlockPos.of(l));
            }
        }
        minerOreIndex = tag.getInt("MinerOreIndex");
        if (tag.contains("MinerOresCounted")) setMinerOresCounted(tag.getInt("MinerOresCounted"));
        if (tag.contains("MinerOresMined")) setMinerOresMined(tag.getInt("MinerOresMined"));
        if (tag.contains("MinerOresLifetime")) setMinerOresLifetime(tag.getInt("MinerOresLifetime"));
        if (tag.contains("LumberLogsSession")) setLumberLogsSession(tag.getInt("LumberLogsSession"));
        if (tag.contains("LumberLogsLifetime")) setLumberLogsLifetime(tag.getInt("LumberLogsLifetime"));
        if (tag.contains("FishCaughtSession")) setFishCaughtSession(tag.getInt("FishCaughtSession"));
        if (tag.contains("FishCaughtLifetime")) setFishCaughtLifetime(tag.getInt("FishCaughtLifetime"));
        if (tag.contains("FarmerHarvestedSession")) setFarmerHarvestedSession(tag.getInt("FarmerHarvestedSession"));
        if (tag.contains("FarmerHarvestedLifetime")) setFarmerHarvestedLifetime(tag.getInt("FarmerHarvestedLifetime"));
        if (tag.contains("FarmerPlantedSession")) setFarmerPlantedSession(tag.getInt("FarmerPlantedSession"));
        if (tag.contains("FarmerPlantedLifetime")) setFarmerPlantedLifetime(tag.getInt("FarmerPlantedLifetime"));
        if (tag.contains("HunterKillsSession")) setHunterKillsSession(tag.getInt("HunterKillsSession"));
        if (tag.contains("HunterKillsLifetime")) setHunterKillsLifetime(tag.getInt("HunterKillsLifetime"));
        if (tag.contains("ChefCookedSession")) setChefCookedSession(tag.getInt("ChefCookedSession"));
        if (tag.contains("ChefCookedLifetime")) setChefCookedLifetime(tag.getInt("ChefCookedLifetime"));
        if (tag.contains("MinerPlanCenter")) {
            int[] arr = tag.getIntArray("MinerPlanCenter");
            if (arr.length == 3) {
                setMinerPlanCenter(new BlockPos(arr[0], arr[1], arr[2]));
            }
        }
        if (tag.contains("MinerPlanRadius")) {
            setMinerPlanRadius(tag.getInt("MinerPlanRadius"));
        }
        if (tag.contains("MinerPlanUp")) {
            setMinerPlanUp(tag.getInt("MinerPlanUp"));
        }
        if (tag.contains("MinerPlanDown")) {
            setMinerPlanDown(tag.getInt("MinerPlanDown"));
        }
        if (tag.contains("LastDeliveryTime")) {
            lastDeliveryGameTime = tag.getLong("LastDeliveryTime");
        }
        if (tag.contains("AssignedChest")) {
            int[] arr = tag.getIntArray("AssignedChest");
            if (arr.length == 3) {
                this.entityData.set(DELIVERY_CHEST, Optional.of(new BlockPos(arr[0], arr[1], arr[2])));
            }
        }
        if (tag.contains("AssignedChestDim")) {
            this.entityData.set(DELIVERY_DIMENSION, tag.getString("AssignedChestDim"));
        }
        if (tag.contains("Personality", 10)) {
            personality.loadFrom(tag.getCompound("Personality"));
        } else {
            // backward compatibility if older saves carry individual keys
            personality.setPrimaryTrait(tag.getString(CompanionPersonality.KEY_PRIMARY));
            personality.setSecondaryTrait(tag.getString(CompanionPersonality.KEY_SECONDARY));
            personality.setBondXp(tag.getInt(CompanionPersonality.KEY_BOND_XP));
            personality.setBondLevel(tag.getInt(CompanionPersonality.KEY_BOND_LEVEL));
            personality.setBackstoryId(tag.getString(CompanionPersonality.KEY_BACKSTORY));
            personality.setMorale(tag.getFloat(CompanionPersonality.KEY_MORALE));
            if (tag.contains(CompanionPersonality.KEY_FIRST_TAMED)) {
                personality.setFirstTamedGameTime(tag.getLong(CompanionPersonality.KEY_FIRST_TAMED));
            }
        }
        if (!tag.contains("Personality", 10) && tag.contains("DistanceTravel")) {
            personality.setDistanceTraveled(tag.getLong("DistanceTravel"));
        }
        if (tag.contains("AgeYears")) {
            personality.setAgeYears(tag.getInt("AgeYears"));
        }
        if (tag.contains("AgeLastCheck")) {
            personality.setLastAgeCheckGameTime(tag.getLong("AgeLastCheck"));
        }
        if (tag.contains("Inventory", 9)) {
            this.inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
        }
        // Migrate the short-lived duplicate equipment store from the previous release.
        SimpleContainer legacyEquipment = new SimpleContainer(manuallyEquipped.length);
        if (tag.contains("DedicatedEquipment", 9)) {
            legacyEquipment.fromTag(tag.getList("DedicatedEquipment", 10), this.registryAccess());
        }
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            int index = dedicatedEquipmentIndex(slot);
            ItemStack legacy = legacyEquipment.getItem(index);
            if (!legacy.isEmpty() && canEquipInSlot(slot, legacy)) {
                super.setItemSlot(slot, legacy.copy());
            }
        }
        Arrays.fill(manuallyEquipped, false);
        byte[] manualSlots = tag.getByteArray("DedicatedEquipmentManual");
        if (manualSlots.length == manuallyEquipped.length) {
            for (int i = 0; i < manuallyEquipped.length; i++) manuallyEquipped[i] = manualSlots[i] != 0;
        } else {
            // Equipment saved before manual-lock metadata was introduced was all player placed.
            for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
                manuallyEquipped[dedicatedEquipmentIndex(slot)] = !super.getItemBySlot(slot).isEmpty();
            }
        }
        this.entityData.set(STAMINA_MAX, Math.max(1, tag.contains("StaminaMax") ? tag.getInt("StaminaMax") : STAMINA_MAX_DEFAULT));
        this.entityData.set(STAMINA, bounded(tag.contains("Stamina") ? tag.getInt("Stamina") : getStaminaMax(), getStaminaMax()));
        int savedManaMax = tag.contains("ManaMax") ? tag.getInt("ManaMax") : MANA_MAX_DEFAULT;
        int manaMax = CompanionResourceRules.manaMaxAtLeastDefault(savedManaMax, MANA_MAX_DEFAULT);
        this.entityData.set(MANA_MAX, manaMax);
        int savedMana = tag.contains("Mana") ? tag.getInt("Mana") : getManaMax();
        if (savedManaMax < MANA_MAX_DEFAULT) {
            // A below-default capacity was invalid data; restore the original pool instead of preserving 1/1.
            savedMana = Math.max(savedMana, MANA_MAX_DEFAULT);
        }
        this.entityData.set(MANA, bounded(savedMana, getManaMax()));
        this.entityData.set(SPELLBOOK, ItemStack.EMPTY);
        if (tag.contains("Spellbook", 9)) {
            SimpleContainer spellbook = new SimpleContainer(1);
            spellbook.fromTag(tag.getList("Spellbook", 10), this.registryAccess());
            setSpellbookItem(spellbook.getItem(0));
        }
        migrateStaleCasterEquipment();
        if (tag.contains("CosmeticArmor", 10)) {
            NonNullList<ItemStack> cosmeticArmor = NonNullList.withSize(4, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("CosmeticArmor"), cosmeticArmor, this.registryAccess());
            restoreCosmeticArmorItem(EquipmentSlot.HEAD, cosmeticArmor.get(0));
            restoreCosmeticArmorItem(EquipmentSlot.CHEST, cosmeticArmor.get(1));
            restoreCosmeticArmorItem(EquipmentSlot.LEGS, cosmeticArmor.get(2));
            restoreCosmeticArmorItem(EquipmentSlot.FEET, cosmeticArmor.get(3));
        } else if (tag.contains("CosmeticArmor", 9)) {
            // Migrate old compact lists by armor type so sparse leggings/boots do not shift upward.
            restoreLegacyCosmeticArmor(tag.getList("CosmeticArmor", 10));
        }
        syncPersonalityToData();
        // reset tracking anchors post-load
        this.lastTrackX = this.getX();
        this.lastTrackY = this.getY();
        this.lastTrackZ = this.getZ();
        // Backfill missing flavor data for pre-journal companions
        rollMissingFlavorData();
        if (entityData.get(FAVORITE_FOOD).isBlank()) {
            assignFavoriteFood();
        }
        if (tag.contains("patrol_pos")) {
            int[] positions = tag.getIntArray("patrol_pos");
            setPatrolPos(new BlockPos(positions[0], positions[1], positions[2]));
        }
        if (tag.contains("radius")) {
            patrolGoal = new PatrolGoal(this, 60, tag.getInt("radius"));
            moveBackGoal = new MoveBackToPatrolGoal(this, tag.getInt("radius"));
            this.goalSelector.addGoal(3, moveBackGoal);
            this.goalSelector.addGoal(3, patrolGoal);
        }
        checkArmor();
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            refreshDeliveryChunkTicket(serverLevel);
        }
        if (tag.contains("Strength")) {
            setStrength(tag.getInt("Strength"));
            setDexterity(tag.getInt("Dexterity"));
            setIntelligence(tag.getInt("Intelligence"));
            setEndurance(tag.getInt("Endurance"));
        } else {
            assignRpgAttributes();
        }
        recomputeEquipmentAttributeBonuses();
        applyRpgAttributeModifiers();
        // Restore the transient level bonus before clamping; otherwise a full leveled companion
        // is first reduced to base health and only regains max health several ticks later.
        refreshLevelHealthModifier();
        if (!hasFullHealthMarker) {
            // Legacy saves have no full-health marker. The pre-level max is the best signal left
            // after a transient level modifier was discarded during vanilla attribute loading.
            wasFullAtSave = savedHealth >= this.getMaxHealth() - (getExpLvl() / 3) - 0.01F;
        }
        if (wasFullAtSave) {
            this.setHealth(this.getMaxHealth());
        } else {
            this.setHealth(Math.min(savedHealth, this.getMaxHealth()));
        }
    }

    /* ---------- Spawning ---------- */

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return null; // companions do not breed
    }

    @Override
    public MobCategory getClassification(boolean forSpawnCount) {
        return MobCategory.AMBIENT;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return CompanionData.isFood(stack);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            // Ensure swing timers advance each frame on the client so attackAnim decays
            // naturally.
            this.updateSwingTime();
        }
        if (this.level().isClientSide()) {
            int swingTick = this.entityData.get(LAST_SWING_TICK);
            if (swingTick != lastAppliedSwingTick) {
                lastAppliedSwingTick = swingTick;
                // Replay the swing locally to guarantee a visible animation even if packets
                // were dropped/suppressed.
                this.swinging = true;
                this.swingTime = 0;
                this.oAttackAnim = 0.0F;
                this.attackAnim = 0.0F;
                this.swingingArm = InteractionHand.MAIN_HAND;
                super.swing(InteractionHand.MAIN_HAND, true);
            }
        }
        if (!this.level().isClientSide()) {
            CompanionVoice.ensureActor(this);
            migrateStaleCasterEquipment();
            if (isTame() && getTarget() == null && this.tickCount % 200 == 0 && this.random.nextInt(5) == 0) {
                CompanionVoice.play(this, ModSounds.Cue.IDLE);
            }
            checkArmor();
            if (this.tickCount % 2 == 0 && this.isTame()
                    && (isPickupEnabled() || getJob() == CompanionJob.HUNTER)) {
                collectNearbyItems();
            }
            boostWaterMovement();
            updateSprintState();
            tickCompanionResources();
            equipOffhandFromInventory();
            if (this.tickCount % 2 == 0) updateHeldLight();
            if (this.tickCount % 20 == 0) consumeUsefulCompanionPotion();
            if (this.tickCount % 100 == 0 && this.level() instanceof ServerLevel server) {
                JobReservations.cleanup(server, server.getGameTime());
                if (isWorkEnabled()) {
                    JobReservations.renew(server, this.getUUID(), server.getGameTime(), JobReservations.DEFAULT_TTL);
                }
            }
            tickBondAndMorale();
            if (this.tickCount % 10 == 0) {
                checkStats();
                if (shouldRequestFood())
                    requestFoodFromOwner();
                LivingEntity target = this.getTarget();
                if (target != null && !target.isAlive()) {
                    this.setTarget(null);
                }
            }
            trackDistanceNearOwner();
            tickAging();
            tickCommittedSwim();
            tickCompanionMount();
            if (isPatrolling() || isWorkEnabled()) {
                cacheCurrentJobWeapon();
                equipJobToolIfNeeded();
            }
        }
        boolean equipmentChanged = recomputeEquipmentAttributeBonuses();
        if (equipmentChanged) {
            applyRpgAttributeModifiers();
            clampHealthToMax();
        }
        if (!level().isClientSide()) {
            personalityRefreshTicker++;
            if (equipmentChanged || personalityRefreshTicker >= 40) {
                personalityRefreshTicker = 0;
                refreshPersonalityModifiers();
            }
        }
        super.tick();
    }

    /**
     * Toggle sprinting based on the player-controlled flag and whether the
     * companion is actively moving/engaged.
     */
    private void updateSprintState() {
        boolean wantsSprint = isSprintEnabled() && !this.isOrderedToSit();
        boolean movingOrFighting = (this.getNavigation() != null && !this.getNavigation().isDone())
                || this.getTarget() != null;
        boolean staminaReady = !isStaminaEnabled()
                || (this.isSprinting() ? getStamina() > 0 : getStamina() >= SPRINT_RESUME_STAMINA);
        if (wantsSprint && movingOrFighting && staminaReady) {
            this.setSprinting(true);
        } else {
            this.setSprinting(false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnDataIn) {
        assignRpgAttributes();
        int baseHealth = ModConfig.safeGet(ModConfig.BASE_HEALTH) + CompanionData.getHealthModifier()
                + getEnduranceBonusHealth();
        modifyMaxHealth(baseHealth - 20, "companion base health", true);
        this.setHealth(this.getMaxHealth());
        setBaseHealth(baseHealth);
        setSex(this.random.nextInt(2));
        CompanionVoice.ensureActor(this);
        setSkinIndex(this.random.nextInt(CompanionData.skins[getSex()].length));
        setCustomName(Component.literal(CompanionData.getRandomName(getSex())));
        setPatrolPos(this.blockPosition());
        setPatrolling(true);
        setPatrolRadius(15);
        patrolGoal = new PatrolGoal(this, 60, getPatrolRadius());
        moveBackGoal = new MoveBackToPatrolGoal(this, getPatrolRadius());
        this.goalSelector.addGoal(3, moveBackGoal);
        this.goalSelector.addGoal(3, patrolGoal);
        setAgeYears(this.random.nextInt(18, 36)); // 18-35 inclusive
        personality.setLastAgeCheckGameTime(level.getLevel().getGameTime());
        personality.rollTraits(this.random, ModConfig.safeGet(ModConfig.TRAITS_ENABLED),
                ModConfig.safeGet(ModConfig.SECONDARY_TRAIT_CHANCE));
        personality.rollBackstory(this.random);
        personality.setMorale(0.0F);
        syncPersonalityToData();
        assignFoodRequirements();
        assignFavoriteFood();

        if (ModConfig.safeGet(ModConfig.SPAWN_ARMOR)) {
            for (int i = 0; i < 4; i++) {
                EquipmentSlot armorType = EquipmentSlot.values()[i + 2]; // FEET..HEAD
                ItemStack itemstack = CompanionData.getSpawnArmor(armorType);
                if (!itemstack.isEmpty()) {
                    // Spawn loadouts go straight to live slots; auto-equip only governs cargo scans.
                    setItemSlot(armorType, itemstack);
                }
            }
            checkArmor();
        }

        // Structure companions very rarely receive a role-compatible legendary item.
        // Arrows stay in inventory so bows and crossbows retain their native projectile path.
        if (reason == MobSpawnType.STRUCTURE && this.random.nextFloat() < 0.01F) {
            ItemStack legendary = com.majorbonghits.moderncompanions.registry.ModItems.structureLegendary(
                    this.random, this.getMainHandItem());
            if (!legendary.isEmpty()) {
                if (legendary.getItem() instanceof net.minecraft.world.item.ArrowItem) {
                    this.inventory.addItem(legendary);
                } else {
                    this.setItemSlot(EquipmentSlot.MAINHAND, legendary);
                }
            }
        }
        recomputeEquipmentAttributeBonuses();
        applyRpgAttributeModifiers();
        clampHealthToMax();
        lastTrackX = this.getX();
        lastTrackY = this.getY();
        lastTrackZ = this.getZ();
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnDataIn);
        if (!this.level().isClientSide()
                && net.neoforged.fml.ModList.get().isLoaded("curios")
                && net.neoforged.fml.ModList.get().isLoaded("sophisticatedbackpacks")) {
            com.majorbonghits.moderncompanions.compat.sophisticatedbackpacks.SophisticatedBackpackCompat
                    .tryEquipSpawnBackpack(this);
        }
        return result;
    }

    /* ---------- Orders & actions ---------- */

    public void cycleOrders() {
        if (isOrderedToSit()) resumeMovementOrder();
        if (isFollowing()) {
            setPatrolling(true);
            setFollowing(false);
            setGuarding(false);
            setPatrolPos(blockPosition());
        } else if (isPatrolling()) {
            setPatrolling(false);
            setFollowing(false);
            setGuarding(true);
            setPatrolPos(blockPosition());
        } else {
            setPatrolling(false);
            setFollowing(true);
            setGuarding(false);
        }
    }

    public void toggleAlert() {
        setAlert(!isAlert());
    }

    public void toggleHunting() {
        setHunting(!isHunting());
    }

    public void toggleSprint() {
        setSprintEnabled(!isSprintEnabled());
    }

    private double followSpeed() {
        double base = 1.3D;
        if (hasTrait("trait_quickstep")) base += 0.05D;
        if (hasTrait("trait_cautious")) base -= 0.05D;
        return Math.max(1.05D, base);
    }

    public void release() {
        if (!this.level().isClientSide()) CompanionVoice.play(this, ModSounds.Cue.FAREWELL);
        if (this.level() instanceof ServerLevel level) clearAssignedMount(level);
        this.setTame(false, true);
        this.setOwnerUUID(null);
        setFollowing(false);
        setAlert(false);
        setHunting(false);
        setPatrolPos(this.blockPosition());
        setPatrolling(true);
        setSprintEnabled(false);
        setPatrolRadius(15);
        assignFoodRequirements();
        if (this.isOrderedToSit()) {
            this.setOrderedToSit(false);
        }
    }

    /* ---------- Experience ---------- */

    public void giveExperiencePoints(int points) {
        int adjusted = Math.max(1, Math.round(points * getExperienceGainMultiplier()));
        this.experienceProgress += (float) adjusted / (float) this.getXpNeededForNextLevel();
        this.totalExperience = Mth.clamp(this.totalExperience + adjusted, 0, Integer.MAX_VALUE);
        syncExpProgress();

        while (this.experienceProgress < 0.0F) {
            float f = this.experienceProgress * (float) this.getXpNeededForNextLevel();
            if (this.getExpLvl() > 0) {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 1.0F + f / (float) this.getXpNeededForNextLevel();
            } else {
                this.giveExperienceLevels(-1);
                this.experienceProgress = 0.0F;
            }
        }

        while (this.experienceProgress >= 1.0F) {
            this.experienceProgress = (this.experienceProgress - 1.0F) * (float) this.getXpNeededForNextLevel();
            this.giveExperienceLevels(1);
            this.experienceProgress /= (float) this.getXpNeededForNextLevel();
        }
        syncExpProgress();
    }

    public void giveExperienceLevels(int levels) {
        setExpLvl(getExpLvl() + levels);
        if (levels > 0) CompanionVoice.play(this, ModSounds.Cue.CELEBRATION);
        if (getExpLvl() < 0) {
            setExpLvl(0);
            this.experienceProgress = 0.0F;
            this.totalExperience = 0;
        }
        refreshLevelHealthModifier();
        syncExpProgress();
        if (levels > 0 && this.getExpLvl() % 5 == 0 && (float) this.lastLevelUpTime < (float) this.tickCount - 100.0F) {
            this.lastLevelUpTime = this.tickCount;
        }
    }

    public int getXpNeededForNextLevel() {
        int level = this.getExpLvl();
        // MMO-style curve: gentle start, then superlinear growth so each level costs
        // meaningfully more XP
        double curve = Math.pow(level + 1, 1.35D);
        int required = (int) Math.round(20 + (curve * 10));
        return Math.max(20, required);
    }

    public void modifyMaxHealth(int change, String name, boolean permanent) {
        AttributeInstance attribute = this.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null)
            return;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, name.replace(" ", "_"));
        attribute.removeModifier(id);
        AttributeModifier modifier = new AttributeModifier(id, change, AttributeModifier.Operation.ADD_VALUE);
        if (permanent) {
            attribute.addPermanentModifier(modifier);
        } else if (change != 0) {
            attribute.addTransientModifier(modifier);
        }
    }

    public void checkStats() {
        refreshLevelHealthModifier();
    }

    private void refreshLevelHealthModifier() {
        modifyMaxHealth(getExpLvl() / 3, "companion level health", false);
    }

    private void syncExpProgress() {
        if (!this.level().isClientSide) {
            this.entityData.set(EXP_PROGRESS, this.experienceProgress);
        }
    }

    /* ---------- Combat & equipment ---------- */

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this.getOwner() && !ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_PLAYER)) {
            return false;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL) && !ModConfig.safeGet(ModConfig.FALL_DAMAGE)) {
            return false;
        }
        float before = this.getHealth();
        float adjusted = applyEnduranceResistance(source, amount);
        hurtArmor(source, adjusted);
        if (ModConfig.safeGet(ModConfig.MORALE_ENABLED)) {
            float projected = before - adjusted;
            if (projected <= this.getMaxHealth() * 0.25F && this.tickCount - lastNearDeathTick > 200) {
                adjustMorale(ModConfig.safeGet(ModConfig.MORALE_NEAR_DEATH_DELTA).floatValue());
                lastNearDeathTick = this.tickCount;
            }
        }
        boolean hurt = super.hurt(source, adjusted);
        if (hurt && this.isAlive()) {
            CompanionVoice.play(this, ModSounds.Cue.PAIN);
            if (before > this.getMaxHealth() * 0.25F && this.getHealth() <= this.getMaxHealth() * 0.25F) {
                CompanionVoice.play(this, ModSounds.Cue.LOW_HEALTH);
            }
        }
        return hurt;
    }

    /** Server-authoritative recovery: combat slows it, then grace accelerates it. */
    private void tickCompanionResources() {
        LivingEntity target = getTarget();
        boolean inCombat = target != null && target.isAlive();
        combatGraceTicks = inCombat ? 0 : Math.min(combatGraceTicks + 1, 100);
        boolean staminaEnabled = isStaminaEnabled();
        if (!staminaEnabled) {
            this.entityData.set(STAMINA, getStaminaMax());
        } else if (isSprinting()) {
            this.entityData.set(STAMINA, CompanionResourceRules.spend(getStamina(), sprintStaminaCost(), getStaminaMax()));
        }
        int interval = CompanionResourceRules.regenInterval(inCombat, combatGraceTicks, hasEffect(MobEffects.REGENERATION));
        if (this.tickCount % interval == 0) {
            if (staminaEnabled) restoreStamina(1);
        }
        int manaInterval = CompanionResourceRules.manaRegenInterval(inCombat, combatGraceTicks, hasEffect(MobEffects.REGENERATION));
        manaInterval = MagicCastingCompat.manaRegenInterval(this, manaInterval);
        if (this.tickCount % manaInterval == 0) {
            restoreMana(1);
        }
    }


    private void consumeUsefulCompanionPotion() {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof CompanionPotionItem potion && potion.isUsefulFor(this)) {
                ItemStack consumed = stack.copyWithCount(1);
                stack.shrink(1);
                playConsumptionEffects(consumed);
                potion.applyTo(this);
                storeOrDrop(potion.emptyVessel());
                return;
            }
        }
    }

    /** Shared PvE/PvP and villager safety gate for every target source. */
    public boolean canHarm(Entity entity) {
        // A companion-owned projectile or explosion must never damage its caster.
        if (entity == this) {
            return false;
        }
        // Owner and same-owner companions stay allies even when PvP is enabled.
        if (entity == this.getOwner()) {
            return false;
        }
        // Beastmaster pets use an owner tag for entities such as Ocelots that cannot be tamed.
        if (Beastmaster.isBeastmasterPet(entity)) {
            return ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS);
        }
        if (entity instanceof Villager && !canHarmVillagers()) {
            return false;
        }
        if (entity instanceof Player) {
            return canHarmPlayers();
        }
        if (entity instanceof TamableAnimal tame && tame.isTame()) {
            if (this.getOwnerUUID() != null && this.getOwnerUUID().equals(tame.getOwnerUUID())) {
                return false;
            }
            return ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS);
        }
        return true;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity previous = getTarget();
        super.setTarget(target != null && !canHarm(target) ? null : target);
        LivingEntity accepted = getTarget();
        if (!this.level().isClientSide() && accepted != null && accepted != previous) {
            if (accepted == getLastHurtByMob()) {
                CompanionVoice.play(this, ModSounds.Cue.UNDER_ATTACK);
            } else {
                CompanionVoice.playEnemySpotted(this, accepted);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        clearHeldLight();
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel server) JobReservations.release(server, this.getUUID());
            com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook.discardFor(this);
            CompanionVoice.play(this, ModSounds.Cue.DEATH);
            clearNegativeEffectsBeforeResurrectionSave();
            if (this instanceof Beastmaster beastmaster) {
                beastmaster.forceDespawnPet();
            }
            dropResurrectionScroll();
        }
        super.die(source);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        clearHeldLight();
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel server) JobReservations.release(server, this.getUUID());
            com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook.discardFor(this);
        }
        super.remove(reason);
    }

    /**
     * Vanilla held torches and lanterns render correctly but do not emit light while carried.
     * Keep one invisible, server-authoritative Light block beside the companion and remove it
     * whenever the item, entity, chunk, or world state changes.
     */
    private void updateHeldLight() {
        if (!(this.level() instanceof ServerLevel server)) return;
        ItemStack offhand = getFunctionalEquipmentItem(EquipmentSlot.OFFHAND);
        if (!isHandheldLight(offhand) || !isAlive()
                || !server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            clearHeldLight();
            return;
        }

        BlockPos desired = blockPosition().above();
        if (getOwner() instanceof Player player && !player.mayInteract(server, desired)) {
            clearHeldLight();
            return;
        }
        if (!server.hasChunkAt(desired)) {
            clearHeldLight();
            return;
        }
        if (desired.equals(heldLightPos) && isTemporaryHeldLight(server.getBlockState(desired))) return;

        clearHeldLight();
        if (!server.getBlockState(desired).isAir()) return;
        BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LightBlock.MAX_LEVEL);
        if (server.setBlock(desired, light, 3)) heldLightPos = desired.immutable();
    }

    private void clearHeldLight() {
        if (heldLightPos == null) return;
        if (this.level() instanceof ServerLevel server && server.hasChunkAt(heldLightPos)
                && isTemporaryHeldLight(server.getBlockState(heldLightPos))) {
            server.setBlock(heldLightPos, Blocks.AIR.defaultBlockState(), 3);
        }
        heldLightPos = null;
    }

    private static boolean isHandheldLight(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) return false;
        return blockItem.getBlock() instanceof TorchBlock || blockItem.getBlock() instanceof LanternBlock;
    }

    private static boolean isTemporaryHeldLight(BlockState state) {
        return state.is(Blocks.LIGHT) && state.getValue(LightBlock.LEVEL) == LightBlock.MAX_LEVEL;
    }

    /** Prevent death-invalid harmful state from being copied into a resurrection scroll. */
    private void clearNegativeEffectsBeforeResurrectionSave() {
        for (MobEffectInstance effect : List.copyOf(this.getActiveEffects())) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                this.removeEffect(effect.getEffect());
            }
        }

        // Mekanism stores radiation as an optional entity capability instead of a MobEffect.
        if (!net.neoforged.fml.ModList.get().isLoaded("mekanism")) return;
        try {
            Class<?> capabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            Object radiationCapability = capabilities.getField("RADIATION_ENTITY").get(null);
            Object radiation = Entity.class
                    .getMethod("getCapability", radiationCapability.getClass())
                    .invoke(this, radiationCapability);
            if (radiation != null) {
                radiation.getClass().getMethod("set", double.class).invoke(radiation, 0.0D);
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Keep Mekanism optional; an unavailable compatibility API must not block death.
        }
    }

    @Override
    public void onRemovedFromLevel() {
        clearHeldLight();
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel server) {
            JobReservations.release(server, this.getUUID());
            com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook.discardFor(this);
            releaseDeliveryChunkTicket(server);
        }
        super.onRemovedFromLevel();
    }

    public void hurtArmor(DamageSource source, float amount) {
        if (!(amount <= 0.0F)) {
            amount /= 4.0F;
            if (amount < 1.0F)
                amount = 1.0F;

            for (ItemStack itemstack : this.getArmorSlots()) {
                if (itemstack.getItem() instanceof ArmorItem armorItem) {
                    itemstack.hurtAndBreak((int) amount, this, armorItem.getEquipmentSlot());
                }
            }
        }
    }

    @Override
    protected void dropEquipment() {
        // Override to prevent duplicating the stored inventory when a scroll drops.
    }

    private void dropResurrectionScroll() {
        if (!this.isTame()) {
            return; // only tamed companions drop scrolls
        }
        ItemStack scroll = ResurrectionScrollItem.createFromCompanion(this,
                ModItems.RESURRECTION_SCROLL.get());
        ItemEntity item = this.spawnAtLocation(scroll);
        if (item != null) {
            item.setUnlimitedLifetime();
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!canHarm(entity)) {
            return false;
        }
        boolean staminaEnabled = isStaminaEnabled();
        int meleeCost = meleeStaminaCost();
        if (!this.level().isClientSide && staminaEnabled && meleeCost > 0 && getStamina() <= 0
                && this.tickCount - lastExhaustedMeleeTick < 20) {
            return false; // Exhaustion slows shared melee cadence without disabling defense.
        }
        forceSwingAnimation(InteractionHand.MAIN_HAND);
        ItemStack itemstack = this.getMainHandItem();
        if (!this.level().isClientSide && !itemstack.isEmpty() && entity instanceof LivingEntity) {
            itemstack.hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
            if (this.getMainHandItem().isEmpty() && this.isTame() && this.getOwner() != null) {
                Component broken = Component.translatable("message.modern_companions.weapon_broke");
                this.getOwner()
                        .sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), broken));
            }
        }
        boolean hit = super.doHurtTarget(entity);
        if (!this.level().isClientSide) {
            if (staminaEnabled && hit) {
                this.entityData.set(STAMINA, CompanionResourceRules.spend(getStamina(), meleeCost, getStaminaMax()));
            }
            if (staminaEnabled && meleeCost > 0 && getStamina() <= 0) lastExhaustedMeleeTick = this.tickCount;
        }
        return hit;
    }

    /**
     * Unconditionally broadcast a swing animation, bypassing the internal "already
     * swinging" guard
     * so rapid hits and server-only damage paths still show the attack motion to
     * all clients.
     */
    private void forceSwingAnimation(InteractionHand hand) {
        if (!(this.level() instanceof ServerLevel server))
            return;
        this.swingTime = 0;
        this.swinging = true;
        this.swingingArm = hand;
        this.entityData.set(LAST_SWING_TICK, this.tickCount);
        ServerChunkCache chunks = server.getChunkSource();
        chunks.broadcastAndSend(this, new ClientboundAnimatePacket(this, hand == InteractionHand.MAIN_HAND ? 0 : 3));
    }

    public void checkArmor() {
        if (!ModConfig.safeGet(ModConfig.AUTO_EQUIP)) return;
        ItemStack head = this.getFunctionalEquipmentItem(EquipmentSlot.HEAD);
        ItemStack chest = this.getFunctionalEquipmentItem(EquipmentSlot.CHEST);
        ItemStack legs = this.getFunctionalEquipmentItem(EquipmentSlot.LEGS);
        ItemStack feet = this.getFunctionalEquipmentItem(EquipmentSlot.FEET);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (itemstack.getItem() instanceof ArmorItem armorItem) {
                switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> {
                        if (!hasDedicatedEquipment(EquipmentSlot.HEAD)
                                && (head.isEmpty() || CompanionData.isBetterArmor(itemstack, head)))
                            setItemSlot(EquipmentSlot.HEAD, itemstack);
                    }
                    case CHEST -> {
                        if (!hasDedicatedEquipment(EquipmentSlot.CHEST)
                                && (chest.isEmpty() || CompanionData.isBetterArmor(itemstack, chest)))
                            setItemSlot(EquipmentSlot.CHEST, itemstack);
                    }
                    case LEGS -> {
                        if (!hasDedicatedEquipment(EquipmentSlot.LEGS)
                                && (legs.isEmpty() || CompanionData.isBetterArmor(itemstack, legs)))
                            setItemSlot(EquipmentSlot.LEGS, itemstack);
                    }
                    case FEET -> {
                        if (!hasDedicatedEquipment(EquipmentSlot.FEET)
                                && (feet.isEmpty() || CompanionData.isBetterArmor(itemstack, feet)))
                            setItemSlot(EquipmentSlot.FEET, itemstack);
                    }
                }
            }
        }
    }

    public void checkWeapon() {
        // base class intentionally does nothing; subclasses choose weapons
    }

    /* ---------- Network-driven flag setters ---------- */
    public void applyFlag(String flag, boolean value) {
        switch (flag) {
            case "follow" -> {
                if (value && getJob().isWorker()) setWorkEnabled(false);
                setFollowing(value);
                if (value) {
                    setPatrolling(false);
                    setGuarding(false);
                }
            }
            case "patrol" -> {
                if (value && getJob().isWorker()) setWorkEnabled(false);
                setPatrolling(value);
                setFollowing(!value);
                setGuarding(false);
                if (value)
                    setPatrolPos(blockPosition());
            }
            case "guard" -> {
                if (value && getJob().isWorker()) setWorkEnabled(false);
                setGuarding(value);
                setPatrolling(false);
                setFollowing(!value);
                if (value)
                    setPatrolPos(blockPosition());
            }
            case "work" -> setWorkEnabled(value);
            case "hunt" -> setHunting(value);
            case "alert" -> setAlert(value);
            case "sprint" -> setSprintEnabled(value);
            case "pickup" -> setPickupEnabled(value);
            case "villagers" -> setCanHarmVillagers(value);
            case "players" -> setCanHarmPlayers(value);
            default -> {
            }
        }
    }

    public boolean getFlagValue(String flag) {
        return switch (flag) {
            case "follow" -> isFollowing();
            case "patrol" -> isPatrolling();
            case "guard" -> isGuarding();
            case "work" -> isWorkEnabled();
            case "hunt" -> isHunting();
            case "alert" -> isAlert();
            case "sprint" -> isSprintEnabled();
            case "pickup" -> isPickupEnabled();
            case "villagers" -> canHarmVillagers();
            case "players" -> canHarmPlayers();
            default -> false;
        };
    }

    /**
     * Gently attract and collect nearby item entities into the companion's
     * inventory to emulate player pickup.
     */
    private void collectNearbyItems() {
        double range = 3.0D;
        var box = this.getBoundingBox().inflate(range);
                for (ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, box,
                e -> e.isAlive() && !e.hasPickUpDelay()
                        && !JobDropClaims.isOwnedByOther(e, getUUID())
                        && !(getJob() == CompanionJob.CHEF && chefGoal != null && chefGoal.shouldDeferPickup(e))
                        && (isPickupEnabled() || JobDropClaims.isOwnedBy(e, getUUID())))) {
            if (item.getItem().isEmpty())
                continue;
            // Blacklist certain items from being auto-picked up (e.g., resurrection
            // scrolls).
            if (item.getItem().is(com.majorbonghits.moderncompanions.core.ModItems.RESURRECTION_SCROLL.get()))
                continue;
            var pull = this.position().subtract(item.position());
            if (pull.lengthSqr() > 0.01) {
                item.setDeltaMovement(item.getDeltaMovement().scale(0.9).add(pull.normalize().scale(0.08)));
            }
            if (this.distanceToSqr(item) <= 2.25D) {
                ItemStack stack = item.getItem();
                ItemStack leftover = tryInsertBackpackFirst(stack);
                if (!leftover.isEmpty()) {
                    leftover = this.inventory.addItem(leftover);
                }
                this.inventory.setChanged();
                if (leftover.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(leftover);
                }
            }
        }
    }

    /** Hunter-owned drops use the same backpack/inventory insertion path even when Pickup is off. */
    public boolean collectOwnedJobDrop(ItemEntity item) {
        if (item == null || !item.isAlive() || !JobDropClaims.isOwnedBy(item, getUUID())) return false;
        ItemStack leftover = tryInsertBackpackFirst(item.getItem().copy());
        if (!leftover.isEmpty()) leftover = this.inventory.addItem(leftover);
        this.inventory.setChanged();
        if (leftover.isEmpty()) {
            item.discard();
            return true;
        }
        item.setItem(leftover);
        return false;
    }

    /** Job-owned collection path for profession drops when the generic Pickup toggle is off. */
    public boolean collectJobItems(AABB area, Predicate<ItemStack> accepted) {
        if (area == null || accepted == null || level().isClientSide()) return false;
        boolean collected = false;
        for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, area,
                candidate -> candidate.isAlive() && !candidate.hasPickUpDelay()
                        && !JobDropClaims.isOwnedByOther(candidate, getUUID())
                        && accepted.test(candidate.getItem()))) {
            ItemStack leftover = tryInsertBackpackFirst(item.getItem().copy());
            if (!leftover.isEmpty()) leftover = inventory.addItem(leftover);
            if (leftover.isEmpty()) {
                item.discard();
                collected = true;
            } else {
                item.setItem(leftover);
            }
        }
        inventory.setChanged();
        return collected;
    }

    /**
     * Common shield detector so we don't place shields into the main hand when falling back.
     */
    protected boolean isShieldItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.SHIELD) || stack.is(TagsInit.Items.SHIELDS)) return true;
        return stack.getItem().builtInRegistryHolder().unwrapKey()
                .map(key -> key.location().getPath().toLowerCase(Locale.ROOT).contains("shield"))
                .orElse(false);
    }

    private boolean isOffhandEquipment(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.TOTEM_OF_UNDYING) || isShieldItem(stack)) return true;
        return stack.getItem() instanceof BlockItem blockItem
                && (blockItem.getBlock() instanceof TorchBlock || blockItem.getBlock() instanceof LanternBlock);
    }

    /**
     * Apply or remove the flat preferred-weapon bonus.
     */
    protected void setPreferredWeaponBonus(boolean enabled) {
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) return;
        damage.removeModifier(PREFERRED_WEAPON_MOD);
        if (enabled) {
            damage.addTransientModifier(new AttributeModifier(PREFERRED_WEAPON_MOD, 2.0D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /**
     * If Sophisticated Backpacks + Curios are present and the companion has a backpack
     * equipped in the back slot, try inserting into it before using the normal inventory.
     */
    private ItemStack tryInsertBackpackFirst(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        if (!net.neoforged.fml.ModList.get().isLoaded("curios") || !net.neoforged.fml.ModList.get().isLoaded("sophisticatedbackpacks")) {
            return stack;
        }
        try {
            var handlerOpt = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(this);
            if (handlerOpt.isEmpty()) return stack;
            var handler = handlerOpt.get();
            var backOpt = handler.getStacksHandler("back");
            if (backOpt.isEmpty()) return stack;
            var stacks = backOpt.get().getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack backpack = stacks.getStackInSlot(i);
                if (backpack.isEmpty()) {
                    continue;
                }
                if (!backpack.getItem().builtInRegistryHolder().key().location().getNamespace().equals("sophisticatedbackpacks")) {
                    continue;
                }

                net.neoforged.neoforge.items.IItemHandler handlerItem = null;
                // Preferred: direct wrapper (matches how SB exposes inventory for IO)
                try {
                    var wrapperCls = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper");
                    var fromStack = wrapperCls.getMethod("fromStack", ItemStack.class);
                    Object wrapper = fromStack.invoke(null, backpack);
                    var getInv = wrapperCls.getMethod("getInventoryForInputOutput");
                    handlerItem = (net.neoforged.neoforge.items.IItemHandler) getInv.invoke(wrapper);
                } catch (Exception ignored) { }

                // Fallback: item capability if wrapper failed
                if (handlerItem == null) {
                    handlerItem = backpack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM, null);
                }
                if (handlerItem == null) continue;

                ItemStack remainder = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(handlerItem, stack, false);
                if (remainder.getCount() != stack.getCount()) {
                    // Inserted at least part; stop processing other containers
                    return remainder;
                }
                stack = remainder;
            }
        } catch (Exception ignored) {
        }
        return stack;
    }

    /* ---------- RPG attribute generation & effects ---------- */

    private void assignRpgAttributes() {
        int[] stats = { 4, 4, 4, 4 }; // STR, DEX, INT, END base
        for (int i = 0; i < 23; i++) {
            stats[this.random.nextInt(stats.length)]++;
        }
        double specialistChance = 0.02D + (this.random.nextDouble() * 0.04D); // 2–6%
        if (this.random.nextDouble() < specialistChance) {
            int pick = this.random.nextInt(stats.length);
            stats[pick] += 5;
            setSpecialistAttributeIndex(pick);
        } else {
            setSpecialistAttributeIndex(-1);
        }
        setStrength(stats[0]);
        setDexterity(stats[1]);
        setIntelligence(stats[2]);
        setEndurance(stats[3]);
    }

    /**
     * Recalculate enchantment-driven bonuses from the companion's worn armor.
     * Returns true when a change is detected so downstream attribute application
     * can be refreshed.
     */
    private boolean recomputeEquipmentAttributeBonuses() {
        int newStr = getEnchantmentBonus(ModEnchantments.EMPOWER);
        int newDex = getEnchantmentBonus(ModEnchantments.NIMBILITY);
        int newInt = getEnchantmentBonus(ModEnchantments.ENLIGHTENMENT);
        int newEnd = getEnchantmentBonus(ModEnchantments.VITALITY);

        if (newStr == equipmentStrengthBonus && newDex == equipmentDexterityBonus
                && newInt == equipmentIntelligenceBonus && newEnd == equipmentEnduranceBonus) {
            return false;
        }

        equipmentStrengthBonus = newStr;
        equipmentDexterityBonus = newDex;
        equipmentIntelligenceBonus = newInt;
        equipmentEnduranceBonus = newEnd;
        return true;
    }

    private int getEnchantmentBonus(ResourceKey<Enchantment> enchantment) {
        var registry = this.level().registryAccess().registry(Registries.ENCHANTMENT);
        if (registry.isEmpty())
            return 0;
        int total = 0;
        for (ItemStack armor : this.getArmorSlots()) {
            total += registry.get().getHolder(enchantment)
                    .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, armor))
                    .orElse(0);
        }
        return total;
    }

    private void applyRpgAttributeModifiers() {
        applyStrengthModifiers();
        applyDexterityModifiers();
        applyEnduranceModifiers();
        // intelligence currently drives XP gain inside giveExperiencePoints; no
        // attribute modifier needed
        refreshPersonalityModifiers();
    }

    private void applyStrengthModifiers() {
        double delta = (getStrength() - 4) * 0.25D; // +0.25 damage per point over base
        applyModifier(Attributes.ATTACK_DAMAGE, "rpg_strength_damage", delta, AttributeModifier.Operation.ADD_VALUE);

        double kb = (getStrength() - 4) * 0.03D;
        applyModifier(Attributes.ATTACK_KNOCKBACK, "rpg_strength_knockback", kb, AttributeModifier.Operation.ADD_VALUE);
    }

    private void applyDexterityModifiers() {
        double speed = (getDexterity() - 4) * 0.003D;
        applyModifier(Attributes.MOVEMENT_SPEED, "rpg_dex_speed", speed, AttributeModifier.Operation.ADD_VALUE);

        double atkSpeed = (getDexterity() - 4) * 0.04D;
        applyModifier(Attributes.ATTACK_SPEED, "rpg_dex_attack_speed", atkSpeed, AttributeModifier.Operation.ADD_VALUE);

        double kbResist = Math.max(0.0D, (getDexterity() - 10) * 0.01D); // slight dodge feel at high dex
        applyModifier(Attributes.KNOCKBACK_RESISTANCE, "rpg_dex_kb_resist", kbResist,
                AttributeModifier.Operation.ADD_VALUE);
    }

    private void applyEnduranceModifiers() {
        int baseline = ModConfig.BASE_HEALTH != null ? ModConfig.safeGet(ModConfig.BASE_HEALTH) : 20;
        int baseBonusHealth = getEnduranceBonusHealth();
        int desiredBase = Math.max(getBaseHealth(), baseline + baseBonusHealth);
        setBaseHealth(desiredBase);
        modifyMaxHealth(desiredBase - 20, "companion base health", true);

        int gearHealthBonus = Math.max(0, getEndurance() - getBaseEndurance());
        applyModifier(Attributes.MAX_HEALTH, "rpg_end_gear_health", gearHealthBonus,
                AttributeModifier.Operation.ADD_VALUE);

        double kbResist = Math.min(0.6D, (getEndurance() - 4) * 0.02D);
        applyModifier(Attributes.KNOCKBACK_RESISTANCE, "rpg_end_kb_resist", kbResist,
                AttributeModifier.Operation.ADD_VALUE);
    }

    private void refreshPersonalityModifiers() {
        if (level().isClientSide()) return;
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        var armor = this.getAttribute(Attributes.ARMOR);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        var kb = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (damage != null) damage.removeModifier(MOD_MORALE_DAMAGE);
        if (armor != null) armor.removeModifier(MOD_MORALE_ARMOR);
        if (speed != null) speed.removeModifier(MOD_TRAIT_QUICKSTEP);
        if (speed != null) speed.removeModifier(MOD_TRAIT_RECKLESS);
        if (kb != null) kb.removeModifier(MOD_TRAIT_STALWART);
        if (damage != null) damage.removeModifier(MOD_TRAIT_BRAVE);
        if (armor != null) armor.removeModifier(MOD_TRAIT_GUARDIAN);
        if (armor != null) armor.removeModifier(MOD_TRAIT_DEVOTED);
        if (damage != null) {
            damage.removeModifier(MOD_TRAIT_NIGHT_OWL_DAMAGE);
            damage.removeModifier(MOD_TRAIT_SUN_DAMAGE);
            damage.removeModifier(MOD_TRAIT_MELANCHOLIC);
        }
        if (speed != null) {
            speed.removeModifier(MOD_TRAIT_NIGHT_OWL_SPEED);
            speed.removeModifier(MOD_TRAIT_SUN_SPEED);
        }

        float morale = getMorale();
        if (morale > 0.5f) {
            if (damage != null) {
                damage.addTransientModifier(new AttributeModifier(MOD_MORALE_DAMAGE, 0.5D, AttributeModifier.Operation.ADD_VALUE));
            }
            if (armor != null) {
                armor.addTransientModifier(new AttributeModifier(MOD_MORALE_ARMOR, 0.5D, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (morale < -0.5f) {
            if (damage != null) {
                damage.addTransientModifier(new AttributeModifier(MOD_MORALE_DAMAGE, -0.5D, AttributeModifier.Operation.ADD_VALUE));
            }
            if (armor != null) {
                armor.addTransientModifier(new AttributeModifier(MOD_MORALE_ARMOR, -0.5D, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (hasTrait("trait_quickstep") && speed != null) {
            speed.addTransientModifier(new AttributeModifier(MOD_TRAIT_QUICKSTEP, 0.02D, AttributeModifier.Operation.ADD_VALUE));
        }
        if (hasTrait("trait_reckless") && speed != null) {
            speed.addTransientModifier(new AttributeModifier(MOD_TRAIT_RECKLESS, 0.01D, AttributeModifier.Operation.ADD_VALUE));
        }
        if (hasTrait("trait_stalwart") && kb != null) {
            kb.addTransientModifier(new AttributeModifier(MOD_TRAIT_STALWART, 0.05D, AttributeModifier.Operation.ADD_VALUE));
        }
        if (hasTrait("trait_brave") && damage != null) {
            damage.addTransientModifier(new AttributeModifier(MOD_TRAIT_BRAVE, 0.25D, AttributeModifier.Operation.ADD_VALUE));
        }
        if (hasTrait("trait_guardian") && armor != null) {
            armor.addTransientModifier(new AttributeModifier(MOD_TRAIT_GUARDIAN, 0.25D, AttributeModifier.Operation.ADD_VALUE));
        }
        if (hasTrait("trait_devoted") && armor != null) {
            armor.addTransientModifier(new AttributeModifier(MOD_TRAIT_DEVOTED, 0.15D, AttributeModifier.Operation.ADD_VALUE));
        }

        // Time-of-day traits
        if (this.level() != null) {
            boolean isDay = this.level().isDay();
            boolean isNight = !isDay;
            if (isNight && hasTrait("trait_night_owl")) {
                if (damage != null) {
                    damage.addTransientModifier(new AttributeModifier(MOD_TRAIT_NIGHT_OWL_DAMAGE, 0.25D, AttributeModifier.Operation.ADD_VALUE));
                }
                if (speed != null) {
                    speed.addTransientModifier(new AttributeModifier(MOD_TRAIT_NIGHT_OWL_SPEED, 0.01D, AttributeModifier.Operation.ADD_VALUE));
                }
            }
            if (isDay && hasTrait("trait_sun_blessed")) {
                if (damage != null) {
                    damage.addTransientModifier(new AttributeModifier(MOD_TRAIT_SUN_DAMAGE, 0.25D, AttributeModifier.Operation.ADD_VALUE));
                }
                if (speed != null) {
                    speed.addTransientModifier(new AttributeModifier(MOD_TRAIT_SUN_SPEED, 0.01D, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        }

        // Melancholic: minor penalty when morale is low
        if (hasTrait("trait_melancholic") && damage != null && getMorale() < -0.2f) {
            damage.addTransientModifier(new AttributeModifier(MOD_TRAIT_MELANCHOLIC, -0.2D, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private int getEnduranceBonusHealth() {
        return Math.max(0, getBaseEndurance() - 4); // +1 hp per END over base (0.5 hearts)
    }

    private float applyEnduranceResistance(DamageSource source, float amount) {
        amount = MagicCastingCompat.reduceSpellDamage(this, source, amount);
        boolean physical = !source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                && !source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!physical) {
            return amount;
        }
        float reduction = (float) Math.min(0.35D, Math.max(0.0D, (getEndurance() - 4) * 0.015D));
        return amount * (1.0F - reduction);
    }

    private void clampHealthToMax() {
        float max = this.getMaxHealth();
        if (this.getHealth() > max) {
            this.setHealth(max);
        }
    }

    private void applyModifier(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            String idName, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null)
            return;
        ResourceLocation id = ResourceLocation
                .fromNamespaceAndPath(com.majorbonghits.moderncompanions.ModernCompanions.MOD_ID, idName);
        instance.removeModifier(id);
        if (value != 0.0D) {
            AttributeModifier modifier = new AttributeModifier(id, value, op);
            instance.addPermanentModifier(modifier);
        }
    }

    private float getExperienceGainMultiplier() {
        float mult = 1.0F + (float) ((getIntelligence() - 4) * 0.03D);
        if (hasTrait("trait_disciplined")) {
            mult += 0.05F;
        }
        return mult;
    }

    public boolean hasTrait(String traitId) {
        if (traitId == null || traitId.isEmpty()) return false;
        return traitId.equals(getPrimaryTraitId()) || traitId.equals(getSecondaryTraitId());
    }

    private boolean shouldRequestFood() {
        return ModConfig.safeGet(ModConfig.LOW_HEALTH_FOOD)
                && this.isTame()
                && this.getHealth() <= this.getMaxHealth() * ModConfig.safeGet(ModConfig.LOW_HEALTH_FOOD_THRESHOLD).floatValue()
                && !hasFoodInInventory()
                && this.tickCount - lastFoodRequestTick > FOOD_REQUEST_COOLDOWN_TICKS;
    }

    private void requestFoodFromOwner() {
        if (this.level().isClientSide())
            return;
        if (!this.isTame())
            return;
        lastFoodRequestTick = this.tickCount;
        if (this.getOwner() instanceof ServerPlayer player) {
            Component text = randomFoodRequestLine();
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), text));
        }
    }

    private void tickBondAndMorale() {
        if (!ModConfig.safeGet(ModConfig.BOND_ENABLED))
            return;
        if (!this.isTame())
            return;
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof Player))
            return;
        double dist2 = this.distanceToSqr(owner);
        if (dist2 > 24 * 24)
            return;
        bondTickCounter++;
        int interval = ModConfig.safeGet(ModConfig.BOND_TICK_INTERVAL);
        if (bondTickCounter >= Math.max(20, interval)) {
            int base = ModConfig.safeGet(ModConfig.BOND_TIME_XP);
            awardBondXp(applyBondTraitMultiplier(base, false, true, false));
            bondTickCounter = 0;
        }
    }

    private int applyBondTraitMultiplier(int base, boolean feeding, boolean passive, boolean resurrect) {
        double mult = 1.0D;
        if (hasTrait("trait_devoted")) mult += 0.2D;
        if (feeding && hasTrait("trait_glutton")) mult += 0.15D;
        if (base <= 0) return 0;
        return Math.max(1, (int) Math.round(base * mult));
    }

    private void trackDistanceNearOwner() {
        double dx = this.getX() - lastTrackX;
        double dz = this.getZ() - lastTrackZ;
        double moved = Math.sqrt(dx * dx + dz * dz);
        lastTrackX = this.getX();
        lastTrackY = this.getY();
        lastTrackZ = this.getZ();
        if (!this.isTame()) return;
        LivingEntity owner = this.getOwner();
        if (owner == null) return;
        if (this.distanceToSqr(owner) > 24 * 24) return;
        if (moved > 0.0001D) {
            distanceAccumulator += moved;
            long whole = (long) distanceAccumulator;
            if (whole > 0) {
                addDistanceTraveled(whole);
                distanceAccumulator -= whole;
            }
        }
    }

    private void tickAging() {
        if (this.level().isClientSide()) return;
        long now = this.level().getGameTime();
        if (personality.getLastAgeCheckGameTime() < 0) {
            personality.setLastAgeCheckGameTime(now);
            return;
        }
        long elapsed = now - personality.getLastAgeCheckGameTime();
        if (elapsed >= AGE_INTERVAL_TICKS) {
            long years = elapsed / AGE_INTERVAL_TICKS;
            if (years > 0) {
                setAgeYears(personality.getAgeYears() + (int) years);
                long remainder = elapsed - years * AGE_INTERVAL_TICKS;
                personality.setLastAgeCheckGameTime(now - remainder);
            }
        }
    }

    /**
     * For older companions without newly-added flavor data, roll safe defaults.
     */
    private void rollMissingFlavorData() {
        if (personality.getPrimaryTrait().isEmpty() && personality.getSecondaryTrait().isEmpty()) {
            // Only roll traits once for legacy companions that had none.
            personality.rollTraits(this.random, ModConfig.safeGet(ModConfig.TRAITS_ENABLED),
                    ModConfig.safeGet(ModConfig.SECONDARY_TRAIT_CHANCE));
        }
        if (personality.getBackstoryId().isEmpty()) {
            personality.rollBackstory(this.random);
        }
        if (personality.getAgeYears() <= 0) {
            setAgeYears(this.random.nextInt(18, 36));
            personality.setLastAgeCheckGameTime(this.level().getGameTime());
        }
        syncPersonalityToData();
    }

    private Component randomFoodRequestLine() {
        return Component.translatable("dialogue.modern_companions.food_request." + this.random.nextInt(322));
    }
}
