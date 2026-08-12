package com.majorbonghits.moderncompanions.entity.magic;

import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Shared AI bridge: one basic cast, one guarded signature cast, and one self utility per kit. */
public abstract class IntegratedMageCompanion extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 160;
    protected static final int BASIC_MANA_COST = 10;
    private static final int UTILITY_MANA_COST = 20;
    private static final int HEAVY_MANA_COST = 35;
    private int utilityCooldown;

    protected IntegratedMageCompanion(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    protected abstract MagicCompanionKit kit();

    /** Expose the kit's target contract to the shared ranged and damage guards. */
    public final boolean requiresHostileTargets() {
        return kit().hostileTargetsOnly;
    }

    protected int basicManaCost() { return BASIC_MANA_COST; }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && utilityCooldown > 0) utilityCooldown--;
        if (!level().isClientSide() && !isSpellCasting() && allowsUtilitySpell() && utilityCooldown <= 0
                && getHealth() * 2.0F < getMaxHealth() && canSpendMana(UTILITY_MANA_COST)) {
            MagicCompanionKit kit = kit();
            beginSpellCast(this, spellCastTimeTicks(kit.ironUtility, 10), () -> {
                if (getHealth() * 2.0F >= getMaxHealth() || !canSpendMana(UTILITY_MANA_COST)) return;
                if (MagicCastingCompat.cast(this, this, kit.ironUtility, kit.arsUtility)) {
                    spendMana(UTILITY_MANA_COST);
                    swingCast();
                    utilityCooldown = 200;
                }
            });
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!safeTarget(target, 2.5F)) return;
        beginSpellCast(target, spellCastTimeTicks(kit().ironBasic, 10),
                () -> castBasicSpellAt(target));
    }

    /** Shared basic spell path for offensive casters and Cleric's owner heal. */
    protected final boolean castBasicSpellAt(LivingEntity target) {
        int manaCost = basicManaCost();
        if (!safeTarget(target, 2.5F) || !canSpendMana(manaCost)) return false;
        aimAt(target);
        MagicCompanionKit kit = kit();
        ItemStack spellbook = getSpellbookItem();
        // A dedicated book supplements the class kit: mix successful light casts so
        // both the book's native active spell and the companion's learned repertoire are used.
        boolean trySpellbookFirst = !spellbook.isEmpty() && getRandom().nextBoolean();
        if (trySpellbookFirst && MagicCastingCompat.castItem(this, target, spellbook)) {
            spendMana(manaCost);
            swingCast();
            return true;
        }
        if (MagicCastingCompat.castHeldItem(this, target)) {
            spendMana(manaCost);
            swingCast();
            return true;
        }
        if (!trySpellbookFirst && !spellbook.isEmpty()
                && MagicCastingCompat.castItem(this, target, spellbook)) {
            spendMana(manaCost);
            swingCast();
            return true;
        }
        if (!MagicCastingCompat.cast(this, target, kit.ironBasic, kit.arsBasic)) return false;
        spendMana(manaCost);
        swingCast();
        return true;
    }

    @Override
    public boolean tryHeavyAttack(LivingEntity target, float distanceFactor) {
        if (heavyCooldown > 0 || !safeTarget(target, 5.0F) || !canSpendMana(HEAVY_MANA_COST)) return false;
        MagicCompanionKit kit = kit();
        return beginSpellCast(target, spellCastTimeTicks(kit.ironHeavy, 10), () -> {
            if (!safeTarget(target, 5.0F) || !canSpendMana(HEAVY_MANA_COST)) return;
            if (MagicCastingCompat.cast(this, target, kit.ironHeavy, kit.arsHeavy)) {
                spendMana(HEAVY_MANA_COST);
                heavyCooldown = magicCooldownTicks(HEAVY_COOLDOWN_TICKS);
                swingCast();
            }
        });
    }

    @Override
    public int getLightIntervalTicks() {
        return magicCooldownTicks(30);
    }

    @Override
    public int getHeavyRecoveryTicks() {
        return magicCooldownTicks(HEAVY_COOLDOWN_TICKS);
    }

    protected final boolean safeTarget(LivingEntity target, float radius) {
        if (!target.isAlive() || !getSensing().hasLineOfSight(target) || target instanceof net.minecraft.world.entity.player.Player
                || !canHarm(target) || (requiresHostileTargets() && target.getType().getCategory() != MobCategory.MONSTER)
                || isAlliedTo(target) || isOwnerInDanger(target, radius)) return false;
        return level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius), this::isAllyNearTarget).isEmpty();
    }

    private boolean isAllyNearTarget(LivingEntity entity) {
        if (entity == this || entity == getTarget()) return false;
        return isAlliedTo(entity) || entity == getOwner() || (entity instanceof TamableAnimal tame && getOwner() != null && getOwner().equals(tame.getOwner()));
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return super.isAlliedTo(other) || other == getOwner();
    }
}
