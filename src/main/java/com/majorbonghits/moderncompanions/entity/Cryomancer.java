package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Focused ice control without terrain-changing fallbacks. */
public class Cryomancer extends IntegratedMageCompanion {
    public Cryomancer(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.CRYOMANCER; }
}
