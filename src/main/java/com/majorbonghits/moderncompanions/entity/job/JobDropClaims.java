package com.majorbonghits.moderncompanions.entity.job;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.UUID;

/** Persistent, short-lived ownership marker for drops produced by a Hunter. */
public final class JobDropClaims {
    public static final String OWNER = "ModernCompanionsJobDropOwner";
    public static final String EXPIRES = "ModernCompanionsJobDropExpires";
    private static final long CLAIM_TICKS = 20L * 60L * 10L;

    private JobDropClaims() {}

    public static void claim(ItemEntity item, AbstractHumanCompanionEntity owner) {
        item.getPersistentData().putUUID(OWNER, owner.getUUID());
        item.getPersistentData().putLong(EXPIRES, item.level().getGameTime() + CLAIM_TICKS);
    }

    public static boolean isClaimed(ItemEntity item) {
        return item.getPersistentData().hasUUID(OWNER)
                && item.getPersistentData().getLong(EXPIRES) > item.level().getGameTime();
    }

    public static boolean isOwnedBy(ItemEntity item, UUID owner) {
        return isClaimed(item) && item.getPersistentData().getUUID(OWNER).equals(owner);
    }

    public static boolean isOwnedByOther(ItemEntity item, UUID owner) {
        return isClaimed(item) && !item.getPersistentData().getUUID(OWNER).equals(owner);
    }
}
