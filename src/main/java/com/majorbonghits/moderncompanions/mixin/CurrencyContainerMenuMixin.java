package com.majorbonghits.moderncompanions.mixin;

import com.majorbonghits.moderncompanions.currency.CurrencyService;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the one currency-specific quick-move path shared by vanilla and modded menus. */
@Mixin(AbstractContainerMenu.class)
public abstract class CurrencyContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void modernCompanions$depositCurrencyOnQuickMove(int slotId, int button, ClickType clickType,
                                                               Player player, CallbackInfo callbackInfo) {
        if (clickType == ClickType.QUICK_MOVE
                && CurrencyService.interceptQuickMove((AbstractContainerMenu) (Object) this, slotId, player)) {
            callbackInfo.cancel();
        }
    }
}
