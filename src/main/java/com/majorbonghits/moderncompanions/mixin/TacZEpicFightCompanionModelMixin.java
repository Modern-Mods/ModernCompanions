package com.majorbonghits.moderncompanions.mixin;

/*
 * Derived from Epic Fight x TacZ Compat by ImperialArchitects.
 * Copyright (c) 2026 ImperialArchitects
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reapplies TacZ's pose after Epic Fight's humanoid-model hook for gun-holding companions. */
@Mixin(value = HumanoidModel.class, priority = 1500)
public abstract class TacZEpicFightCompanionModelMixin {
    @Shadow public ModelPart head;
    @Shadow public ModelPart body;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void modernCompanions$reapplyTacZGunPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                      float ageInTicks, float netHeadYaw, float headPitch,
                                                      CallbackInfo ci) {
        if (ageInTicks == 0.0F || !(entity instanceof AbstractHumanCompanionEntity)
                || !FirearmSupport.isTacZFirearm(entity.getMainHandItem())) return;
        try {
            Class.forName("com.tacz.guns.client.animation.third.InnerThirdPersonManager")
                    .getMethod("setRotationAnglesHead", LivingEntity.class, ModelPart.class, ModelPart.class,
                            ModelPart.class, ModelPart.class, float.class)
                    .invoke(null, entity, this.rightArm, this.leftArm, this.body, this.head, ageInTicks);
        } catch (ReflectiveOperationException ignored) {
            // TacZ remains optional; its native fallback renderer stays in control when unavailable.
        }
    }
}
