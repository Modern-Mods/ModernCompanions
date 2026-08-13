package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.core.ModEnchantments;
import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.core.ModConfig;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Dedicated creative tab for Modern Companions spawn eggs + weapons.
 */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("modern_companions",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modern_companions"))
                    .icon(() -> new ItemStack(com.majorbonghits.moderncompanions.core.ModItems.KNIGHT_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        // Spawn eggs first
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.KNIGHT_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ARCHER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ARBALIST_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.AXEGUARD_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.VANGUARD_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.BERSERKER_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.BEASTMASTER_SPAWN_EGG.get());
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.CLERIC_SPAWN_EGG);
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ALCHEMIST_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.SCOUT_SPAWN_EGG.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.STORMCALLER_SPAWN_EGG.get());
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.FIREARM_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.PISTOL_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.SMG_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.RIFLE_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.SHOTGUN_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.SNIPER_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.MACHINE_GUN_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.HEAVY_SPECIALIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.FIRE_MAGE_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.LIGHTNING_MAGE_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.NECROMANCER_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.WIZARD_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.SORCERER_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.WARLOCK_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.WITCH_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.HAG_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.CRYOMANCER_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.DRUID_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.ILLUSIONIST_SPAWN_EGG);
                        magic(output, com.majorbonghits.moderncompanions.core.ModItems.BATTLEMAGE_SPAWN_EGG);
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.RESURRECTION_SCROLL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.COMPANION_MOVER.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ASSIGNMENT_WAND.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.STORED_COMPANION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.SUMMONING_WAND.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.PLACEMENT_WAND.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.ANIMAL_WAND.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.COMPANION_TABLE.get());
                        if (ModConfig.safeGet(ModConfig.CURRENCIES_ENABLED)) {
                            com.majorbonghits.moderncompanions.core.ModItems.getCurrencies()
                                    .forEach(currency -> output.accept(currency.get()));
                        }
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.EMPTY_ROUND_VESSEL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.EMPTY_RECTANGLE_VESSEL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.EMPTY_PYRAMID_VESSEL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.EMPTY_HEXAGON_VESSEL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.EMPTY_DROPLET_VESSEL.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.HEALTH_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.REGENERATION_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.STAMINA_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.MANA_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.REJUVENATION_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.SHIELD_POTION.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.HEALTH_PACK.get());
                        output.accept(com.majorbonghits.moderncompanions.core.ModItems.RECIPE.get());

                        // Attribute enchantment books (I–III)
                        addEnchantBooks(params, output, ModEnchantments.EMPOWER);
                        addEnchantBooks(params, output, ModEnchantments.NIMBILITY);
                        addEnchantBooks(params, output, ModEnchantments.ENLIGHTENMENT);
                        addEnchantBooks(params, output, ModEnchantments.VITALITY);

                        // Weapon variants grouped by type
                        for (WeaponType type : WeaponType.values()) {
                            com.majorbonghits.moderncompanions.registry.ModItems.getItemsByType(type).forEach(output::accept);
                        }

                        // Legendary items are loot-only, but remain available for creative testing and decoration.
                        com.majorbonghits.moderncompanions.registry.ModItems.getLegendaryItems().forEach(output::accept);
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    private static void magic(CreativeModeTab.Output output, net.neoforged.neoforge.registries.DeferredHolder<Item, Item> item) {
        if (item != null) output.accept(item.get());
    }

    private static void addEnchantBooks(CreativeModeTab.ItemDisplayParameters params, CreativeModeTab.Output output,
                                        ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantKey) {
        var lookupOpt = params.holders().lookup(Registries.ENCHANTMENT);
        lookupOpt.ifPresent(lookup -> lookup.get(enchantKey).ifPresent(holder -> addBookLevels(output, holder)));
    }

    private static void addBookLevels(CreativeModeTab.Output output, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> holder) {
        for (int level = 1; level <= 3; level++) {
            ItemStack stack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level));
            applyCustomModelData(holder.unwrapKey().orElse(null), level, stack);
            output.accept(stack);
        }
    }

    private static void applyCustomModelData(ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key, int level, ItemStack stack) {
        if (key == null) return;
        int base;
        if (key.equals(ModEnchantments.EMPOWER)) {
            base = 410000;
        } else if (key.equals(ModEnchantments.NIMBILITY)) {
            base = 410010;
        } else if (key.equals(ModEnchantments.ENLIGHTENMENT)) {
            base = 410020;
        } else if (key.equals(ModEnchantments.VITALITY)) {
            base = 410030;
        } else {
            return;
        }
        int cmd = base + level;
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(cmd));
    }
}
