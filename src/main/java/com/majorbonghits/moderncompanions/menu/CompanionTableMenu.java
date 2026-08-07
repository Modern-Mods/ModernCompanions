package com.majorbonghits.moderncompanions.menu;

import com.mojang.datafixers.util.Pair;
import com.majorbonghits.moderncompanions.core.ModBlocks;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.personality.CompanionPersonality;
import com.majorbonghits.moderncompanions.item.StoredCompanionItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Four-input Companion Table menu. The trait ids are sent as small integer data slots so the
 * client renders the server's rolled choices without trusting client-supplied trait strings.
 */
public final class CompanionTableMenu extends AbstractContainerMenu {
    public static final int SOUL_GEM_SLOT = 0;
    public static final int LAPIS_SLOT = 1;
    public static final int ECHO_SHARD_SLOT = 2;
    public static final int CATALYST_SLOT = 3;
    private static final int TABLE_SLOT_COUNT = 4;
    private static final int PLAYER_INVENTORY_START = TABLE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    private final BlockPos tablePos;
    private final Container tableSlots = new SimpleContainer(TABLE_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            CompanionTableMenu.this.slotsChanged(this);
        }
    };
    private final int[] traitOptions = {-1, -1, -1};
    private final DataSlot enchantmentSeed = DataSlot.standalone();

    public CompanionTableMenu(int id, Inventory playerInventory, BlockPos tablePos) {
        super(com.majorbonghits.moderncompanions.core.ModMenuTypes.COMPANION_TABLE_MENU.get(), id);
        this.tablePos = tablePos.immutable();

        this.addSlot(new Slot(tableSlots, SOUL_GEM_SLOT, 15, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.STORED_COMPANION.get()) && StoredCompanionItem.hasCompanionData(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(tableSlots, LAPIS_SLOT, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS,
                        ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli"));
            }
        });
        this.addSlot(new Slot(tableSlots, ECHO_SHARD_SLOT, 15, 67) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ECHO_SHARD);
            }
        });
        this.addSlot(new Slot(tableSlots, CATALYST_SLOT, 35, 67) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return StoredCompanionItem.isTraitCatalyst(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, 103 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 161));
        }

        this.addDataSlot(enchantmentSeed).set(playerInventory.player.getEnchantmentSeed());
        for (int i = 0; i < traitOptions.length; i++) {
            this.addDataSlot(DataSlot.shared(traitOptions, i));
        }
        refreshTraitOptions();
    }

    public CompanionTableMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, buffer.readBlockPos());
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory == tableSlots) {
            refreshTraitOptions();
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0 || buttonId >= traitOptions.length * 2) {
            return false;
        }
        int traitSlot = buttonId / traitOptions.length;
        int optionIndex = buttonId % traitOptions.length;
        String trait = getTraitOption(optionIndex);
        if (trait == null) {
            return false;
        }

        // Vanilla calls this method client-side before sending the button packet. The actual
        // transaction still runs below on the server, but the preview must accept valid rows so
        // multiplayer clients do not suppress their own click packet.
        if (player.level().isClientSide) {
            return true;
        }

        boolean changed = StoredCompanionItem.reforgeInTable(player, tablePos, traitSlot, trait,
                getTraitOptionIds(), tableSlots.getItem(SOUL_GEM_SLOT), tableSlots.getItem(LAPIS_SLOT),
                tableSlots.getItem(ECHO_SHARD_SLOT), tableSlots.getItem(CATALYST_SLOT));
        if (changed) {
            tableSlots.setChanged();
        }
        return changed;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(tablePos).is(ModBlocks.COMPANION_TABLE.get())
                && player.canInteractWithBlock(tablePos, 4.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot clickedSlot = this.slots.get(index);
        if (!clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = clickedSlot.getItem();
        ItemStack moved = source.copy();

        if (index < PLAYER_INVENTORY_START) {
            if (!moveItemStackTo(source, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = matchingTableSlot(source);
            if (target < 0 || !moveItemStackTo(source, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            clickedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }
        clickedSlot.onTake(player, source);
        return moved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, tableSlots);
    }

    public BlockPos getTablePos() {
        return tablePos;
    }

    public String getTraitOption(int index) {
        if (index < 0 || index >= traitOptions.length) {
            return null;
        }
        int traitIndex = traitOptions[index];
        return traitIndex >= 0 && traitIndex < CompanionPersonality.TRAITS.size()
                ? CompanionPersonality.TRAITS.get(traitIndex) : null;
    }

    public List<String> getTraitOptionIds() {
        return java.util.Arrays.stream(traitOptions)
                .filter(index -> index >= 0 && index < CompanionPersonality.TRAITS.size())
                .mapToObj(CompanionPersonality.TRAITS::get)
                .toList();
    }

    public ItemStack getSoulGem() {
        return tableSlots.getItem(SOUL_GEM_SLOT);
    }

    private int matchingTableSlot(ItemStack stack) {
        if (this.getSlot(SOUL_GEM_SLOT).mayPlace(stack)) return SOUL_GEM_SLOT;
        if (this.getSlot(LAPIS_SLOT).mayPlace(stack)) return LAPIS_SLOT;
        if (this.getSlot(ECHO_SHARD_SLOT).mayPlace(stack)) return ECHO_SHARD_SLOT;
        if (this.getSlot(CATALYST_SLOT).mayPlace(stack)) return CATALYST_SLOT;
        return -1;
    }

    private void refreshTraitOptions() {
        ItemStack soulGem = tableSlots.getItem(SOUL_GEM_SLOT);
        ItemStack catalyst = tableSlots.getItem(CATALYST_SLOT);
        if (!StoredCompanionItem.hasCompanionData(soulGem) || !StoredCompanionItem.isTraitCatalyst(catalyst)) {
            java.util.Arrays.fill(traitOptions, -1);
            return;
        }

        String primary = StoredCompanionItem.getTraitId(soulGem, CompanionPersonality.KEY_PRIMARY);
        String secondary = StoredCompanionItem.getTraitId(soulGem, CompanionPersonality.KEY_SECONDARY);
        long seed = enchantmentSeed.get()
                ^ (long) BuiltInRegistries.ITEM.getKey(catalyst.getItem()).hashCode();
        RandomSource random = RandomSource.create(seed ^ soulGem.getOrDefault(DataComponents.ENTITY_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag().toString().hashCode());
        List<String> options = StoredCompanionItem.rollTraitOptions(catalyst, primary, secondary, random);
        java.util.Arrays.fill(traitOptions, -1);
        for (int i = 0; i < Math.min(traitOptions.length, options.size()); i++) {
            traitOptions[i] = CompanionPersonality.TRAITS.indexOf(options.get(i));
        }
    }

}
