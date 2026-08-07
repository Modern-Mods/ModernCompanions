package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.block.CompanionTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the Companion Table separately from its block item. */
public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModernCompanions.MOD_ID);

    /** Full-copy properties keep the vanilla table's hardness, shape, and rendering behavior. */
    public static final DeferredBlock<CompanionTableBlock> COMPANION_TABLE = BLOCKS.registerBlock(
            "companion_table", CompanionTableBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
