package com.majorbonghits.moderncompanions.mixin;

import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stops the vanilla random teleport entry point for captured Endermen. */
@Mixin(EnderMan.class)
public abstract class HostilePetEndermanMixin {
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void modernCompanions$preventPetRandomTeleport(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (Beastmaster.isBeastmasterPet((EnderMan) (Object) this)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
