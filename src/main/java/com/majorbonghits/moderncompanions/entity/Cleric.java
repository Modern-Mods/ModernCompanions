package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Primary healer; its loaded spell mod owns healing and cleanse semantics. */
public class Cleric extends IntegratedMageCompanion {
    public Cleric(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.CLERIC; }
}
