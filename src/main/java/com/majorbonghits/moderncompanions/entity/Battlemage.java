package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;

/** Armoured close-range caster. */
public class Battlemage extends IntegratedMageCompanion {
    private static final double MELEE_FALLBACK_REACH = 2.5D;

    public Battlemage(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        // The casting goal owns range when Mana is available; fists/gear take over while it refreshes.
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return !isSpellCasting() && !canUseRangedAttack() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isSpellCasting() && !canUseRangedAttack() && super.canContinueToUse();
            }
        });
    }

    @Override
    public boolean canUseRangedAttack() {
        return canSpendMana(BASIC_MANA_COST) && !isEnemyWithinMeleeReach();
    }

    private boolean isEnemyWithinMeleeReach() {
        LivingEntity target = getTarget();
        return target != null && target.isAlive()
                && distanceToSqr(target) <= MELEE_FALLBACK_REACH * MELEE_FALLBACK_REACH;
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.BATTLEMAGE; }
}
