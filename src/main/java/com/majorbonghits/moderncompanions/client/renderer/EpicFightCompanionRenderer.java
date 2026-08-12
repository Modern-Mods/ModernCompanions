package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/** Uses Epic Fight's armature while retaining the companion player model's visible parts and scale. */
public final class EpicFightCompanionRenderer extends PHumanoidRenderer<AbstractHumanCompanionEntity,
        LivingEntityPatch<AbstractHumanCompanionEntity>, PlayerModel<AbstractHumanCompanionEntity>, CompanionRenderer, HumanoidMesh> {
    public EpicFightCompanionRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(Meshes.BIPED, context, entityType);
        ModelManager modelManager = context.getModelManager();
        // Epic Fight replaces the vanilla renderer, so bridge both player-shaped armor layers explicitly.
        this.addPatchedLayerAlways(CompanionRenderer.WideCompanionArmorLayer.class,
                new CompanionWearableItemLayer(Meshes.BIPED, modelManager, false));
        this.addPatchedLayerAlways(CompanionRenderer.SlimCompanionArmorLayer.class,
                new CompanionWearableItemLayer(Meshes.ALEX, modelManager, true));
    }

    /** Epic Fight bypasses vanilla layer predicates, so only the active Steve/Alex armor bridge may draw. */
    private static final class CompanionWearableItemLayer extends WearableItemLayer<AbstractHumanCompanionEntity,
            LivingEntityPatch<AbstractHumanCompanionEntity>, PlayerModel<AbstractHumanCompanionEntity>, HumanoidMesh> {
        private final boolean alex;

        private CompanionWearableItemLayer(AssetAccessor<HumanoidMesh> mesh, ModelManager modelManager, boolean alex) {
            super(mesh, false, modelManager);
            this.alex = alex;
        }

        @Override
        public void renderLayer(AbstractHumanCompanionEntity entity, LivingEntityPatch<AbstractHumanCompanionEntity> patch,
                                RenderLayer<AbstractHumanCompanionEntity, PlayerModel<AbstractHumanCompanionEntity>> layer,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses,
                                float bob, float yRot, float xRot, float partialTicks) {
            if (entity.usesAlexModel() == alex) {
                super.renderLayer(entity, patch, layer, poseStack, buffer, packedLight, poses,
                        bob, yRot, xRot, partialTicks);
            }
        }
    }

    @Override
    public void render(AbstractHumanCompanionEntity entity, LivingEntityPatch<AbstractHumanCompanionEntity> patch,
                       CompanionRenderer renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight,
                       float partialTicks) {
        entity.setEquipmentRenderContext(true);
        try {
            super.render(entity, patch, renderer, buffer, poseStack, packedLight, partialTicks);
        } finally {
            entity.setEquipmentRenderContext(false);
        }
    }

    @Override
    protected void prepareModel(HumanoidMesh mesh, AbstractHumanCompanionEntity entity,
                                LivingEntityPatch<AbstractHumanCompanionEntity> patch, CompanionRenderer renderer) {
        super.prepareModel(mesh, entity, patch, renderer);
        renderer.setModelProperties(entity);
        PlayerModel<AbstractHumanCompanionEntity> model = renderer.getModel();
        mesh.head.setHidden(!model.head.visible);
        mesh.hat.setHidden(!model.hat.visible);
        mesh.jacket.setHidden(!model.jacket.visible);
        mesh.torso.setHidden(!model.body.visible);
        mesh.leftArm.setHidden(!model.leftArm.visible);
        mesh.leftLeg.setHidden(!model.leftLeg.visible);
        mesh.leftPants.setHidden(!model.leftPants.visible);
        mesh.leftSleeve.setHidden(!model.leftSleeve.visible);
        mesh.rightArm.setHidden(!model.rightArm.visible);
        mesh.rightLeg.setHidden(!model.rightLeg.visible);
        mesh.rightPants.setHidden(!model.rightPants.visible);
        mesh.rightSleeve.setHidden(!model.rightSleeve.visible);
    }

    @Override
    public void mulPoseStack(PoseStack poseStack, yesman.epicfight.api.model.Armature armature,
                             AbstractHumanCompanionEntity entity, LivingEntityPatch<AbstractHumanCompanionEntity> patch,
                             float partialTicks) {
        super.mulPoseStack(poseStack, armature, entity, patch, partialTicks);
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public AssetAccessor<HumanoidMesh> getDefaultMesh() {
        return Meshes.BIPED;
    }

    @Override
    public AssetAccessor<HumanoidMesh> getMeshProvider(LivingEntityPatch<AbstractHumanCompanionEntity> patch) {
        return patch.getOriginal().usesAlexModel() ? Meshes.ALEX : Meshes.BIPED;
    }
}
