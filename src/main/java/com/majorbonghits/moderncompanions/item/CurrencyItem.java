package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.currency.CurrencyService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A loot-only denomination whose displayed value follows the common config. */
public final class CurrencyItem extends Item {
    private final String valueKey;

    public CurrencyItem(String valueKey, Properties properties) {
        super(properties);
        this.valueKey = valueKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.modern_companions.currency.value",
                CurrencyService.configuredValue(valueKey)).withStyle(ChatFormatting.GRAY));
    }
}
