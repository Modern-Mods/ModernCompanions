package com.majorbonghits.moderncompanions.registry;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.item.BasicWeaponItem;
import com.majorbonghits.moderncompanions.item.ClubItem;
import com.majorbonghits.moderncompanions.item.DaggerItem;
import com.majorbonghits.moderncompanions.item.GlaiveItem;
import com.majorbonghits.moderncompanions.item.HammerItem;
import com.majorbonghits.moderncompanions.item.SpearItem;
import com.majorbonghits.moderncompanions.struct.WeaponType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Dynamically registers every material/weapon permutation the same way BasicWeapons does.
 */
public final class ModItems {
    private ModItems() {
    }

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    private static final Map<WeaponType, List<Supplier<Item>>> ITEMS_BY_TYPE = new EnumMap<>(WeaponType.class);

    private static final Tier LEGENDARY_TIER = Tiers.NETHERITE;

    public static final DeferredItem<Item> CANDLE_SWORD = legendary("candle_sword",
            () -> new SpearItem(LEGENDARY_TIER, 8.0F, -2.5F, 2.5D, legendaryProperties()));
    public static final DeferredItem<Item> FIREAXE = legendary("fireaxe",
            () -> new AxeItem(LEGENDARY_TIER, nativeToolProperties(7.0F, -2.9F)));
    public static final DeferredItem<Item> HAMMER_HEAD = legendary("hammer_head",
            () -> new HammerItem(LEGENDARY_TIER, 12.0F, -3.6F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> IRON_MACE = legendary("iron_mace",
            () -> new MaceItem(maceProperties(12.0F, -3.4F, 750)));
    public static final DeferredItem<Item> SAI = legendary("sai",
            () -> new DaggerItem(LEGENDARY_TIER, 5.0F, -1.2F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> SPOON = legendary("spoon",
            () -> new ClubItem(LEGENDARY_TIER, 11.0F, -3.0F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> SWORDFISH = legendary("swordfish",
            () -> new BasicWeaponItem(LEGENDARY_TIER, net.minecraft.tags.BlockTags.SWORD_EFFICIENT,
                    10.0F, -2.2F, 0.0D, legendaryProperties()) {});
    public static final DeferredItem<Item> XMAS_SWORD = legendary("xmas_sword",
            () -> new BasicWeaponItem(LEGENDARY_TIER, net.minecraft.tags.BlockTags.SWORD_EFFICIENT,
                    8.0F, -2.0F, 0.0D, legendaryProperties()) {});

    public static final DeferredItem<Item> CRIMSON_ARROW = legendaryArrow("crimson_arrow");
    public static final DeferredItem<Item> CRIMSON_AXE = legendary("crimson_axe",
            () -> new AxeItem(LEGENDARY_TIER, nativeToolProperties(8.0F, -3.0F)));
    public static final DeferredItem<Item> CRIMSON_HAMMER = legendary("crimson_hammer",
            () -> new HammerItem(LEGENDARY_TIER, 13.0F, -3.6F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> CRIMSON_MACE = legendary("crimson_mace",
            () -> new MaceItem(maceProperties(14.0F, -3.5F, 800)));
    public static final DeferredItem<Item> CRIMSON_SABER = legendary("crimson_saber",
            () -> new BasicWeaponItem(LEGENDARY_TIER, net.minecraft.tags.BlockTags.SWORD_EFFICIENT,
                    12.0F, -2.1F, 0.0D, legendaryProperties()) {});
    public static final DeferredItem<Item> CRIMSON_SPEAR = legendary("crimson_spear",
            () -> new SpearItem(LEGENDARY_TIER, 10.0F, -2.5F, 2.5D, legendaryProperties()));
    public static final DeferredItem<Item> CRIMSON_SYCTHE = legendary("crimson_sycthe",
            () -> new GlaiveItem(LEGENDARY_TIER, 12.0F, -3.0F, 1.75D, legendaryProperties()));
    public static final DeferredItem<Item> CRIMSON_TRIDENT = legendary("crimson_trident",
            () -> new TridentItem(tridentProperties(12.0F, -2.9F, 500)));

    public static final DeferredItem<Item> ICE_ARROW = legendaryArrow("ice_arrow");
    public static final DeferredItem<Item> ICE_AXE = legendary("ice_axe",
            () -> new AxeItem(LEGENDARY_TIER, nativeToolProperties(7.0F, -3.0F)));
    public static final DeferredItem<Item> ICE_CLEAVER = legendary("ice_cleaver",
            () -> new BasicWeaponItem(LEGENDARY_TIER, net.minecraft.tags.BlockTags.SWORD_EFFICIENT,
                    14.0F, -3.2F, 0.0D, legendaryProperties()) {});
    public static final DeferredItem<Item> ICE_HAMMER = legendary("ice_hammer",
            () -> new HammerItem(LEGENDARY_TIER, 12.0F, -3.4F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> ICE_LANCE = legendary("ice_lance",
            () -> new SpearItem(LEGENDARY_TIER, 10.0F, -2.4F, 3.0D, legendaryProperties()));
    public static final DeferredItem<Item> ICE_MACE = legendary("ice_mace",
            () -> new MaceItem(maceProperties(13.0F, -3.4F, 800)));
    public static final DeferredItem<Item> ICE_SCYTHE = legendary("ice_scythe",
            () -> new GlaiveItem(LEGENDARY_TIER, 11.0F, -2.9F, 2.0D, legendaryProperties()));
    public static final DeferredItem<Item> ICE_SPEAR = legendary("ice_spear",
            () -> new SpearItem(LEGENDARY_TIER, 9.0F, -2.4F, 2.75D, legendaryProperties()));
    public static final DeferredItem<Item> ICE_TRIDENT = legendary("ice_trident",
            () -> new TridentItem(tridentProperties(11.0F, -2.8F, 500)));

    public static final DeferredItem<Item> MOLTEN_ARROW = legendaryArrow("molten_arrow");
    public static final DeferredItem<Item> MOLTEN_AXE = legendary("molten_axe",
            () -> new AxeItem(LEGENDARY_TIER, nativeToolProperties(9.0F, -3.0F)));
    public static final DeferredItem<Item> MOLTEN_HAMMER = legendary("molten_hammer",
            () -> new HammerItem(LEGENDARY_TIER, 14.0F, -3.6F, 0.0D, legendaryProperties()));
    public static final DeferredItem<Item> MOLTEN_HOE = legendary("molten_hoe",
            () -> new HoeItem(LEGENDARY_TIER, nativeToolProperties(8.0F, -2.0F)));
    public static final DeferredItem<Item> MOLTEN_PICKAXE = legendary("molten_pickaxe",
            () -> new PickaxeItem(LEGENDARY_TIER, nativeToolProperties(8.0F, -2.8F)));
    public static final DeferredItem<Item> MOLTEN_SCYTHE = legendary("molten_scythe",
            () -> new GlaiveItem(LEGENDARY_TIER, 13.0F, -3.0F, 1.75D, legendaryProperties()));
    public static final DeferredItem<Item> MOLTEN_SHOVEL = legendary("molten_shovel",
            () -> new ShovelItem(LEGENDARY_TIER, nativeToolProperties(8.0F, -3.0F)));
    public static final DeferredItem<Item> MOLTEN_SPEAR = legendary("molten_spear",
            () -> new SpearItem(LEGENDARY_TIER, 11.0F, -2.5F, 2.5D, legendaryProperties()));
    public static final DeferredItem<Item> MOLTEN_SWORD = legendary("molten_sword",
            () -> new BasicWeaponItem(LEGENDARY_TIER, net.minecraft.tags.BlockTags.SWORD_EFFICIENT,
                    12.0F, -2.2F, 0.0D, legendaryProperties()) {});

    private static final List<DeferredItem<Item>> LEGENDARY_ITEMS = List.of(
            CANDLE_SWORD, FIREAXE, HAMMER_HEAD, IRON_MACE, SAI, SPOON, SWORDFISH, XMAS_SWORD,
            CRIMSON_ARROW, CRIMSON_AXE, CRIMSON_HAMMER, CRIMSON_MACE, CRIMSON_SABER, CRIMSON_SPEAR,
            CRIMSON_SYCTHE, CRIMSON_TRIDENT, ICE_ARROW, ICE_AXE, ICE_CLEAVER, ICE_HAMMER, ICE_LANCE,
            ICE_MACE, ICE_SCYTHE, ICE_SPEAR, ICE_TRIDENT, MOLTEN_ARROW, MOLTEN_AXE, MOLTEN_HAMMER,
            MOLTEN_HOE, MOLTEN_PICKAXE, MOLTEN_SCYTHE, MOLTEN_SHOVEL, MOLTEN_SPEAR, MOLTEN_SWORD);

    private static final List<DeferredItem<Item>> STRUCTURE_SWORDS = List.of(
            SWORDFISH, XMAS_SWORD, CRIMSON_SABER, ICE_CLEAVER, MOLTEN_SWORD);
    private static final List<DeferredItem<Item>> STRUCTURE_AXES = List.of(
            FIREAXE, CRIMSON_AXE, ICE_AXE, MOLTEN_AXE);
    private static final List<DeferredItem<Item>> STRUCTURE_TRIDENTS = List.of(CRIMSON_TRIDENT, ICE_TRIDENT);
    private static final List<DeferredItem<Item>> STRUCTURE_ARROWS = List.of(CRIMSON_ARROW, ICE_ARROW, MOLTEN_ARROW);
    private static final List<DeferredItem<Item>> STRUCTURE_MELEE = List.of(
            CANDLE_SWORD, FIREAXE, HAMMER_HEAD, IRON_MACE, SAI, SPOON, SWORDFISH, XMAS_SWORD,
            CRIMSON_AXE, CRIMSON_HAMMER, CRIMSON_MACE, CRIMSON_SABER, CRIMSON_SPEAR,
            CRIMSON_SYCTHE, CRIMSON_TRIDENT, ICE_AXE, ICE_CLEAVER, ICE_HAMMER, ICE_LANCE,
            ICE_MACE, ICE_SCYTHE, ICE_SPEAR, ICE_TRIDENT, MOLTEN_AXE, MOLTEN_HAMMER,
            MOLTEN_HOE, MOLTEN_PICKAXE, MOLTEN_SCYTHE, MOLTEN_SHOVEL, MOLTEN_SPEAR, MOLTEN_SWORD);

    private record MaterialEntry(Tier tier, String prefix, UnaryOperator<Item.Properties> settingsModifier) {
        MaterialEntry(Tier tier, String prefix) {
            this(tier, prefix, UnaryOperator.identity());
        }
    }

    private static final Tier BRONZE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 350;
        }

        @Override
        public float getSpeed() {
            return 7.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 2.5F;
        }

        @Override
        public int getEnchantmentValue() {
            return 13;
        }

        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return net.minecraft.world.item.crafting.Ingredient.EMPTY;
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL;
        }
    };

    private static final List<MaterialEntry> MATERIALS = new ArrayList<>(
        List.of(
            new MaterialEntry(Tiers.WOOD, "wooden"),
            new MaterialEntry(Tiers.STONE, "stone"),
            new MaterialEntry(Tiers.IRON, "iron"),
            new MaterialEntry(Tiers.GOLD, "golden"),
            new MaterialEntry(Tiers.DIAMOND, "diamond"),
            new MaterialEntry(Tiers.NETHERITE, "netherite", props -> props.fireResistant())
        )
    );

    static {
        // Optional bronze support — only registered when the bronze mod is present.
        if (ModList.get().isLoaded("bronze")) {
            MATERIALS.add(new MaterialEntry(BRONZE_TIER, "bronze"));
        }

        for (MaterialEntry material : MATERIALS) {
            registerAllWeaponsForMaterial(material);
        }
    }

    private static void registerAllWeaponsForMaterial(MaterialEntry material) {
        for (WeaponType type : WeaponType.values()) {
            String itemId = material.prefix() + "_" + type.getId();
            Item.Properties properties = material.settingsModifier().apply(new Item.Properties());

            float damageModifier = WeaponType.getDamageModifier(type, material.tier());
            float speedModifier = WeaponType.getSpeedModifier(type, material.tier());
            float reachModifier = WeaponType.getReachModifier(type, material.tier());

            DeferredItem<Item> registered = ITEMS.register(itemId, () -> type.create(material.tier(), damageModifier, speedModifier, reachModifier, properties));
            ITEMS_BY_TYPE.computeIfAbsent(type, k -> new ArrayList<>()).add(registered);
        }
    }

    private static DeferredItem<Item> legendary(String id, Supplier<? extends Item> item) {
        return ITEMS.register(id, item);
    }

    private static DeferredItem<Item> legendaryArrow(String id) {
        return legendary(id, () -> new ArrowItem(legendaryProperties().stacksTo(64)));
    }

    private static Item.Properties legendaryProperties() {
        return new Item.Properties().rarity(Rarity.EPIC).fireResistant();
    }

    private static Item.Properties maceProperties(float damage, float speed, int durability) {
        return combatProperties(damage, speed)
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .durability(durability);
    }

    private static Item.Properties tridentProperties(float damage, float speed, int durability) {
        return combatProperties(damage, speed)
                .component(DataComponents.TOOL, TridentItem.createToolProperties())
                .durability(durability);
    }

    private static Item.Properties nativeToolProperties(float damage, float speed) {
        return combatProperties(damage + LEGENDARY_TIER.getAttackDamageBonus(), speed);
    }

    private static Item.Properties combatProperties(float damage, float speed) {
        return legendaryProperties().component(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage,
                                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed,
                                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build());
    }

    /** Rare structure loadouts stay compatible with the companion's existing combat role. */
    public static ItemStack structureLegendary(RandomSource random, ItemStack current) {
        if (current.getItem() instanceof BowItem || current.getItem() instanceof CrossbowItem) {
            return randomStack(random, STRUCTURE_ARROWS, 8);
        }
        if (current.getItem() instanceof AxeItem) return randomStack(random, STRUCTURE_AXES, 1);
        if (current.getItem() instanceof SwordItem) return randomStack(random, STRUCTURE_SWORDS, 1);
        if (current.getItem() instanceof TridentItem) return randomStack(random, STRUCTURE_TRIDENTS, 1);
        if (current.getItem() instanceof PickaxeItem) return MOLTEN_PICKAXE.get().getDefaultInstance();
        if (current.getItem() instanceof ShovelItem) return MOLTEN_SHOVEL.get().getDefaultInstance();
        if (current.getItem() instanceof HoeItem) return MOLTEN_HOE.get().getDefaultInstance();
        if (current.isEmpty() || current.getItem() instanceof DiggerItem || current.getItem() instanceof com.majorbonghits.moderncompanions.item.BasicWeaponSweeplessItem) {
            return randomStack(random, STRUCTURE_MELEE, 1);
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack randomStack(RandomSource random, List<DeferredItem<Item>> choices, int count) {
        return choices.get(random.nextInt(choices.size())).get().getDefaultInstance().copyWithCount(count);
    }

    public static List<Item> getItemsByType(WeaponType type) {
        return ITEMS_BY_TYPE.getOrDefault(type, Collections.emptyList())
            .stream()
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .toList();
    }

    /** Exposes every loot-only legendary item to creative tabs without making it craftable. */
    public static List<Item> getLegendaryItems() {
        return LEGENDARY_ITEMS.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
