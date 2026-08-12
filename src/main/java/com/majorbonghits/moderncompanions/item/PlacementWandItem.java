package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.compat.sophisticatedbackpacks.SophisticatedBackpackCompat;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deploys stored companions together, or stores nearby owned companions while sneaking in air. */
public class PlacementWandItem extends Item {
    private static final int CAPTURE_RADIUS = 32;
    private static final int PLACEMENT_RADIUS = 4;
    private static final int[] VERTICAL_OFFSETS = {0, -1, 1};

    public PlacementWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockPos origin = context.getClickedPos().relative(context.getClickedFace());
        int placed = deployGems(server, player, origin);
        if (placed == 0) {
            return InteractionResult.FAIL;
        }

        server.playSound(null, context.getClickedPos(), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.PLAYERS,
                0.8F, 1.2F);
        player.displayClientMessage(Component.translatable(
                "message.modern_companions.placement_wand.placed", placed), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return player.isShiftKeyDown()
                    ? InteractionResultHolder.success(stack)
                    : InteractionResultHolder.pass(stack);
        }
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        int captured = captureCompanions(server, player);
        if (captured == 0) {
            return InteractionResultHolder.pass(stack);
        }

        server.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                0.8F, 1.1F);
        player.displayClientMessage(Component.translatable(
                "message.modern_companions.placement_wand.captured", captured), true);
        return InteractionResultHolder.consume(stack);
    }

    private int deployGems(ServerLevel level, Player player, BlockPos origin) {
        Set<BlockPos> occupiedSpots = new HashSet<>();
        int placed = 0;
        boolean backpackIntegrationLoaded = ModList.get().isLoaded("curios")
                && ModList.get().isLoaded("sophisticatedbackpacks");
        if (backpackIntegrationLoaded) {
            IItemHandler backpack = SophisticatedBackpackCompat.getEquippedBackpackInventory(player);
            if (backpack != null) {
                List<Integer> gemSlots = new ArrayList<>();
                for (int slot = 0; slot < backpack.getSlots(); slot++) {
                    ItemStack gem = backpack.getStackInSlot(slot);
                    if (gem.is(ModItems.STORED_COMPANION.get()) && StoredCompanionItem.hasCompanionData(gem)) {
                        gemSlots.add(slot);
                    }
                }

                for (int slot : gemSlots) {
                    ItemStack gem = backpack.getStackInSlot(slot);
                    while (!gem.isEmpty() && gem.is(ModItems.STORED_COMPANION.get())
                            && StoredCompanionItem.hasCompanionData(gem)) {
                        if (!placeGem(level, player, origin, gem, occupiedSpots)) {
                            break;
                        }
                        backpack.extractItem(slot, 1, false);
                        placed++;
                        gem = backpack.getStackInSlot(slot);
                    }
                }
            }
        }

        Inventory inventory = player.getInventory();
        List<Integer> gemSlots = new ArrayList<>();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack gem = inventory.getItem(slot);
            if (gem.is(ModItems.STORED_COMPANION.get()) && StoredCompanionItem.hasCompanionData(gem)) {
                gemSlots.add(slot);
            }
        }

        for (int slot : gemSlots) {
            ItemStack gem = inventory.getItem(slot);
            while (!gem.isEmpty() && gem.is(ModItems.STORED_COMPANION.get())
                    && StoredCompanionItem.hasCompanionData(gem)) {
                if (!placeGem(level, player, origin, gem, occupiedSpots)) {
                    break;
                }
                gem.shrink(1);
                placed++;
            }
        }
        return placed;
    }

    private boolean placeGem(ServerLevel level, Player player, BlockPos origin, ItemStack gem,
            Set<BlockPos> occupiedSpots) {
        BlockPos spot = findPlacementSpot(level, origin, occupiedSpots);
        if (spot == null) {
            return false;
        }
        Entity companion = StoredCompanionItem.placeCompanion(level, gem, Vec3.atBottomCenterOf(spot), player);
        if (companion == null) {
            return false;
        }
        occupiedSpots.add(spot);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, spot);
        return true;
    }

    @Nullable
    private BlockPos findPlacementSpot(ServerLevel level, BlockPos origin, Set<BlockPos> occupiedSpots) {
        for (int vertical : VERTICAL_OFFSETS) {
            for (int radius = 0; radius <= PLACEMENT_RADIUS; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        BlockPos candidate = origin.offset(dx, vertical, dz);
                        if (isSafeSpot(level, candidate, occupiedSpots)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeSpot(ServerLevel level, BlockPos candidate, Set<BlockPos> occupiedSpots) {
        if (candidate.getY() <= level.getMinBuildHeight() || occupiedSpots.contains(candidate)
                || !level.isEmptyBlock(candidate) || !level.isEmptyBlock(candidate.above())
                || !level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) {
            return false;
        }

        AABB bounds = new AABB(candidate.getX() + 0.2D, candidate.getY(), candidate.getZ() + 0.2D,
                candidate.getX() + 0.8D, candidate.getY() + 1.85D, candidate.getZ() + 0.8D);
        if (!level.noCollision(bounds)) {
            return false;
        }
        return level.getEntities((Entity) null, bounds, Entity::isAlive).isEmpty();
    }

    private int captureCompanions(ServerLevel level, Player player) {
        AABB searchBox = player.getBoundingBox().inflate(CAPTURE_RADIUS);
        List<AbstractHumanCompanionEntity> nearby = level.getEntitiesOfClass(AbstractHumanCompanionEntity.class,
                searchBox, companion -> companion.isAlive() && companion.isOwnedBy(player));
        // Count only the equipped back-slot backpack; a backpack carried in player inventory is not a target.
        boolean backpackIntegrationLoaded = ModList.get().isLoaded("curios")
                && ModList.get().isLoaded("sophisticatedbackpacks");
        int backpackSlots = backpackIntegrationLoaded
                ? SophisticatedBackpackCompat.countEquippedBackpackSlots(player,
                        new ItemStack(ModItems.STORED_COMPANION.get()))
                : 0;
        int captureLimit = PlacementWandCaptureRules.captureLimit(
                backpackSlots + countFreeInventorySlots(player), nearby.size());
        int captured = 0;
        for (AbstractHumanCompanionEntity companion : nearby) {
            if (captured >= captureLimit) {
                break;
            }
            if (!companion.isAlive() || !companion.isOwnedBy(player)) {
                continue;
            }
            ItemStack gem = CompanionMoverItem.captureCompanion(companion);
            ItemStack remainder = backpackIntegrationLoaded
                    ? SophisticatedBackpackCompat.insertIntoEquippedBackpack(player, gem)
                    : gem;
            if (!remainder.isEmpty()) {
                // The normal-inventory fallback runs only after the equipped backpack rejects the gem.
                int freeSlot = player.getInventory().getFreeSlot();
                if (freeSlot >= 0) {
                    player.getInventory().setItem(freeSlot, remainder);
                } else {
                    // Preserve the captured companion if a container changes between the capacity check and insertion.
                    player.drop(remainder, false);
                }
            }
            companion.discard();
            captured++;
        }
        return captured;
    }

    private static int countFreeInventorySlots(Player player) {
        Inventory inventory = player.getInventory();
        int free = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                free++;
            }
        }
        return free;
    }

}
