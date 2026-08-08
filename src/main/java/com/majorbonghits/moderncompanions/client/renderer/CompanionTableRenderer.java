package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.block.CompanionTableBlockEntity;
import com.majorbonghits.moderncompanions.ModernCompanions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Companion-owned copy of the vanilla enchanting-table book renderer. */
public final class CompanionTableRenderer implements BlockEntityRenderer<CompanionTableBlockEntity> {
    private static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, "entity/companion_table_book"));

    private final BookModel bookModel;

    public CompanionTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(CompanionTableBlockEntity table, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5F, 0.75F, 0.5F);
        float time = table.time + partialTick;
        pose.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);

        float angle = table.rot - table.oRot;
        while (angle >= Mth.PI) {
            angle -= Mth.TWO_PI;
        }
        while (angle < -Mth.PI) {
            angle += Mth.TWO_PI;
        }
        float rotation = table.oRot + angle * partialTick;
        pose.mulPose(Axis.YP.rotation(-rotation));
        pose.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float flip = Mth.lerp(partialTick, table.oFlip, table.flip);
        float leftPage = Mth.frac(flip + 0.25F) * 1.6F - 0.3F;
        float rightPage = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
        float open = Mth.lerp(partialTick, table.oOpen, table.open);
        bookModel.setupAnim(time, Mth.clamp(leftPage, 0.0F, 1.0F), Mth.clamp(rightPage, 0.0F, 1.0F), open);
        VertexConsumer vertices = BOOK_LOCATION.buffer(buffers, RenderType::entitySolid);
        bookModel.render(pose, vertices, light, overlay, -1);
        pose.popPose();
    }
}
