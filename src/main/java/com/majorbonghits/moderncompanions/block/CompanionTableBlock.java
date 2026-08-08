package com.majorbonghits.moderncompanions.block;

import com.mojang.serialization.MapCodec;
import com.majorbonghits.moderncompanions.menu.CompanionTableMenu;
import com.majorbonghits.moderncompanions.core.ModBlockEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Companion Table owns the small amount of enchanting-table behavior it needs. Keeping the
 * block and block entity types local prevents compatibility changes to Minecraft's enchanting
 * table from being applied to this separate table.
 */
public final class CompanionTableBlock extends BaseEntityBlock {
    public static final MapCodec<CompanionTableBlock> CODEC = simpleCodec(CompanionTableBlock::new);
    protected static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    public static final List<BlockPos> BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2)
            .filter(offset -> Math.abs(offset.getX()) == 2 || Math.abs(offset.getZ()) == 2)
            .map(BlockPos::immutable)
            .toList();

    public CompanionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CompanionTableBlock> codec() {
        return CODEC;
    }

    /** Copy vanilla bookshelf checks locally for the table's client-only particle effect. */
    public static boolean isValidBookShelf(Level level, BlockPos pos, BlockPos offset) {
        return level.getBlockState(pos.offset(offset)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                && level.getBlockState(pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2))
                        .is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        for (BlockPos offset : BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) == 0 && isValidBookShelf(level, pos, offset)) {
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 2.0D,
                        pos.getZ() + 0.5D, offset.getX() + random.nextFloat() - 0.5D,
                        offset.getY() - random.nextFloat() - 1.0F, offset.getZ() + random.nextFloat() - 0.5D);
            }
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompanionTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntityTypes.COMPANION_TABLE.get(),
                        CompanionTableBlockEntity::bookAnimationTick)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        return openMenu(state, level, pos, player);
    }

    /** Open the table even while holding an input item so every resource can be inserted through the menu. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        return level.isClientSide ? ItemInteractionResult.SUCCESS : toItemResult(openMenu(state, level, pos, player));
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (id, inventory, ignored) -> new CompanionTableMenu(id, inventory, pos),
                Component.translatable("container.modern_companions.companion_table"));
    }

    private InteractionResult openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        player.openMenu(getMenuProvider(state, level, pos), buffer -> buffer.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.CONSUME
                ? ItemInteractionResult.CONSUME : ItemInteractionResult.SUCCESS;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
