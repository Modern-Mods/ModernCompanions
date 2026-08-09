package com.majorbonghits.moderncompanions.currency;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

/** Server-authoritative cursor interactions for deposits and card combining. */
public final class CurrencyInteractions {
    private CurrencyInteractions() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CurrencyInteractions::onStackedOnOther);
    }

    private static void onStackedOnOther(ItemStackedOnOtherEvent event) {
        Player player = event.getPlayer();
        if (!CurrencyService.enabled() || player.getAbilities().instabuild) return;

        ItemStack carried = event.getCarriedItem();
        ItemStack target = event.getStackedOnItem();
        boolean physicalDeposit = CurrencyService.isPhysicalCurrency(carried)
                && CurrencyService.isCreditCard(target);
        boolean cardCombine = CurrencyService.isCreditCard(carried)
                && CurrencyService.isCreditCard(target);
        if (!physicalDeposit && !cardCombine) return;

        // The event must stop vanilla's swap/stack path even when a server-side validation fails.
        event.setCanceled(true);
        if (player.level().isClientSide) return;

        Slot slot = event.getSlot();
        if (!slot.mayPickup(player)) return;

        if (physicalDeposit) {
            int count = event.getClickAction() == ClickAction.PRIMARY ? carried.getCount() : 1;
            if (CurrencyService.deposit(target, carried, count)) {
                event.getCarriedSlotAccess().set(carried.isEmpty() ? ItemStack.EMPTY : carried);
                slot.setChanged();
                player.containerMenu.broadcastChanges();
            }
            return;
        }

        if (carried.getCount() != 1 || target.getCount() != 1) return;
        if (CurrencyService.transferBalance(target, carried)) {
            event.getCarriedSlotAccess().set(ItemStack.EMPTY);
            slot.setChanged();
            player.containerMenu.broadcastChanges();
        }
    }
}
