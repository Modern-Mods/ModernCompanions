package com.majorbonghits.moderncompanions.entity.ai;

/** No-world regression check for the Radius-relative teleport leash threshold. */
public final class FollowLeashRulesTest {
    public static void main(String[] args) {
        assert FollowLeashRules.teleportDistanceSquared(5) == 100.0D;
        assert FollowLeashRules.teleportDistanceSquared(2) == 49.0D;
        assert FollowLeashRules.teleportDistanceSquared(128) == 17689.0D;
    }
}
