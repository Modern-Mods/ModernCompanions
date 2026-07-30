package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Armoured close-range caster. */
public class Battlemage extends IntegratedMageCompanion {
    public Battlemage(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.BATTLEMAGE; }
}
