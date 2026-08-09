package com.majorbonghits.moderncompanions.currency;

import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.core.ModDataComponents;
import com.majorbonghits.moderncompanions.core.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Single source of truth for physical denominations and wallet operations. */
public final class CurrencyService {
    private static final List<Denomination> PHYSICAL_DENOMINATIONS = List.of(
            new Denomination(ModItems.TIN, "tin"),
            new Denomination(ModItems.COPPER, "copper"),
            new Denomination(ModItems.SILVER, "silver"),
            new Denomination(ModItems.GOLD, "gold"),
            new Denomination(ModItems.DOLLAR, "dollar"),
            new Denomination(ModItems.STACK, "stack"),
            new Denomination(ModItems.GOLD_STACK, "gold_stack"));

    private CurrencyService() {
    }

    public static boolean enabled() {
        return ModConfig.safeGet(ModConfig.CURRENCIES_ENABLED);
    }

    public static boolean isCreditCard(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.CREDIT_CARD.get());
    }

    public static boolean isPhysicalCurrency(ItemStack stack) {
        return denomination(stack) != null;
    }

    public static long unitValue(ItemStack stack) {
        Denomination denomination = denomination(stack);
        return denomination == null ? 0L : configuredValue(denomination.valueKey());
    }

    public static long configuredValue(String valueKey) {
        return Math.max(0L, ModConfig.currencyValue(valueKey));
    }

    public static long stackValue(ItemStack stack) {
        return stackValue(stack, stack.getCount());
    }

    /** Returns -1 only when the requested multiplication would overflow a long. */
    public static long stackValue(ItemStack stack, int count) {
        long unitValue = unitValue(stack);
        if (unitValue == 0L || count <= 0) return 0L;
        return unitValue > Long.MAX_VALUE / count ? -1L : unitValue * count;
    }

    public static CardData ensureCard(ItemStack card) {
        if (!isCreditCard(card)) throw new IllegalArgumentException("Not a Credit Card");
        CardData data = card.get(ModDataComponents.CREDIT_CARD.get());
        if (data == null) {
            data = new CardData(UUID.randomUUID(), 0L);
            card.set(ModDataComponents.CREDIT_CARD.get(), data);
        }
        return data;
    }

    public static long cardBalance(ItemStack card) {
        if (!isCreditCard(card)) return 0L;
        return ensureCard(card).balance();
    }

    public static UUID cardId(ItemStack card) {
        return ensureCard(card).cardId();
    }

    public static void setCardBalance(ItemStack card, long balance) {
        CardData data = ensureCard(card);
        card.set(ModDataComponents.CREDIT_CARD.get(), new CardData(data.cardId(), Math.max(0L, balance)));
    }

    public static boolean canAddBalance(ItemStack card, long amount) {
        if (!isCreditCard(card) || card.getCount() != 1 || amount < 0L) return false;
        return safeAdd(cardBalance(card), amount) >= 0L;
    }

    public static boolean addBalance(ItemStack card, long amount) {
        if (!canAddBalance(card, amount)) return false;
        setCardBalance(card, safeAdd(cardBalance(card), amount));
        return true;
    }

    public static boolean deposit(ItemStack card, ItemStack physicalCurrency, int count) {
        if (!enabled() || !isCreditCard(card) || card.getCount() != 1
                || !isPhysicalCurrency(physicalCurrency) || count <= 0 || count > physicalCurrency.getCount()) return false;
        long amount = stackValue(physicalCurrency, count);
        if (amount <= 0L || !addBalance(card, amount)) return false;
        physicalCurrency.shrink(count);
        return true;
    }

    /** Transfers only value; the caller owns consuming the source card after success. */
    public static boolean transferBalance(ItemStack target, ItemStack source) {
        if (!isCreditCard(target) || !isCreditCard(source) || target.getCount() != 1 || source.getCount() != 1
                || cardId(target).equals(cardId(source))) return false;
        return addBalance(target, cardBalance(source));
    }

    public static ItemStack createLootCard(RandomSource random) {
        ItemStack card = new ItemStack(ModItems.CREDIT_CARD.get());
        ensureCard(card);
        setCardBalance(card, 5L + random.nextInt(7_496));
        return card;
    }

    /** Selects the richest player-owned card for automatic deposits. */
    @Nullable
    public static ItemStack highestBalanceCard(Player player) {
        List<ItemStack> cards = playerCards(player);
        int index = CurrencyRules.highestBalanceIndex(cardBalances(cards));
        return index < 0 ? null : cards.get(index);
    }

    /** Selects the smallest player-owned card that can cover the complete cost. */
    @Nullable
    public static ItemStack lowestSufficientCard(Player player, long cost) {
        if (cost <= 0L) return null;
        List<ItemStack> cards = playerCards(player);
        int index = CurrencyRules.lowestSufficientIndex(cardBalances(cards), cost);
        return index < 0 ? null : cards.get(index);
    }

    /** Server-side payment API for future shops and dialogue objectives. */
    public static boolean pay(Player player, long cost) {
        if (!enabled() || player.level().isClientSide || cost <= 0L) return false;
        ItemStack card = lowestSufficientCard(player, cost);
        if (card == null) return false;
        setCardBalance(card, cardBalance(card) - cost);
        return true;
    }

    /** Handles only physical-currency quick moves; every other item keeps vanilla behavior. */
    public static boolean interceptQuickMove(AbstractContainerMenu menu, int slotId, Player player) {
        if (!enabled() || player.getAbilities().instabuild || slotId < 0 || slotId >= menu.slots.size()) return false;
        Slot slot = menu.getSlot(slotId);
        ItemStack source = slot.getItem();
        if (!isPhysicalCurrency(source)) return false;
        ItemStack target = highestBalanceCard(player);
        if (target == null) return false;
        if (player.level().isClientSide) return true;
        if (!slot.mayPickup(player)) return false;

        int count = source.getCount();
        long amount = stackValue(source, count);
        if (amount <= 0L || !canAddBalance(target, amount)) return false;

        ItemStack removed = slot.safeTake(count, count, player);
        if (removed.isEmpty() || removed.getCount() != count) return false;
        setCardBalance(target, cardBalance(target) + amount);
        player.getInventory().setChanged();
        menu.broadcastChanges();
        return true;
    }

    /** Returns exact adjacent-denomination input/output counts, or null when values are invalid. */
    public static Conversion conversion(Item from, Item to) {
        int fromIndex = denominationIndex(from);
        int toIndex = denominationIndex(to);
        if (fromIndex < 0 || toIndex != fromIndex + 1) return null;

        long fromValue = configuredValue(PHYSICAL_DENOMINATIONS.get(fromIndex).valueKey());
        long toValue = configuredValue(PHYSICAL_DENOMINATIONS.get(toIndex).valueKey());
        CurrencyRules.Conversion conversion = CurrencyRules.conversion(fromValue, toValue);
        return conversion == null ? null : new Conversion(conversion.fromCount(), conversion.toCount());
    }

    /** Returns the one-output adjacent ratio for callers that only support one crafted item. */
    public static int conversionRatio(Item from, Item to) {
        Conversion conversion = conversion(from, to);
        return conversion == null || conversion.toCount() != 1 ? 0 : conversion.fromCount();
    }

    public record Conversion(int fromCount, int toCount) {
    }

    static long safeAdd(long first, long second) {
        return CurrencyRules.safeAdd(first, second);
    }

    public static Item randomPhysicalCurrency(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 35) return ModItems.TIN.get();
        if (roll < 60) return ModItems.COPPER.get();
        if (roll < 78) return ModItems.SILVER.get();
        if (roll < 90) return ModItems.GOLD.get();
        if (roll < 96) return ModItems.DOLLAR.get();
        if (roll < 99) return ModItems.STACK.get();
        return ModItems.GOLD_STACK.get();
    }

    private static @Nullable Denomination denomination(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (Denomination denomination : PHYSICAL_DENOMINATIONS) {
            if (stack.is(denomination.item().get())) return denomination;
        }
        return null;
    }

    private static int denominationIndex(Item item) {
        for (int index = 0; index < PHYSICAL_DENOMINATIONS.size(); index++) {
            if (PHYSICAL_DENOMINATIONS.get(index).item().get() == item) return index;
        }
        return -1;
    }

    private static List<ItemStack> playerCards(Player player) {
        List<ItemStack> cards = new ArrayList<>();
        for (ItemStack candidate : player.getInventory().items) {
            if (isCreditCard(candidate) && candidate.getCount() == 1) cards.add(candidate);
        }
        for (ItemStack candidate : player.getInventory().offhand) {
            if (isCreditCard(candidate) && candidate.getCount() == 1) cards.add(candidate);
        }
        return cards;
    }

    private static List<Long> cardBalances(List<ItemStack> cards) {
        List<Long> balances = new ArrayList<>(cards.size());
        for (ItemStack card : cards) balances.add(cardBalance(card));
        return balances;
    }

    private record Denomination(DeferredHolder<Item, Item> item, String valueKey) {
    }
}
