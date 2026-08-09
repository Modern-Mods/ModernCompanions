package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.currency.CurrencyService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Non-stackable wallet item whose identity and balance live in a data component. */
public final class CreditCardItem extends Item {
    public CreditCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        // Old plain Credit Cards are upgraded as soon as their ItemStack is loaded.
        CurrencyService.ensureCard(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        CurrencyService.ensureCard(stack);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.modern_companions.credit_card.balance",
                CurrencyService.cardBalance(stack)).withStyle(ChatFormatting.GOLD));
    }
}
