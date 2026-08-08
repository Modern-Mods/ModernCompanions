package com.majorbonghits.moderncompanions.core;

import com.majorbonghits.moderncompanions.ModernCompanions;
import com.majorbonghits.moderncompanions.block.CompanionTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Own block-entity registrations kept separate from vanilla block-entity types. */
public final class ModBlockEntityTypes {
    private ModBlockEntityTypes() {
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModernCompanions.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompanionTableBlockEntity>> COMPANION_TABLE =
            BLOCK_ENTITY_TYPES.register("companion_table", () -> BlockEntityType.Builder
                    .of(CompanionTableBlockEntity::new, ModBlocks.COMPANION_TABLE.get())
                    .build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
