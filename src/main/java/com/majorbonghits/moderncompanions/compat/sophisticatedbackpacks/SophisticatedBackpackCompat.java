package com.majorbonghits.moderncompanions.compat.sophisticatedbackpacks;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.function.Function;

/**
 * Bridges companion Curios slots to Sophisticated Backpacks' own item context.
 * Reflection keeps both integrations optional while still opening their native screen.
 */
public final class SophisticatedBackpackCompat {
    private static final String HANDLER = "modern_companions_companion_backpack";
    private static boolean handlerRegistered;

    private SophisticatedBackpackCompat() {}

    public static void registerHandler() {
        if (handlerRegistered || !ModList.get().isLoaded("sophisticatedbackpacks")) return;
        try {
            Class<?> providerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider");
            Class<?> countGetterClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryHandler$SlotCountGetter");
            Class<?> stackGetterClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryHandler$SlotStackGetter");
            Object provider = providerClass.getMethod("get").invoke(null);
            Object countGetter = Proxy.newProxyInstance(countGetterClass.getClassLoader(), new Class<?>[]{countGetterClass},
                    (proxy, method, args) -> method.getName().equals("getSlotCount") ? 1 : objectMethod(proxy, method, args));
            Object stackGetter = Proxy.newProxyInstance(stackGetterClass.getClassLoader(), new Class<?>[]{stackGetterClass},
                    (proxy, method, args) -> method.getName().equals("getStackInSlot")
                            ? getBackpack((Player) args[0], (String) args[1]) : objectMethod(proxy, method, args));
            Function<Player, java.util.Set<String>> identifiers = player -> Collections.emptySet();
            providerClass.getMethod("addPlayerInventoryHandler", String.class, Function.class, countGetterClass, stackGetterClass,
                            boolean.class, boolean.class, boolean.class, boolean.class)
                    .invoke(provider, HANDLER, identifiers, countGetter, stackGetter, false, false, false, false);
            handlerRegistered = true;
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.warn("Could not register Sophisticated Backpacks companion handler", exception);
        }
    }

    public static boolean open(ServerPlayer player, AbstractHumanCompanionEntity companion) {
        if (!handlerRegistered || getBackpack(player, Integer.toString(companion.getId())).isEmpty()) return false;
        try {
            Class<?> contextClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext");
            Object context = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext$Item")
                    .getConstructor(String.class, String.class, int.class).newInstance(HANDLER, Integer.toString(companion.getId()), 0);
            Constructor<?> container = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer")
                    .getConstructor(int.class, Player.class, contextClass);
            Method writeContext = contextClass.getMethod("toBuffer", FriendlyByteBuf.class);
            player.openMenu(new SimpleMenuProvider((id, inventory, menuPlayer) -> newContainer(container, id, menuPlayer, context),
                    Component.literal("Backpack - " + companion.getName().getString())), buffer -> writeContext(writeContext, context, buffer));
            return true;
        } catch (ReflectiveOperationException exception) {
            Constants.LOG.warn("Could not open Sophisticated Backpack for companion {}", companion.getId(), exception);
            return false;
        }
    }

    /** Reports whether this companion has an equipped Sophisticated Backpack. */
    public static boolean hasBackpack(AbstractHumanCompanionEntity companion) {
        return handlerRegistered && !getBackpack(companion).isEmpty();
    }

    private static AbstractContainerMenu newContainer(Constructor<?> constructor, int id, Player player, Object context) {
        try {
            return (AbstractContainerMenu) constructor.newInstance(id, player, context);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create Sophisticated Backpack menu", exception);
        }
    }

    private static void writeContext(Method method, Object context, FriendlyByteBuf buffer) {
        try {
            method.invoke(context, buffer);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not encode Sophisticated Backpack menu", exception);
        }
    }

    private static ItemStack getBackpack(Player player, String identifier) {
        try {
            Entity entity = player.level().getEntity(Integer.parseInt(identifier));
            if (!(entity instanceof AbstractHumanCompanionEntity companion)) return ItemStack.EMPTY;
            return getBackpack(companion);
        } catch (NumberFormatException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack getBackpack(AbstractHumanCompanionEntity companion) {
        return CuriosApi.getCuriosInventory(companion).flatMap(handler -> handler.getStacksHandler("back"))
                .map(handler -> handler.getStacks()).map(stacks -> {
                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        ItemStack stack = stacks.getStackInSlot(slot);
                        if (isBackpack(stack)) return stack;
                    }
                    return ItemStack.EMPTY;
                }).orElse(ItemStack.EMPTY);
    }

    private static boolean isBackpack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            return Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem").isInstance(stack.getItem());
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> HANDLER;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> null;
        };
    }
}
