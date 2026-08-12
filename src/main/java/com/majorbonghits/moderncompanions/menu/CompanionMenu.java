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
    private static final int SPELLBOOK_SLOT_COUNT = 1;
    private static final int COSMETIC_SLOT_COUNT = 4;
    private static final int TOTAL_EQUIPMENT_SLOT_COUNT = EQUIPMENT_SLOT_COUNT + SPELLBOOK_SLOT_COUNT + COSMETIC_SLOT_COUNT;
    private static final int SPELLBOOK_SLOT_INDEX = EQUIPMENT_SLOT_COUNT;
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

        // Functional equipment delegates to the entity; the dummy container is client-safe fallback state.
        Container equipment = new SimpleContainer(TOTAL_EQUIPMENT_SLOT_COUNT);
        this.addSlot(new CompanionEquipmentSlot(equipment, 0, 9, 37, EquipmentSlot.HEAD, companion, "empty_armor_slot_helmet"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 1, 9, 56, EquipmentSlot.CHEST, companion, "empty_armor_slot_chestplate"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 2, 9, 73, EquipmentSlot.LEGS, companion, "empty_armor_slot_leggings"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 3, 9, 91, EquipmentSlot.FEET, companion, "empty_armor_slot_boots"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 4, 78, 37, EquipmentSlot.MAINHAND, companion, "empty_slot_sword"));
        this.addSlot(new CompanionEquipmentSlot(equipment, 5, 78, 91, EquipmentSlot.OFFHAND, companion, "empty_armor_slot_shield"));
        boolean magicalCompanion = companion != null && companion.hasMana();
        this.addSlot(new CompanionSpellbookSlot(equipment, SPELLBOOK_SLOT_INDEX,
                magicalCompanion ? 78 : -100, magicalCompanion ? 56 : -100, companion));

        // Cosmetic slots live off-screen; CompanionScreen draws and clicks them only while its popup is open.
        for (int i = 0; i < COSMETIC_SLOT_COUNT; i++) {
            this.addSlot(new CompanionCosmeticArmorSlot(equipment, TOTAL_EQUIPMENT_SLOT_COUNT - COSMETIC_SLOT_COUNT + i,
                    -100, -100,
                    cosmeticSlot(i), companion));
        }

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
            if (index < TOTAL_EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, TOTAL_EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (companion != null && companion.equipBetterFromPlayer(stack)) {
                // Equipped one item; the companion method returned the replaced item to cargo.
            } else if (!this.moveItemStackTo(stack, TOTAL_EQUIPMENT_SLOT_COUNT, TOTAL_EQUIPMENT_SLOT_COUNT + COMPANION_SLOT_COUNT, false)) {
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

    public int getCosmeticArmorSlotIndex(int index) {
        if (index < 0 || index >= COSMETIC_SLOT_COUNT) return -1;
        return EQUIPMENT_SLOT_COUNT + SPELLBOOK_SLOT_COUNT + index;
    }

    private static EquipmentSlot cosmeticSlot(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> throw new IllegalArgumentException("Unsupported cosmetic armor slot: " + index);
        };
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

    /** Equipment slots are views over the companion's live equipment, so menu extraction cannot fork a stack. */
    public static class CompanionEquipmentSlot extends Slot {
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
        public ItemStack getItem() {
            return companion == null ? super.getItem() : companion.getFunctionalEquipmentItem(equipmentSlot);
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
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
            return companion == null ? super.remove(amount) : companion.removeEquipment(equipmentSlot, amount);
        }

        public EquipmentSlot getEquipmentSlot() {
            return equipmentSlot;
        }
    }

    /** Dedicated persisted spellbook view; only mana-bearing companions can use it. */
    public static class CompanionSpellbookSlot extends Slot {
        private final AbstractHumanCompanionEntity companion;

        CompanionSpellbookSlot(Container container, int index, int x, int y, AbstractHumanCompanionEntity companion) {
            super(container, index, x, y);
            this.companion = companion;
            // The supplied magical equipment panel already contains the empty spellbook slot art.
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return companion != null && companion.canEquipSpellbook(stack);
        }

        @Override
        public ItemStack getItem() {
            return companion == null ? super.getItem() : companion.getSpellbookItem();
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (companion != null) companion.setSpellbookItem(stack);
            else super.set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return companion == null ? super.remove(amount) : companion.removeSpellbook(amount);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** Menu-backed view of the companion's cosmetic armor without changing functional equipment. */
    public static class CompanionCosmeticArmorSlot extends Slot {
        private final EquipmentSlot equipmentSlot;
        private final AbstractHumanCompanionEntity companion;

        CompanionCosmeticArmorSlot(Container container, int index, int x, int y, EquipmentSlot equipmentSlot,
                AbstractHumanCompanionEntity companion) {
            super(container, index, x, y);
            this.equipmentSlot = equipmentSlot;
            this.companion = companion;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return companion != null && companion.canEquipCosmeticArmor(equipmentSlot, stack);
        }

        @Override
        public ItemStack getItem() {
            return companion == null ? super.getItem() : companion.getCosmeticArmorItem(equipmentSlot);
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (companion != null) companion.setCosmeticArmorItem(equipmentSlot, stack);
            else super.set(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return companion == null ? super.remove(amount) : companion.removeCosmeticArmor(equipmentSlot, amount);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
