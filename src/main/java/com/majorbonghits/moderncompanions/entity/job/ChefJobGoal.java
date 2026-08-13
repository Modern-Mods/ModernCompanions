package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Chef job converts raw food in the companion inventory when standing near a
 * heat source (campfire/furnace/smoker) through native recipe/workstation behavior.
 */
public class ChefJobGoal extends ResumableJobGoal {
    private static final int COOK_COOLDOWN = 40;

    private static final TagKey<Item> RAW_MEAT = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("modern_companions", "raw_meat"));

    private final AbstractHumanCompanionEntity companion;
    private final int searchRadius;
    private final boolean enabled;
    private BlockPos heatSource;
    private BlockPos heatStand;
    private BlockPos supplyChest;
    private BlockPos supplyStand;
    private int cooldown;
    private ItemStack pendingFurnaceOutput = ItemStack.EMPTY;
    private ItemStack pendingCampfireOutput = ItemStack.EMPTY;
    private int pendingCampfireSlot = -1;
    private long pendingCampfireReadyAt = -1L;
    private int pendingCampfireInventoryCount = -1;
    private boolean restoredPlan;

    public ChefJobGoal(AbstractHumanCompanionEntity companion, int searchRadius, boolean enabled) {
        super(companion, CompanionJob.CHEF);
        this.companion = companion;
        this.searchRadius = Math.max(3, searchRadius);
        this.enabled = enabled;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!isActiveJob()) return false;
        if (pendingFurnaceOutput.isEmpty() && pendingCampfireOutput.isEmpty()
                && findFirstRawIngredient().isEmpty()) {
            if (prepareSupply()) {
                phase(JobPhase.TRAVELLING, "job_status.modern_companions.getting_raw_meat", supplyChest);
                return true;
            }
            waiting("job_status.modern_companions.no_raw_meat");
            return false;
        }
        if (heatSource == null) {
            companion.getJobCheckpointTarget().filter(this::isHeatSource).ifPresent(saved -> {
                heatSource = saved;
                heatStand = WorkerSite.findApproachStand(companion, saved, 2);
            });
        }
        if (heatSource == null || heatStand == null || !isHeatSource(heatSource)) heatSource = findHeatSource();
        if (heatSource != null && heatStand != null && !reserve("workstation:" + heatSource.asLong())) {
            waiting("job_status.modern_companions.workstation_reserved");
            return false;
        }
        return heatSource != null && heatStand != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (supplyChest != null && supplyStand != null) {
            return isActiveJob() && WorkerSite.canPlanStand(companion, supplyChest, supplyStand, WorkerSite.INTERACT_RANGE_SQR);
        }
        return isActiveJob() && heatSource != null && heatStand != null && isHeatSource(heatSource)
                && WorkerSite.canPlanStand(companion, heatSource, heatStand, WorkerSite.INTERACT_RANGE_SQR);
    }

    @Override
    public void start() {
        if (supplyChest != null) moveToSupply(); else moveToHeat();
    }

    @Override
    public void stop() {
        // A workstation is a resumable checkpoint, not disposable preemption state.
        companion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (supplyChest != null) {
            serviceSupplyChest();
            return;
        }
        if (heatSource == null || heatStand == null) return;
        if (!isHeatSource(heatSource) || !WorkerSite.canPlanStand(companion, heatSource, heatStand, WorkerSite.INTERACT_RANGE_SQR)) {
            heatSource = findHeatSource();
            if (heatSource == null) return;
            moveToHeat();
            return;
        }
        double dist = companion.distanceToSqr(heatStand.getX() + 0.5D, heatStand.getY(), heatStand.getZ() + 0.5D);
        if (dist > 2.25D) {
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.travelling_to_heat", heatSource);
            if (companion.getNavigation().isDone()) moveToHeat();
            return;
        }
        if (!WorkerSite.canActFromStand(companion, heatSource, heatStand, WorkerSite.INTERACT_RANGE_SQR)) {
            waiting("job_status.modern_companions.heat_blocked");
            return;
        }
        if (companion.level() instanceof ServerLevel server && !pendingCampfireOutput.isEmpty()
                && collectCampfireOutput(server)) return;
        if (cooldown-- > 0) return;
        phase(JobPhase.WORKING, "job_status.modern_companions.cooking", heatSource);
        cooldown = COOK_COOLDOWN;
        cookOneItem();
    }

    private void cookOneItem() {
        if (!(companion.level() instanceof ServerLevel server)) return;
        var be = server.getBlockEntity(heatSource);
        boolean cooked = false;
        if (be instanceof AbstractFurnaceBlockEntity furnace) {
            if (!hasFuel(furnace) && pendingFurnaceOutput.isEmpty() && !findFirstRawIngredient().isEmpty()) {
                companion.setJobStatus("job_status.modern_companions.no_fuel");
                return;
            }
            cooked = pullCooked(furnace) || cookInFurnace(furnace);
        }
        if (!cooked && be instanceof CampfireBlockEntity campfire) {
            cooked = collectCampfireOutput(server);
        }
        if (!cooked && be instanceof CampfireBlockEntity campfire) {
            cookInCampfire(server, campfire);
        }
    }

    private boolean cookInFurnace(AbstractFurnaceBlockEntity furnace) {
        if (!pendingFurnaceOutput.isEmpty()) return false;
        if (!hasFuel(furnace)) return false;
        int inputSlot = 0;
        int fuelSlot = 1;
        int outputSlot = 2;

        // Clean finished cooked items first to avoid jammed output.
        ItemStack output = furnace.getItem(outputSlot);
        if (!output.isEmpty() && !pendingFurnaceOutput.isEmpty() && ItemStack.isSameItemSameComponents(output, pendingFurnaceOutput)) {
            ItemStack moved = output.copy();
            furnace.setItem(outputSlot, ItemStack.EMPTY);
            ItemStack leftover = companion.getInventory().addItem(moved);
            if (!leftover.isEmpty()) companion.spawnAtLocation(leftover);
            furnace.setChanged();
            pendingFurnaceOutput = ItemStack.EMPTY;
        }

        ItemStack rawStack = findFirstRawIngredient();
        if (rawStack.isEmpty()) return false;
        ItemStack cookedStack = furnace.getBlockState().is(Blocks.SMOKER)
                ? recipeResult(rawStack, RecipeType.SMOKING)
                : recipeResult(rawStack, RecipeType.SMELTING);
        if (cookedStack.isEmpty()) return false;

        ItemStack input = furnace.getItem(inputSlot);
        if (!input.isEmpty() && (!ItemStack.isSameItemSameComponents(input, rawStack) || input.getCount() >= input.getMaxStackSize())) {
            return false;
        }

        ItemStack existingOutput = furnace.getItem(outputSlot);
        if (!existingOutput.isEmpty() && (!ItemStack.isSameItemSameComponents(existingOutput, cookedStack) || existingOutput.getCount() >= existingOutput.getMaxStackSize())) {
            return false;
        }

        ItemStack inserted = rawStack.split(1);
        if (input.isEmpty()) {
            furnace.setItem(inputSlot, inserted);
        } else {
            input.grow(1);
            furnace.setItem(inputSlot, input);
        }
        furnace.setChanged();
        pendingFurnaceOutput = cookedStack.copyWithCount(1);
        savePlan();
        return true;
    }

    private boolean pullCooked(AbstractFurnaceBlockEntity furnace) {
        ItemStack output = furnace.getItem(2);
        if (output.isEmpty() || pendingFurnaceOutput.isEmpty() || !ItemStack.isSameItemSameComponents(output, pendingFurnaceOutput)) {
            return false;
        }
        ItemStack moved = output.copy();
        ItemStack leftover = companion.getInventory().addItem(moved);
        furnace.setItem(2, leftover);
        furnace.setChanged();
        if (!leftover.isEmpty()) {
            companion.setJobStatus("job_status.modern_companions.inventory_full");
            companion.requestImmediateDelivery(null);
            return false;
        }
        pendingFurnaceOutput = ItemStack.EMPTY;
        savePlan();
        if (companion.hasDeliverableCargo()) companion.requestImmediateDelivery(null);
        return true;
    }

    private boolean hasFuel(AbstractFurnaceBlockEntity furnace) {
        boolean lit = furnace.getBlockState().hasProperty(AbstractFurnaceBlock.LIT) && furnace.getBlockState().getValue(AbstractFurnaceBlock.LIT);
        boolean stocked = !furnace.getItem(1).isEmpty();
        return lit || stocked;
    }

    private ItemStack findFirstRawIngredient() {
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (cookable(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean prepareSupply() {
        if (!(companion.level() instanceof ServerLevel server)) return false;
        BlockPos chest = companion.getWorkCenter().orElse(null);
        if (chest == null || !server.isLoaded(chest)) return false;
        BlockPos stand = WorkerSite.findApproachStand(companion, chest, 2);
        if (stand == null || !reserve("chest:" + chest.asLong())) return false;
        supplyChest = chest;
        supplyStand = stand;
        return true;
    }

    private void serviceSupplyChest() {
        if (!(companion.level() instanceof ServerLevel server) || supplyStand == null) return;
        if (companion.distanceToSqr(supplyStand.getX() + 0.5D, supplyStand.getY(), supplyStand.getZ() + 0.5D) > 2.25D) {
            phase(JobPhase.TRAVELLING, "job_status.modern_companions.getting_raw_meat", supplyChest);
            if (companion.getNavigation().isDone()) moveToSupply();
            return;
        }
        if (!WorkerSite.canActFromStand(companion, supplyChest, supplyStand, WorkerSite.INTERACT_RANGE_SQR)) {
            waiting("job_status.modern_companions.chest_blocked");
            return;
        }
        BlockPos suppliedChest = supplyChest;
        ItemStack raw = companion.withdrawOneFromChest(server, suppliedChest, this::cookable);
        release("chest:" + suppliedChest.asLong());
        supplyChest = null;
        supplyStand = null;
        if (raw.isEmpty()) {
            waiting("job_status.modern_companions.no_raw_meat");
            return;
        }
        companion.getInventory().addItem(raw);
        phase(JobPhase.SEARCHING, "job_status.modern_companions.cooking");
    }

    private boolean cookable(ItemStack stack) {
        return stack.is(RAW_MEAT) && (!recipeResult(stack, RecipeType.SMOKING).isEmpty()
                || !recipeResult(stack, RecipeType.SMELTING).isEmpty()
                || !recipeResult(stack, RecipeType.CAMPFIRE_COOKING).isEmpty());
    }

    private void cookInCampfire(ServerLevel server, CampfireBlockEntity campfire) {
        if (!pendingCampfireOutput.isEmpty()) return;
        ItemStack raw = findFirstRawIngredient();
        if (raw.isEmpty()) return;
        var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(raw), server);
        if (recipe.isEmpty()) return;
        ItemStack input = raw.copyWithCount(1);
        ItemStack output = recipe.get().value().assemble(new SingleRecipeInput(input), server.registryAccess());
        int emptySlot = -1;
        for (int slot = 0; slot < campfire.getItems().size(); slot++) {
            if (campfire.getItems().get(slot).isEmpty()) {
                emptySlot = slot;
                break;
            }
        }
        if (emptySlot < 0) return;
        int inventoryCount = countInventory(output);
        int cookingTime = recipe.get().value().getCookingTime();
        if (!output.isEmpty() && campfire.placeFood(companion, input, recipe.get().value().getCookingTime())) {
            raw.shrink(1);
            pendingCampfireOutput = output.copyWithCount(1);
            pendingCampfireSlot = emptySlot;
            pendingCampfireReadyAt = server.getGameTime() + cookingTime;
            pendingCampfireInventoryCount = inventoryCount;
            savePlan();
        }
    }

    private boolean collectCampfireOutput(ServerLevel server) {
        if (pendingCampfireOutput.isEmpty()) {
            return false;
        }
        if (server.getGameTime() < pendingCampfireReadyAt) return false;
        if (pendingCampfireInventoryCount >= 0
                && countInventory(pendingCampfireOutput) >= pendingCampfireInventoryCount + 1) {
            clearCampfireBatch();
            if (companion.hasDeliverableCargo()) companion.requestImmediateDelivery(null);
            return true;
        }
        if (!(server.getBlockEntity(heatSource) instanceof CampfireBlockEntity campfire)) return false;
        if (pendingCampfireSlot >= 0 && pendingCampfireSlot < campfire.getItems().size()
                && !campfire.getItems().get(pendingCampfireSlot).isEmpty()) return false;
        for (ItemEntity item : server.getEntitiesOfClass(ItemEntity.class,
                new AABB(heatSource).inflate(1.0D), candidate -> candidate.isAlive()
                        && candidate.getAge() <= 20
                        && ItemStack.isSameItemSameComponents(candidate.getItem(), pendingCampfireOutput))) {
            ItemStack leftover = companion.getInventory().addItem(item.getItem().copy());
            if (!leftover.isEmpty()) {
                item.setItem(leftover);
                companion.setJobStatus("job_status.modern_companions.inventory_full");
                companion.requestImmediateDelivery(null);
                return false;
            }
            item.discard();
            clearCampfireBatch();
            if (companion.hasDeliverableCargo()) companion.requestImmediateDelivery(null);
            return true;
        }
        return false;
    }

    private int countInventory(ItemStack wanted) {
        int count = 0;
        for (int i = 0; i < companion.getInventory().getContainerSize(); i++) {
            ItemStack stack = companion.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) count += stack.getCount();
        }
        return count;
    }

    private void clearCampfireBatch() {
        pendingCampfireOutput = ItemStack.EMPTY;
        pendingCampfireSlot = -1;
        pendingCampfireReadyAt = -1L;
        pendingCampfireInventoryCount = -1;
        savePlan();
    }

    private <T extends AbstractCookingRecipe> ItemStack recipeResult(ItemStack raw, RecipeType<T> type) {
        if (!(companion.level() instanceof ServerLevel server)) return ItemStack.EMPTY;
        return server.getRecipeManager().getRecipeFor(type, new SingleRecipeInput(raw), server)
                .map(holder -> holder.value().assemble(new SingleRecipeInput(raw), server.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    private BlockPos findHeatSource() {
        BlockPos origin = companion.getWorkCenter().orElse(companion.blockPosition());
        Level level = companion.level();
        int radius = Math.max(3, Math.min(searchRadius, companion.getPatrolRadius()));
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius),
                origin.offset(radius, 2, radius))) {
            if (!isHeatSource(pos)) continue;
            BlockPos stand = WorkerSite.findStand(companion, pos, 2);
            if (stand != null) {
                heatStand = stand;
                heatSource = pos.immutable();
                savePlan();
                return heatSource;
            }
        }
        return null;
    }

    private boolean isHeatSource(BlockPos pos) {
        var state = companion.level().getBlockState(pos);
        return state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.FURNACE) || state.is(Blocks.SMOKER);
    }

    private void moveToHeat() {
        if (heatSource == null) return;
        if (heatStand != null) companion.getNavigation().moveTo(heatStand.getX() + 0.5D, heatStand.getY(), heatStand.getZ() + 0.5D, 1.0D);
    }

    private boolean isActiveJob() {
        restorePlan();
        if (!enabled) return false;
        if (companion.getJob() != CompanionJob.CHEF) return false;
        if (!workActive(enabled)) return false;
        if (companion.isOrderedToSit() || !companion.isTame()) return false;
        if (companion.getWorkCenter().isEmpty()) { companion.setJobStatus("job_status.modern_companions.assign_chest"); return false; }
        return true;
    }

    private void moveToSupply() {
        if (supplyStand != null) companion.getNavigation().moveTo(supplyStand.getX() + 0.5D, supplyStand.getY(), supplyStand.getZ() + 0.5D, 1.0D);
    }

    private void restorePlan() {
        if (restoredPlan) return;
        restoredPlan = true;
        CompoundTag payload = companion.getJobPlanPayload();
        BlockPos savedHeat = readPos(payload, "Heat");
        BlockPos savedStand = readPos(payload, "Stand");
        BlockPos checkpoint = companion.getJobCheckpointTarget().orElse(null);
        if (savedHeat == null && checkpoint != null && isHeatSource(checkpoint)) savedHeat = checkpoint;
        if (savedHeat != null && isHeatSource(savedHeat)) {
            heatSource = savedHeat;
            heatStand = savedStand != null ? savedStand : WorkerSite.findApproachStand(companion, savedHeat, 2);
        }
        if (payload.contains("FurnaceOutput", 10)) {
            pendingFurnaceOutput = ItemStack.parseOptional(companion.registryAccess(), payload.getCompound("FurnaceOutput"));
        }
        if (payload.contains("CampfireOutput", 10)) {
            pendingCampfireOutput = ItemStack.parseOptional(companion.registryAccess(), payload.getCompound("CampfireOutput"));
            pendingCampfireSlot = payload.contains("CampfireSlot") ? payload.getInt("CampfireSlot") : -1;
            pendingCampfireReadyAt = payload.contains("CampfireReady")
                    ? payload.getLong("CampfireReady") : companion.level().getGameTime();
            pendingCampfireInventoryCount = payload.contains("CampfireInventoryCount")
                    ? payload.getInt("CampfireInventoryCount") : -1;
        }
    }

    private void savePlan() {
        CompoundTag payload = companion.getJobPlanPayload();
        putPos(payload, "Heat", heatSource);
        putPos(payload, "Stand", heatStand);
        if (pendingFurnaceOutput.isEmpty()) payload.remove("FurnaceOutput");
        else payload.put("FurnaceOutput", pendingFurnaceOutput.save(companion.registryAccess()));
        if (pendingCampfireOutput.isEmpty()) {
            payload.remove("CampfireOutput");
            payload.remove("CampfireSlot");
            payload.remove("CampfireReady");
            payload.remove("CampfireInventoryCount");
        } else {
            payload.put("CampfireOutput", pendingCampfireOutput.save(companion.registryAccess()));
            payload.putInt("CampfireSlot", pendingCampfireSlot);
            payload.putLong("CampfireReady", pendingCampfireReadyAt);
            payload.putInt("CampfireInventoryCount", pendingCampfireInventoryCount);
        }
        companion.setJobPlanPayload(payload);
    }

    private static void putPos(CompoundTag tag, String key, BlockPos pos) {
        if (pos == null) tag.remove(key); else tag.putLong(key, pos.asLong());
    }

    private static BlockPos readPos(CompoundTag tag, String key) {
        return tag.contains(key) ? BlockPos.of(tag.getLong(key)) : null;
    }
}
