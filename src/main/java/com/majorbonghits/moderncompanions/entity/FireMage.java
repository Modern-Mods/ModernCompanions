package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Fire artillery with an Iron's/Ars path and the original fireball fallback. */
public class FireMage extends IntegratedMageCompanion {
    private static final int LEGACY_HEAVY_COOLDOWN_TICKS = 560;

    public FireMage(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) {
            super.performRangedAttack(target, distanceFactor);
            return;
        }
        if (!(this.level() instanceof ServerLevel server) || target == null
                || !canSpendMana(BASIC_MANA_COST) || isOwnerInDanger(target, 2.5F)) return;
        aimAt(target);
        Vec3 dir = new Vec3(target.getX() - this.getX(), target.getY(0.5D) - this.getEyeY(),
                target.getZ() - this.getZ()).normalize();
        if (dir.lengthSqr() < 1.0E-6D) return;
        var fireball = new com.majorbonghits.moderncompanions.entity.projectile.NonIgnitingSmallFireball(server, this, dir);
        fireball.setPos(this.getX(), this.getEyeY() - 0.2F, this.getZ());
        fireball.shoot(dir.x, dir.y, dir.z, 1.15F, 0.0F);
        fireball.setOwner(this);
        server.addFreshEntity(fireball);
        spendMana(BASIC_MANA_COST);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) return super.tryHeavyAttack(target, distanceFactor);
        if (heavyCooldown > 0 || !(this.level() instanceof ServerLevel server)
                || target == null || !canSpendMana(HEAVY_MANA_COST)
                || isOwnerInDanger(target, 4.0F) || this.random.nextBoolean()) return false;
        aimAt(target);
        Vec3 dir = new Vec3(target.getX() - this.getX(), target.getY(0.35F) - this.getEyeY(),
                target.getZ() - this.getZ()).normalize();
        var fireball = new com.majorbonghits.moderncompanions.entity.projectile.NonExplodingLargeFireball(
                server, this, dir.scale(0.9D), 1);
        fireball.setPos(this.getX(), this.getEyeY() - 0.1F, this.getZ());
        fireball.shoot(dir.x, dir.y, dir.z, 1.1F, 0.0F);
        fireball.setOwner(this);
        server.addFreshEntity(fireball);
        spendMana(HEAVY_MANA_COST);
        heavyCooldown = LEGACY_HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return MagicCastingCompat.available() ? super.getLightIntervalTicks() : 32;
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return MagicCastingCompat.available() ? super.getHeavyRecoveryTicks() : LEGACY_HEAVY_COOLDOWN_TICKS;
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.FIRE_MAGE; }
}
