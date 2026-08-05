package com.majorbonghits.moderncompanions.client.renderer;

import com.majorbonghits.moderncompanions.entity.projectile.HolySparkProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The projectile is rendered entirely by its vanilla sparkle particle trail. */
public class HolySparkProjectileRenderer extends EntityRenderer<HolySparkProjectile> {
    private static final ResourceLocation PARTICLE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/particles.png");

    public HolySparkProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HolySparkProjectile projectile) {
        return PARTICLE_TEXTURE;
    }
}
