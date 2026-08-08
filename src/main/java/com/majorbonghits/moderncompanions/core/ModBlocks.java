package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.block.CompanionTableBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the Companion Table separately from its block item. */
public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModernCompanions.MOD_ID);

    /** These are the vanilla table's material properties without sharing its block behavior. */
    public static final DeferredBlock<CompanionTableBlock> COMPANION_TABLE = BLOCKS.registerBlock(
            "companion_table", CompanionTableBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops().lightLevel(state -> 7).strength(5.0F, 1200.0F));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
