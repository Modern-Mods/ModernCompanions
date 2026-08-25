package com.majorbonghits.moderncompanions.entity.projectile;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.CompanionProtectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Small fireball that damages targets without igniting terrain. */
public class NonIgnitingSmallFireball extends SmallFireball {
    public NonIgnitingSmallFireball(EntityType<? extends NonIgnitingSmallFireball> type, Level level) {
        super(type, level);
    }

    public NonIgnitingSmallFireball(Level level, LivingEntity owner, Vec3 power) {
        super(level, owner, power);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) explodeEffect();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (this.level() instanceof ServerLevel
                && this.getOwner() instanceof AbstractHumanCompanionEntity companion
                && hit instanceof LivingEntity living
                && CompanionProtectionEvents.canDamage(companion, living)) {
            DamageSource source = this.damageSources().fireball(this, companion);
            living.hurt(source, 10.0F);
        }
        if (!this.level().isClientSide) explodeEffect();
    }

    private void explodeEffect() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, ExplosionInteraction.NONE);
        this.discard();
    }
}
