package com.majorbonghits.moderncompanions.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

/** Uses the static summon-gem textures without applying a runtime spawn-egg palette. */
public class SummonGemItem extends DeferredSpawnEggItem {
    public SummonGemItem(Supplier<? extends EntityType<? extends Mob>> type, Properties properties) {
        super(type, 0xFFFFFF, 0xFFFFFF, properties);
    }
}
