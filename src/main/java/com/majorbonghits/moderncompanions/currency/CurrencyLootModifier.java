package com.majorbonghits.moderncompanions.currency;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.majorbonghits.moderncompanions.core.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds configured denominations to vanilla chest loot without touching mob or block drops. */
public final class CurrencyLootModifier extends LootModifier {
    public static final MapCodec<CurrencyLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions")
                    .forGetter(modifier -> modifier.conditions))
                    .apply(instance, CurrencyLootModifier::new));

    public CurrencyLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ModConfig.safeGet(ModConfig.CURRENCIES_ENABLED) || !isVanillaChest(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        RandomSource random = context.getRandom();
        if (random.nextInt(100) < ModConfig.safeGet(ModConfig.CURRENCY_CARD_LOOT_CHANCE)) {
            generatedLoot.add(CurrencyService.createLootCard(random));
        }
        if (random.nextInt(100) >= ModConfig.safeGet(ModConfig.CURRENCY_LOOT_DISPERSE)) return generatedLoot;

        int min = Math.min(ModConfig.safeGet(ModConfig.CURRENCY_LOOT_MIN_COUNT),
                ModConfig.safeGet(ModConfig.CURRENCY_LOOT_MAX_COUNT));
        int max = Math.max(ModConfig.safeGet(ModConfig.CURRENCY_LOOT_MIN_COUNT),
                ModConfig.safeGet(ModConfig.CURRENCY_LOOT_MAX_COUNT));
        for (int roll = 0, rolls = ModConfig.safeGet(ModConfig.CURRENCY_LOOT_ROLLS); roll < rolls; roll++) {
            int count = min + (max == min ? 0 : random.nextInt(max - min + 1));
            generatedLoot.add(new ItemStack(CurrencyService.randomPhysicalCurrency(random), count));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.CURRENCY.get();
    }

    private static boolean isVanillaChest(ResourceLocation lootTableId) {
        return "minecraft".equals(lootTableId.getNamespace()) && lootTableId.getPath().startsWith("chests/");
    }
}
