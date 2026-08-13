package com.majorbonghits.moderncompanions.entity.job;

import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Per-server ephemeral target claims. Expiry prevents abandoned plans blocking workers forever. */
public final class JobReservations {
    private static final Map<ServerLevel, Map<ReservationKey, Claim>> CLAIMS = new WeakHashMap<>();

    private JobReservations() {}

    public static synchronized boolean claim(ServerLevel level, String key, UUID owner, long now, long ttl) {
        return claim(level, typeFor(key), key, owner, key, now, ttl);
    }

    public static synchronized boolean claim(ServerLevel level, ReservationType type, String key, UUID owner,
                                             String purpose, long now, long ttl) {
        cleanup(level, now);
        Map<ReservationKey, Claim> claims = CLAIMS.computeIfAbsent(level, ignored -> new HashMap<>());
        ReservationKey reservationKey = new ReservationKey(type, key);
        Claim current = claims.get(reservationKey);
        if (current != null && !current.owner().equals(owner)) return false;
        claims.put(reservationKey, new Claim(owner, type, purpose == null ? "" : purpose,
                now + Math.max(1L, ttl)));
        return true;
    }

    public static synchronized void release(ServerLevel level, UUID owner) {
        Map<ReservationKey, Claim> claims = CLAIMS.get(level);
        if (claims != null) claims.entrySet().removeIf(entry -> entry.getValue().owner().equals(owner));
    }

    public static synchronized void release(ServerLevel level, ReservationType type, String key, UUID owner) {
        Map<ReservationKey, Claim> claims = CLAIMS.get(level);
        if (claims == null) return;
        ReservationKey reservationKey = new ReservationKey(type, key);
        Claim claim = claims.get(reservationKey);
        if (claim != null && claim.owner().equals(owner)) claims.remove(reservationKey);
    }

    public static synchronized void cleanup(ServerLevel level, long now) {
        Map<ReservationKey, Claim> claims = CLAIMS.get(level);
        if (claims == null) return;
        claims.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        if (claims.isEmpty()) CLAIMS.remove(level);
    }

    public static synchronized void clear(ServerLevel level) {
        CLAIMS.remove(level);
    }

    public static synchronized int size(ServerLevel level) {
        Map<ReservationKey, Claim> claims = CLAIMS.get(level);
        return claims == null ? 0 : claims.size();
    }

    private static ReservationType typeFor(String key) {
        if (key == null) return ReservationType.BLOCK;
        int separator = key.indexOf(':');
        if (separator <= 0) return ReservationType.BLOCK;
        return switch (key.substring(0, separator)) {
            case "tree" -> ReservationType.COMPONENT;
            case "animal" -> ReservationType.ENTITY;
            case "drop" -> ReservationType.DROP;
            case "workstation" -> ReservationType.WORKSTATION;
            case "shore" -> ReservationType.SHORE;
            case "chest" -> ReservationType.CHEST;
            case "route" -> ReservationType.ROUTE;
            default -> ReservationType.BLOCK;
        };
    }

    private record ReservationKey(ReservationType type, String key) {}
    public record Claim(UUID owner, ReservationType type, String purpose, long expiresAt) {}
}
