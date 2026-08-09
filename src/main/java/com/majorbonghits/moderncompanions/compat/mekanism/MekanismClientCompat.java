package com.majorbonghits.moderncompanions.compat.mekanism;

import com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.layer.MekanismArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Adds Mekanism's special-gear layer to player-shaped companion renderers. */
public final class MekanismClientCompat {
    private MekanismClientCompat() {}

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        ModelManager modelManager = event.getContext().getModelManager();
        for (var entityType : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(entityType);
            if (renderer instanceof CompanionRenderer companion) {
                addLayer(companion, modelManager, false);
                addLayer(companion, modelManager, true);
            }
        }
    }

    private static void addLayer(CompanionRenderer renderer, ModelManager modelManager, boolean alex) {
        renderer.addLayer(new CompanionMekanismArmorLayer(renderer, modelManager, alex));
    }

    /** Filters the copied Mekanism model to the same Steve/Alex shape as vanilla companion armor. */
    private static final class CompanionMekanismArmorLayer extends MekanismArmorLayer<AbstractHumanCompanionEntity,
            PlayerModel<AbstractHumanCompanionEntity>, HumanoidModel<AbstractHumanCompanionEntity>> {
        private final boolean alex;

        private CompanionMekanismArmorLayer(CompanionRenderer renderer, ModelManager modelManager, boolean alex) {
            super(renderer, renderer.getArmorLayer(alex), modelManager);
            this.alex = alex;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           AbstractHumanCompanionEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.usesAlexModel() == alex) {
                super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                        partialTick, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }
}
