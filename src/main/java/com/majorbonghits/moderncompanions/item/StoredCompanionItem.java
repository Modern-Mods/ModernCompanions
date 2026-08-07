package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.entity.personality.CompanionPersonality;
import com.majorbonghits.moderncompanions.entity.personality.SoulReforgingRules;
import com.majorbonghits.moderncompanions.menu.TraitReforgingMenu;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Item that holds a living companion's full entity data so it can be redeployed later.
 */
public class StoredCompanionItem extends Item {
    private static final String COMPANION_NAME_TAG = "CompanionName";
    private static final String ENTITY_TYPE_TAG = "id";
    private static final int PRIMARY_REFORGE_COST = 15;
    private static final int SECONDARY_REFORGE_COST = 5;
    private static final Map<Item, List<String>> TRAIT_CATALYSTS = Map.of(
            Items.BLAZE_ROD, List.of("trait_brave", "trait_reckless", "trait_sun_blessed"),
            Items.TURTLE_SCUTE, List.of("trait_cautious", "trait_stalwart", "trait_guardian"),
            Items.RABBIT_FOOT, List.of("trait_quickstep", "trait_lucky", "trait_night_owl"),
            Items.PHANTOM_MEMBRANE, List.of("trait_night_owl", "trait_reckless", "trait_melancholic"),
            Items.GLOWSTONE_DUST, List.of("trait_sun_blessed", "trait_brave", "trait_quickstep"),
            Items.PRISMARINE_SHARD, List.of("trait_guardian", "trait_devoted", "trait_cautious"),
            Items.CAKE, List.of("trait_glutton", "trait_jokester", "trait_melancholic"),
            Items.CLOCK, List.of("trait_disciplined", "trait_devoted", "trait_cautious"),
            Items.HEART_OF_THE_SEA, List.of("trait_devoted", "trait_guardian", "trait_stalwart"),
            Items.SOUL_SOIL, List.of("trait_melancholic", "trait_jokester", "trait_disciplined"));

