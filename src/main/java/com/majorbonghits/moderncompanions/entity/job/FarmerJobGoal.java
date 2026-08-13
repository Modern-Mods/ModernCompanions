package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;

/** Safe, resumable crop worker with separate planting, harvest, and growth queues. */
public final class FarmerJobGoal extends ResumableJobGoal {
    private static final int SEARCH_COOLDOWN = 40;
    private static final int SEARCH_COLUMNS_PER_TICK = 96;
    private static final int MAX_SCAN_DEPTH = 48;
    private static final int MAX_ACTION_RETRIES = 3;
    private static final int MAX_STALL_TICKS = 120;

    private enum Action { PLANTING, HARVESTING, BONE_MEAL }

    private record PlantingChoice(ItemStack stack, Block block) { }

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private final Deque<BlockPos> plantQueue = new ArrayDeque<>();
    private final Deque<BlockPos> harvestQueue = new ArrayDeque<>();
    private final Deque<BlockPos> boneMealQueue = new ArrayDeque<>();
    private final Map<BlockPos, Block> preferredCrops = new HashMap<>();
    private final Map<BlockPos, Integer> blockedUntil = new HashMap<>();

    private BlockPos target;
    private BlockPos stand;
    private Action action;
    private int searchCooldown;
    private int scanColumn;
    private BlockPos scanCenter;
    private int scanRadius;
    private boolean scanComplete;
    private int workTicks;
    private int swingCooldown;
    private int stallTicks;
    private Vec3 lastPosition = Vec3.ZERO;
    private boolean restoredPlan;

    public FarmerJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        super(companion, CompanionJob.FARMER);
        this.companion = companion;
        this.searchRadius = Math.max(4, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (!retryReady()) return false;
        if (target == null && !selectNextTarget()) {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            if (!discoverTargets()) return false;
            if (!selectNextTarget()) return false;
        }
        if (target == null) return false;
        if (isBackedOff(target)) {
            waiting("job_status.modern_companions.farm_blocked");
            return false;
        }
        if (!reserve("farm:" + target.asLong())) {
            waiting("job_status.modern_companions.farm_reserved");
            return false;
        }
        if (stand == null) stand = WorkerSite.findApproachStand(companion, target, 2);
        phase(JobPhase.TRAVELLING, actionStatus(), target);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveJob() && target != null;
    }

    @Override
    public void start() {
        lastPosition = companion.position();
        moveToStand();
    }

    @Override
    public void stop() {
        companion.getNavigation().stop();
        workTicks = 0;
        swingCooldown = 0;
        stallTicks = 0;
    }

    @Override
    public void tick() {
        if (!isActiveJob() || target == null) return;
        if (!retryReady()) return;
        if (!targetStillMatchesAction()) {
            requeueCurrentTarget();
            return;
        }
        if (stand == null) stand = WorkerSite.findApproachStand(companion, target, 2);
        if (stand == null) {
            fail("job_status.modern_companions.farm_route_blocked");
            return;
        }

        double distance = companion.distanceToSqr(Vec3.atCenterOf(stand));
        if (distance > 2.25D) {
            phase(JobPhase.TRAVELLING, actionStatus(), target);
            if (companion.position().distanceToSqr(lastPosition) < 0.04D) {
                if (++stallTicks >= MAX_STALL_TICKS) {
                    fail("job_status.modern_companions.farm_route_blocked");
                }
            } else {
                lastPosition = companion.position();
                stallTicks = 0;
            }
            if (target != null && companion.getNavigation().isDone()) moveToStand();
            return;
        }
        stallTicks = 0;
        if (!WorkerSite.canActFromStand(companion, target, stand, WorkerSite.INTERACT_RANGE_SQR)) {
            fail("job_status.modern_companions.farm_blocked");
            return;
        }

        companion.getLookControl().setLookAt(Vec3.atCenterOf(target));
        switch (action) {
            case HARVESTING -> tickHarvest();
            case PLANTING -> plant();
            case BONE_MEAL -> boneMeal();
        }
    }

