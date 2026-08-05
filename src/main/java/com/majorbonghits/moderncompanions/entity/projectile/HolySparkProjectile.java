package com.majorbonghits.moderncompanions.entity.projectile;

import com.majorbonghits.moderncompanions.core.ModEntityTypes;
import com.majorbonghits.moderncompanions.entity.magic.AbstractMageCompanion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A lightweight holy projectile whose trail is made from vanilla sparkle particles. */
public class HolySparkProjectile extends Projectile {
    private static final int MAX_LIFETIME_TICKS = 40;
    private static final float BASE_DAMAGE = 6.0F;
    private static final float UNDEAD_DAMAGE_MULTIPLIER = 2.0F;

    public HolySparkProjectile(EntityType<? extends HolySparkProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public HolySparkProjectile(Level level, AbstractMageCompanion owner, LivingEntity target) {
        this(ModEntityTypes.HOLY_SPARK.get(), level);
        setOwner(owner);
        Vec3 origin = owner.getEyePosition().add(owner.getLookAngle().scale(0.35D));
        Vec3 direction = target.getEyePosition().subtract(origin).normalize();
        setPos(origin);
        setDeltaMovement(direction.scale(1.6D));
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > MAX_LIFETIME_TICKS) {
            discard();
            return;
        }

        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            level().addParticle(ParticleTypes.WAX_ON, getX(), getY(), getZ(), 0.0D, 0.02D, 0.0D);
        }

        Vec3 movement = getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            if (!isAlive()) return;
        }
        setPos(position().add(movement));
        updateRotation();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity owner = getOwner();
        if (!level().isClientSide() && owner instanceof AbstractMageCompanion caster
                && result.getEntity() instanceof LivingEntity target
                && caster.canHarm(target)) {
            float damage = caster.magicDamage(BASE_DAMAGE);
            if (target.getType().is(EntityTypeTags.UNDEAD)) {
                damage *= UNDEAD_DAMAGE_MULTIPLIER;
            }
            target.hurt(caster.damageSources().mobAttack(caster), damage);
        }
        discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        discard();
    }
}