    public StoredCompanionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createFromCompanion(AbstractHumanCompanionEntity companion, Item storedItem) {
        ItemStack stack = new ItemStack(storedItem);
        storeCompanionData(stack, companion);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true); // visually distinct from normal gems
        return stack;
    }

    public static boolean hasCompanionData(ItemStack stack) {
        CustomData stored = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        return !stored.isEmpty() && stored.copyTag().contains(ENTITY_TYPE_TAG);
    }

    @Nullable
    private static ResourceLocation readEntityId(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryParse(data.copyTag().getString(ENTITY_TYPE_TAG));
    }

    private static void storeCompanionData(ItemStack stack, AbstractHumanCompanionEntity companion) {
        CompoundTag entityData = new CompoundTag();
        companion.saveWithoutId(entityData);
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(companion.getType());
        entityData.putString(ENTITY_TYPE_TAG, typeId.toString());

        // Reset transient state so the redeployed companion spawns safely.
        entityData.putFloat("Health", companion.getMaxHealth());
        entityData.remove("DeathTime");
        entityData.remove("HurtTime");
        entityData.remove("HurtByTimestamp");
        entityData.remove("Pos");
        entityData.remove("Motion");
        entityData.remove("Rotation");

        CustomData.set(DataComponents.ENTITY_DATA, stack, entityData);
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putString(COMPANION_NAME_TAG, companion.getName().getString()));

        stack.set(DataComponents.ITEM_NAME,
                Component.translatable("item.modern_companions.stored_companion.named", companion.getName()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (level.getBlockState(context.getClickedPos()).is(Blocks.ENCHANTING_TABLE)) {
            return openTraitReforging(context, stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!hasCompanionData(stack)) {
            notifyMissingData(context.getPlayer());
            return InteractionResult.FAIL;
        }

        Vec3 spawnPos = context.getClickLocation().add(Vec3.atLowerCornerOf(context.getClickedFace().getNormal()).scale(0.02));
        Entity placed = placeCompanion(serverLevel, stack, spawnPos, context.getPlayer());
        if (placed != null) {
            stack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, BlockPos.containing(placed.position()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    /** Opens the server-authoritative Soul Reforging choices without consuming anything yet. */
    private InteractionResult openTraitReforging(UseOnContext context, ItemStack stack) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        if (!hasCompanionData(stack)) {
            notifyMissingData(player);
            return InteractionResult.FAIL;
        }
        if (!isOwnedBy(stack, player)) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.not_owner"), true);
            return InteractionResult.FAIL;
        }

        InteractionHand catalystHand = context.getHand() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack catalyst = player.getItemInHand(catalystHand);
        if (!isTraitCatalyst(catalyst)) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_catalyst"), true);
            return InteractionResult.FAIL;
        }
        if (!hasItem(player, Items.LAPIS_LAZULI) || !hasItem(player, Items.ECHO_SHARD)) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_materials"), true);
            return InteractionResult.FAIL;
        }
        int bondLevel = getBondLevel(stack);
        if (bondLevel < 1) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_bond"), true);
            return InteractionResult.FAIL;
        }

        CompoundTag personality = getEntityData(stack).getCompound("Personality");
        List<String> options = rollTraitOptions(catalyst, personality.getString(CompanionPersonality.KEY_PRIMARY),
                personality.getString(CompanionPersonality.KEY_SECONDARY), player.getRandom());
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new TraitReforgingMenu(id, inventory, context.getHand(),
                        context.getClickedPos(), options),
                Component.translatable("gui.modern_companions.soul_reforging.title")), buffer -> {
                    buffer.writeByte(context.getHand().ordinal());
                    buffer.writeBlockPos(context.getClickedPos());
                    options.forEach(buffer::writeUtf);
                });
        return InteractionResult.CONSUME;
    }

    /** Applies a menu choice after every ownership, resource, and distance check is repeated on the server. */
    public static boolean reforge(Player player, InteractionHand hand, BlockPos tablePos, int traitSlot,
            String trait, List<String> options) {
        if (!(player instanceof ServerPlayer) || !player.level().getBlockState(tablePos).is(Blocks.ENCHANTING_TABLE)
                || player.distanceToSqr(Vec3.atCenterOf(tablePos)) > 64.0D) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!hasCompanionData(stack) || !isOwnedBy(stack, player) || !options.contains(trait)
                || !CompanionPersonality.TRAITS.contains(trait)) {
            return false;
        }

        CompoundTag entityData = getEntityData(stack);
        CompoundTag personality = entityData.getCompound("Personality");
        String primary = personality.getString(CompanionPersonality.KEY_PRIMARY);
        String secondary = personality.getString(CompanionPersonality.KEY_SECONDARY);
        if (trait.equals(primary) || trait.equals(secondary)) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.same_trait"), true);
            return false;
        }

        int requiredBond = traitSlot == 0 ? 2 : 1;
        int levelCost = traitSlot == 0 ? PRIMARY_REFORGE_COST : SECONDARY_REFORGE_COST;
        if (traitSlot < 0 || traitSlot > 1 || getBondLevel(stack) < requiredBond) {
            player.displayClientMessage(Component.translatable(
                    traitSlot == 0 ? "message.modern_companions.soul_reforging.primary_bond"
                            : "message.modern_companions.soul_reforging.needs_bond"), true);
            return false;
        }

        InteractionHand catalystHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack catalyst = player.getItemInHand(catalystHand);
        if (!isTraitCatalyst(catalyst)) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_catalyst"), true);
            return false;
        }
        if (!player.getAbilities().instabuild
                && (!hasItem(player, Items.LAPIS_LAZULI) || !hasItem(player, Items.ECHO_SHARD))) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_materials"), true);
            return false;
        }
        if (!player.getAbilities().instabuild && player.experienceLevel < levelCost) {
            player.displayClientMessage(Component.translatable(
                    "message.modern_companions.soul_reforging.needs_xp", levelCost), true);
            return false;
        }

        if (!player.getAbilities().instabuild) {
            consumeItem(player, Items.LAPIS_LAZULI);
            consumeItem(player, Items.ECHO_SHARD);
            consumeItem(player, catalyst.getItem());
            player.giveExperienceLevels(-levelCost);
        }
        personality.putString(traitSlot == 0 ? CompanionPersonality.KEY_PRIMARY : CompanionPersonality.KEY_SECONDARY,
                trait);
        entityData.put("Personality", personality);
        CustomData.set(DataComponents.ENTITY_DATA, stack, entityData);
        player.level().playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, tablePos.getX() + 0.5D, tablePos.getY() + 1.0D,
                    tablePos.getZ() + 0.5D, 20, 0.35D, 0.45D, 0.35D, 0.1D);
        }
        player.displayClientMessage(Component.translatable(
                "message.modern_companions.soul_reforging.success"), true);
        return true;
    }

    public static boolean isTraitCatalyst(ItemStack stack) {
        return !stack.isEmpty() && TRAIT_CATALYSTS.containsKey(stack.getItem());
    }

    public static List<String> rollTraitOptions(ItemStack catalyst, String primary, String secondary,
            RandomSource random) {
        return SoulReforgingRules.rollOptions(TRAIT_CATALYSTS.getOrDefault(catalyst.getItem(), List.of()),
                primary, secondary, new Random(random.nextLong()));
    }

    public static String getTraitId(ItemStack stack, String key) {
        return getEntityData(stack).getCompound("Personality").getString(key);
    }

    private static int getBondLevel(ItemStack stack) {
        return getEntityData(stack).getCompound("Personality").getInt(CompanionPersonality.KEY_BOND_LEVEL);
    }

    private static boolean isOwnedBy(ItemStack stack, Player player) {
        CompoundTag entityData = getEntityData(stack);
        return entityData.hasUUID("Owner") && player.getUUID().equals(entityData.getUUID("Owner"));
    }

    private static CompoundTag getEntityData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag();
    }

    private static boolean hasItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) return true;
        }
        return false;
    }

    private static void consumeItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!hasCompanionData(stack)) {
            notifyMissingData(player);
            return InteractionResultHolder.fail(stack);
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        Vec3 spawnPos = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.02));
        Entity placed = placeCompanion(serverLevel, stack, spawnPos, player);
        if (placed != null) {
            stack.consume(1, player);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, BlockPos.containing(placed.position()));
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Nullable
    private Entity placeCompanion(ServerLevel level, ItemStack stack, Vec3 pos, @Nullable Player player) {
        ResourceLocation typeId = readEntityId(stack);
        if (typeId == null) {
            return null;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeId);
        if (!(type.create(level) instanceof AbstractHumanCompanionEntity companion)) {
            return null; // safeguard against wrong item data
        }

        CustomData storedData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        CompoundTag tag = storedData.copyTag();

        companion.load(tag);
        double safeY = Math.max(pos.y(), level.getMinBuildHeight() + 0.01D);
        companion.moveTo(pos.x(), safeY, pos.z(), level.random.nextFloat() * 360.0F, 0.0F);
        companion.setHealth(companion.getMaxHealth());
        companion.setDeltaMovement(Vec3.ZERO);
        companion.setOnGround(true);
        level.addFreshEntity(companion);

        if (player != null) {
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        }

        return companion;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(DataComponents.ITEM_NAME)) {
            return stack.get(DataComponents.ITEM_NAME);
        }
        return Component.translatable("item.modern_companions.stored_companion");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getString(COMPANION_NAME_TAG);
        if (!name.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.modern_companions.stored_companion.bound", name));
        } else {
            tooltip.add(Component.translatable("tooltip.modern_companions.stored_companion.empty"));
        }
        tooltip.add(Component.translatable("tooltip.modern_companions.stored_companion.reforging"));
    }

    private void notifyMissingData(@Nullable Player player) {
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("tooltip.modern_companions.stored_companion.empty"), true);
        }
    }
}
