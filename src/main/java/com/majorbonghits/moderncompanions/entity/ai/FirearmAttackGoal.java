package com.majorbonghits.moderncompanions.entity.ai;

import com.majorbonghits.moderncompanions.compat.firearms.FirearmSupport;
import com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Uses TacZ's living-entity gun operator for companion firearm combat. */
public final class FirearmAttackGoal extends Goal {
    private static final double MAX_RANGE_SQR = 20.0D * 20.0D;
    private final AbstractHumanCompanionEntity companion;
    private int lastAmmoNotice = -200;

    public FirearmAttackGoal(AbstractHumanCompanionEntity companion) {
        this.companion = companion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return companion.getTarget() != null && companion.getTarget().isAlive()
                && companion.canHarm(companion.getTarget()) && FirearmSupport.equipFirearm(companion)
                && FirearmSupport.isAllowedFirearm(companion, companion.getMainHandItem());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        if (FirearmSupport.isTacZFirearm(companion.getMainHandItem())) FirearmSupport.drawTacZ(companion);
    }

    @Override
    public void tick() {
        LivingEntity target = companion.getTarget();
        if (target == null) return;
        companion.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (companion.distanceToSqr(target) > MAX_RANGE_SQR || !companion.getSensing().hasLineOfSight(target)) {
            companion.getNavigation().moveTo(target, 1.05D);
            return;
        }
        companion.getNavigation().stop();
        aimAt(target);
        FirearmSupport.TacZShotResult result = FirearmSupport.shootTacZ(companion, target);
        if (result == FirearmSupport.TacZShotResult.NO_AMMO) {
            if (FirearmSupport.canReloadTacZ(companion)) {
                FirearmSupport.reloadTacZ(companion);
            } else if (companion.tickCount - lastAmmoNotice >= 200 && companion.getOwner() != null) {
                lastAmmoNotice = companion.tickCount;
                companion.getOwner().sendSystemMessage(Component.translatable("message.modern_companions.needs_firearm_ammo", companion.getName()));
            }
        }
    }

    private void aimAt(LivingEntity target) {
        double dx = target.getX() - companion.getX();
        double dy = target.getEyeY() - companion.getEyeY();
        double dz = target.getZ() - companion.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        companion.setYRot((float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F);
        companion.setXRot((float) -(Mth.atan2(dy, horizontal) * (180.0D / Math.PI)));
        companion.yBodyRot = companion.getYRot();
    }
}
