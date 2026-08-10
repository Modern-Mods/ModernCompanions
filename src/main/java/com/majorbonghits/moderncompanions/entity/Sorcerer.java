package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Elemental caster, deliberately distinct from Wizard control. */
public class Sorcerer extends IntegratedMageCompanion {
    public Sorcerer(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    /** Chain Lightning must never choose players or friendly entities as a chained victim. */
    @Override
    public boolean canHarm(Entity entity) {
        if (entity instanceof Player) return false;
        if (isCompanionOrPet(entity)) return ModConfig.safeGet(ModConfig.FRIENDLY_FIRE_COMPANIONS);
        return entity.getType().getCategory() == MobCategory.MONSTER;
    }

    private boolean isCompanionOrPet(Entity entity) {
        return entity instanceof AbstractHumanCompanionEntity
                || Beastmaster.isBeastmasterPet(entity)
                || entity instanceof TamableAnimal tame && tame.isTame();
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.SORCERER; }
}
