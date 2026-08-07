package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.entity.personality.CompanionPersonality;
import com.majorbonghits.moderncompanions.entity.personality.SoulReforgingRules;

import java.util.List;
import java.util.Random;

/** Focused no-world check for catalyst pools and duplicate-trait protection. */
public final class SoulReforgingRulesTest {
    public static void main(String[] args) {
        List<String> preferred = List.of("trait_brave", "trait_reckless", "trait_sun_blessed");
        List<String> options = SoulReforgingRules.rollOptions(preferred,
                "trait_brave", "trait_guardian", new Random(11L));
        assert options.size() == 3;
        assert !options.contains("trait_brave");
        assert !options.contains("trait_guardian");
        assert options.stream().distinct().count() == 3;
        assert options.contains("trait_reckless") || options.contains("trait_sun_blessed");
        assert CompanionPersonality.TRAITS.containsAll(options);
    }
}
