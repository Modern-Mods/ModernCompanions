package com.majorbonghits.moderncompanions.currency;

import com.mojang.serialization.MapCodec;
import com.majorbonghits.moderncompanions.ModernCompanions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Registers the single data-driven loot hook used by currencies. */
public final class ModLootModifiers {
    private ModLootModifiers() {
    }

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ModernCompanions.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<CurrencyLootModifier>> CURRENCY =
            SERIALIZERS.register("currency", () -> CurrencyLootModifier.CODEC);

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
