package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import com.majorbonghits.moderncompanions.core.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/** One reusable companion implementation whose firearm specialty is persistent data. */
public class FirearmSpecialist extends AbstractHumanCompanionEntity {
    private static final EntityDataAccessor<String> SPECIALTY = SynchedEntityData.defineId(
            FirearmSpecialist.class, EntityDataSerializers.STRING);
    private static final String SPECIALTY_TAG = "FirearmSpecialty";
    private static final int SPECIALTY_WEIGHT_TOTAL = 100;

    public FirearmSpecialist(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPECIALTY, FirearmSupport.Specialty.UNASSIGNED.id());
    }

    public FirearmSupport.Specialty getFirearmSpecialty() {
        return FirearmSupport.Specialty.fromId(this.entityData.get(SPECIALTY));
    }

    @Override
    public Component getClassDisplayName() {
        return Component.translatable(getFirearmSpecialty().displayNameKey());
    }

    public void setFirearmSpecialty(FirearmSupport.Specialty specialty) {
        this.entityData.set(SPECIALTY,
                specialty == null ? FirearmSupport.Specialty.UNASSIGNED.id() : specialty.id());
    }

    /** Replaces the random spawn roll when a specialty-specific summon gem is used. */
    public void applySummonedSpecialty(FirearmSupport.Specialty specialty) {
        if (specialty == null || specialty == FirearmSupport.Specialty.UNASSIGNED) return;
        setFirearmSpecialty(specialty);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (FirearmSupport.isTacZFirearm(inventory.getItem(slot))) inventory.setItem(slot, ItemStack.EMPTY);
        }
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            FirearmSupport.SpawnLoadout loadout = FirearmSupport.createSpawnLoadout(
                    specialty, random, level().registryAccess());
            if (!loadout.gun().isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, loadout.gun());
            if (!loadout.ammo().isEmpty()) inventory.setItem(5, loadout.ammo());
        }
        FirearmSupport.equipFirearm(this);
    }

    @Override
    public boolean isFirearmAllowed(ItemStack stack) {
        return getFirearmSpecialty() != FirearmSupport.Specialty.UNASSIGNED
                && FirearmSupport.classify(stack).orElse(FirearmSupport.Specialty.UNASSIGNED) == getFirearmSpecialty();
    }

    @Override
    public boolean canEquipInSlot(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && !stack.isEmpty()) {
            return FirearmSupport.isAllowedFirearm(this, stack);
        }
        return super.canEquipInSlot(slot, stack);
    }

    @Override
    protected boolean isAutomaticMainHandCandidate(ItemStack stack) {
        return FirearmSupport.isAllowedFirearm(this, stack);
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && getFirearmSpecialty() != FirearmSupport.Specialty.UNASSIGNED) {
            normalizeSpecialtyWeapon();
            FirearmSupport.equipFirearm(this);
        }
        super.tick();
    }

    private void normalizeSpecialtyWeapon() {
        ItemStack hand = getItemBySlot(EquipmentSlot.MAINHAND);
        if (hand.isEmpty()) return;
        if (!FirearmSupport.isTacZFirearm(hand)) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return;
        }
        // TacZ's resource index can still be rebuilding during entity load. Preserve the
        // serialized gun until classification is available; a known wrong category is removed.
        var category = FirearmSupport.classify(hand);
        if (category.isPresent() && category.get() != getFirearmSpecialty()) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(SPECIALTY_TAG, getFirearmSpecialty().id());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        setFirearmSpecialty(FirearmSupport.Specialty.fromId(tag.getString(SPECIALTY_TAG)));
        super.readAdditionalSaveData(tag);
        if (!level().isClientSide() && getFirearmSpecialty() != FirearmSupport.Specialty.UNASSIGNED) {
            normalizeSpecialtyWeapon();
            FirearmSupport.equipFirearm(this);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        if (getFirearmSpecialty() == FirearmSupport.Specialty.UNASSIGNED) {
            setFirearmSpecialty(chooseSpecialty(random));
        }
        if (ModConfig.safeGet(ModConfig.SPAWN_WEAPON)) {
            FirearmSupport.SpawnLoadout loadout = FirearmSupport.createSpawnLoadout(
                    getFirearmSpecialty(), random, level.registryAccess());
            if (!loadout.gun().isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, loadout.gun());
            if (!loadout.ammo().isEmpty()) inventory.setItem(5, loadout.ammo());
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private static FirearmSupport.Specialty chooseSpecialty(RandomSource random) {
        int roll = random.nextInt(SPECIALTY_WEIGHT_TOTAL);
        for (FirearmSupport.Specialty specialty : FirearmSupport.Specialty.values()) {
            if (specialty == FirearmSupport.Specialty.UNASSIGNED) continue;
            roll -= specialty.weight();
            if (roll < 0) return specialty;
        }
        return FirearmSupport.Specialty.PISTOL;
    }

}
