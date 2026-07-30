package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Curses and short-lived area denial. */
public class Hag extends IntegratedMageCompanion {
    public Hag(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }
    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.HAG; }
}
