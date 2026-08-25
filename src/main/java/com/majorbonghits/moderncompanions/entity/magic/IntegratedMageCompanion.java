package com.majorbonghits.moderncompanions.entity.magic;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/** Shared AI bridge: one basic cast, one guarded signature cast, and one self utility per kit. */
public abstract class IntegratedMageCompanion extends AbstractMageCompanion {
    private static final int HEAVY_COOLDOWN_TICKS = 160;
    protected static final int BASIC_MANA_COST = 10;
    private static final int UTILITY_MANA_COST = 20;
    protected static final int HEAVY_MANA_COST = 35;
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

    protected final ItemStack createKitSpawnWeapon() {
        return BuiltInRegistries.ITEM.get(Constants.id(kit().spawnWeaponId())).getDefaultInstance();
    }

    /** Installs the kit's one physical starter in the live hand after all base spawn setup is complete. */
    private void equipKitSpawnWeapon() {
        if (!ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) return;
        ItemStack starter = createKitSpawnWeapon();
        if (starter.isEmpty()) return;

        ItemStack hand = getFunctionalEquipmentItem(EquipmentSlot.MAINHAND);
        if (!hand.isEmpty() && !ItemStack.isSameItemSameComponents(hand, starter)) return;

        // A previous loadout path could leave the same starter in cargo; keep exactly one live copy.
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), starter)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        if (hand.isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, starter);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        equipKitSpawnWeapon();
        return result;
    }

    @Override
    public boolean canUseRangedAttack() {
        // Keep the ranged goal idle when the shared mana pool cannot pay for a basic cast.
        return canSpendMana(basicManaCost());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && utilityCooldown > 0) utilityCooldown--;
        if (!level().isClientSide() && MagicCastingCompat.available()
                && !isSpellCasting() && allowsUtilitySpell() && utilityCooldown <= 0
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
        if (!MagicCastingCompat.available() || !canSpendMana(basicManaCost())
                || target == null || !safeTarget(target, 2.5F)) return;
        ItemStack spellbook = getSpellbookItem();
        int fallbackCastTime = spellCastTimeTicks(kit().ironBasic, 10);
        int castTime = MagicCastingCompat.spellbookCastTimeTicks(this, spellbook, fallbackCastTime);
        beginSpellCast(target, castTime,
                () -> castBasicSpellAt(target));
    }

    /** Shared basic spell path for offensive casters and Cleric's owner heal. */
    protected final boolean castBasicSpellAt(LivingEntity target) {
        int manaCost = basicManaCost();
        if (!safeTarget(target, 2.5F) || !canSpendMana(manaCost)) return false;
        aimAt(target);
        MagicCompanionKit kit = kit();
        ItemStack spellbook = getSpellbookItem();
        // A dedicated spellbook uses its own selected spell timing and native cooldown path.
        if (!spellbook.isEmpty() && MagicCastingCompat.castItem(this, target, spellbook)) {
            spendMana(manaCost);
            swingCast();
            return true;
        }
        if (MagicCastingCompat.castHeldItem(this, target)) {
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
        if (!MagicCastingCompat.available() || !isWithinHeavyAttackRange(target) || heavyCooldown > 0
                || !safeTarget(target, 5.0F)
                || !canSpendMana(HEAVY_MANA_COST)) return false;
        MagicCompanionKit kit = kit();
        return beginSpellCast(target, spellCastTimeTicks(kit.ironHeavy, 10), () -> {
            if (!isWithinHeavyAttackRange(target) || !safeTarget(target, 5.0F)
                    || !canSpendMana(HEAVY_MANA_COST)) return;
            aimAt(target);
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
