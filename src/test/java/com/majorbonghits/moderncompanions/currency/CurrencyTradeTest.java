package com.majorbonghits.moderncompanions.currency;

import java.util.List;
import java.util.UUID;

/** Small parser check kept runnable without a Minecraft world or JEI. */
public final class CurrencyTradeTest {
    private CurrencyTradeTest() {
    }

    public static void main(String[] args) {
        assert CurrencyTrade.parse("minecraft:emerald|1|-|0|minecraft:gold_ingot|2").isPresent();
        assert CurrencyTrade.parse("minecraft:emerald|1|minecraft:wheat|20|minecraft:gold_ingot|1").isPresent();
        assert CurrencyTrade.parse("minecraft:emerald|0|-|0|minecraft:gold_ingot|1").isEmpty();
        assert CurrencyTrade.parse("minecraft:emerald|1|-|garbage|minecraft:gold_ingot|1").isEmpty();
        assert CurrencyTrade.parse("minecraft:emerald|1|-|0|not an id|1").isEmpty();
        assert CurrencyTrade.parse("minecraft:emerald|1|-|0|minecraft:gold_ingot").isEmpty();

        assert CurrencyRules.safeAdd(12L, 30L) == 42L;
        assert CurrencyRules.safeAdd(Long.MAX_VALUE, 1L) == -1L;
        assert CurrencyRules.safeAdd(-1L, 1L) == -1L;

        assert CurrencyRules.highestBalanceIndex(List.of(200L, 4_600L, 850L)) == 1;
        assert CurrencyRules.highestBalanceIndex(List.of(10L, 10L)) == 0;
        assert CurrencyRules.lowestSufficientIndex(List.of(250L, 375L, 6_800L), 300L) == 1;
        assert CurrencyRules.lowestSufficientIndex(List.of(250L, 375L), 400L) == -1;
        assert CurrencyRules.lowestSufficientIndex(List.of(0L, 10L), 0L) == -1;

        CurrencyRules.Conversion tinToCopper = CurrencyRules.conversion(1L, 5L);
        assert tinToCopper.fromCount() == 5 && tinToCopper.toCount() == 1;
        assert 1L * tinToCopper.fromCount() == 5L * tinToCopper.toCount();
        CurrencyRules.Conversion silverToGold = CurrencyRules.conversion(10L, 25L);
        assert silverToGold.fromCount() == 5 && silverToGold.toCount() == 2;
        assert 10L * silverToGold.fromCount() == 25L * silverToGold.toCount();

        UUID id = UUID.randomUUID();
        CardData empty = new CardData(id, -75L);
        assert empty.cardId().equals(id);
        assert empty.balance() == 0L;
        assert new CardData(empty.cardId(), 725L).cardId().equals(id);
    }
}
