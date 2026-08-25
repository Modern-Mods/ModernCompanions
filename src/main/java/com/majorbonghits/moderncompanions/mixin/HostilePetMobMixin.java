package com.majorbonghits.moderncompanions.mixin;

import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps captured sun-sensitive hostile mobs from burning after they become pets. */
@Mixin(Mob.class)
public abstract class HostilePetMobMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void modernCompanions$preventPetSunburn(CallbackInfoReturnable<Boolean> callbackInfo) {
        if (Beastmaster.isBeastmasterPet((Mob) (Object) this)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
