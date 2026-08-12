package com.majorbonghits.moderncompanions.client;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Keeps the material and legendary held-model IDs shared by both client mixins. */
public final class HeldWeaponModelIds {
    private static final List<String> MATERIAL_PREFIXES = List.of(
            "wooden", "stone", "iron", "golden", "diamond", "netherite", "bronze");
    private static final List<String> HELD_WEAPON_TYPES = List.of("spear", "quarterstaff", "glaive");
    private static final List<String> LEGENDARY_BASE_NAMES = List.of(
            "candle_sword",
            "crimson_hammer", "crimson_spear", "crimson_sycthe", "crimson_trident",
            "ice_axe", "ice_cleaver", "ice_hammer", "ice_lance", "ice_mace", "ice_scythe", "ice_spear", "ice_trident",
            "molten_hammer", "molten_scythe", "molten_spear",
            "spoon", "xmas_sword");

    private static final List<String> ALL_BASE_NAMES = Stream.concat(
            MATERIAL_PREFIXES.stream().flatMap(material -> HELD_WEAPON_TYPES.stream()
                    .map(type -> material + "_" + type)),
            LEGENDARY_BASE_NAMES.stream()).toList();
    private static final Set<String> ALL_BASE_NAME_SET = Set.copyOf(ALL_BASE_NAMES);

    private HeldWeaponModelIds() {
    }

    public static List<String> allBaseNames() {
        return ALL_BASE_NAMES;
    }

    public static boolean hasHeldModel(String itemId) {
        return ALL_BASE_NAME_SET.contains(itemId);
    }
}
