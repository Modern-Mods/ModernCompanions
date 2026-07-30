package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Shield is an effect so Minecraft removes its armor modifier on expiry, death, and unload. */
public final class ModEffects {
    private ModEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ModernCompanions.MOD_ID);
    // Visible markers keep vanilla mechanics while giving each potion its supplied HUD icon.
    public static final DeferredHolder<MobEffect, MobEffect> COMPANION_REGENERATION = marker("regeneration", 0x3DBA5D);
    public static final DeferredHolder<MobEffect, MobEffect> COMPANION_STAMINA = marker("stamina", 0x59B84B);
    public static final DeferredHolder<MobEffect, MobEffect> COMPANION_MANA = marker("mana", 0x5A7EEA);
    public static final DeferredHolder<MobEffect, MobEffect> COMPANION_REJUVENATION = marker("rejuvenation", 0xE18A39);
    public static final DeferredHolder<MobEffect, MobEffect> COMPANION_SHIELD = EFFECTS.register("companion_shield", () ->
            new ShieldEffect()
                    .addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "potion_shield"), 4.0D, AttributeModifier.Operation.ADD_VALUE));

    private static DeferredHolder<MobEffect, MobEffect> marker(String name, int color) {
        return EFFECTS.register(name, () -> new MarkerEffect(color));
    }

    private static final class MarkerEffect extends MobEffect {
        private MarkerEffect(int color) { super(MobEffectCategory.BENEFICIAL, color); }
    }

    private static final class ShieldEffect extends MobEffect {
        private ShieldEffect() { super(MobEffectCategory.BENEFICIAL, 0xF4F4F4); }
    }
}
