package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Focused ice control without terrain-changing fallbacks. */
public class Cryomancer extends IntegratedMageCompanion {
    public Cryomancer(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    /** Frostwave is a close AoE: approach the target and cast in place instead of kiting away. */
    @Override public float getMinimumCastingRange() { return 0.0F; }

    @Override public float getHeavyAttackRange() { return 5.0F; }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.CRYOMANCER; }
}
