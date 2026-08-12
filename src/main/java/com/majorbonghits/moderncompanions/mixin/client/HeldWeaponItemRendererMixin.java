package com.majorbonghits.moderncompanions.mixin.client;

import com.majorbonghits.moderncompanions.Constants;
import com.majorbonghits.moderncompanions.item.BasicWeaponItem;
import com.majorbonghits.moderncompanions.item.BasicWeaponSweeplessItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Selects the existing long weapon model only while a material weapon is equipped. */
@Mixin(ItemRenderer.class)
public abstract class HeldWeaponItemRendererMixin {
    @Shadow @Final private ItemModelShaper itemModelShaper;

    @ModifyVariable(method = "render", at = @At(value = "HEAD"), argsOnly = true)
    public BakedModel modernCompanions$selectHeldModel(
            BakedModel originalModel, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
            PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay,
            BakedModel ignoredModel) {
        if (displayContext == ItemDisplayContext.GUI
                || displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.FIXED) {
            return originalModel;
        }

        Item item = stack.getItem();
        if (!(item instanceof BasicWeaponItem || item instanceof BasicWeaponSweeplessItem)) {
            return originalModel;
        }

        String itemId = item.getDescriptionId()
                .replace("item." + Constants.MOD_ID + ".", "");
        if (!modernCompanions$hasHeldModel(itemId)) {
            return originalModel;
        }

        ModelResourceLocation heldModelId = new ModelResourceLocation(
                Constants.id(itemId + "_held"), "inventory");
        return itemModelShaper.getModelManager().getModel(heldModelId);
    }

    @Unique
    private static boolean modernCompanions$hasHeldModel(String itemId) {
        boolean materialVariant = itemId.startsWith("wooden_")
                || itemId.startsWith("stone_")
                || itemId.startsWith("iron_")
                || itemId.startsWith("golden_")
                || itemId.startsWith("diamond_")
                || itemId.startsWith("netherite_")
                || itemId.startsWith("bronze_");
        return materialVariant && (itemId.endsWith("_spear") || itemId.endsWith("_quarterstaff"));
    }
}
