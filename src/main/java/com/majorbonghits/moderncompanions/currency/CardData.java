package com.majorbonghits.moderncompanions.currency;

import java.util.Objects;
import java.util.UUID;

/** Persistent identity and balance carried by one Credit Card ItemStack. */
public record CardData(UUID cardId, long balance) {
    public CardData {
        Objects.requireNonNull(cardId, "cardId");
        balance = Math.max(0L, balance);
    }
}
