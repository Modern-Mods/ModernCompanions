package com.majorbonghits.moderncompanions.compat.firearms;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Reflection keeps the optional TacZ integration from loading when the mod is absent. */
public final class FirearmSupport {
    public enum TacZShotResult { SUCCESS, NO_AMMO, WAIT, FAILED }

    /** Matches TacZ's index type strings without making TacZ a compile dependency. */
    public enum Specialty {
        UNASSIGNED("unassigned", "Firearm Specialist", 0),
        PISTOL("pistol", "Pistol Specialist", 30),
        SMG("smg", "SMG Specialist", 20),
        RIFLE("rifle", "Rifle Specialist", 25),
        SHOTGUN("shotgun", "Shotgun Specialist", 15),
        SNIPER("sniper", "Sniper", 4),
        MACHINE_GUN("machine_gun", "MG Specialist", 5),
        HEAVY("heavy", "Heavy Specialist", 1);

        private final String id;
        private final String displayName;
        private final int weight;

        Specialty(String id, String displayName, int weight) {
            this.id = id;
            this.displayName = displayName;
            this.weight = weight;
        }

        public String id() {
            return id;
        }

        public int weight() {
            return weight;
        }

        public String displayName() {
            return displayName;
        }

        public static Specialty fromId(String id) {
            if (id == null) return UNASSIGNED;
            for (Specialty specialty : values()) {
                if (specialty.id.equals(id)) return specialty;
            }
            return UNASSIGNED;
        }

        public static Optional<Specialty> fromTacZType(String type) {
            if (type == null) return Optional.empty();
            return switch (type.trim().toLowerCase(java.util.Locale.ROOT)) {
                case "pistol" -> Optional.of(PISTOL);
                case "smg" -> Optional.of(SMG);
                case "rifle" -> Optional.of(RIFLE);
                case "shotgun" -> Optional.of(SHOTGUN);
                case "sniper" -> Optional.of(SNIPER);
                case "mg", "machine_gun", "machinegun", "machine gun" -> Optional.of(MACHINE_GUN);
                case "rpg", "heavy" -> Optional.of(HEAVY);
                default -> Optional.empty();
            };
        }
    }

    public record SpawnLoadout(ItemStack gun, ItemStack ammo) {
        public static SpawnLoadout empty() {
            return new SpawnLoadout(ItemStack.EMPTY, ItemStack.EMPTY);
        }
    }

    private FirearmSupport() {}

    public static boolean isFirearm(ItemStack stack) {
        return isTacZFirearm(stack);
    }

    public static boolean isTacZFirearm(ItemStack stack) {
        return isInstance(stack, "tacz", "com.tacz.guns.api.item.gun.AbstractGunItem");
    }

    public static boolean isAllowedFirearm(AbstractHumanCompanionEntity companion, ItemStack stack) {
        return isFirearm(stack) && companion.isFirearmAllowed(stack);
    }

