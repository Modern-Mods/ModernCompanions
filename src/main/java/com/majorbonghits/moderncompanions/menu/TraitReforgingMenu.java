package com.majorbonghits.moderncompanions.menu;

import com.majorbonghits.moderncompanions.core.ModMenuTypes;
import com.majorbonghits.moderncompanions.item.StoredCompanionItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Choice menu for the three Soul Reforging outcomes rolled by the server. */
public class TraitReforgingMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final BlockPos tablePos;
    private final List<String> options;

    public TraitReforgingMenu(int id, Inventory inventory, InteractionHand hand, BlockPos tablePos,
            List<String> options) {
        super(ModMenuTypes.TRAIT_REFORGING_MENU.get(), id);
        this.playerInventory = inventory;
        this.hand = hand;
        this.tablePos = tablePos;
        this.options = List.copyOf(options);
    }

    public TraitReforgingMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, InteractionHand.values()[Math.max(0, Math.min(1, buffer.readByte()))],
                buffer.readBlockPos(), List.of(buffer.readUtf(), buffer.readUtf(), buffer.readUtf()));
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(tablePos).is(Blocks.ENCHANTING_TABLE)
                && player.distanceToSqr(Vec3.atCenterOf(tablePos)) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId < 0 || buttonId >= options.size() * 2) return false;
        int traitSlot = buttonId / options.size();
        int optionIndex = buttonId % options.size();
        if (StoredCompanionItem.reforge(player, hand, tablePos, traitSlot, options.get(optionIndex), options)) {
            player.closeContainer();
            return true;
        }
        return false;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public String getOption(int index) {
        return options.get(index);
    }

    public ItemStack getSoulGem() {
        return playerInventory.player.getItemInHand(hand);
    }
}
