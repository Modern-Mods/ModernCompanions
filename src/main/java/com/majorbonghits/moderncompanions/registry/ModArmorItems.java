package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Owns the imported Medieval Armory equipment instead of replacing any
 * vanilla item.  The supplied armor atlases remain ArmorMaterial layers, while
 * the client registers the original 128x128 humanoid geometry for these items.
 */
public final class ModArmorItems {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Constants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    private static final Map<EquipmentSlot, List<DeferredItem<ArmorItem>>> BY_SLOT =
            new EnumMap<>(EquipmentSlot.class);
    private static final List<DeferredItem<ArmorItem>> ALL_ARMOR = new ArrayList<>();
    private static final List<DeferredItem<ArmorItem>> MEDIEVAL_ARMOR = new ArrayList<>();
    private static final ArmorItem.Type[] WEARABLE_TYPES = {
            ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE,
            ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS
    };

    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> GAMBISON = material(
            "gambison", "gambison", 15, SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(ItemTags.WOOL), 0, 2, 2, 0, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> IRON_MAIL = material(
            "iron_mail", "iron_mail", 9, SoundEvents.ARMOR_EQUIP_CHAIN,
            () -> Ingredient.of(Items.IRON_INGOT), 1, 4, 5, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> IRON_MAIL_CLOTH = material(
            "iron_mail_white_cloth", "iron_mail_white_cloth", 9, SoundEvents.ARMOR_EQUIP_CHAIN,
            () -> Ingredient.of(Items.IRON_INGOT), 1, 4, 5, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> COPPER_SCALE = material(
            "copper_scale", "copper_scale", 15, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.COPPER_INGOT), 1, 3, 4, 1, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIAMOND_WEAVE = material(
            "diamond_weave", "diamond_weave", 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND), 2, 5, 7, 3, 2.0F, 0.0F);

    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIGHT_IRON = material(
            "light_iron_armor", "light_iron_armor", 9, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT), 1, 4, 5, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> MEDIUM_IRON = material(
            "medium_iron_armor", "medium_iron_armor", 9, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT), 2, 5, 7, 3, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_IRON = material(
            "heavy_iron_armor", "heavy_iron_armor", 9, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT), 2, 6, 8, 3, 2.0F, 0.1F);

    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIGHT_DIAMOND = material(
            "light_diamond", "light_diamond", 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND), 2, 5, 7, 3, 2.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> MEDIUM_DIAMOND = material(
            "medium_diamond", "medium_diamond", 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND), 3, 6, 8, 4, 2.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_DIAMOND = material(
            "heavy_diamond", "heavy_diamond", 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND), 4, 7, 9, 4, 4.0F, 0.1F);

    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> LIGHT_NETHERITE = material(
            "light_netherite", "light_netherite", 9, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT), 3, 6, 8, 4, 3.0F, 0.1F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> MEDIUM_NETHERITE = material(
            "medium_netherite", "medium_netherite", 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT), 4, 7, 9, 5, 3.0F, 0.1F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEAVY_NETHERITE = material(
            "heavy_netherite", "heavy_netherite", 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT), 5, 8, 10, 5, 6.0F, 0.2F);

    // Vanilla-rework materials are independent holders and therefore do not
    // alter the behavior, names, or textures of minecraft:* armor.
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> RUGGED_LEATHER = material(
            "rugged_leather", "rugged_leather", 15, SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.LEATHER), 1, 2, 3, 1, 0.0F, 0.0F, true);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> WORN_CHAINMAIL = material(
            "worn_chainmail", "worn_chainmail", 12, SoundEvents.ARMOR_EQUIP_CHAIN,
            () -> Ingredient.of(Items.IRON_INGOT), 1, 4, 5, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCHAIC_IRON = material(
            "archaic_iron", "archaic_iron", 9, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT), 2, 5, 6, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCHAIC_GOLDEN = material(
            "archaic_golden", "archaic_golden", 25, SoundEvents.ARMOR_EQUIP_GOLD,
            () -> Ingredient.of(Items.GOLD_INGOT), 1, 3, 5, 2, 0.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCHAIC_DIAMOND = material(
            "archaic_diamond", "archaic_diamond", 10, SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(Items.DIAMOND), 3, 6, 8, 3, 2.0F, 0.0F);
    private static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCHAIC_NETHERITE = material(
            "archaic_netherite", "archaic_netherite", 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT), 3, 6, 8, 3, 3.0F, 0.1F);

    static {
        registerSet("gambison", GAMBISON, 5, false, WEARABLE_TYPES);
        registerSet("iron_mail", IRON_MAIL, 15, false, WEARABLE_TYPES);
        registerSet("iron_mail_white_cloth", IRON_MAIL_CLOTH, 15, false,
                new ArmorItem.Type[]{ArmorItem.Type.BOOTS, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS});
        registerSet("copper_scale", COPPER_SCALE, 12, false,
                new ArmorItem.Type[]{ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS});
        registerSet("diamond_weave", DIAMOND_WEAVE, 33, false, WEARABLE_TYPES);
        registerSet("light_iron_armor", LIGHT_IRON, 20, false, WEARABLE_TYPES);
        registerSet("medium_iron_armor", MEDIUM_IRON, 23, false, WEARABLE_TYPES);
        registerSet("heavy_iron_armor", HEAVY_IRON, 26, false, WEARABLE_TYPES);
        registerSet("light_diamond", LIGHT_DIAMOND, 33, false, WEARABLE_TYPES);
        registerSet("medium_diamond", MEDIUM_DIAMOND, 36, false, WEARABLE_TYPES);
        registerSet("heavy_diamond", HEAVY_DIAMOND, 39, false, WEARABLE_TYPES);
        registerSet("light_netherite", LIGHT_NETHERITE, 37, true, WEARABLE_TYPES);
        registerSet("medium_netherite", MEDIUM_NETHERITE, 40, true, WEARABLE_TYPES);
        registerSet("heavy_netherite", HEAVY_NETHERITE, 43, true, WEARABLE_TYPES);

        // Keep the custom-model set separate from the independent vanilla-rework sets.
        MEDIEVAL_ARMOR.addAll(ALL_ARMOR);

        registerSet("rugged_leather", RUGGED_LEATHER, 5, false, WEARABLE_TYPES);
        registerSet("worn_chainmail", WORN_CHAINMAIL, 15, false, WEARABLE_TYPES);
        registerSet("archaic_iron", ARCHAIC_IRON, 15, false, WEARABLE_TYPES);
        registerSet("archaic_golden", ARCHAIC_GOLDEN, 7, false, WEARABLE_TYPES);
        registerSet("archaic_diamond", ARCHAIC_DIAMOND, 33, false, WEARABLE_TYPES);
        registerSet("archaic_netherite", ARCHAIC_NETHERITE, 37, true, WEARABLE_TYPES);
    }

    private ModArmorItems() {
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> material(
            String id, String texture, int enchantmentValue, Holder<SoundEvent> equipSound,
            Supplier<Ingredient> repairIngredient, int boots, int leggings, int chestplate, int helmet,
            float toughness, float knockbackResistance) {
        return material(id, texture, enchantmentValue, equipSound, repairIngredient, boots, leggings,
                chestplate, helmet, toughness, knockbackResistance, false);
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> material(
            String id, String texture, int enchantmentValue, Holder<SoundEvent> equipSound,
            Supplier<Ingredient> repairIngredient, int boots, int leggings, int chestplate, int helmet,
            float toughness, float knockbackResistance, boolean dyeable) {
        return ARMOR_MATERIALS.register(id, () -> {
            Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
            defense.put(ArmorItem.Type.BOOTS, boots);
            defense.put(ArmorItem.Type.LEGGINGS, leggings);
            defense.put(ArmorItem.Type.CHESTPLATE, chestplate);
            defense.put(ArmorItem.Type.HELMET, helmet);
            defense.put(ArmorItem.Type.BODY, chestplate);
            ResourceLocation asset = Constants.id(texture);
            List<ArmorMaterial.Layer> layers = dyeable
                    ? List.of(new ArmorMaterial.Layer(asset, "", true),
                    new ArmorMaterial.Layer(asset, "_overlay", false))
                    : List.of(new ArmorMaterial.Layer(asset));
            return new ArmorMaterial(defense, enchantmentValue, equipSound, repairIngredient, layers,
                    toughness, knockbackResistance);
        });
    }

    private static void registerSet(String setId, Holder<ArmorMaterial> material, int durabilityMultiplier,
                                     boolean fireResistant, ArmorItem.Type[] types) {
        Set<ArmorItem.Type> requested = EnumSet.of(types[0], types);
        for (ArmorItem.Type type : requested) {
            String id = setId + "_" + type.getName();
            Item.Properties properties = new Item.Properties().durability(type.getDurability(durabilityMultiplier));
            if (fireResistant) properties = properties.fireResistant();
            Item.Properties finalProperties = properties;
            DeferredItem<ArmorItem> item = ITEMS.register(id,
                    () -> new ArmorItem(material, type, finalProperties));
            ALL_ARMOR.add(item);
            BY_SLOT.computeIfAbsent(type.getSlot(), ignored -> new ArrayList<>()).add(item);
        }
    }

    /** Every imported armor item, including the partial cloth and copper sets. */
    public static List<Item> getAllArmorItems() {
        return ALL_ARMOR.stream().map(DeferredItem::get).map(item -> (Item) item).toList();
    }

    /** The 54 Medieval Armory pieces that use the supplied custom humanoid meshes. */
    public static List<Item> getMedievalArmorItems() {
        return MEDIEVAL_ARMOR.stream().map(DeferredItem::get).map(item -> (Item) item).toList();
    }

    /** Selects only imported armor so every registered piece can appear in spawn loadouts. */
    public static ItemStack randomSpawnArmor(EquipmentSlot slot, java.util.Random random) {
        List<DeferredItem<ArmorItem>> choices = BY_SLOT.getOrDefault(slot, List.of());
        if (choices.isEmpty()) return ItemStack.EMPTY;
        return choices.get(random.nextInt(choices.size())).get().getDefaultInstance();
    }

    public static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
        ITEMS.register(modBus);
    }
}
