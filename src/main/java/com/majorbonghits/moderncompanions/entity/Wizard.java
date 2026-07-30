package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Arcane control companion. */
public class Wizard extends IntegratedMageCompanion {
    public Wizard(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.WIZARD; }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.hasIronSummonedSwords(this)) return false;
        return super.tryHeavyAttack(target, distanceFactor);
    }
}
