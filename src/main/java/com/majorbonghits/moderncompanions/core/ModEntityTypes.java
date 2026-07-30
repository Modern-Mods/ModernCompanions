package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of all companion entity types.
 */
public final class ModEntityTypes {
    private ModEntityTypes() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ModernCompanions.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Knight>> KNIGHT =
            ENTITY_TYPES.register("knight", () -> EntityType.Builder.of(Knight::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("knight")));

    public static final DeferredHolder<EntityType<?>, EntityType<Archer>> ARCHER =
            ENTITY_TYPES.register("archer", () -> EntityType.Builder.of(Archer::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("archer")));

    public static final DeferredHolder<EntityType<?>, EntityType<Arbalist>> ARBALIST =
            ENTITY_TYPES.register("arbalist", () -> EntityType.Builder.of(Arbalist::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("arbalist")));

    public static final DeferredHolder<EntityType<?>, EntityType<Axeguard>> AXEGUARD =
            ENTITY_TYPES.register("axeguard", () -> EntityType.Builder.of(Axeguard::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("axeguard")));

    public static final DeferredHolder<EntityType<?>, EntityType<Vanguard>> VANGUARD =
            ENTITY_TYPES.register("vanguard", () -> EntityType.Builder.of(Vanguard::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("vanguard")));

    public static final DeferredHolder<EntityType<?>, EntityType<Berserker>> BERSERKER =
            ENTITY_TYPES.register("berserker", () -> EntityType.Builder.of(Berserker::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("berserker")));

    public static final DeferredHolder<EntityType<?>, EntityType<Beastmaster>> BEASTMASTER =
            ENTITY_TYPES.register("beastmaster", () -> EntityType.Builder.of(Beastmaster::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("beastmaster")));

    // Register no magic entity at all unless at least one upstream spell API is loaded.
    public static final DeferredHolder<EntityType<?>, EntityType<Cleric>> CLERIC = magic("cleric", Cleric::new);

    public static final DeferredHolder<EntityType<?>, EntityType<Alchemist>> ALCHEMIST =
            ENTITY_TYPES.register("alchemist", () -> EntityType.Builder.of(Alchemist::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("alchemist")));

    public static final DeferredHolder<EntityType<?>, EntityType<Scout>> SCOUT =
            ENTITY_TYPES.register("scout", () -> EntityType.Builder.of(Scout::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("scout")));

    public static final DeferredHolder<EntityType<?>, EntityType<Stormcaller>> STORMCALLER =
            ENTITY_TYPES.register("stormcaller", () -> EntityType.Builder.of(Stormcaller::new, MobCategory.AMBIENT)
                    .sized(0.6F, 1.8F)
                    .build(id("stormcaller")));

    public static final DeferredHolder<EntityType<?>, EntityType<FireMage>> FIRE_MAGE = magic("fire_mage", FireMage::new);
    public static final DeferredHolder<EntityType<?>, EntityType<LightningMage>> LIGHTNING_MAGE = magic("lightning_mage", LightningMage::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Necromancer>> NECROMANCER = magic("necromancer", Necromancer::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Wizard>> WIZARD = magic("wizard", Wizard::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Sorcerer>> SORCERER = magic("sorcerer", Sorcerer::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Warlock>> WARLOCK = magic("warlock", Warlock::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Witch>> WITCH = magic("witch", Witch::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Hag>> HAG = magic("hag", Hag::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Cryomancer>> CRYOMANCER = magic("cryomancer", Cryomancer::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Druid>> DRUID = magic("druid", Druid::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Illusionist>> ILLUSIONIST = magic("illusionist", Illusionist::new);
    public static final DeferredHolder<EntityType<?>, EntityType<Battlemage>> BATTLEMAGE = magic("battlemage", Battlemage::new);

    public static final DeferredHolder<EntityType<?>, EntityType<com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook>> COMPANION_FISHING_HOOK =
            ENTITY_TYPES.register("companion_fishing_hook", () -> EntityType.Builder.<com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook>of(
                            com.majorbonghits.moderncompanions.entity.projectile.CompanionFishingHook::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build(id("companion_fishing_hook")));

    private static String id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path).toString();
    }

    private static <T extends TamableAnimal> DeferredHolder<EntityType<?>, EntityType<T>> magic(String id, EntityType.EntityFactory<T> factory) {
        return MagicCastingCompat.available() ? ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, MobCategory.AMBIENT)
                .sized(0.6F, 1.8F).build(id(id))) : null;
    }
}
