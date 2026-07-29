package com.majorbonghits.moderncompanions.compat.firearms;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/** Reflection keeps the optional TacZ integration from loading when the mod is absent. */
public final class FirearmSupport {
    public enum TacZShotResult { SUCCESS, NO_AMMO, WAIT, FAILED }

    private FirearmSupport() {}

    public static boolean isFirearm(ItemStack stack) {
        return isTacZFirearm(stack);
    }

    public static boolean isTacZFirearm(ItemStack stack) {
        return isInstance(stack, "tacz", "com.tacz.guns.api.item.gun.AbstractGunItem");
    }

    public static boolean equipFirearm(AbstractHumanCompanionEntity companion) {
        if (isFirearm(companion.getMainHandItem())) return true;
        for (int slot = 0; slot < companion.getInventory().getContainerSize(); slot++) {
            ItemStack stack = companion.getInventory().getItem(slot);
            if (isFirearm(stack)) {
                companion.setItemSlot(EquipmentSlot.MAINHAND, stack);
                return true;
            }
        }
        return false;
    }

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
