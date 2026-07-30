package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Per-server ephemeral target claims. Expiry prevents abandoned plans blocking workers forever. */
public final class JobReservations {
    private static final Map<ServerLevel, Map<String, Claim>> CLAIMS = new WeakHashMap<>();

    private JobReservations() {}

    public static synchronized boolean claim(ServerLevel level, String key, UUID owner, long now, long ttl) {
        Map<String, Claim> claims = CLAIMS.computeIfAbsent(level, ignored -> new HashMap<>());
        claims.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
        Claim current = claims.get(key);
        if (current != null && !current.owner.equals(owner)) return false;
        claims.put(key, new Claim(owner, now + Math.max(1L, ttl)));
        return true;
    }

    public static synchronized void release(ServerLevel level, UUID owner) {
        Map<String, Claim> claims = CLAIMS.get(level);
        if (claims != null) claims.entrySet().removeIf(entry -> entry.getValue().owner.equals(owner));
    }

    private record Claim(UUID owner, long expiresAt) {}
}
