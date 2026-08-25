package com.majorbonghits.moderncompanions.mixin;

import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps a captured Creeper's attack blast non-griefing; Beastmaster owns respawn. */
@Mixin(Creeper.class)
public abstract class HostilePetCreeperMixin {
    @Shadow
    private int explosionRadius;

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void modernCompanions$explodeAsPet(CallbackInfo callbackInfo) {
        Creeper creeper = (Creeper) (Object) this;
        if (!Beastmaster.isBeastmasterPet(creeper)) return;

        float power = creeper.isPowered() ? 2.0F : 1.0F;
        creeper.level().explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(),
                this.explosionRadius * power, false, Level.ExplosionInteraction.NONE);
        creeper.discard();
        callbackInfo.cancel();
    }
}
