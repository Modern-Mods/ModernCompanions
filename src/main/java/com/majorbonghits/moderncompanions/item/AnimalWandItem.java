package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;

/** Captures eligible non-hostile mobs into Soul Orbs without losing their entity data. */
public class AnimalWandItem extends Item {
    public AnimalWandItem(Properties properties) {
        super(properties.stacksTo(1).durability(128));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity,
            InteractionHand hand) {
        if (!(entity instanceof Mob mob) || !SoulOrbItem.isEligibleAnimal(mob)
                || Beastmaster.isBeastmasterPet(mob)) {
            return InteractionResult.PASS;
        }
        Level level = entity.level();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack orb = SoulOrbItem.createFromAnimal(mob, ModItems.SOUL_ORB.get());
        if (!player.getInventory().add(orb)) {
            player.drop(orb, false);
        }
        mob.discard();
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.7F, 1.4F);
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                    12, 0.25D, 0.35D, 0.25D, 0.03D);
        }
        return InteractionResult.CONSUME;
    }
}
