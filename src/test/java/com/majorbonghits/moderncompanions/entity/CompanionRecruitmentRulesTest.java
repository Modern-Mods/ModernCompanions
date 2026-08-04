package com.majorbonghits.moderncompanions.entity;

import java.util.List;

/** No-world regression check for exact per-companion recruitment parsing. */
public final class CompanionRecruitmentRulesTest {
    public static void main(String[] args) {
        var rules = CompanionRecruitmentRules.parse(List.of(
                "modern_companions:archer|minecraft:bread|3",
                "modern_companions:archer|minecraft:iron_ingot|2",
                "modern_companions:archer|minecraft:bread|1",
                "broken entry",
                "modern_companions:archer|minecraft:diamond|0"));

        assert rules.get("modern_companions:archer").get("minecraft:bread") == 4;
        assert rules.get("modern_companions:archer").get("minecraft:iron_ingot") == 2;
        assert !rules.get("modern_companions:archer").containsKey("minecraft:diamond");
        assert CompanionRecruitmentRules.parseEntry("*|minecraft:apple|5").isPresent();
        assert CompanionRecruitmentRules.parseEntry("modern_companions:archer|minecraft:apple|-1").isEmpty();
    }
}
