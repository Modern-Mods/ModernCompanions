package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PHumanoidRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/** Uses Epic Fight's armature while retaining the companion player model's visible parts and scale. */
public final class EpicFightCompanionRenderer extends PHumanoidRenderer<AbstractHumanCompanionEntity,
        LivingEntityPatch<AbstractHumanCompanionEntity>, PlayerModel<AbstractHumanCompanionEntity>, CompanionRenderer, HumanoidMesh> {
    public EpicFightCompanionRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
        super(Meshes.BIPED, context, entityType);
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
}
