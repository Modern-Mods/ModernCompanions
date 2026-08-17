package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A consumed, data-bearing Soul Orb for one captured mob. */
public class SoulOrbItem extends Item {
    private static final String ENTITY_NAME_TAG = "AnimalName";
    private static final String ENTITY_TYPE_TAG = "id";
    private static final String CAPTURE_KIND_TAG = "CaptureKind";
    private static final String HOSTILE_CAPTURE_KIND = "hostile";

    public SoulOrbItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** One predicate is shared by capture, release, and Beastmaster replacement validation. */
    public static boolean isEligibleAnimal(Entity entity) {
        return entity instanceof Mob mob && mob.isAlive()
                && !(mob instanceof AbstractHumanCompanionEntity)
                && mob.getType().getCategory() != MobCategory.MONSTER;
    }

    /** Hostile capture uses the Enemy contract plus NeoForge's data-driven boss tag. */
    public static boolean isEligibleHostile(Entity entity) {
        return entity instanceof Mob mob && mob.isAlive()
                && !(mob instanceof AbstractHumanCompanionEntity)
                && (mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER)
                && !mob.getType().is(Tags.EntityTypes.BOSSES);
    }

    public static ItemStack createFromAnimal(Mob animal, Item orbItem) {
        return createFromMob(animal, orbItem,
                animal instanceof Enemy || animal.getType().getCategory() == MobCategory.MONSTER
                        ? HOSTILE_CAPTURE_KIND : "animal");
    }

    public static ItemStack createFromHostile(Mob hostile, Item orbItem) {
        return createFromMob(hostile, orbItem, HOSTILE_CAPTURE_KIND);
    }

    private static ItemStack createFromMob(Mob mob, Item orbItem, String captureKind) {
        ItemStack stack = new ItemStack(orbItem);
        CompoundTag entityData = new CompoundTag();
        // saveWithoutId includes UUID, equipment, inventories, variants, names, and mod NBT.
        mob.saveWithoutId(entityData);
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        entityData.putString(ENTITY_TYPE_TAG, typeId.toString());
        entityData.putString(CAPTURE_KIND_TAG, captureKind);
        entityData.remove("Pos");
        entityData.remove("Motion");
        entityData.remove("Rotation");
        CustomData.set(DataComponents.ENTITY_DATA, stack, entityData);
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putString(ENTITY_NAME_TAG, mob.getName().getString()));
        stack.set(DataComponents.ITEM_NAME, orbName(mob.getName()));
        return stack;
    }

    public static boolean hasSoulData(ItemStack stack) {
        CompoundTag data = getAnimalData(stack);
        return data.contains(ENTITY_TYPE_TAG) && !data.getString(ENTITY_TYPE_TAG).isEmpty();
    }

    public static boolean hasAnimalData(ItemStack stack) {
        return hasSoulData(stack);
    }

    public static boolean isHostileCapture(ItemStack stack) {
        return isHostileCapture(getAnimalData(stack));
    }

    private static boolean isHostileCapture(CompoundTag data) {
        return HOSTILE_CAPTURE_KIND.equals(data.getString(CAPTURE_KIND_TAG));
    }

    public static CompoundTag getAnimalData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag();
    }

    @Nullable
    public static Mob createEntity(ServerLevel level, CompoundTag data) {
        ResourceLocation typeId = ResourceLocation.tryParse(data.getString(ENTITY_TYPE_TAG));
        if (typeId == null) {
            return null;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(typeId).orElse(null);
        if (type == null) {
            return null;
        }
        Entity entity = type.create(level);
        if (isHostileCapture(data)) {
            return isEligibleHostile(entity) ? (Mob) entity : null;
        }
        return isEligibleAnimal(entity) ? (Mob) entity : null;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity,
            InteractionHand hand) {
        if (!(entity instanceof Beastmaster beastmaster)) {
            return InteractionResult.PASS;
        }
        if (!hasSoulData(stack)) {
            notifyPlayer(player, "tooltip.modern_companions.soul_orb.empty");
            return InteractionResult.sidedSuccess(entity.level().isClientSide());
        }
        if (!beastmaster.isTame() || !player.getUUID().equals(beastmaster.getOwnerUUID())) {
            notifyPlayer(player, "message.modern_companions.soul_orb.not_owner");
            return InteractionResult.sidedSuccess(entity.level().isClientSide());
        }
        if (isHostileCapture(stack) && beastmaster.getExpLvl() < 20) {
            notifyPlayer(player, "message.modern_companions.soul_orb.needs_level");
            return InteractionResult.sidedSuccess(entity.level().isClientSide());
        }
        if (entity.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack oldPetOrb = beastmaster.swapPet(getAnimalData(stack), ModItems.SOUL_ORB.get());
        if (oldPetOrb == null) {
            notifyPlayer(player, "message.modern_companions.soul_orb.swap_failed");
            return InteractionResult.FAIL;
        }
        if (!player.getInventory().add(oldPetOrb)) {
            player.drop(oldPetOrb, false);
        }
        stack.consume(1, player);
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.NEUTRAL,
                0.8F, 1.2F);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Vec3 spawnPos = context.getClickLocation()
                .add(Vec3.atLowerCornerOf(context.getClickedFace().getNormal()).scale(0.02D));
        return spawn(context.getPlayer(), context.getItemInHand(), (ServerLevel) level, spawnPos);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        Vec3 spawnPos = hit.getLocation()
                .add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.02D));
        InteractionResult result = spawn(player, stack, (ServerLevel) level, spawnPos);
        return result.consumesAction() ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    private InteractionResult spawn(@Nullable Player player, ItemStack stack, ServerLevel level, Vec3 position) {
        if (!hasSoulData(stack)) {
            notifyPlayer(player, "tooltip.modern_companions.soul_orb.empty");
            return InteractionResult.FAIL;
        }
        if (isHostileCapture(stack)) {
            notifyPlayer(player, "message.modern_companions.soul_orb.hostile_requires_beastmaster");
            return InteractionResult.FAIL;
        }
        CompoundTag data = getAnimalData(stack);
        Mob animal = createEntity(level, data);
        if (animal == null) {
            notifyPlayer(player, "message.modern_companions.soul_orb.invalid");
            return InteractionResult.FAIL;
        }
        animal.load(data);
        animal.moveTo(position.x(), Math.max(position.y(), level.getMinBuildHeight() + 0.01D), position.z(),
                level.random.nextFloat() * 360.0F, 0.0F);
        animal.setDeltaMovement(Vec3.ZERO);
        animal.setOnGround(true);
        animal.setPersistenceRequired();
        if (!level.addFreshEntity(animal)) {
            animal.discard();
            notifyPlayer(player, "message.modern_companions.soul_orb.invalid");
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        level.playSound(null, BlockPos.containing(position), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.NEUTRAL,
                0.8F, 1.2F);
        level.gameEvent(player, GameEvent.ENTITY_PLACE, BlockPos.containing(animal.position()));
        return InteractionResult.CONSUME;
    }

    private static Component orbName(Component animalName) {
        return Component.translatable("item.modern_companions.soul_orb.named", animalName)
                .withStyle(ChatFormatting.AQUA);
    }

    private static void notifyPlayer(@Nullable Player player, String key) {
        if (player != null) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ITEM_NAME, Component.translatable("item.modern_companions.soul_orb"));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        if (hasAnimalData(stack)) {
            String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                    .getString(ENTITY_NAME_TAG);
            tooltip.add(Component.translatable("tooltip.modern_companions.soul_orb.bound", name)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.modern_companions.soul_orb.empty"));
        }
    }
}
