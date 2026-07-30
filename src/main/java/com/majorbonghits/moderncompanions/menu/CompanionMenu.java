package com.majorbonghits.moderncompanions.menu;

import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/**
 * Companion inventory menu (7x9 slots) plus player inventory/hotbar.
 */
public class CompanionMenu extends AbstractContainerMenu {
    private static final int COMPANION_ROWS = 7;
    private static final int COMPANION_SLOT_COUNT = COMPANION_ROWS * 9;
    private static final int EQUIPMENT_SLOT_COUNT = 6;
    private static final int CONTENT_X_OFFSET = 103;
    private final Container container;
    private final int companionId;
    private final AbstractHumanCompanionEntity companion;

    public CompanionMenu(int id, Inventory playerInv, int companionId) {
        this(id, playerInv, resolveContainer(playerInv, companionId), companionId, resolveEntity(playerInv, companionId));
    }

    public CompanionMenu(int id, Inventory playerInv, AbstractHumanCompanionEntity companion) {
        this(id, playerInv, companion.getInventory(), companion.getId(), companion);
    }

    private CompanionMenu(int id, Inventory playerInv, Container container, int companionId, AbstractHumanCompanionEntity companion) {
        super(ModMenuTypes.COMPANION_MENU.get(), id);
        this.container = container;
        this.companionId = companionId;
        this.companion = companion;
        checkContainerSize(container, COMPANION_SLOT_COUNT);
        container.startOpen(playerInv.player);

        // The dedicated equipment store keeps worn items out of the cargo inventory.
        Container equipment = companion == null ? new SimpleContainer(EQUIPMENT_SLOT_COUNT) : companion.getEquipmentInventory();
        this.addSlot(new CompanionEquipmentSlot(equipment, 0, 9, 37, EquipmentSlot.HEAD, companion, "empty_armor_slot_helmet"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 1, 9, 56, EquipmentSlot.CHEST, companion, "empty_armor_slot_chestplate"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 2, 9, 73, EquipmentSlot.LEGS, companion, "empty_armor_slot_leggings"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 3, 9, 91, EquipmentSlot.FEET, companion, "empty_armor_slot_boots"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 4, 78, 37, EquipmentSlot.MAINHAND, companion, "empty_slot_sword"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 5, 78, 91, EquipmentSlot.OFFHAND, companion, "empty_armor_slot_shield"));

        // Companion inventory slots
        for (int row = 0; row < COMPANION_ROWS; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, CONTENT_X_OFFSET + 8 + col * 18, 24 + row * 18));
            }
        }

        // The new texture has fixed seven-row and player-grid anchors.
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, CONTENT_X_OFFSET + 8 + col * 18, 165 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, CONTENT_X_OFFSET + 8 + col * 18, 223));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (companion != null && companion.equipBetterFromPlayer(stack)) {
                // Equipped one item; the companion method returned the replaced item to cargo.
            } else if (!this.moveItemStackTo(stack, EQUIPMENT_SLOT_COUNT, EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public int getCompanionId() {
        return companionId;
    }

    public AbstractHumanCompanionEntity getCompanion() {
        return companion;
    }

    private static Container resolveContainer(Inventory inv, int id) {
        AbstractHumanCompanionEntity c = resolveEntity(inv, id);
        return c != null ? c.getInventory() : new SimpleContainer(COMPANION_ROWS * 9);
    }

    private static AbstractHumanCompanionEntity resolveEntity(Inventory inv, int id) {
        if (inv.player.level().getEntity(id) instanceof AbstractHumanCompanionEntity c) {
            return c;
        }
        return null;
    }

    /** Equipment slots sync their persistent backing store to the companion's live render state. */
    private static class CompanionEquipmentSlot extends Slot {
        private final EquipmentSlot equipmentSlot;
        private final AbstractHumanCompanionEntity companion;

        CompanionEquipmentSlot(Container container, int index, int x, int y, EquipmentSlot equipmentSlot,
                AbstractHumanCompanionEntity companion, String emptyIcon) {
            super(container, index, x, y);
            this.equipmentSlot = equipmentSlot;
            this.companion = companion;
            this.setBackground(InventoryMenu.BLOCK_ATLAS, ResourceLocation.withDefaultNamespace("item/" + emptyIcon));
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return companion == null ? equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR
                    || stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == equipmentSlot
                    : companion.canEquipInSlot(equipmentSlot, stack);
        }

        @Override
        public void set(ItemStack stack) {
            if (companion != null) {
                companion.setManualEquipment(equipmentSlot, stack);
            } else {
                super.set(stack);
            }
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack removed = super.remove(amount);
            if (companion != null) {
                companion.setManualEquipment(equipmentSlot, getItem());
            }
            return removed;
        }
    }
}
