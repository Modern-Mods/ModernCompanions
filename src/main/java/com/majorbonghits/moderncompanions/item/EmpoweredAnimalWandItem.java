package com.majorbonghits.moderncompanions.item;

import com.majorbonghits.moderncompanions.core.ModItems;
import com.majorbonghits.moderncompanions.entity.Beastmaster;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Ancient-Debris upgrade that captures hostile mobs into Soul Orbs. */
public class EmpoweredAnimalWandItem extends Item {
    public EmpoweredAnimalWandItem(Properties properties) {
        super(properties.stacksTo(1).durability(128).rarity(Rarity.EPIC));
    }

    /** Uses the vanilla mob XP reward as the difficulty-scaled capture cost. */
    public static int captureExperienceCost(Mob mob, ServerLevel level, Player player) {
        return Math.max(1, mob.getExperienceReward(level, player));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity,
            InteractionHand hand) {
        if (!(entity instanceof Mob mob) || !SoulOrbItem.isEligibleHostile(mob)
                || Beastmaster.isBeastmasterPet(mob)) {
            return InteractionResult.PASS;
        }

        Level level = entity.level();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel server = (ServerLevel) level;
        int inventorySlot = player.getInventory().getFreeSlot();
        if (inventorySlot < 0) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.modern_companions.empowered_wand.needs_inventory"), true);
            return InteractionResult.FAIL;
        }

        int cost = captureExperienceCost(mob, server, player);
        if (!player.getAbilities().instabuild && player.totalExperience < cost) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.modern_companions.empowered_wand.needs_xp", cost), true);
            return InteractionResult.FAIL;
        }

        // Commit storage first; XP and entity removal happen only after the orb is secured.
        ItemStack orb = SoulOrbItem.createFromHostile(mob, ModItems.SOUL_ORB.get());
        player.getInventory().setItem(inventorySlot, orb);
        if (!player.getAbilities().instabuild) {
            player.giveExperiencePoints(-cost);
        }
        mob.discard();

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
        level.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.7F, 1.4F);
        server.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                12, 0.25D, 0.35D, 0.25D, 0.03D);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.modern_companions.empowered_wand.captured", cost), true);
        return InteractionResult.CONSUME;
    }
}
