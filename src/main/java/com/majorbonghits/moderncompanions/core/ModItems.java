package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.compat.magic.MagicCastingCompat;
import com.majorbonghits.moderncompanions.item.AssignmentWandItem;
import com.majorbonghits.moderncompanions.item.CompanionMoverItem;
import com.majorbonghits.moderncompanions.item.ResurrectionScrollItem;
import com.majorbonghits.moderncompanions.item.SummoningWandItem;
import com.majorbonghits.moderncompanions.item.StoredCompanionItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred registration of mod items (currently companion spawn eggs).
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ModernCompanions.MOD_ID);

    public static final DeferredHolder<Item, Item> RESURRECTION_SCROLL = ITEMS.register("resurrection_scroll",
            () -> new ResurrectionScrollItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> STORED_COMPANION = ITEMS.register("stored_companion",
            () -> new StoredCompanionItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, Item> COMPANION_MOVER = ITEMS.register("companion_mover",
            () -> new CompanionMoverItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> ASSIGNMENT_WAND = ITEMS.register("assignment_wand",
            () -> new AssignmentWandItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredHolder<Item, Item> SUMMONING_WAND = ITEMS.register("summoning_wand",
            () -> new SummoningWandItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> ARBALIST_SPAWN_EGG = ITEMS.register("arbalist_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ARBALIST, 0xE8AF5A, 0xFF0000, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ARCHER_SPAWN_EGG = ITEMS.register("archer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ARCHER, 0xE8AF5A, 0x0000FF, new Item.Properties()));

    public static final DeferredHolder<Item, Item> AXEGUARD_SPAWN_EGG = ITEMS.register("axeguard_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.AXEGUARD, 0xE8AF5A, 0x00FF00, new Item.Properties()));

    public static final DeferredHolder<Item, Item> KNIGHT_SPAWN_EGG = ITEMS.register("knight_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.KNIGHT, 0xE8AF5A, 0xFFFF00, new Item.Properties()));

    public static final DeferredHolder<Item, Item> VANGUARD_SPAWN_EGG = ITEMS.register("vanguard_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.VANGUARD, 0x6E7F8C, 0x2E4B66, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BERSERKER_SPAWN_EGG = ITEMS.register("berserker_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.BERSERKER, 0xA1372F, 0xF28705, new Item.Properties()));

    public static final DeferredHolder<Item, Item> BEASTMASTER_SPAWN_EGG = ITEMS.register("beastmaster_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.BEASTMASTER, 0x5A7A3C, 0xF2D479, new Item.Properties()));

    public static final DeferredHolder<Item, Item> CLERIC_SPAWN_EGG = magicEgg("cleric_spawn_egg", ModEntityTypes.CLERIC, 0xE8E0B0, 0xFFD700);

    public static final DeferredHolder<Item, Item> ALCHEMIST_SPAWN_EGG = ITEMS.register("alchemist_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.ALCHEMIST, 0x9C7AC2, 0x55FFAA, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SCOUT_SPAWN_EGG = ITEMS.register("scout_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.SCOUT, 0x7BAFD4, 0x1B5D85, new Item.Properties()));

    public static final DeferredHolder<Item, Item> STORMCALLER_SPAWN_EGG = ITEMS.register("stormcaller_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntityTypes.STORMCALLER, 0xB0E0FF, 0xFFD166, new Item.Properties()));

    public static final DeferredHolder<Item, Item> FIRE_MAGE_SPAWN_EGG = magicEgg("fire_mage_spawn_egg", ModEntityTypes.FIRE_MAGE, 0xFF6B3D, 0xA8320F);
    public static final DeferredHolder<Item, Item> LIGHTNING_MAGE_SPAWN_EGG = magicEgg("lightning_mage_spawn_egg", ModEntityTypes.LIGHTNING_MAGE, 0x9BD7FF, 0x3659A6);
    public static final DeferredHolder<Item, Item> NECROMANCER_SPAWN_EGG = magicEgg("necromancer_spawn_egg", ModEntityTypes.NECROMANCER, 0x5A5A5A, 0x2B1B3D);
    public static final DeferredHolder<Item, Item> WIZARD_SPAWN_EGG = magicEgg("wizard_spawn_egg", ModEntityTypes.WIZARD, 0x6E5ACD, 0xB8A9FF);
    public static final DeferredHolder<Item, Item> SORCERER_SPAWN_EGG = magicEgg("sorcerer_spawn_egg", ModEntityTypes.SORCERER, 0xD85D36, 0xF4C95D);
    public static final DeferredHolder<Item, Item> WARLOCK_SPAWN_EGG = magicEgg("warlock_spawn_egg", ModEntityTypes.WARLOCK, 0x261447, 0xA35CFF);
    public static final DeferredHolder<Item, Item> WITCH_SPAWN_EGG = magicEgg("witch_spawn_egg", ModEntityTypes.WITCH, 0x4E7A34, 0xBADE65);
    public static final DeferredHolder<Item, Item> HAG_SPAWN_EGG = magicEgg("hag_spawn_egg", ModEntityTypes.HAG, 0x56304A, 0xA26A8D);
    public static final DeferredHolder<Item, Item> CRYOMANCER_SPAWN_EGG = magicEgg("cryomancer_spawn_egg", ModEntityTypes.CRYOMANCER, 0x96E7FF, 0x377DFF);
    public static final DeferredHolder<Item, Item> DRUID_SPAWN_EGG = magicEgg("druid_spawn_egg", ModEntityTypes.DRUID, 0x567D46, 0xC4D66B);
    public static final DeferredHolder<Item, Item> ILLUSIONIST_SPAWN_EGG = magicEgg("illusionist_spawn_egg", ModEntityTypes.ILLUSIONIST, 0x6D4C8D, 0xE6B8FF);
    public static final DeferredHolder<Item, Item> BATTLEMAGE_SPAWN_EGG = magicEgg("battlemage_spawn_egg", ModEntityTypes.BATTLEMAGE, 0x4A5B6A, 0xD7B56D);

    private static DeferredHolder<Item, Item> magicEgg(String id, DeferredHolder<EntityType<?>, ? extends EntityType<? extends Mob>> type, int primary, int secondary) {
        return MagicCastingCompat.available() ? ITEMS.register(id,
                () -> new DeferredSpawnEggItem(type, primary, secondary, new Item.Properties())) : null;
    }

}
