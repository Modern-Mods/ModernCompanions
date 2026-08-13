package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.item.AlchemistRecipeData;
import com.majorbonghits.moderncompanions.item.AlchemistRecipeItem;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Potion support that uses real splash projectiles and falls back to native melee AI. */
public class Alchemist extends AbstractHumanCompanionEntity {
    private static final int THROW_COOLDOWN_TICKS = 24;
    private static final int BREW_CHECK_TICKS = 20;
    private static final int BREW_TIME_TICKS = 200; // Half of a vanilla Brewing Stand's 400-tick brew time.
    private static final double COMBAT_RADIUS = 14.0D;
    private static final double MIN_HOSTILE_THROW_DISTANCE = 3.2D;

    private int throwCooldown;
    private int brewCooldown;
    private int brewTicker;
    @Nullable
    private AlchemistRecipeData brewingRecipe;
    private int brewingRecipeSlot = -1;

    public Alchemist(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            public boolean canUse() {
                return !hasUsefulPotionAction() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !hasUsefulPotionAction() && super.canContinueToUse();
            }
        });
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (throwCooldown > 0) throwCooldown--;
            if (brewCooldown > 0) brewCooldown--;
            tickBrewing();
            if (throwCooldown <= 0) throwBestPotion();
            if (!hasUsefulPotionAction()) equipMeleeWeapon();
        }
        super.tick();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON) && this.inventory.getItem(4).isEmpty()) {
            // A spawned Alchemist gets one real heal to demonstrate the ranged role immediately.
            this.inventory.setItem(4, PotionContents.createItemStack(Items.SPLASH_POTION,
                    net.minecraft.world.item.alchemy.Potions.HEALING));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private void tickBrewing() {
        if (brewingRecipe != null) {
            if (brewCooldown <= 0) finishBrewing();
            return;
        }
        if (brewCooldown > 0 || ++brewTicker < BREW_CHECK_TICKS) return;
        brewTicker = 0;

        for (int recipeSlot = 0; recipeSlot < this.inventory.getContainerSize(); recipeSlot++) {
            ItemStack recipeStack = this.inventory.getItem(recipeSlot);
            AlchemistRecipeData recipe = AlchemistRecipeItem.data(recipeStack);
            if (recipe == null) continue;

            ItemStack output = com.majorbonghits.moderncompanions.item.AlchemistBrewing.createSplash(recipe);
            if (output.isEmpty() || !hasInventorySpace(output)) return;

            if (findIngredientSlots(recipe) == null) return;
            // Keep the ingredients in place until the timed brew completes; failed completion never consumes them.
            brewingRecipe = recipe;
            brewingRecipeSlot = recipeSlot;
            brewCooldown = BREW_TIME_TICKS;
            return;
        }
    }

    private void finishBrewing() {
        AlchemistRecipeData recipe = brewingRecipe;
        int recipeSlot = brewingRecipeSlot;
        brewingRecipe = null;
        brewingRecipeSlot = -1;
        if (recipe == null || recipeSlot < 0 || recipeSlot >= this.inventory.getContainerSize()) return;

        AlchemistRecipeData currentRecipe = AlchemistRecipeItem.data(this.inventory.getItem(recipeSlot));
        if (!recipe.equals(currentRecipe)) return;

        ItemStack output = com.majorbonghits.moderncompanions.item.AlchemistBrewing.createSplash(recipe);
        int[] ingredientSlots = findIngredientSlots(recipe);
        if (output.isEmpty() || ingredientSlots == null || !hasInventorySpace(output)) return;

        for (int slot : ingredientSlots) this.inventory.getItem(slot).shrink(1);
        this.inventory.addItem(output);
        this.inventory.setChanged();
    }

    @Nullable
    private int[] findIngredientSlots(AlchemistRecipeData recipe) {
        int[] ingredientSlots = new int[recipe.ingredients().size()];
        for (int index = 0; index < ingredientSlots.length; index++) {
            ingredientSlots[index] = findIngredientSlot(recipe.ingredients().get(index), ingredientSlots, index);
            if (ingredientSlots[index] < 0) return null;
        }
        return ingredientSlots;
    }

    private int findIngredientSlot(net.minecraft.resources.ResourceLocation id, int[] usedSlots, int usedCount) {
        Item ingredient = com.majorbonghits.moderncompanions.item.AlchemistBrewing.resolveIngredient(id);
        if (ingredient == Items.AIR) return -1;
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            boolean alreadyUsed = false;
            for (int index = 0; index < usedCount; index++) {
                if (usedSlots[index] == slot) {
                    alreadyUsed = true;
                    break;
                }
            }
            ItemStack stack = this.inventory.getItem(slot);
            if (!alreadyUsed && stack.getItem() == ingredient && !stack.isEmpty()) return slot;
        }
        return -1;
    }

    private boolean hasInventorySpace(ItemStack output) {
        int remaining = output.getCount();
        for (int slot = 0; slot < this.inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty()) {
                remaining -= this.inventory.getMaxStackSize(output);
            } else if (ItemStack.isSameItemSameComponents(stack, output)) {
                remaining -= Math.max(0, this.inventory.getMaxStackSize(stack) - stack.getCount());
            }
        }
        return remaining <= 0;
    }

    @Override
    protected int getInventoryStackLimit(ItemStack stack, int vanillaLimit) {
        // Potions remain normal single-item stacks everywhere else; only this companion's cargo is virtual-stacked.
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) ? 64 : vanillaLimit;
    }

    private void throwBestPotion() {
        PotionAction action = findPotionAction();
        if (action == null) return;
        // Recheck hostile visibility after selection so terrain changes cannot turn the throw into a ground splash.
        if (!isAlly(action.target()) && !this.hasLineOfSight(action.target())) return;

        ItemStack thrownStack = action.stack().copyWithCount(1);
        // Face the selected entity immediately; the projectile must follow the same explicit aim point.
        this.lookAt(EntityAnchorArgument.Anchor.EYES, action.target().getEyePosition());
        this.getNavigation().stop();
        ThrownPotion thrown = new ThrownPotion(this.level(), this);
        thrown.setItem(thrownStack);
        thrown.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());

        double dx = action.target().getX() - this.getX();
        double dz = action.target().getZ() - this.getZ();
        double dy = action.target().getY() + action.target().getBbHeight() * 0.5D - this.getEyeY();
        // Vanilla witch-style spread made these throws visibly random; use the selected target with no inaccuracy.
        thrown.shoot(dx, dy, dz, 0.75F, 0.0F);
        this.level().addFreshEntity(thrown);

        action.stack().shrink(1);
        this.inventory.setChanged();
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        this.playSound(SoundEvents.SPLASH_POTION_THROW, 0.5F,
                0.4F / (this.random.nextFloat() * 0.4F + 0.8F));
        throwCooldown = THROW_COOLDOWN_TICKS;
    }

    private PotionAction findPotionAction() {
        List<LivingEntity> allies = nearbyAllies();
        List<LivingEntity> enemies = nearbyEnemies();
        PotionAction best = null;

        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (!stack.is(Items.SPLASH_POTION)) continue;
            best = better(best, evaluatePotion(stack, allies, enemies));
        }
        ItemStack hand = this.getMainHandItem();
        if (hand.is(Items.SPLASH_POTION)) {
            best = better(best, evaluatePotion(hand, allies, enemies));
        }
        return best;
    }

    @Nullable
    private PotionAction evaluatePotion(ItemStack stack, List<LivingEntity> allies, List<LivingEntity> enemies) {
        String potion = potionId(stack);
        if (potion == null) return null;
        String behaviorPotion = potion.startsWith("long_") ? potion.substring("long_".length()) : potion;

        LivingEntity lowest = lowestHealthAlly(allies);
        LivingEntity threat = bestEnemy(enemies);
        LivingEntity player = this.getOwner() instanceof LivingEntity owner ? owner : this;
        double lowestRatio = lowest == null ? 1.0D : healthRatio(lowest);
        int cluster = threat == null ? 0 : nearbyEnemyCount(threat, enemies);
        boolean intense = enemies.size() >= 3 || cluster >= 3;
        boolean fighting = threat != null || this.getTarget() != null;
        boolean critical = lowestRatio < 0.32D;
        double score = -1.0D;
        LivingEntity target = null;

        switch (behaviorPotion) {
            case "healing" -> {
                if (lowest != null && lowestRatio < 0.78D && !hasPotionEffect(lowest, potion)) {
                    target = lowest;
                    score = 80.0D + (1.0D - lowestRatio) * 80.0D;
                }
            }
            case "strong_healing" -> {
                if (lowest != null && lowestRatio < 0.38D) {
                    target = lowest;
                    score = 180.0D + (1.0D - lowestRatio) * 100.0D;
                }
            }
            case "regeneration" -> {
                if (fighting && lowest != null && lowestRatio < 0.90D && lowestRatio > 0.45D
                        && !hasPotionEffect(lowest, potion)) {
                    target = lowest;
                    score = 95.0D;
                }
            }
            case "strong_regeneration" -> {
                if (fighting && lowest != null && lowestRatio < 0.65D && intense
                        && !hasPotionEffect(lowest, potion)) {
                    target = lowest;
                    score = 150.0D;
                }
            }
            case "harming" -> {
                if (threat != null) {
                    target = threat;
                    score = 105.0D + cluster * 12.0D;
                }
            }
            case "strong_harming" -> {
                if (threat != null) {
                    target = threat;
                    score = 170.0D + cluster * 15.0D + threat.getMaxHealth() * 0.4D;
                }
            }
            case "poison" -> {
                if (threat != null && threat.getMaxHealth() >= 20.0F && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 100.0D + threat.getMaxHealth() * 0.4D;
                }
            }
            case "strong_poison" -> {
                if (threat != null && threat.getMaxHealth() >= 26.0F && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 125.0D + threat.getMaxHealth() * 0.5D;
                }
            }
            case "strength" -> {
                if (fighting && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 115.0D;
                }
            }
            case "strong_strength" -> {
                if (fighting && (intense || (threat != null && threat.getMaxHealth() >= 35.0F))
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 165.0D;
                }
            }
            case "swiftness" -> {
                if (fighting && threat != null && (isAroundPlayer(threat) || this.distanceToSqr(threat) > 36.0D)
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 88.0D;
                }
            }
            case "strong_swiftness" -> {
                if (fighting && threat != null && (healthRatio(player) < 0.45D || this.distanceToSqr(threat) > 144.0D)
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 145.0D;
                }
            }
            case "leaping" -> {
                if (threat != null && Math.abs(threat.getY() - player.getY()) > 2.5D
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 70.0D;
                }
            }
            case "strong_leaping" -> {
                if (threat != null && Math.abs(threat.getY() - player.getY()) > 5.0D
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 100.0D;
                }
            }
            case "fire_resistance" -> {
                if ((player.isOnFire() || player.isInLava() || enemies.stream().anyMatch(Entity::fireImmune))
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 190.0D;
                }
            }
            case "water_breathing" -> {
                if (player.isInWater() && player.getAirSupply() < player.getMaxAirSupply() - 20
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 175.0D;
                }
            }
            case "night_vision" -> {
                if (this.level().getMaxLocalRawBrightness(player.blockPosition()) < 7
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 65.0D;
                }
            }
            case "invisibility" -> {
                if (critical && enemies.size() > allies.size() && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 155.0D;
                }
            }
            case "slow_falling" -> {
                if ((player.fallDistance > 3.0F || player.getDeltaMovement().y < -0.2D)
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 195.0D;
                }
            }
            case "turtle_master" -> {
                if (critical && cluster >= 2 && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 175.0D;
                }
            }
            case "strong_turtle_master" -> {
                if (healthRatio(player) < 0.20D && (cluster >= 3 || intense)
                        && !hasPotionEffect(player, potion)) {
                    target = player;
                    score = 230.0D;
                }
            }
            case "weakness" -> {
                if (threat != null && threat.getMaxHealth() >= 24.0F && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 102.0D + threat.getMaxHealth() * 0.35D;
                }
            }
            case "slowness" -> {
                if (threat != null && (isAroundPlayer(threat) || this.getTarget() == threat)
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 98.0D + cluster * 8.0D;
                }
            }
            case "strong_slowness" -> {
                if (threat != null && (threat.getMaxHealth() >= 30.0F || cluster >= 3)
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 145.0D + cluster * 10.0D;
                }
            }
            case "wind_charged" -> {
                if (threat != null && healthRatio(threat) < 0.35D && cluster >= 2
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 110.0D + cluster * 10.0D;
                }
            }
            case "weaving" -> {
                if (threat != null && healthRatio(threat) < 0.45D && cluster >= 2
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 108.0D + cluster * 9.0D;
                }
            }
            case "oozing" -> {
                if (threat != null && healthRatio(threat) < 0.25D && (cluster >= 3 || enemies.size() >= 4)
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 90.0D + cluster * 8.0D;
                }
            }
            case "infested" -> {
                if (threat != null && threat.getMaxHealth() >= 30.0F && enemies.size() > allies.size()
                        && !hasPotionEffect(threat, potion)) {
                    target = threat;
                    score = 115.0D + threat.getMaxHealth() * 0.3D;
                }
            }
        }

        boolean hostile = target != null && !isAlly(target);
        if (hostile && this.distanceToSqr(target) < MIN_HOSTILE_THROW_DISTANCE * MIN_HOSTILE_THROW_DISTANCE) {
            return null;
        }
        return score > 0.0D && target != null ? new PotionAction(stack, target, score) : null;
    }

    @Nullable
    private PotionAction better(@Nullable PotionAction current, @Nullable PotionAction candidate) {
        if (candidate == null) return current;
        return current == null || candidate.score() > current.score() ? candidate : current;
    }

    private List<LivingEntity> nearbyAllies() {
        List<LivingEntity> allies = new ArrayList<>();
        allies.add(this);
        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive() && this.distanceToSqr(owner) <= COMBAT_RADIUS * COMBAT_RADIUS) {
            allies.add(owner);
        }
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(COMBAT_RADIUS), this::isAlly)) {
            if (!allies.contains(candidate)) allies.add(candidate);
        }
        return allies;
    }

    private List<LivingEntity> nearbyEnemies() {
        List<LivingEntity> enemies = new ArrayList<>();
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(COMBAT_RADIUS), this::isCombatEnemy)) {
            if (!enemies.contains(candidate)) enemies.add(candidate);
        }
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive() && this.distanceToSqr(current) <= COMBAT_RADIUS * COMBAT_RADIUS
                && !enemies.contains(current) && isCombatEnemy(current)) {
            enemies.add(current);
        }
        return enemies;
    }

    private boolean isCombatEnemy(LivingEntity candidate) {
        // Hostile throws require a direct trace; enemies below or behind terrain are not splash targets.
        if (!candidate.isAlive() || candidate == this || isAlly(candidate)
                || !this.canHarm(candidate) || !this.hasLineOfSight(candidate)) return false;
        return candidate == this.getTarget()
                || candidate.getType().getCategory() == MobCategory.MONSTER
                || candidate instanceof Mob mob && mob.getTarget() == this;
    }

    private boolean isAlly(Entity entity) {
        if (entity == this || entity == this.getOwner()) return true;
        UUID ownerId = this.getOwnerUUID();
        if (ownerId == null) return false;
        if (entity instanceof AbstractHumanCompanionEntity companion) {
            return ownerId.equals(companion.getOwnerUUID());
        }
        return entity instanceof TamableAnimal tame && tame.isTame() && ownerId.equals(tame.getOwnerUUID());
    }

    @Nullable
    private LivingEntity lowestHealthAlly(List<LivingEntity> allies) {
        return allies.stream().filter(LivingEntity::isAlive)
                .min(Comparator.comparingDouble(Alchemist::healthRatio)).orElse(null);
    }

    @Nullable
    private LivingEntity bestEnemy(List<LivingEntity> enemies) {
        return enemies.stream().max(Comparator.comparingDouble(enemy ->
                enemy.getMaxHealth() * 0.4D + nearbyEnemyCount(enemy, enemies) * 12.0D
                        - this.distanceToSqr(enemy) * 0.02D)).orElse(null);
    }

    private int nearbyEnemyCount(LivingEntity center, List<LivingEntity> enemies) {
        int count = 0;
        for (LivingEntity enemy : enemies) {
            if (center.distanceToSqr(enemy) <= 12.25D) count++;
        }
        return count;
    }

    private boolean isAroundPlayer(LivingEntity enemy) {
        LivingEntity owner = this.getOwner();
        return owner != null && owner.distanceToSqr(enemy) <= 36.0D;
    }

    private void equipMeleeWeapon() {
        ItemStack hand = this.getMainHandItem();
        if (!hand.isEmpty() && !(hand.getItem() instanceof PotionItem)
                && !(hand.getItem() instanceof AlchemistRecipeItem)) {
            return;
        }
        ItemStack firearm = getEquippedOrInventoryFirearm();
        if (!firearm.isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, firearm);
            setPreferredWeaponBonus(true);
            return;
        }
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            ItemStack stack = this.inventory.getItem(slot);
            if (stack.getItem() instanceof com.majorbonghits.moderncompanions.item.DaggerItem
                    || stack.getItem() instanceof com.majorbonghits.moderncompanions.item.QuarterstaffItem) {
                this.setItemSlot(EquipmentSlot.MAINHAND, stack);
                setPreferredWeaponBonus(true);
                return;
            }
        }
        setPreferredWeaponBonus(false);
    }

    private boolean hasUsefulPotionAction() {
        return findPotionAction() != null;
    }

    @Nullable
    private static String potionId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().flatMap(holder -> holder.unwrapKey().map(key -> key.location().getPath())).orElse(null);
    }

    private static boolean hasPotionEffect(LivingEntity target, String potion) {
        String base = potion;
        if (base.startsWith("strong_")) base = base.substring("strong_".length());
        if (base.startsWith("long_")) base = base.substring("long_".length());
        return switch (base) {
            case "healing", "harming" -> false;
            case "regeneration" -> target.hasEffect(MobEffects.REGENERATION);
            case "poison" -> target.hasEffect(MobEffects.POISON);
            case "strength" -> target.hasEffect(MobEffects.DAMAGE_BOOST);
            case "swiftness" -> target.hasEffect(MobEffects.MOVEMENT_SPEED);
            case "leaping" -> target.hasEffect(MobEffects.JUMP);
            case "fire_resistance" -> target.hasEffect(MobEffects.FIRE_RESISTANCE);
            case "water_breathing" -> target.hasEffect(MobEffects.WATER_BREATHING);
            case "night_vision" -> target.hasEffect(MobEffects.NIGHT_VISION);
            case "invisibility" -> target.hasEffect(MobEffects.INVISIBILITY);
            case "slow_falling" -> target.hasEffect(MobEffects.SLOW_FALLING);
            case "turtle_master" -> target.hasEffect(MobEffects.DAMAGE_RESISTANCE);
            case "weakness" -> target.hasEffect(MobEffects.WEAKNESS);
            case "slowness" -> target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
            case "wind_charged" -> target.hasEffect(MobEffects.WIND_CHARGED);
            case "weaving" -> target.hasEffect(MobEffects.WEAVING);
            case "oozing" -> target.hasEffect(MobEffects.OOZING);
            case "infested" -> target.hasEffect(MobEffects.INFESTED);
            default -> false;
        };
    }

    private static double healthRatio(LivingEntity entity) {
        return entity.getMaxHealth() <= 0.0F ? 1.0D : entity.getHealth() / entity.getMaxHealth();
    }

    private record PotionAction(ItemStack stack, LivingEntity target, double score) {
    }
}