    /** Reads the active TacZ resource-pack category, not the item or registry name. */
    public static Optional<Specialty> classify(ItemStack stack) {
        if (!isTacZFirearm(stack)) return Optional.empty();
        try {
            Class<?> gunApi = Class.forName("com.tacz.guns.api.item.IGun");
            Object gun = stack.getItem();
            ResourceLocation gunId = (ResourceLocation) gunApi.getMethod("getGunId", ItemStack.class)
                    .invoke(gun, stack);
            Class<?> timelessApi = Class.forName("com.tacz.guns.api.TimelessAPI");
            Optional<?> index = (Optional<?>) timelessApi.getMethod("getCommonGunIndex", ResourceLocation.class)
                    .invoke(null, gunId);
            if (index.isEmpty()) return Optional.empty();
            String type = (String) index.get().getClass().getMethod("getType").invoke(index.get());
            return Specialty.fromTacZType(type);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public static boolean equipFirearm(AbstractHumanCompanionEntity companion) {
        if (isAllowedFirearm(companion, companion.getMainHandItem())) return true;
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            ItemStack stack = companion.getInventory().getItem(slot);
            if (isAllowedFirearm(companion, stack)) {
                companion.setItemSlot(EquipmentSlot.MAINHAND, stack);
                return isAllowedFirearm(companion, companion.getMainHandItem());
            }
        }
        return false;
    }

    /** Builds a supplied TacZ gun and matching reserve ammo from the active gun pack. */
    public static SpawnLoadout createSpawnLoadout(Specialty specialty, RandomSource random,
                                                  HolderLookup.Provider registries) {
        if (!ModList.get().isLoaded("tacz") || specialty == null || specialty == Specialty.UNASSIGNED) {
            return SpawnLoadout.empty();
        }
        try {
            Class<?> timelessApi = Class.forName("com.tacz.guns.api.TimelessAPI");
            Iterable<?> entries = (Iterable<?>) timelessApi.getMethod("getAllCommonGunIndex").invoke(null);
            List<GunCandidate> candidates = new ArrayList<>();
            for (Object entryObject : entries) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
                Object index = entry.getValue();
                String type = (String) index.getClass().getMethod("getType").invoke(index);
                if (Specialty.fromTacZType(type).orElse(Specialty.UNASSIGNED) != specialty) continue;
                Object gunData = index.getClass().getMethod("getGunData").invoke(index);
                ResourceLocation ammoId = (ResourceLocation) gunData.getClass().getMethod("getAmmoId").invoke(gunData);
                int ammoAmount = (int) gunData.getClass().getMethod("getAmmoAmount").invoke(gunData);
                if (entry.getKey() instanceof ResourceLocation gunId && ammoId != null && ammoAmount > 0) {
                    candidates.add(new GunCandidate(gunId, ammoAmount, ammoId));
                }
            }
            if (candidates.isEmpty()) return SpawnLoadout.empty();
            GunCandidate candidate = candidates.get(random.nextInt(candidates.size()));

            Class<?> gunBuilderType = Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");
            Object gunBuilder = gunBuilderType.getMethod("create").invoke(null);
            gunBuilder = gunBuilderType.getMethod("setId", ResourceLocation.class).invoke(gunBuilder, candidate.gunId());
            gunBuilder = gunBuilderType.getMethod("setAmmoCount", int.class).invoke(gunBuilder, candidate.ammoAmount());
            ItemStack gun = (ItemStack) gunBuilderType.getMethod("build", HolderLookup.Provider.class)
                    .invoke(gunBuilder, registries);
            if (gun.isEmpty()) return SpawnLoadout.empty();

            Class<?> ammoBuilderType = Class.forName("com.tacz.guns.api.item.builder.AmmoItemBuilder");
            Object ammoBuilder = ammoBuilderType.getMethod("create").invoke(null);
            ammoBuilder = ammoBuilderType.getMethod("setId", ResourceLocation.class).invoke(ammoBuilder, candidate.ammoId());
            ammoBuilder = ammoBuilderType.getMethod("setCount", int.class)
                    .invoke(ammoBuilder, Math.min(64, candidate.ammoAmount() * 2));
            ItemStack ammo = (ItemStack) ammoBuilderType.getMethod("build").invoke(ammoBuilder);
            return new SpawnLoadout(gun, ammo);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return SpawnLoadout.empty();
        }
    }

    private record GunCandidate(ResourceLocation gunId, int ammoAmount, ResourceLocation ammoId) {}

    public static TacZShotResult shootTacZ(AbstractHumanCompanionEntity companion, LivingEntity target) {
        if (!isTacZFirearm(companion.getMainHandItem())) return TacZShotResult.FAILED;
        try {
            Class<?> operatorType = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            Object operator = operatorType.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, companion);
            Supplier<Float> pitch = companion::getXRot;
            Supplier<Float> yaw = companion::getYRot;
            Object result = operatorType.getMethod("shoot", Supplier.class, Supplier.class).invoke(operator, pitch, yaw);
            return switch (String.valueOf(result)) {
                case "SUCCESS" -> TacZShotResult.SUCCESS;
                case "NO_AMMO" -> TacZShotResult.NO_AMMO;
                default -> TacZShotResult.WAIT;
            };
        } catch (ReflectiveOperationException ignored) {
            return TacZShotResult.FAILED;
        }
    }

    /** TacZ only fires a gun registered with its living-entity operator. */
    public static boolean drawTacZ(AbstractHumanCompanionEntity companion) {
        if (!isTacZFirearm(companion.getMainHandItem())) return false;
        try {
            Class<?> operatorType = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            Object operator = operatorType.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, companion);
            operatorType.getMethod("draw", Supplier.class).invoke(operator, (Supplier<ItemStack>) companion::getMainHandItem);
            operatorType.getMethod("aim", boolean.class).invoke(operator, true);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static boolean reloadTacZ(AbstractHumanCompanionEntity companion) {
        if (!isInstance(companion.getMainHandItem(), "tacz", "com.tacz.guns.api.item.gun.AbstractGunItem")) return false;
        try {
            Class<?> operatorType = Class.forName("com.tacz.guns.api.entity.IGunOperator");
            Object operator = operatorType.getMethod("fromLivingEntity", LivingEntity.class).invoke(null, companion);
            operatorType.getMethod("reload").invoke(operator);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static boolean canReloadTacZ(AbstractHumanCompanionEntity companion) {
        if (!isTacZFirearm(companion.getMainHandItem())) return false;
        try {
            Class<?> gunType = Class.forName("com.tacz.guns.api.item.gun.AbstractGunItem");
            return (boolean) gunType.getMethod("canReload", LivingEntity.class, ItemStack.class)
                    .invoke(companion.getMainHandItem().getItem(), companion, companion.getMainHandItem());
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean isInstance(ItemStack stack, String modId, String className) {
        if (stack.isEmpty() || !ModList.get().isLoaded(modId)) return false;
        try {
            return Class.forName(className).isInstance(stack.getItem());
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
