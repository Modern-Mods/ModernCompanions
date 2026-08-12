package com.majorbonghits.moderncompanions.mixin.client;

import com.majorbonghits.moderncompanions.Constants;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/** Registers the existing held models so the item renderer can select them by context. */
@Mixin(ModelBakery.class)
public abstract class HeldWeaponModelBakeryMixin {
    private static final List<String> MATERIAL_PREFIXES = List.of(
            "wooden", "stone", "iron", "golden", "diamond", "netherite", "bronze");

    @Shadow
    protected abstract void registerModel(ModelResourceLocation modelId, UnbakedModel model);

    @Shadow
    protected abstract UnbakedModel getModel(ResourceLocation location);

    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0))
    private void modernCompanions$registerHeldModels(
            BlockColors blockColors, ProfilerFiller profilerFiller,
            Map<ResourceLocation, BlockModel> modelResources,
            Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources,
            CallbackInfo callbackInfo) {
        // Only register models whose base item exists; bronze therefore remains optional.
        for (String material : MATERIAL_PREFIXES) {
            modernCompanions$registerHeldModel(material + "_spear");
            modernCompanions$registerHeldModel(material + "_quarterstaff");
            modernCompanions$registerHeldModel(material + "_glaive");
        }
    }

    @Unique
    private void modernCompanions$registerHeldModel(String baseName) {
        ResourceLocation baseModelLocation = Constants.id("item/" + baseName);
        if (getModel(baseModelLocation) == null) {
            return;
        }

        UnbakedModel heldModel = getModel(Constants.id("item/" + baseName + "_held"));
        if (heldModel != null) {
            registerModel(new ModelResourceLocation(Constants.id(baseName + "_held"), "inventory"), heldModel);
        }
    }
}