    private void tickHarvest() {
        if (workTicks <= 0) {
            phase(JobPhase.WORKING, "job_status.modern_companions.harvesting", target);
            workTicks = computeHarvestTicks(target);
            swingCooldown = 0;
        }
        if (swingCooldown-- <= 0) {
            companion.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
            swingCooldown = 6;
        }
        workTicks--;
        if (workTicks <= 0) harvest();
    }

    private void harvest() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        BlockState state = server.getBlockState(target);
        Block crop = state.getBlock();
        WorkerActionResult result = WorkerBlockActions.breakBlockResult(companion, target, stand,
                WorkerSite.INTERACT_RANGE_SQR);
        if (result != WorkerActionResult.SUCCESS) {
            fail(resultStatus(result));
            return;
        }
        preferredCrops.put(target.immutable(), crop);
        companion.incrementFarmerHarvestedSession();
        workTicks = 0;
        swingCooldown = 0;
        action = Action.PLANTING;
        phase(JobPhase.COLLECTING, "job_status.modern_companions.planting", target);
        savePlan();
    }

    private void plant() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        PlantingChoice choice = findPlantingChoice(target);
        if (choice == null) {
            fail("job_status.modern_companions.no_seeds");
            return;
        }
        ItemStack seed = choice.stack();
        BlockState plantState = choice.block().defaultBlockState();
        if (!isCropLike(plantState) || !plantState.canSurvive(server, target)) {
            fail("job_status.modern_companions.farm_blocked");
            return;
        }
        phase(JobPhase.WORKING, "job_status.modern_companions.planting", target);
        WorkerActionResult result = WorkerBlockActions.placeResult(companion, target, stand, plantState);
        if (result != WorkerActionResult.SUCCESS) {
            fail(resultStatus(result));
            return;
        }
        preferredCrops.remove(target);
        companion.incrementFarmerPlantedSession();
        completeTarget();
    }

    private void boneMeal() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        ItemStack meal = findBoneMeal();
        if (meal.isEmpty()) {
            fail("job_status.modern_companions.no_bone_meal");
            return;
        }
        BlockState state = server.getBlockState(target);
        if (!(state.getBlock() instanceof BonemealableBlock bonemealable)
                || !bonemealable.isValidBonemealTarget(server, target, state)) {
            requeueCurrentTarget();
            return;
        }
        phase(JobPhase.WORKING, "job_status.modern_companions.growing", target);
        WorkerActionResult result = WorkerBlockActions.boneMealResult(companion, target, stand, meal);
        if (result != WorkerActionResult.SUCCESS) {
            fail(resultStatus(result));
            return;
        }
        if (isMatureCrop(server.getBlockState(target))) completeTarget();
        else requeueCurrentTarget();
    }

    private boolean discoverTargets() {
        BlockPos center = companion.getWorkCenter().orElse(companion.blockPosition());
        int radius = effectiveRadius();
        if (scanCenter == null || !scanCenter.equals(center) || scanRadius != radius) {
            scanCenter = center.immutable();
            scanRadius = radius;
            scanColumn = 0;
            scanComplete = false;
            clearQueues();
        }
        int side = radius * 2 + 1;
        int total = side * side;
        Level level = companion.level();
        for (int budget = 0; budget < SEARCH_COLUMNS_PER_TICK && scanColumn < total; budget++) {
            long encoded = WorkerSafetyPredicates.spiralOffset(scanColumn++);
            int dx = (int) (encoded >> 32);
            int dz = (int) encoded;
            BlockPos column = center.offset(dx, 0, dz);
            if (center.distSqr(column) > (long) radius * radius || !level.hasChunkAt(column)) continue;
            int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
            int bottom = Math.max(level.getMinBuildHeight(), top - MAX_SCAN_DEPTH);
            for (int y = top; y >= bottom; y--) {
                enqueueClassified(new BlockPos(column.getX(), y, column.getZ()));
            }
        }
        if (scanColumn < total) return !plantQueue.isEmpty() || !harvestQueue.isEmpty() || !boneMealQueue.isEmpty();
        scanComplete = true;
        if (plantQueue.isEmpty() && harvestQueue.isEmpty() && boneMealQueue.isEmpty()) {
            companion.setJobStatus("job_status.modern_companions.no_crops");
            searchCooldown = SEARCH_COOLDOWN;
            scanColumn = 0;
            scanComplete = false;
            return false;
        }
        return true;
    }

    private void enqueueClassified(BlockPos pos) {
        if (!companion.isInWorkArea(pos) || isBackedOff(pos)) return;
        Level level = companion.level();
        BlockState state = level.getBlockState(pos);
        if (isMatureCrop(state)) {
            addIfMissing(harvestQueue, pos);
        } else if (isPlantableTarget(pos)) {
            addIfMissing(plantQueue, pos);
        } else if (canUseBoneMeal(pos, state)) {
            addIfMissing(boneMealQueue, pos);
        }
    }

    private boolean selectNextTarget() {
        if (target != null) return true;
        // Finish mature crops first so a field does not stay unharvested while
        // the worker services every empty farmland square in scan order.
        BlockPos next = pollValid(harvestQueue, Action.HARVESTING);
        if (next == null) next = pollValid(plantQueue, Action.PLANTING);
        if (next == null) next = pollValid(boneMealQueue, Action.BONE_MEAL);
        if (next == null) return false;
        target = next.immutable();
        stand = null;
        workTicks = 0;
        swingCooldown = 0;
        savePlan();
        return true;
    }

    private BlockPos pollValid(Deque<BlockPos> queue, Action expected) {
        while (!queue.isEmpty()) {
            BlockPos pos = queue.pollFirst();
            if (isBackedOff(pos)) continue;
            BlockState state = companion.level().getBlockState(pos);
            boolean valid = switch (expected) {
                case PLANTING -> isPlantableTarget(pos);
                case HARVESTING -> isMatureCrop(state);
                case BONE_MEAL -> canUseBoneMeal(pos, state);
            };
            if (valid) {
                action = expected;
                return pos;
            }
        }
        return null;
    }

    private boolean targetStillMatchesAction() {
        if (target == null || action == null) return false;
        BlockState state = companion.level().getBlockState(target);
        return switch (action) {
            case PLANTING -> isPlantableTarget(target);
            case HARVESTING -> isMatureCrop(state);
            case BONE_MEAL -> canUseBoneMeal(target, state);
        };
    }

    private void requeueCurrentTarget() {
        if (target == null) return;
        BlockPos old = target;
        target = null;
        stand = null;
        workTicks = 0;
        enqueueClassified(old);
        savePlan();
    }

    private void completeTarget() {
        BlockPos completed = target;
        target = null;
        stand = null;
        action = null;
        workTicks = 0;
        swingCooldown = 0;
        blockedUntil.remove(completed);
        release("farm:" + completed.asLong());
        companion.checkpointJob(JobPhase.SEARCHING, null);
        savePlan();
    }

    private void fail(String status) {
        if (retry(status, MAX_ACTION_RETRIES)) return;
        BlockPos failed = target == null ? null : target.immutable();
        if (failed != null) blockedUntil.put(failed, companion.tickCount + 200);
        workTicks = 0;
        swingCooldown = 0;
        companion.checkpointJob(JobPhase.WAITING, failed);
        savePlan();
    }

    private String resultStatus(WorkerActionResult result) {
        return switch (result) {
            case INVENTORY_FULL -> "job_status.modern_companions.inventory_full";
            case TOOL_MISSING -> "job_status.modern_companions.no_hoe";
            case PROTECTED -> "job_status.modern_companions.farm_protected";
            case INVALID_TARGET -> "job_status.modern_companions.farm_blocked";
            case RETRYABLE_BLOCKED, UNLOADED, UNSAFE, SUCCESS -> "job_status.modern_companions.farm_blocked";
        };
    }

    private void moveToStand() {
        if (stand != null) companion.getNavigation().moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 1.0D);
    }

    private int computeHarvestTicks(BlockPos pos) {
        BlockState state = companion.level().getBlockState(pos);
        float hardness = Math.max(0.05F, state.getDestroySpeed(companion.level(), pos));
        float speed = Math.max(0.05F, companion.getMainHandItem().getDestroySpeed(state));
        int ticks = (int) Math.ceil(8.0D * hardness / speed);
        return Math.max(4, Math.min(40, ticks));
    }

    private String actionStatus() {
        return switch (action) {
            case PLANTING -> "job_status.modern_companions.planting";
            case HARVESTING -> "job_status.modern_companions.harvesting";
            case BONE_MEAL -> "job_status.modern_companions.growing";
            case null -> "job_status.modern_companions.searching";
        };
    }

    private boolean isActiveJob() {
        restorePlan();
        if (!enabled || companion.getJob() != CompanionJob.FARMER || !workActive(enabled)) return false;
        if (!companion.isTame() || companion.isOrderedToSit()) return false;
        companion.ensureJobToolEquipped();
        if (!JobToolPolicy.matches(CompanionJob.FARMER, companion.getMainHandItem())) {
            companion.setJobStatus("job_status.modern_companions.no_hoe");
            return false;
        }
        if (companion.getWorkCenter().isEmpty()) {
            companion.setJobStatus("job_status.modern_companions.assign_chest");
            return false;
        }
        return true;
    }

    private void restorePlan() {
        if (restoredPlan) return;
        restoredPlan = true;
        CompoundTag payload = companion.getJobPlanPayload();
        BlockPos saved = payload.contains("FieldCell") ? BlockPos.of(payload.getLong("FieldCell")) : null;
        if (saved == null) saved = companion.getJobCheckpointTarget().orElse(null);
        if (saved == null || !companion.isInWorkArea(saved)) return;
        try {
            action = Action.valueOf(payload.getString("FieldAction"));
        } catch (IllegalArgumentException ignored) {
            action = companion.level().getBlockState(saved).isAir() ? Action.PLANTING : Action.HARVESTING;
        }
        target = saved.immutable();
        if (payload.contains("FieldStand")) stand = BlockPos.of(payload.getLong("FieldStand"));
        if (payload.contains("Crop", 8)) {
            ResourceLocation id = ResourceLocation.tryParse(payload.getString("Crop"));
            if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
                preferredCrops.put(target, BuiltInRegistries.BLOCK.get(id));
            }
        }
    }

    private void savePlan() {
        CompoundTag payload = companion.getJobPlanPayload();
        if (target == null) {
            payload.remove("FieldCell");
            payload.remove("FieldStand");
            payload.remove("FieldAction");
            payload.remove("Crop");
        } else {
            payload.putLong("FieldCell", target.asLong());
            if (stand == null) payload.remove("FieldStand");
            else payload.putLong("FieldStand", stand.asLong());
            if (action == null) payload.remove("FieldAction");
            else payload.putString("FieldAction", action.name());
            Block crop = preferredCrops.get(target);
            if (crop == null) payload.remove("Crop");
            else payload.putString("Crop", BuiltInRegistries.BLOCK.getKey(crop).toString());
        }
        companion.setJobPlanPayload(payload);
    }

    private int effectiveRadius() {
        return Math.min(128, Math.max(searchRadius, companion.getPatrolRadius()));
    }

    private boolean isPlantableTarget(BlockPos pos) {
        Level level = companion.level();
        if (!level.getBlockState(pos).isAir()) return false;
        Block preferred = preferredCrops.get(pos);
        if (preferred != null) {
            BlockState preferredState = preferred.defaultBlockState();
            return isCropLike(preferredState) && preferredState.canSurvive(level, pos);
        }
        BlockState support = level.getBlockState(pos.below());
        return support.is(Blocks.FARMLAND) || support.is(Blocks.SOUL_SAND);
    }

    private boolean isMatureCrop(BlockState state) {
        if (!isCropLike(state)) return false;
        if (state.hasProperty(DoublePlantBlock.HALF)
                && state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.LOWER) return false;
        IntegerProperty age = ageProperty(state);
        return age != null && state.getValue(age) >= age.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private boolean canUseBoneMeal(BlockPos pos, BlockState state) {
        if (!ModConfig.safeGet(ModConfig.JOB_FARMER_BONE_MEAL_ENABLED) || findBoneMeal().isEmpty()) return false;
        return isCropLike(state) && state.getBlock() instanceof BonemealableBlock bonemealable
                && bonemealable.isValidBonemealTarget(companion.level(), pos, state);
    }

    private boolean isCropLike(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CropBlock || block instanceof StemBlock || block instanceof NetherWartBlock
                || state.is(BlockTags.CROPS);
    }

    private IntegerProperty ageProperty(BlockState state) {
        if (state.hasProperty(CropBlock.AGE)) return CropBlock.AGE;
        if (state.hasProperty(StemBlock.AGE)) return StemBlock.AGE;
        if (state.hasProperty(NetherWartBlock.AGE)) return NetherWartBlock.AGE;
        if (state.hasProperty(BlockStateProperties.AGE_3)) return BlockStateProperties.AGE_3;
        if (state.hasProperty(BlockStateProperties.AGE_2)) return BlockStateProperties.AGE_2;
        if (state.hasProperty(BlockStateProperties.AGE_1)) return BlockStateProperties.AGE_1;
        return null;
    }

    /**
     * Most vanilla seed items are ItemNameBlockItems, but an explicit mapping
     * keeps ordinary Item implementations and compatible modded crop items
     * from turning replanting into a class-cast assumption.
     */
    private PlantingChoice findPlantingChoice(BlockPos pos) {
        Block preferred = preferredCrops.get(pos);
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            ItemStack stack = companion.getInventory().getItem(slot);
            Block block = plantingBlockFor(stack);
            if (block == null) continue;
            if (preferred != null && block != preferred) continue;
            BlockState state = block.defaultBlockState();
            if (isCropLike(state) && state.canSurvive(companion.level(), pos)) {
                return new PlantingChoice(stack, block);
            }
        }
        return null;
    }

    private Block plantingBlockFor(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) return blockItem.getBlock();
        Item item = stack.getItem();
        if (item == Items.WHEAT_SEEDS) return Blocks.WHEAT;
        if (item == Items.CARROT) return Blocks.CARROTS;
        if (item == Items.POTATO) return Blocks.POTATOES;
        if (item == Items.BEETROOT_SEEDS) return Blocks.BEETROOTS;
        if (item == Items.PUMPKIN_SEEDS) return Blocks.PUMPKIN_STEM;
        if (item == Items.MELON_SEEDS) return Blocks.MELON_STEM;
        if (item == Items.NETHER_WART) return Blocks.NETHER_WART;
        if (item == Items.TORCHFLOWER_SEEDS) return Blocks.TORCHFLOWER_CROP;
        if (item == Items.PITCHER_POD) return Blocks.PITCHER_CROP;
        return null;
    }

    private ItemStack findBoneMeal() {
        if (!ModConfig.safeGet(ModConfig.JOB_FARMER_BONE_MEAL_ENABLED)) return ItemStack.EMPTY;
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            ItemStack stack = companion.getInventory().getItem(slot);
            if (stack.is(Items.BONE_MEAL)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean isBackedOff(BlockPos pos) {
        return blockedUntil.getOrDefault(pos, 0) > companion.tickCount;
    }

    private void addIfMissing(Deque<BlockPos> queue, BlockPos pos) {
        if (!queue.contains(pos)) queue.addLast(pos.immutable());
    }

    private void clearQueues() {
        plantQueue.clear();
        harvestQueue.clear();
        boneMealQueue.clear();
    }
}
