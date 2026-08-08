package com.majorbonghits.moderncompanions.block;

import com.majorbonghits.moderncompanions.core.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Local copy of the vanilla book animation state; it never uses the vanilla table entity type. */
public final class CompanionTableBlockEntity extends BlockEntity {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    private static final RandomSource RANDOM = RandomSource.create();

    public CompanionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.COMPANION_TABLE.get(), pos, state);
    }

    /** Vanilla book animation, isolated so compatibility hooks on the vanilla entity cannot leak in. */
    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state,
            CompanionTableBlockEntity table) {
        table.oOpen = table.open;
        table.oRot = table.rot;
        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                3.0D, false);
        if (player != null) {
            double x = player.getX() - (pos.getX() + 0.5D);
            double z = player.getZ() - (pos.getZ() + 0.5D);
            table.tRot = (float) Mth.atan2(z, x);
            table.open += 0.1F;
            if (table.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float oldFlipTarget = table.flipT;
                do {
                    table.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (oldFlipTarget == table.flipT);
            }
        } else {
            table.tRot += 0.02F;
            table.open -= 0.1F;
        }

        while (table.rot >= Mth.PI) {
            table.rot -= Mth.TWO_PI;
        }
        while (table.rot < -Mth.PI) {
            table.rot += Mth.TWO_PI;
        }
        while (table.tRot >= Mth.PI) {
            table.tRot -= Mth.TWO_PI;
        }
        while (table.tRot < -Mth.PI) {
            table.tRot += Mth.TWO_PI;
        }

        float angle = table.tRot - table.rot;
        while (angle >= Mth.PI) {
            angle -= Mth.TWO_PI;
        }
        while (angle < -Mth.PI) {
            angle += Mth.TWO_PI;
        }
        table.rot += angle * 0.4F;
        table.open = Mth.clamp(table.open, 0.0F, 1.0F);
        ++table.time;
        table.oFlip = table.flip;
        float flipDelta = (table.flipT - table.flip) * 0.4F;
        flipDelta = Mth.clamp(flipDelta, -0.2F, 0.2F);
        table.flipA += (flipDelta - table.flipA) * 0.9F;
        table.flip += table.flipA;
    }
}
