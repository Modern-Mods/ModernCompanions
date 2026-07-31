package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import com.majorbonghits.moderncompanions.entity.FirearmSpecialist;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

/** A normal summon gem that fixes the spawned specialist's firearm category. */
public final class FirearmSpecialistSummonGemItem extends net.neoforged.neoforge.common.DeferredSpawnEggItem {
    private final FirearmSupport.Specialty specialty;

    public FirearmSpecialistSummonGemItem(
            java.util.function.Supplier<? extends EntityType<? extends Mob>> type,
            int backgroundColor, int highlightColor, FirearmSupport.Specialty specialty, Properties properties) {
        super(type, backgroundColor, highlightColor, properties);
        this.specialty = specialty;
    }

    @Override
    protected DispenseItemBehavior createDispenseBehavior() {
        return (source, stack) -> {
            Direction face = source.state().getValue(DispenserBlock.FACING);
            Entity spawned = spawn(source.level(), stack, null, source.pos().relative(face),
                    MobSpawnType.DISPENSER, face != Direction.UP, false);
            if (spawned == null) return ItemStack.EMPTY;
            stack.shrink(1);
            source.level().gameEvent(GameEvent.ENTITY_PLACE, source.pos(), GameEvent.Context.of(source.state()));
            return stack;
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState state = level.getBlockState(clicked);
        if (level.getBlockEntity(clicked) instanceof Spawner spawner) {
            spawner.setEntityId(getType(stack), level.getRandom());
            level.sendBlockUpdated(clicked, state, state, 3);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, clicked);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }

        BlockPos spawnPos = state.getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(face);
        Entity spawned = spawn(server, stack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG,
                true, !clicked.equals(spawnPos) && face == Direction.UP);
        if (spawned != null) {
            stack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clicked);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        BlockPos pos = hit.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) return InteractionResultHolder.pass(stack);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            return InteractionResultHolder.fail(stack);
        }
        Entity spawned = spawn(server, stack, player, pos, MobSpawnType.SPAWN_EGG, false, false);
        if (spawned == null) return InteractionResultHolder.pass(stack);
        stack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.gameEvent(player, GameEvent.ENTITY_PLACE, spawned.position());
        return InteractionResultHolder.consume(stack);
    }

    private Entity spawn(ServerLevel level, ItemStack stack, Player player, BlockPos pos,
                         MobSpawnType reason, boolean align, boolean invert) {
        Entity entity = getType(stack).spawn(level, stack, player, pos, reason, align, invert);
        if (entity instanceof FirearmSpecialist specialist) specialist.applySummonedSpecialty(specialty);
        return entity;
    }
}
