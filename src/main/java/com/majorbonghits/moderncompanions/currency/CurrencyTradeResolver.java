package com.majorbonghits.moderncompanions.currency;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Resolves valid configured trade ids at JEI registration time. */
public final class CurrencyTradeResolver {
    private CurrencyTradeResolver() {
    }

    public static Optional<Resolved> resolve(CurrencyTrade trade) {
        Optional<Item> first = resolveItem(trade.firstItem());
        Optional<Item> second = "-".equals(trade.secondItem()) ? Optional.empty() : resolveItem(trade.secondItem());
        Optional<Item> output = resolveItem(trade.outputItem());
        if (first.isEmpty() || output.isEmpty() || (!"-".equals(trade.secondItem()) && second.isEmpty())) {
            return Optional.empty();
        }
        return Optional.of(new Resolved(
                new ItemStack(first.get(), trade.firstCount()),
                second.map(item -> new ItemStack(item, trade.secondCount())).orElse(ItemStack.EMPTY),
                new ItemStack(output.get(), trade.outputCount())));
    }

    private static Optional<Item> resolveItem(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);
    }

    public record Resolved(ItemStack firstInput, ItemStack secondInput, ItemStack output) {
    }
}
