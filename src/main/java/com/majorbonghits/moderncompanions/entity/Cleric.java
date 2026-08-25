package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.entity.magic.IntegratedMageCompanion;
import com.majorbonghits.moderncompanions.entity.magic.MagicCompanionKit;
import com.majorbonghits.moderncompanions.entity.projectile.HolySparkProjectile;
import com.majorbonghits.moderncompanions.item.QuarterstaffItem;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

/** Cleric with the original standalone support kit when no optional spell mod is loaded. */
public class Cleric extends IntegratedMageCompanion {
    private static final int CLERIC_SPELL_MANA_COST = 6;
    private static final int OWNER_HEAL_COOLDOWN_TICKS = 30;
    private static final int ALLY_HEAL_COOLDOWN_TICKS = 30;
    private static final int SELF_HEAL_COOLDOWN_TICKS = 30;
    private static final float HEAL_AMOUNT = 8.0F;
    private static final int SUPPORT_CAST_FALLBACK_TICKS = 10;
    private static final double HEALING_DISTANCE = 10.0D;
    private static final double ALLY_HEAL_RADIUS = 32.0D;
    private int ownerHealCooldown;
    private int allyHealCooldown;
    private int selfHealCooldown;
    private int healTicker;

    public Cleric(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new Goal() {
            {
                setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            }

            @Override
            public boolean canUse() {
                return isHealingMode()
                        && getTarget() != null && getTarget().isAlive();
            }

            @Override
            public boolean canContinueToUse() {
                return canUse();
            }

            @Override
            public void tick() {
                keepDistanceFrom(getTarget());
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return !isHealingMode() && !canUseRangedAttack() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isHealingMode() && !canUseRangedAttack() && super.canContinueToUse();
            }
        });
    }

    @Override
    protected boolean usesRangedCombat() {
        return true;
    }

    @Override
    public boolean canUseRangedAttack() {
        return !isHealingMode() && canSpendMana(CLERIC_SPELL_MANA_COST);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (target == null || !canUseRangedAttack() || !safeTarget(target, 2.5F)) return;
        beginSpellCast(target, spellCastTimeTicks("heal", SUPPORT_CAST_FALLBACK_TICKS), () -> {
            if (!canUseRangedAttack() || !safeTarget(target, 2.5F)) return;
            aimAt(target);
            level().addFreshEntity(new HolySparkProjectile(level(), this, target));
            spendMana(CLERIC_SPELL_MANA_COST);
            swingCast();
        });
    }

    @Override
    protected int basicManaCost() {
        return CLERIC_SPELL_MANA_COST;
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        return false;
    }

    @Override
    protected boolean allowsUtilitySpell() {
        return false;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            if (!MagicCastingCompat.available()) {
                checkStaff();
                if (ownerHealCooldown > 0) ownerHealCooldown--;
                if (allyHealCooldown > 0) allyHealCooldown--;
                if (selfHealCooldown > 0) selfHealCooldown--;
                // The standalone support loop uses the same mana-gated priorities as native healing.
                tickHealing();
                tickBlessings();
            } else if (!isSpellCasting()) {
                if (ownerHealCooldown > 0) ownerHealCooldown--;
                if (allyHealCooldown > 0) allyHealCooldown--;
                if (selfHealCooldown > 0) selfHealCooldown--;
                LivingEntity owner = getOwner();
                if (ownerNeedsHealing() && ownerHealCooldown <= 0 && owner != null && healOwner(owner)) {
                    ownerHealCooldown = OWNER_HEAL_COOLDOWN_TICKS;
                } else if (!ownerNeedsHealing()) {
                    AbstractHumanCompanionEntity ally = injuredAlly();
                    if (ally != null && allyHealCooldown <= 0 && healAlly(ally)) {
                        allyHealCooldown = ALLY_HEAL_COOLDOWN_TICKS;
                    } else if (ally == null && selfNeedsHealing() && selfHealCooldown <= 0 && healSelf()) {
                        selfHealCooldown = SELF_HEAL_COOLDOWN_TICKS;
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);
        if (!MagicCastingCompat.available() && !level().isClientSide() && entity instanceof Mob mob
                && (mob.getType().is(EntityTypeTags.UNDEAD) || mob instanceof Zombie)) {
            mob.hurt(damageSources().mobAttack(this), 3.0F);
        }
        return hit;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!MagicCastingCompat.available() && ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            // The shared superclass owns the live sword; only the fallback totem is class-specific.
            this.setItemSlot(EquipmentSlot.OFFHAND, Items.TOTEM_OF_UNDYING.getDefaultInstance());
            checkStaff();
        }
        return result;
    }

    private boolean healOwner(LivingEntity owner) {
        return healDirect(owner);
    }

    private boolean healAlly(AbstractHumanCompanionEntity ally) {
        return healDirect(ally);
    }

    private boolean healDirect(LivingEntity target) {
        if (!canSpendMana(basicManaCost()) || target == null || !target.isAlive()
                || target.getHealth() >= target.getMaxHealth()) return false;
        return beginSpellCast(target, spellCastTimeTicks("heal", SUPPORT_CAST_FALLBACK_TICKS), () -> {
            if (!target.isAlive() || target.getHealth() >= target.getMaxHealth()
                    || !canSpendMana(basicManaCost())) return;
            aimAt(target);
            float before = target.getHealth();
            target.heal(HEAL_AMOUNT);
            if (target.getHealth() <= before) return;
            spendMana(basicManaCost());
            swingCast();
        });
    }

    private boolean healSelf() {
        return healDirect(this);
    }

    private boolean ownerNeedsHealing() {
        LivingEntity owner = getOwner();
        return owner != null && owner.getHealth() < owner.getMaxHealth() - 0.5F;
    }

    private boolean selfNeedsHealing() {
        return getHealth() < getMaxHealth() - 0.5F;
    }

    private AbstractHumanCompanionEntity injuredAlly() {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        return level().getEntitiesOfClass(AbstractHumanCompanionEntity.class, getBoundingBox().inflate(ALLY_HEAL_RADIUS))
                .stream()
                .filter(ally -> ally != this && ally.isAlive() && ownerId.equals(ally.getOwnerUUID())
                        && ally.getHealth() < ally.getMaxHealth() - 0.5F)
                .min(Comparator.comparingDouble(ally -> ally.getHealth() / ally.getMaxHealth()))
                .orElse(null);
    }

    private boolean isHealingMode() {
        return canSpendMana(basicManaCost())
                && (ownerNeedsHealing() || injuredAlly() != null || selfNeedsHealing());
    }

    private void keepDistanceFrom(LivingEntity target) {
        if (target == null) return;
        double dx = getX() - target.getX();
        double dz = getZ() - target.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 0.001D) {
            dx = -getLookAngle().x;
            dz = -getLookAngle().z;
            distance = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        }
        if (distance < HEALING_DISTANCE) {
            getNavigation().moveTo(getX() + dx / distance * 2.5D, getY(),
                    getZ() + dz / distance * 2.5D, 1.1D);
        } else {
            getNavigation().stop();
        }
        getLookControl().setLookAt(target, 45.0F, 45.0F);
    }

    private void checkStaff() {
        ItemStack hand = this.getFunctionalEquipmentItem(EquipmentSlot.MAINHAND);
        ItemStack firearm = getEquippedOrInventoryFirearm();
        if (!firearm.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(hand, firearm)) this.setItemSlot(EquipmentSlot.MAINHAND, firearm);
            setPreferredWeaponBonus(true);
            return;
        }
        ItemStack preferred = ItemStack.EMPTY;
        ItemStack fallback = !hand.isEmpty() && !isShieldItem(hand) ? hand : ItemStack.EMPTY;
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            if (preferred.isEmpty() && (stack.is(Items.GOLDEN_SWORD) || stack.getItem() instanceof BowItem
                    || stack.getItem() instanceof QuarterstaffItem)) preferred = stack;
            if (fallback.isEmpty() && !isShieldItem(stack)) fallback = stack;
        }
        ItemStack desired = !preferred.isEmpty() ? preferred : fallback;
        if (!ItemStack.isSameItemSameComponents(hand, desired)) this.setItemSlot(EquipmentSlot.MAINHAND, desired);
        setPreferredWeaponBonus(!preferred.isEmpty() && ItemStack.isSameItemSameComponents(desired, preferred));
        ItemStack offhand = this.getFunctionalEquipmentItem(EquipmentSlot.OFFHAND);
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING) && offhand.isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, stack);
                offhand = stack;
            }
        }
    }

    private void tickHealing() {
        if (isSpellCasting() || ++healTicker % 30 != 0) return;
        LivingEntity owner = getOwner();
        if (ownerNeedsHealing() && ownerHealCooldown <= 0 && owner != null && healOwner(owner)) {
            ownerHealCooldown = OWNER_HEAL_COOLDOWN_TICKS;
            return;
        }
        AbstractHumanCompanionEntity ally = injuredAlly();
        if (ally != null && allyHealCooldown <= 0 && healAlly(ally)) {
            allyHealCooldown = ALLY_HEAL_COOLDOWN_TICKS;
            return;
        }
        if (selfNeedsHealing() && selfHealCooldown <= 0 && healSelf()) {
            selfHealCooldown = SELF_HEAL_COOLDOWN_TICKS;
            return;
        }
    }

    private void tickBlessings() {
        if (isSpellCasting() || !canSpendMana(CLERIC_SPELL_MANA_COST)
                || this.random.nextInt(120) != 0) return;
        LivingEntity first = level().getEntities(this, getBoundingBox().inflate(6.0D), this::isAlly)
                .stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .findFirst()
                .orElse(null);
        if (first == null) return;
        aimAt(first);
        level().getEntities(this, getBoundingBox().inflate(6.0D), this::isAlly)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, true, true));
                        living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, true));
                    }
                });
        spendMana(CLERIC_SPELL_MANA_COST);
        swingCast();
    }

    private boolean isAlly(Entity entity) {
        if (entity == this) return false;
        if (entity instanceof AbstractHumanCompanionEntity companion) {
            return companion.getOwner() != null && this.getOwner() != null && companion.getOwner() == this.getOwner();
        }
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame() && this.getOwner() != null && this.getOwner().equals(tamable.getOwner());
        }
        return entity == this.getOwner();
    }

    @Override
    protected MagicCompanionKit kit() {
        return MagicCompanionKit.CLERIC;
    }
}
