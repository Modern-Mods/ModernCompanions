package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import com.majorbonghits.moderncompanions.client.renderer.CompanionSkinManager;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

/**
 * Renderer that mimics the original Human Companions player-like visuals.
 */
public class CompanionRenderer extends HumanoidMobRenderer<AbstractHumanCompanionEntity, PlayerModel<AbstractHumanCompanionEntity>> {
    private static boolean suppressPreviewNameplate;
    private final PlayerModel<AbstractHumanCompanionEntity> wideModel;
    private final PlayerModel<AbstractHumanCompanionEntity> slimModel;
    private final CompanionArmorLayer wideArmorLayer;
    private final CompanionArmorLayer slimArmorLayer;

    /** Restricts nameplate suppression to the inventory preview, never the world renderer. */
    public static void setPreviewNameplateSuppressed(boolean value) {
        suppressPreviewNameplate = value;
    }

    public CompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.wideModel = this.getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), true);
        this.wideArmorLayer = new CompanionArmorLayer(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager(), false);
        this.addLayer(this.wideArmorLayer);
        this.slimArmorLayer = new CompanionArmorLayer(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)),
                context.getModelManager(), true);
        this.addLayer(this.slimArmorLayer);
    }

    /** Supplies the matching vanilla armor layer to optional renderer integrations. */
    public HumanoidArmorLayer<AbstractHumanCompanionEntity, PlayerModel<AbstractHumanCompanionEntity>,
            HumanoidModel<AbstractHumanCompanionEntity>> getArmorLayer(boolean alex) {
        return alex ? slimArmorLayer : wideArmorLayer;
    }

    @Override
    public void render(AbstractHumanCompanionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        entity.setEquipmentRenderContext(true);
        try {
            this.setModelProperties(entity);
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        } finally {
            entity.setEquipmentRenderContext(false);
        }
    }

    @Override
    protected boolean shouldShowName(AbstractHumanCompanionEntity entity) {
        return !suppressPreviewNameplate && super.shouldShowName(entity);
    }

    /** Shares the player-model visibility and held-item state with Epic Fight's patched renderer. */
    void setModelProperties(AbstractHumanCompanionEntity companion) {
        // Select the synced model before either vanilla or Epic Fight reads the render model.
        this.model = companion.usesAlexModel() ? slimModel : wideModel;
        PlayerModel<AbstractHumanCompanionEntity> model = this.getModel();
        HumanoidModel.ArmPose main = armPose(companion, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose off = armPose(companion, InteractionHand.OFF_HAND);

        if (companion.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = main;
            model.leftArmPose = off;
        } else {
            model.rightArmPose = off;
            model.leftArmPose = main;
        }
    }

    private static HumanoidModel.ArmPose armPose(AbstractHumanCompanionEntity companion, InteractionHand hand) {
        ItemStack stack = companion.getItemInHand(hand);
        if (stack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (companion.getUsedItemHand() == hand && companion.getUseItemRemainingTicks() > 0) {
            UseAnim anim = stack.getUseAnimation();
            if (anim == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (anim == UseAnim.CROSSBOW && hand == companion.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
        } else if (!companion.swinging && stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    /** Keeps armor arms aligned with the selected Steve/Alex body instead of masking the choice. */
    private static final class CompanionArmorLayer extends HumanoidArmorLayer<AbstractHumanCompanionEntity,
            PlayerModel<AbstractHumanCompanionEntity>, HumanoidModel<AbstractHumanCompanionEntity>> {
        private final boolean alex;

        private CompanionArmorLayer(CompanionRenderer renderer, HumanoidModel<AbstractHumanCompanionEntity> innerModel,
                                    HumanoidModel<AbstractHumanCompanionEntity> outerModel, ModelManager modelManager,
                                    boolean alex) {
            super(renderer, innerModel, outerModel, modelManager);
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

    @Override
    public ResourceLocation getTextureLocation(AbstractHumanCompanionEntity entity) {
        // Prefer custom URL skins; fall back to bundled texture if download fails.
        ResourceLocation fallback = entity.getDefaultSkinTexture();
        return entity.getCustomSkinUrl()
                .map(url -> CompanionSkinManager.getOrCreate(url, fallback))
                .orElse(fallback);
    }

    @Override
    protected void scale(AbstractHumanCompanionEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
