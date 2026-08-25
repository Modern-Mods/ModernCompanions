package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/** Attrition caster with a provider path and the original wither-skull/minion fallback. */
public class Necromancer extends IntegratedMageCompanion {
    private static final int LEGACY_HEAVY_COOLDOWN_TICKS = 180;
    private final Random rng = new Random();

    public Necromancer(EntityType<? extends TamableAnimal> type, Level level) { super(type, level); }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) {
            super.performRangedAttack(target, distanceFactor);
            return;
        }
        if (!(this.level() instanceof ServerLevel server) || target == null
                || !canSpendMana(BASIC_MANA_COST) || this.isAlliedTo(target)
                || isOwnerInDanger(target, 3.0F)) return;
        aimAt(target);
        Vec3 dir = target.position().add(0, Math.min(0.1F, target.getBbHeight() * 0.08F), 0)
                .subtract(this.position()).normalize().scale(0.55D);
        var skull = com.majorbonghits.moderncompanions.core.ModEntityTypes.SOFT_WITHER_SKULL.get().create(server);
        if (skull == null) return;
        skull.setNoGravity(true);
        skull.shoot(dir.x, dir.y, dir.z, 1.25F, 0.0F);
        skull.setPos(this.getX(), this.getY() + 1.1F, this.getZ());
        skull.setOwner(this);
        server.addFreshEntity(skull);
        spendMana(BASIC_MANA_COST);
        swingCast();
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (MagicCastingCompat.available()) return super.tryHeavyAttack(target, distanceFactor);
        if (heavyCooldown > 0 || !(this.level() instanceof ServerLevel server)
                || target == null || !canSpendMana(HEAVY_MANA_COST)) return false;
        List<SummonedWitherSkeleton> owned = server.getEntitiesOfClass(SummonedWitherSkeleton.class,
                this.getBoundingBox().inflate(128.0D, 64.0D, 128.0D),
                skel -> skel.getSummoner() == this && skel.isAlive());
        if (!owned.isEmpty()) return false;
        aimAt(target);
        int toSummon = 1 + rng.nextInt(3);
        int spawned = 0;
        int lifetimeSeconds = 60 + rng.nextInt(121);
        for (int i = 0; i < toSummon; i++) {
            SummonedWitherSkeleton skeleton = com.majorbonghits.moderncompanions.core.ModEntityTypes.SUMMONED_WITHER_SKELETON.get().create(server);
            if (skeleton == null) continue;
            double offsetX = (rng.nextDouble() - 0.5D) * 2.5D;
            double offsetZ = (rng.nextDouble() - 0.5D) * 2.5D;
            skeleton.moveTo(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ, this.getYRot(), 0.0F);
            skeleton.configureSummon(this, lifetimeSeconds);
            if (!skeleton.isFriendlyTo(target)) skeleton.setTarget(target);
            server.addFreshEntity(skeleton);
            spawned++;
        }
        if (spawned == 0) return false;
        spendMana(HEAVY_MANA_COST);
        heavyCooldown = LEGACY_HEAVY_COOLDOWN_TICKS;
        swingCast();
        return true;
    }

    @Override
    public int getLightIntervalTicks() {
        return MagicCastingCompat.available() ? super.getLightIntervalTicks() : 20;
    }

    @Override
    public float getPreferredRange() {
        return MagicCastingCompat.available() ? super.getPreferredRange() : 20.0F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!MagicCastingCompat.available() && ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            // The bone remains a summon component in cargo; the shared superclass owns the live dagger.
            this.inventory.setItem(5, Items.BONE.getDefaultInstance());
        }
        return result;
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return MagicCastingCompat.available() ? super.getHeavyRecoveryTicks() : LEGACY_HEAVY_COOLDOWN_TICKS;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof SummonedWitherSkeleton summoned && summoned.getSummoner() == this) return true;
        return super.isAlliedTo(other);
    }

    @Override protected MagicCompanionKit kit() { return MagicCompanionKit.NECROMANCER; }
}
