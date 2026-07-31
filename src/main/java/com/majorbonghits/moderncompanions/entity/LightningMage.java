package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Precision lightning caster with a capped upstream signature spell. */
public class LightningMage extends IntegratedMageCompanion {
    public LightningMage(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.LIGHTNING_MAGE; }
}
