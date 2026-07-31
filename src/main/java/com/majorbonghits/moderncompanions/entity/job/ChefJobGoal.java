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
        if (findFirstRawIngredient().isEmpty()) {
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
            cooked = pullCooked(furnace) || cookInFurnace(furnace);
        }
        if (!cooked && be instanceof CampfireBlockEntity campfire) {
            cookInCampfire(server, campfire);
        }
    }

    private boolean cookInFurnace(AbstractFurnaceBlockEntity furnace) {
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
        return true;
    }

    private boolean pullCooked(AbstractFurnaceBlockEntity furnace) {
        ItemStack output = furnace.getItem(2);
        if (output.isEmpty() || pendingFurnaceOutput.isEmpty() || !ItemStack.isSameItemSameComponents(output, pendingFurnaceOutput)) {
            return false;
        }
        ItemStack moved = output.copy();
        furnace.setItem(2, ItemStack.EMPTY);
        ItemStack leftover = companion.getInventory().addItem(moved);
        if (!leftover.isEmpty()) {
            companion.spawnAtLocation(leftover);
        }
        furnace.setChanged();
        pendingFurnaceOutput = ItemStack.EMPTY;
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
        ItemStack raw = companion.withdrawOneFromChest(server, supplyChest, this::cookable);
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
        ItemStack raw = findFirstRawIngredient();
        if (raw.isEmpty()) return;
        var recipe = server.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(raw), server);
        if (recipe.isEmpty()) return;
        if (campfire.placeFood(companion, raw.copyWithCount(1), recipe.get().value().getCookingTime())) raw.shrink(1);
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
                return pos.immutable();
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
}
