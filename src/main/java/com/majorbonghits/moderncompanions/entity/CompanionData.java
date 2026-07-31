package com.majorbonghits.moderncompanions.entity;

import com.majorbonghits.moderncompanions.ModernCompanions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * Shared data tables (names, skins, foods) brought forward from the original Companions mod.
 */
public class CompanionData {
    public static final Random rand = new Random();

    public static final Item[] ALL_FOODS = new Item[]{
            Items.COOKIE,
            Items.BREAD,
            Items.MELON_SLICE,
            Items.APPLE,
            Items.SWEET_BERRIES,
            Items.CARROT,
            Items.BAKED_POTATO,
            Items.COOKED_SALMON,
            Items.COOKED_COD,
            Items.COOKED_MUTTON,
            Items.COOKED_PORKCHOP,
            Items.COOKED_BEEF,
            Items.COOKED_CHICKEN,
            Items.PUMPKIN_PIE,
            Items.GLOW_BERRIES,
            Items.POTATO,
            Items.BEETROOT,
            Items.DRIED_KELP,
            Items.COOKED_RABBIT
    };

    /** Higher-tier foods/drinks companions can consume for healing but will not request while taming. */
    public static final Item[] EXTRA_HEAL_CONSUMABLES = new Item[]{
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_CARROT,
            Items.HONEY_BOTTLE,
            Items.MUSHROOM_STEW,
            Items.BEETROOT_SOUP,
            Items.RABBIT_STEW
    };

    /** Resource tiers used for the second taming requirement. */
    public static final Item[] COMMON_RESOURCE_ITEMS = new Item[]{
            Items.COAL,
            Items.CHARCOAL,
            Items.COPPER_INGOT,
            Items.IRON_INGOT,
            Items.REDSTONE,
            Items.LAPIS_LAZULI,
            Items.FLINT,
            Items.CLAY_BALL,
            Items.STRING,
            Items.LEATHER,
            Items.BONE,
            Items.FEATHER,
    };

    public static final Item[] UNCOMMON_RESOURCE_ITEMS = new Item[]{
            Items.GOLD_INGOT,
            Items.AMETHYST_SHARD,
            Items.SLIME_BALL,
            Items.GUNPOWDER,
            Items.GLOWSTONE_DUST,
            Items.PRISMARINE_SHARD,
            Items.PRISMARINE_CRYSTALS,
            Items.ENDER_PEARL,
            Items.OBSIDIAN,
    };

    public static final Item[] RARE_RESOURCE_ITEMS = new Item[]{
            Items.DIAMOND,
            Items.EMERALD,
            Items.BLAZE_ROD,
            Items.MAGMA_CREAM,
    };

    private static final String REACHED_NETHER = "ModernCompanionsReachedNether";
    private static final String REACHED_OCEAN = "ModernCompanionsReachedOcean";

    /** Records progression once so returning to the Overworld does not reset unlocked materials. */
    public static void updateResourceProgress(Player player) {
        var data = player.getPersistentData();
        if (!data.getBoolean(REACHED_NETHER) && player.level().dimension() == Level.NETHER) {
            data.putBoolean(REACHED_NETHER, true);
        }
        if (!data.getBoolean(REACHED_OCEAN) && player.level().getBiome(player.blockPosition()).is(BiomeTags.IS_OCEAN)) {
            data.putBoolean(REACHED_OCEAN, true);
        }
    }

    private static final Set<Item> DISALLOWED_FOODS = Set.of(
            // Harmful or unpredictable
            Items.SPIDER_EYE,
            Items.ROTTEN_FLESH,
            Items.POISONOUS_POTATO,
            Items.PUFFERFISH,
            Items.SUSPICIOUS_STEW,
            Items.CHORUS_FRUIT,

            // Raw meat
            Items.BEEF,
            Items.PORKCHOP,
            Items.CHICKEN,
            Items.MUTTON,
            Items.RABBIT,

            // Raw fish
            Items.COD,
            Items.SALMON,
            Items.TROPICAL_FISH
    );

     public static final MutableComponent[] tameFail = new MutableComponent[]{
        Component.translatable("dialogue.modern_companions.tame_fail.0"),
        Component.translatable("dialogue.modern_companions.tame_fail.1"),
        Component.translatable("dialogue.modern_companions.tame_fail.2"),
        Component.translatable("dialogue.modern_companions.tame_fail.3"),
        Component.translatable("dialogue.modern_companions.tame_fail.4"),
        Component.translatable("dialogue.modern_companions.tame_fail.5"),
        Component.translatable("dialogue.modern_companions.tame_fail.6"),
        Component.translatable("dialogue.modern_companions.tame_fail.7"),
        Component.translatable("dialogue.modern_companions.tame_fail.8"),
        Component.translatable("dialogue.modern_companions.tame_fail.9"),
        Component.translatable("dialogue.modern_companions.tame_fail.10"),
        Component.translatable("dialogue.modern_companions.tame_fail.11"),
        Component.translatable("dialogue.modern_companions.tame_fail.12"),
        Component.translatable("dialogue.modern_companions.tame_fail.13"),
        Component.translatable("dialogue.modern_companions.tame_fail.14"),
        Component.translatable("dialogue.modern_companions.tame_fail.15"),
        Component.translatable("dialogue.modern_companions.tame_fail.16"),
        Component.translatable("dialogue.modern_companions.tame_fail.17"),
        Component.translatable("dialogue.modern_companions.tame_fail.18"),
        Component.translatable("dialogue.modern_companions.tame_fail.19"),
        Component.translatable("dialogue.modern_companions.tame_fail.20"),
        Component.translatable("dialogue.modern_companions.tame_fail.21"),
        Component.translatable("dialogue.modern_companions.tame_fail.22"),
        Component.translatable("dialogue.modern_companions.tame_fail.23"),
        Component.translatable("dialogue.modern_companions.tame_fail.24"),
        Component.translatable("dialogue.modern_companions.tame_fail.25"),
        Component.translatable("dialogue.modern_companions.tame_fail.26"),
        Component.translatable("dialogue.modern_companions.tame_fail.27"),
        Component.translatable("dialogue.modern_companions.tame_fail.28"),
        Component.translatable("dialogue.modern_companions.tame_fail.29")
    };

    public static final MutableComponent[] notTamed = new MutableComponent[]{
            Component.translatable("dialogue.modern_companions.not_tamed.0"),
            Component.translatable("dialogue.modern_companions.not_tamed.1"),
            Component.translatable("dialogue.modern_companions.not_tamed.2"),
            Component.translatable("dialogue.modern_companions.not_tamed.3"),
            Component.translatable("dialogue.modern_companions.not_tamed.4"),
            Component.translatable("dialogue.modern_companions.not_tamed.5"),
            Component.translatable("dialogue.modern_companions.not_tamed.6"),
            Component.translatable("dialogue.modern_companions.not_tamed.7"),
            Component.translatable("dialogue.modern_companions.not_tamed.8"),
            Component.translatable("dialogue.modern_companions.not_tamed.9"),
            Component.translatable("dialogue.modern_companions.not_tamed.10"),
            Component.translatable("dialogue.modern_companions.not_tamed.11"),
            Component.translatable("dialogue.modern_companions.not_tamed.12"),
            Component.translatable("dialogue.modern_companions.not_tamed.13"),
            Component.translatable("dialogue.modern_companions.not_tamed.14"),
            Component.translatable("dialogue.modern_companions.not_tamed.15"),
            Component.translatable("dialogue.modern_companions.not_tamed.16"),
            Component.translatable("dialogue.modern_companions.not_tamed.17"),
            Component.translatable("dialogue.modern_companions.not_tamed.18"),
            Component.translatable("dialogue.modern_companions.not_tamed.19"),
            Component.translatable("dialogue.modern_companions.not_tamed.20"),
            Component.translatable("dialogue.modern_companions.not_tamed.21"),
            Component.translatable("dialogue.modern_companions.not_tamed.22"),
            Component.translatable("dialogue.modern_companions.not_tamed.23"),
            Component.translatable("dialogue.modern_companions.not_tamed.24"),
            Component.translatable("dialogue.modern_companions.not_tamed.25"),
            Component.translatable("dialogue.modern_companions.not_tamed.26"),
            Component.translatable("dialogue.modern_companions.not_tamed.27"),
            Component.translatable("dialogue.modern_companions.not_tamed.28"),
            Component.translatable("dialogue.modern_companions.not_tamed.29")
    };

    public static final MutableComponent[] WRONG_FOOD = new MutableComponent[]{
            Component.translatable("dialogue.modern_companions.wrong_food.0"),
            Component.translatable("dialogue.modern_companions.wrong_food.1"),
            Component.translatable("dialogue.modern_companions.wrong_food.2"),
            Component.translatable("dialogue.modern_companions.wrong_food.3"),
            Component.translatable("dialogue.modern_companions.wrong_food.4"),
            Component.translatable("dialogue.modern_companions.wrong_food.5"),
            Component.translatable("dialogue.modern_companions.wrong_food.6"),
            Component.translatable("dialogue.modern_companions.wrong_food.7"),
            Component.translatable("dialogue.modern_companions.wrong_food.8"),
            Component.translatable("dialogue.modern_companions.wrong_food.9"),
            Component.translatable("dialogue.modern_companions.wrong_food.10"),
            Component.translatable("dialogue.modern_companions.wrong_food.11"),
            Component.translatable("dialogue.modern_companions.wrong_food.12"),
            Component.translatable("dialogue.modern_companions.wrong_food.13"),
            Component.translatable("dialogue.modern_companions.wrong_food.14"),
            Component.translatable("dialogue.modern_companions.wrong_food.15"),
            Component.translatable("dialogue.modern_companions.wrong_food.16"),
            Component.translatable("dialogue.modern_companions.wrong_food.17"),
            Component.translatable("dialogue.modern_companions.wrong_food.18"),
            Component.translatable("dialogue.modern_companions.wrong_food.19"),
            Component.translatable("dialogue.modern_companions.wrong_food.20"),
            Component.translatable("dialogue.modern_companions.wrong_food.21"),
            Component.translatable("dialogue.modern_companions.wrong_food.22"),
            Component.translatable("dialogue.modern_companions.wrong_food.23"),
            Component.translatable("dialogue.modern_companions.wrong_food.24"),
            Component.translatable("dialogue.modern_companions.wrong_food.25"),
            Component.translatable("dialogue.modern_companions.wrong_food.26"),
            Component.translatable("dialogue.modern_companions.wrong_food.27"),
            Component.translatable("dialogue.modern_companions.wrong_food.28"),
            Component.translatable("dialogue.modern_companions.wrong_food.29")
    };

    public static final MutableComponent[] ENOUGH_FOOD = new MutableComponent[]{
            Component.translatable("dialogue.modern_companions.enough_food.0"),
            Component.translatable("dialogue.modern_companions.enough_food.1"),
            Component.translatable("dialogue.modern_companions.enough_food.2"),
            Component.translatable("dialogue.modern_companions.enough_food.3"),
            Component.translatable("dialogue.modern_companions.enough_food.4"),
            Component.translatable("dialogue.modern_companions.enough_food.5"),
            Component.translatable("dialogue.modern_companions.enough_food.6"),
            Component.translatable("dialogue.modern_companions.enough_food.7"),
            Component.translatable("dialogue.modern_companions.enough_food.8"),
            Component.translatable("dialogue.modern_companions.enough_food.9"),
            Component.translatable("dialogue.modern_companions.enough_food.10"),
            Component.translatable("dialogue.modern_companions.enough_food.11"),
            Component.translatable("dialogue.modern_companions.enough_food.12"),
            Component.translatable("dialogue.modern_companions.enough_food.13"),
            Component.translatable("dialogue.modern_companions.enough_food.14"),
            Component.translatable("dialogue.modern_companions.enough_food.15"),
            Component.translatable("dialogue.modern_companions.enough_food.16"),
            Component.translatable("dialogue.modern_companions.enough_food.17"),
            Component.translatable("dialogue.modern_companions.enough_food.18"),
            Component.translatable("dialogue.modern_companions.enough_food.19"),
            Component.translatable("dialogue.modern_companions.enough_food.20"),
            Component.translatable("dialogue.modern_companions.enough_food.21"),
            Component.translatable("dialogue.modern_companions.enough_food.22"),
            Component.translatable("dialogue.modern_companions.enough_food.23"),
            Component.translatable("dialogue.modern_companions.enough_food.24"),
            Component.translatable("dialogue.modern_companions.enough_food.25"),
            Component.translatable("dialogue.modern_companions.enough_food.26"),
            Component.translatable("dialogue.modern_companions.enough_food.27"),
            Component.translatable("dialogue.modern_companions.enough_food.28"),
            Component.translatable("dialogue.modern_companions.enough_food.29")
    };

    public static final Class<?>[] alertMobs = new Class<?>[]{
            Blaze.class,
            Breeze.class,
            EnderMan.class,
            Endermite.class,
            Ghast.class,
            Giant.class,
            Guardian.class,
            Hoglin.class,
            MagmaCube.class,
            Phantom.class,
            Shulker.class,
            Silverfish.class,
            Slime.class,
            Spider.class,
            Vex.class,
            AbstractSkeleton.class,
            Zoglin.class,
            Zombie.class,
            AbstractPiglin.class,
            ZombifiedPiglin.class,
            WitherBoss.class,
            EnderDragon.class,
            Warden.class,
            Raider.class
    };

    public static final Class<?>[] huntMobs = new Class<?>[]{
        Chicken.class,
        Cow.class,
        Pig.class,
        Rabbit.class,
        Sheep.class,
        Goat.class
    };

    // Male (0) / female (1) skins. Every entry mirrors a bundled 64x64 texture.
    public static final ResourceLocation[][] skins = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/male/medieval-man-hugh.png"),
                    tex("textures/entities/male/alexandros.png"),
                    tex("textures/entities/male/cyrus.png"),
                    tex("textures/entities/male/diokles.png"),
                    tex("textures/entities/male/dion.png"),
                    tex("textures/entities/male/georgios.png"),
                    tex("textures/entities/male/ioannis.png"),
                    tex("textures/entities/male/medieval-peasant-schwaechlich.png"),
                    tex("textures/entities/male/medieval-peasant-without-vest.png"),
                    tex("textures/entities/male/medieval-peasant-with-vest-on.png"),
                    tex("textures/entities/male/panos.png"),
                    tex("textures/entities/male/viking-blue-tunic.png"),
                    tex("textures/entities/male/cronos-jojo.png"),
                    tex("textures/entities/male/medieval-man-alard.png"),
                    tex("textures/entities/male/peasant-ginger.png"),
                    tex("textures/entities/male/townsman-green-tunic.png"),
                    tex("textures/entities/male/polish-farmer.png"),
                    tex("textures/entities/male/peasant.png"),
                    tex("textures/entities/male/rustic-farmer.png"),
                    tex("textures/entities/male/medieval-villager.png"),
                    tex("textures/entities/male/anglobodyguard.png"),
                    tex("textures/entities/male/anglobodyguard2.png"),
                    tex("textures/entities/male/anglowarrior.png"),
                    tex("textures/entities/male/byzantinecataphract1.png"),
                    tex("textures/entities/male/byzantinecataphract2.png"),
                    tex("textures/entities/male/byzantinecommander1.png"),
                    tex("textures/entities/male/byzantineimperialguard.png"),
                    tex("textures/entities/male/byzantineimperialguard2.png"),
                    tex("textures/entities/male/byzantinelegionary1.png"),
                    tex("textures/entities/male/byzantinelegionary2.png"),
                    tex("textures/entities/male/byzantinelegionary3.png"),
                    tex("textures/entities/male/byzantinesoldier1.png"),
                    tex("textures/entities/male/byzantinesoldier2.png"),
                    tex("textures/entities/male/byzantinesoldier3.png"),
                    tex("textures/entities/male/byzantinesoldier4.png"),
                    tex("textures/entities/male/champion.png"),
                    tex("textures/entities/male/chinese-dressed-man-by-shady-warlock.png"),
                    tex("textures/entities/male/danisharcher.png"),
                    tex("textures/entities/male/danishbodyguard.png"),
                    tex("textures/entities/male/danishbodyguard2.png"),
                    tex("textures/entities/male/danishveteranwarrior.png"),
                    tex("textures/entities/male/danishwarrior.png"),
                    tex("textures/entities/male/desertmercenary.png"),
                    tex("textures/entities/male/earlyromansoldier.png"),
                    tex("textures/entities/male/earlyromansoldier2.png"),
                    tex("textures/entities/male/easterlingarcher.png"),
                    tex("textures/entities/male/easterlingeliteguard.png"),
                    tex("textures/entities/male/easterlingmameluke1.png"),
                    tex("textures/entities/male/easterlingmameluke2.png"),
                    tex("textures/entities/male/easterlingmameluke3.png"),
                    tex("textures/entities/male/easterlingmerchant.png"),
                    tex("textures/entities/male/easterlingslavegirl.png"),
                    tex("textures/entities/male/easterlingtrader.png"),
                    tex("textures/entities/male/easterlingveteran.png"),
                    tex("textures/entities/male/easterlingveteran2.png"),
                    tex("textures/entities/male/easterlingwarrior.png"),
                    tex("textures/entities/male/easterlingwarrior2.png"),
                    tex("textures/entities/male/easterlingwarrior3.png"),
                    tex("textures/entities/male/frankisharcher.png"),
                    tex("textures/entities/male/frankishbodyguard.png"),
                    tex("textures/entities/male/frankishsoldier.png"),
                    tex("textures/entities/male/frisiiwarrior.png"),
                    tex("textures/entities/male/frisiiwarrior2.png"),
                    tex("textures/entities/male/geatishveteranwarrior.png"),
                    tex("textures/entities/male/geatishwarrior.png"),
                    tex("textures/entities/male/geatishwarrior2.png"),
                    tex("textures/entities/male/germanicarcher.png"),
                    tex("textures/entities/male/germanicbodyguard.png"),
                    tex("textures/entities/male/germanicbodyguard2.png"),
                    tex("textures/entities/male/germanicbodyguard3.png"),
                    tex("textures/entities/male/germanicbodyguard4.png"),
                    tex("textures/entities/male/germanicbodyguard5.png"),
                    tex("textures/entities/male/germanicfarmer.png"),
                    tex("textures/entities/male/germanicskirmisher.png"),
                    tex("textures/entities/male/germanicskirmisher2.png"),
                    tex("textures/entities/male/germanicveteranwarrior.png"),
                    tex("textures/entities/male/germanicveteranwarrior2.png"),
                    tex("textures/entities/male/germanicwarrior.png"),
                    tex("textures/entities/male/germanicwarrior2.png"),
                    tex("textures/entities/male/germanicwarrior3.png"),
                    tex("textures/entities/male/germanicwarrior4.png"),
                    tex("textures/entities/male/germanicwolfwarrior.png"),
                    tex("textures/entities/male/jutishwarrior.png"),
                    tex("textures/entities/male/king-by-chabilulu.png"),
                    tex("textures/entities/male/knight-by-a1yssa.png"),
                    tex("textures/entities/male/knight-by-dinowcookie.png"),
                    tex("textures/entities/male/knight-by-hatsy.png"),
                    tex("textures/entities/male/knight-by-romto.png"),
                    tex("textures/entities/male/knight-by-sunnyfeather.png"),
                    tex("textures/entities/male/langobardfarmowner.png"),
                    tex("textures/entities/male/levy1.png"),
                    tex("textures/entities/male/levy2.png"),
                    tex("textures/entities/male/levy3.png"),
                    tex("textures/entities/male/macedonianhoplite.png"),
                    tex("textures/entities/male/macedonianhoplite2.png"),
                    tex("textures/entities/male/macedonianhoplite4.png"),
                    tex("textures/entities/male/macedonianhoplite5.png"),
                    tex("textures/entities/male/macedoniansoldier.png"),
                    tex("textures/entities/male/macedoniansoldier2.png"),
                    tex("textures/entities/male/macedonianwarrior.png"),
                    tex("textures/entities/male/maerck.png"),
                    tex("textures/entities/male/maerck2.png"),
                    tex("textures/entities/male/maerck3.png"),
                    tex("textures/entities/male/medievalinfantryman.png"),
                    tex("textures/entities/male/nordicbard.png"),
                    tex("textures/entities/male/nordicbard2.png"),
                    tex("textures/entities/male/nordicchild1.png"),
                    tex("textures/entities/male/nordicelitewarrior.png"),
                    tex("textures/entities/male/nordicelitewarrior2.png"),
                    tex("textures/entities/male/nordicfreemen1.png"),
                    tex("textures/entities/male/nordicfreemen10.png"),
                    tex("textures/entities/male/nordicfreemen11.png"),
                    tex("textures/entities/male/nordicfreemen12.png"),
                    tex("textures/entities/male/nordicfreemen2.png"),
                    tex("textures/entities/male/nordicfreemen3.png"),
                    tex("textures/entities/male/nordicfreemen4.png"),
                    tex("textures/entities/male/nordicfreemen5.png"),
                    tex("textures/entities/male/nordicfreemen6.png"),
                    tex("textures/entities/male/nordicfreemen7.png"),
                    tex("textures/entities/male/nordicfreemen8.png"),
                    tex("textures/entities/male/nordicfreemen9.png"),
                    tex("textures/entities/male/nordicking1.png"),
                    tex("textures/entities/male/nordicking2.png"),
                    tex("textures/entities/male/nordicking3.png"),
                    tex("textures/entities/male/nordickingwitharmor1.png"),
                    tex("textures/entities/male/nordicnobleman.png"),
                    tex("textures/entities/male/nordicnobleman10.png"),
                    tex("textures/entities/male/nordicnobleman2.png"),
                    tex("textures/entities/male/nordicnobleman3.png"),
                    tex("textures/entities/male/nordicnobleman4.png"),
                    tex("textures/entities/male/nordicnobleman5.png"),
                    tex("textures/entities/male/nordicnobleman6.png"),
                    tex("textures/entities/male/nordicnobleman7.png"),
                    tex("textures/entities/male/nordicnobleman8.png"),
                    tex("textures/entities/male/nordicnobleman9.png"),
                    tex("textures/entities/male/nordicpriest.png"),
                    tex("textures/entities/male/nordicsmith.png"),
                    tex("textures/entities/male/nordicsmith2.png"),
                    tex("textures/entities/male/nordictrader.png"),
                    tex("textures/entities/male/nordictrader2.png"),
                    tex("textures/entities/male/nordicvillageelder.png"),
                    tex("textures/entities/male/norsetownsmen1.png"),
                    tex("textures/entities/male/norsetownsmen2.png"),
                    tex("textures/entities/male/norsetownsmen3.png"),
                    tex("textures/entities/male/norsetownsmen4.png"),
                    tex("textures/entities/male/norsetownsmen5.png"),
                    tex("textures/entities/male/norsewarrior1.png"),
                    tex("textures/entities/male/oldnorseman1.png"),
                    tex("textures/entities/male/oldnorsewarrior1.png"),
                    tex("textures/entities/male/oldsaxontrader.png"),
                    tex("textures/entities/male/outlaw.png"),
                    tex("textures/entities/male/pillager-male-by-the-muzik.png"),
                    tex("textures/entities/male/poacher.png"),
                    tex("textures/entities/male/priest.png"),
                    tex("textures/entities/male/raider.png"),
                    tex("textures/entities/male/richtraveller.png"),
                    tex("textures/entities/male/saxonarcher.png"),
                    tex("textures/entities/male/saxonbodyguard.png"),
                    tex("textures/entities/male/saxonnoblemen.png"),
                    tex("textures/entities/male/saxonveteranwarrior.png"),
                    tex("textures/entities/male/saxonwarrior.png"),
                    tex("textures/entities/male/saxonwarrior2.png"),
                    tex("textures/entities/male/southerncivilian1.png"),
                    tex("textures/entities/male/southerncivilian10.png"),
                    tex("textures/entities/male/southerncivilian11.png"),
                    tex("textures/entities/male/southerncivilian12.png"),
                    tex("textures/entities/male/southerncivilian13.png"),
                    tex("textures/entities/male/southerncivilian14.png"),
                    tex("textures/entities/male/southerncivilian15.png"),
                    tex("textures/entities/male/southerncivilian16.png"),
                    tex("textures/entities/male/southerncivilian2.png"),
                    tex("textures/entities/male/southerncivilian3.png"),
                    tex("textures/entities/male/southerncivilian4.png"),
                    tex("textures/entities/male/southerncivilian5.png"),
                    tex("textures/entities/male/southerncivilian6.png"),
                    tex("textures/entities/male/southerncivilian7.png"),
                    tex("textures/entities/male/southerncivilian8.png"),
                    tex("textures/entities/male/southerncivilian9.png"),
                    tex("textures/entities/male/southernnobleman1.png"),
                    tex("textures/entities/male/southernnobleman2.png"),
                    tex("textures/entities/male/southernnobleman3.png"),
                    tex("textures/entities/male/southernnobleman4.png"),
                    tex("textures/entities/male/southernrobber.png"),
                    tex("textures/entities/male/steppearcher.png"),
                    tex("textures/entities/male/steppearcher2.png"),
                    tex("textures/entities/male/steppeelitewarrior.png"),
                    tex("textures/entities/male/steppeelitewarrior2.png"),
                    tex("textures/entities/male/steppewarrior1.png"),
                    tex("textures/entities/male/steppewarrior2.png"),
                    tex("textures/entities/male/steppewarrior3.png"),
                    tex("textures/entities/male/steppewarrior4.png"),
                    tex("textures/entities/male/sturgiawarrior1.png"),
                    tex("textures/entities/male/sturgiawarrior2.png"),
                    tex("textures/entities/male/sveawarrior.png"),
                    tex("textures/entities/male/traveller.png"),
                    tex("textures/entities/male/ulfhedinn.png"),
                    tex("textures/entities/male/warlock-by-shady-warlock.png"),
                    tex("textures/entities/male/youngwarrior.png"),
                    tex("textures/entities/male/youngwarrior1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/female/a-rogue-i-guess.png"),
                    tex("textures/entities/female/deidre-gramville.png"),
                    tex("textures/entities/female/deidre-gramville2.png"),
                    tex("textures/entities/female/eleora-halle.png"),
                    tex("textures/entities/female/fantastic-blue.png"),
                    tex("textures/entities/female/ftu-emma.png"),
                    tex("textures/entities/female/girl-medieval-peasant.png"),
                    tex("textures/entities/female/medieval-barmaid.png"),
                    tex("textures/entities/female/runaway.png"),
                    tex("textures/entities/female/shannon-flux.png"),
                    tex("textures/entities/female/the-traveller.png"),
                    tex("textures/entities/female/x-ayesha.png"),
                    tex("textures/entities/female/berry-farmer-female-by-iisweetstrawberry.png"),
                    tex("textures/entities/female/chinese-dressed-woman-by-scarletbox.png"),
                    tex("textures/entities/female/elfprincess.png"),
                    tex("textures/entities/female/elfprincess2.png"),
                    tex("textures/entities/female/nordicfreewoman1.png"),
                    tex("textures/entities/female/nordicfreewoman2.png"),
                    tex("textures/entities/female/nordicfreewoman3.png"),
                    tex("textures/entities/female/nordicfreewoman4.png"),
                    tex("textures/entities/female/nordicfreewoman5.png"),
                    tex("textures/entities/female/nordicfreewoman6.png"),
                    tex("textures/entities/female/nordicfreewoman7.png"),
                    tex("textures/entities/female/nordicfreewoman8.png"),
                    tex("textures/entities/female/nordicnoblewoman.png"),
                    tex("textures/entities/female/nordicqueen1.png"),
                    tex("textures/entities/female/nordicslavegirl.png"),
                    tex("textures/entities/female/nordicwarriorgirl.png"),
                    tex("textures/entities/female/nordicwarriorgirl2.png"),
                    tex("textures/entities/female/oldlady.png"),
                    tex("textures/entities/female/oldlady2.png"),
                    tex("textures/entities/female/pillager-female-by-shady-warlock.png"),
                    tex("textures/entities/female/queen-by-bowman.png"),
                    tex("textures/entities/female/snowprincess.png"),
                    tex("textures/entities/female/southerncivilianfemale1.png"),
                    tex("textures/entities/female/southerncivilianfemale10.png"),
                    tex("textures/entities/female/southerncivilianfemale11.png"),
                    tex("textures/entities/female/southerncivilianfemale2.png"),
                    tex("textures/entities/female/southerncivilianfemale3.png"),
                    tex("textures/entities/female/southerncivilianfemale4.png"),
                    tex("textures/entities/female/southerncivilianfemale5.png"),
                    tex("textures/entities/female/southerncivilianfemale6.png"),
                    tex("textures/entities/female/southerncivilianfemale7.png"),
                    tex("textures/entities/female/southerncivilianfemale8.png"),
                    tex("textures/entities/female/southerncivilianfemale9.png"),
                    tex("textures/entities/female/witch-by-strawberry.png"),
                    tex("textures/entities/female/girl1.png"),
                    tex("textures/entities/female/girl10.png"),
                    tex("textures/entities/female/girl11.png"),
                    tex("textures/entities/female/girl12.png"),
                    tex("textures/entities/female/girl13.png"),
                    tex("textures/entities/female/girl14.png"),
                    tex("textures/entities/female/girl15.png"),
                    tex("textures/entities/female/girl16.png"),
                    tex("textures/entities/female/girl17.png"),
                    tex("textures/entities/female/girl18.png"),
                    tex("textures/entities/female/girl19.png"),
                    tex("textures/entities/female/girl2.png"),
                    tex("textures/entities/female/girl20.png"),
                    tex("textures/entities/female/girl21.png"),
                    tex("textures/entities/female/girl22.png"),
                    tex("textures/entities/female/girl23.png"),
                    tex("textures/entities/female/girl24.png"),
                    tex("textures/entities/female/girl25.png"),
                    tex("textures/entities/female/girl26.png"),
                    tex("textures/entities/female/girl27.png"),
                    tex("textures/entities/female/girl28.png"),
                    tex("textures/entities/female/girl29.png"),
                    tex("textures/entities/female/girl3.png"),
                    tex("textures/entities/female/girl30.png"),
                    tex("textures/entities/female/girl31.png"),
                    tex("textures/entities/female/girl32.png"),
                    tex("textures/entities/female/girl33.png"),
                    tex("textures/entities/female/girl34.png"),
                    tex("textures/entities/female/girl35.png"),
                    tex("textures/entities/female/girl36.png"),
                    tex("textures/entities/female/girl37.png"),
                    tex("textures/entities/female/girl38.png"),
                    tex("textures/entities/female/girl39.png"),
                    tex("textures/entities/female/girl4.png"),
                    tex("textures/entities/female/girl40.png"),
                    tex("textures/entities/female/girl41.png"),
                    tex("textures/entities/female/girl42.png"),
                    tex("textures/entities/female/girl43.png"),
                    tex("textures/entities/female/girl44.png"),
                    tex("textures/entities/female/girl45.png"),
                    tex("textures/entities/female/girl46.png"),
                    tex("textures/entities/female/girl47.png"),
                    tex("textures/entities/female/girl48.png"),
                    tex("textures/entities/female/girl49.png"),
                    tex("textures/entities/female/girl5.png"),
                    tex("textures/entities/female/girl50.png"),
                    tex("textures/entities/female/girl6.png"),
                    tex("textures/entities/female/girl7.png"),
                    tex("textures/entities/female/girl8.png"),
                    tex("textures/entities/female/girl9.png")
            }
    };

    public static final ResourceLocation[][] maleArmor = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/armor/chainmail_arms_layer_2.png"),
                    tex("textures/entities/armor/chainmail_arms_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/iron_arms_layer_2.png"),
                    tex("textures/entities/armor/iron_arms_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/steel_layer_2.png"),
                    tex("textures/entities/armor/steel_layer_1.png")
            }
    };

    public static final ResourceLocation[][] femaleArmor = new ResourceLocation[][]{
            new ResourceLocation[]{
                    tex("textures/entities/armor/chainmail_layer_2.png"),
                    tex("textures/entities/armor/chainmail_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/iron_layer_2.png"),
                    tex("textures/entities/armor/iron_layer_1.png")
            },
            new ResourceLocation[]{
                    tex("textures/entities/armor/steel_layer_2.png"),
                    tex("textures/entities/armor/steel_layer_1.png")
            }
    };

    public static int getHealthModifier() {
        float healthFloat = rand.nextFloat();
        if (healthFloat <= 0.03) return -4;
        if (healthFloat <= 0.1) return -3;
        if (healthFloat <= 0.2) return -2;
        if (healthFloat <= 0.35) return -1;
        if (healthFloat <= 0.65) return 0;
        if (healthFloat <= 0.8) return 1;
        if (healthFloat <= 0.9) return 2;
        if (healthFloat <= 0.97) return 3;
        return 4;
    }

    public static ItemStack getSpawnArmor(EquipmentSlot armorType) {
        float materialFloat = rand.nextFloat();
        if (materialFloat <= 0.40F) {
            return ItemStack.EMPTY;
        } else if (materialFloat <= 0.70F) {
            return switch (armorType) {
                case HEAD -> Items.LEATHER_HELMET.getDefaultInstance();
                case CHEST -> Items.LEATHER_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.LEATHER_LEGGINGS.getDefaultInstance();
                case FEET -> Items.LEATHER_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        } else if (materialFloat <= 0.90F) {
            return switch (armorType) {
                case HEAD -> Items.CHAINMAIL_HELMET.getDefaultInstance();
                case CHEST -> Items.CHAINMAIL_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.CHAINMAIL_LEGGINGS.getDefaultInstance();
                case FEET -> Items.CHAINMAIL_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        } else {
            return switch (armorType) {
                case HEAD -> Items.IRON_HELMET.getDefaultInstance();
                case CHEST -> Items.IRON_CHESTPLATE.getDefaultInstance();
                case LEGS -> Items.IRON_LEGGINGS.getDefaultInstance();
                case FEET -> Items.IRON_BOOTS.getDefaultInstance();
                default -> ItemStack.EMPTY;
            };
        }
    }

    public static String getRandomName(int sex) {
        String firstName = firstNames[sex][rand.nextInt(firstNames[sex].length)];
        String lastName = lastNames[rand.nextInt(lastNames.length)];
        return firstName + " " + lastName;
    }

    public static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    public static boolean isArmorSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return isArmorSlot(armor.getEquipmentSlot());
        }
        return false;
    }

    public static boolean isBetterArmor(ItemStack candidate, ItemStack current) {
        if (!(candidate.getItem() instanceof ArmorItem candArmor) || !(current.getItem() instanceof ArmorItem curArmor)) {
            return current.isEmpty();
        }
        if (candArmor.getEquipmentSlot() != curArmor.getEquipmentSlot()) {
            return current.isEmpty();
        }
        if (candArmor.getMaterial() == ArmorMaterials.NETHERITE && curArmor.getMaterial() != ArmorMaterials.NETHERITE) {
            return true;
        }
        return candArmor.getDefense() > curArmor.getDefense();
    }

    public static Map<Item, Integer> getRandomFoodRequirement(Random random) {
        return getRandomFoodRequirement(random, false, false);
    }

    public static Map<Item, Integer> getRandomFoodRequirement(Random random, Player player) {
        updateResourceProgress(player);
        var data = player.getPersistentData();
        return getRandomFoodRequirement(random, data.getBoolean(REACHED_NETHER), data.getBoolean(REACHED_OCEAN));
    }

    private static Map<Item, Integer> getRandomFoodRequirement(Random random, boolean reachedNether, boolean reachedOcean) {
        Map<Item, Integer> food = new HashMap<>();
        Item foodItem = pickAllowedFood(random);
        Item resourceItem = pickResource(random, reachedNether, reachedOcean);
        // 2–5 food, 2–6 resource
        food.put(foodItem, random.nextInt(4) + 2);
        food.put(resourceItem, random.nextInt(5) + 2);
        return food;
    }

    public static boolean isFood(ItemStack stack) {
        Item item = stack.getItem();
        if (DISALLOWED_FOODS.contains(item)) return false;
        for (Item food : ALL_FOODS) {
            if (food.equals(item)) return true;
        }
        for (Item bonus : EXTRA_HEAL_CONSUMABLES) {
            if (bonus.equals(item)) return true;
        }
        return isHealingPotion(stack);
    }

    /** Allow only regen/healing potions (no splash/harmful mixes) as valid consumables. */
    public static boolean isHealingPotion(ItemStack stack) {
        if (!(stack.getItem() instanceof PotionItem)) return false;

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean hasHealingEffect = false;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                return false;
            }
            if (effect.getEffect().is(MobEffects.HEAL) || effect.getEffect().is(MobEffects.REGENERATION)) {
                hasHealingEffect = true;
            }
        }
        return hasHealingEffect;
    }

    private static Item pickAllowedFood(Random random) {
        Item candidate;
        do {
            candidate = ALL_FOODS[random.nextInt(ALL_FOODS.length)];
        } while (DISALLOWED_FOODS.contains(candidate));
        return candidate;
    }

    static Item pickResource(Random random, boolean reachedNether, boolean reachedOcean) {
        Item[] tier = random.nextFloat() < 0.70F ? COMMON_RESOURCE_ITEMS
                : random.nextFloat() < (0.25F / 0.30F) ? UNCOMMON_RESOURCE_ITEMS : RARE_RESOURCE_ITEMS;
        int available = 0;
        for (Item item : tier) {
            if (isResourceAvailable(item, reachedNether, reachedOcean)) available++;
        }
        int selected = random.nextInt(available);
        for (Item item : tier) {
            if (isResourceAvailable(item, reachedNether, reachedOcean) && selected-- == 0) return item;
        }
        throw new IllegalStateException("Resource tier unexpectedly had no available items");
    }

    private static boolean isResourceAvailable(Item item, boolean reachedNether, boolean reachedOcean) {
        if (!reachedNether && (item == Items.QUARTZ || item == Items.GLOWSTONE_DUST
                || item == Items.BLAZE_ROD || item == Items.MAGMA_CREAM)) return false;
        return reachedOcean || (item != Items.PRISMARINE_SHARD && item != Items.PRISMARINE_CRYSTALS);
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path);
    }

    // Humanoid companion names
    // male=0, female=1
    public static final String[][] firstNames = new String[][]{
            new String[]{
                    // Modern and common human names.
                    "Aaron", "Abel", "Abraham", "Adam", "Adrian", "Aidan", "Aiden", "Albert",
                    "Alfred", "Andrew", "Anthony", "Arthur", "Asher", "Austin", "Barrett", "Barry",
                    "Beau", "Benjamin", "Blake", "Bobby", "Brad", "Bradley", "Brandon", "Brent",
                    "Brett", "Brian", "Brody", "Bryan", "Caleb", "Calvin", "Cameron", "Carl",
                    "Carlos", "Casey", "Carter", "Cedric", "Chad", "Charles", "Charlie", "Christian",
                    "Christopher", "Clark", "Clayton", "Clifford", "Cody", "Colby", "Cole", "Colin",
                    "Collin", "Connor", "Conrad", "Corey", "Craig", "Damian", "Damien", "Damon",
                    "Daniel", "Darren", "Darryl", "David", "Dean", "Declan", "Dennis", "Derek",
                    "Derrick", "Desmond", "Devin", "Diego", "Dominic", "Donald", "Donovan", "Douglas",
                    "Drew", "Dustin", "Dylan", "Edward", "Edwin", "Eli", "Elias", "Elijah",
                    "Elliot", "Elliott", "Ethan", "Eugene", "Evan", "Everett", "Felix", "Fernando",
                    "Finley", "Finn", "Francis", "Francisco", "Frank", "Franklin", "Gabriel", "Gage",
                    "Gareth", "Gavin", "George", "Gerald", "Gilbert", "Glen", "Glenn", "Gordon",
                    "Graham", "Grant", "Grayson", "Greg", "Gregory", "Harley", "Harold", "Harrison",
                    "Harry", "Harvey", "Hayden", "Heath", "Hector", "Henry", "Hudson", "Hugh",
                    "Hugo", "Hunter", "Ian", "Isaac", "Isaiah", "Israel", "Jack", "Jackson",
                    "Jacob", "Jaden", "Jake", "James", "Jamie", "Jared", "Jason", "Jasper",
                    "Javier", "Jeff", "Jeffrey", "Jeremiah", "Jeremy", "Jerome", "Jesse", "Jesus",
                    "Joel", "John", "Johnny", "Jonah", "Jonathan", "Jordan", "Jorge", "Jose",
                    "Joseph", "Joshua", "Josiah", "Juan", "Jude", "Julian", "Julio", "Justin",
                    "Kaden", "Kai", "Kaleb", "Karl", "Kayden", "Keith", "Kelvin", "Kenneth",
                    "Kevin", "Kieran", "Kyle", "Landon", "Larry", "Lawrence", "Lee", "Leo",
                    "Leon", "Leonard", "Leroy", "Liam", "Logan", "Lonnie", "Louis", "Luca",
                    "Lucas", "Luis", "Luke", "Malcolm", "Manuel", "Marcus", "Mario", "Mark",
                    "Marshall", "Martin", "Mason", "Mateo", "Matthew", "Maurice", "Max", "Maximilian",
                    "Maxwell", "Micah", "Michael", "Miguel", "Miles", "Mitchell", "Morgan", "Nate",
                    "Nathan", "Nathaniel", "Neil", "Nelson", "Nicholas", "Nico", "Nolan", "Noah",
                    "Norman", "Oliver", "Omar", "Oscar", "Owen", "Parker", "Patrick", "Paul",
                    "Peter", "Philip", "Phillip", "Preston", "Quentin", "Quinn", "Rafael", "Ralph",
                    "Ramon", "Randall", "Randy", "Raphael", "Ray", "Raymond", "Reece", "Reed",
                    "Reid", "Rhys", "Ricardo", "Richard", "Rick", "Ricky", "Riley", "Roberto",
                    "Robert", "Rodney", "Roger", "Roland", "Roman", "Ronald", "Ronnie", "Ross",
                    "Roy", "Russell", "Ryan", "Samuel", "Scott", "Sean", "Sebastian", "Sergio",
                    "Seth", "Shane", "Shaun", "Shawn", "Silas", "Simon", "Spencer", "Stanley",
                    "Stephen", "Steven", "Stuart", "Terrence", "Theodore", "Thomas", "Timothy", "Todd",
                    "Tom", "Travis", "Trevor", "Tristan", "Troy", "Tyler", "Tyrone", "Victor",
                    "Vincent", "Warren", "Wayne", "Wesley", "Weston", "Wilfred", "Will", "William",
                    "Wyatt", "Xavier", "Zach", "Zachariah", "Zachary",
                    // Regional and historical human names.
                    "Abner", "Adolfo", "Alaric", "Alberto", "Alden", "Alec", "Alejandro", "Alessandro",
                    "Alistair", "Amir", "Anders", "Andrei", "Angelo", "Ansel", "Anton", "Archibald",
                    "Archer", "Armand", "Armando", "Arno", "August", "Augustus", "Basil", "Benedict",
                    "Bennett", "Berkley", "Bernard", "Blaise", "Bo", "Boris", "Bruno", "Bryce",
                    "Callum", "Casimir", "Chester", "Cillian", "Clement", "Clive", "Coby", "Colson",
                    "Constantine", "Cullen", "Curtis", "Dante", "Darius", "Darwin", "Dashiell", "Dawson",
                    "Dorian", "Drake", "Duncan", "Edgar", "Edmund", "Eduardo", "Emanuel", "Emilio",
                    "Emmett", "Enrique", "Ernest", "Esteban", "Ezekiel", "Fabian", "Felipe", "Frederick",
                    "Gael", "Gideon", "Giovanni", "Griffin", "Hank", "Harlan", "Harris", "Hendrik",
                    "Holden", "Homer", "Horatio", "Igor", "Immanuel", "Ivan", "Jamal", "Joaquin",
                    "Jonas", "Jovan", "Khalil", "Kirby", "Klaus", "Lars", "Leander", "Leif",
                    "Lionel", "Lucian", "Magnus", "Malachi", "Marcel", "Marco", "Matthias", "Mauricio",
                    "Merrick", "Milo", "Mohamed", "Nikolai", "Orlando", "Otto", "Pascal", "Percival",
                    "Pierce", "Rainer", "Roderick", "Santino", "Saul", "Simeon", "Stefan", "Sullivan",
                    "Thaddeus", "Valentin", "Vance", "Vaughan", "Waldo", "Yannis", "Yusuf", "Zane",
                    "Zavier",
                    // Medieval and human-fantasy names.
                    "Aelfric", "Aldous", "Alwyn", "Anselm", "Arcturus", "Arlen", "Arvid", "Athelstan",
                    "Balian", "Bartholomew", "Beorn", "Bran", "Brannon", "Cadoc", "Caelan", "Cahir",
                    "Cerdic", "Cian", "Cormac", "Dagobert", "Darragh", "Eadric", "Eamon", "Eldric",
                    "Elric", "Emeric", "Erec", "Everard", "Faelan", "Fenric", "Fintan", "Galahad",
                    "Garrick", "Godfrey", "Hadrian", "Halvard", "Hawthorne", "Ivar", "Jareth", "Kael",
                    "Kendric", "Leofric", "Loric", "Lothar", "Lucan", "Mordecai", "Nereus", "Oberon",
                    "Osric", "Ragnar", "Rhydderch", "Riven", "Sable", "Sigurd", "Soren", "Taran",
                    "Tiberius", "Torin", "Tristram", "Ulric", "Valerian", "Varek", "Wulfric", "Yorick",
                    "Zorion",
                    // Elvish names.
                    "Aelamir", "Aelion", "Aerendyl", "Aerendir", "Aerion", "Althandir", "Althir", "Amrion",
                    "Arannis", "Aravel", "Arionel", "Arphen", "Caelendir", "Caelion", "Caladren", "Calion",
                    "Daerion", "Elaran", "Elarion", "Elendir", "Elion", "Eryndor", "Faelar", "Faelion",
                    "Galandir", "Haladren", "Ithilion", "Kaelith", "Laerion", "Laeroth", "Lethandir", "Lorandir",
                    "Maelor", "Naerion", "Quarion", "Raelith", "Saelion", "Silvaren", "Sylvaran", "Taelion",
                    "Taeriel", "Thalandir", "Thalion", "Therendir", "Vaelion", "Valandor", "Varendir", "Yllarion",
                    "Zephariel", "Aerthas", "Belanor", "Ceryn", "Daelith", "Elvaran", "Galathor", "Iltherion",
                    "Lorien", "Myrion", "Nelaeryn", "Olarion", "Phaendar", "Ravael", "Seldorin", "Tathren",
                    // Dwarvish names.
                    "Adrik", "Agmund", "Baern", "Baldrik", "Barend", "Beldrum", "Bofrik", "Brannik",
                    "Brokkar", "Dagnir", "Dalgrim", "Dolgan", "Dorin", "Dorrak", "Dravik", "Durgrim",
                    "Eberk", "Fargrim", "Fundrik", "Garrum", "Gimrik", "Gormund", "Grundar", "Harbek",
                    "Hrogar", "Kadrin", "Kazrik", "Kildrak", "Korgrim", "Morgran", "Norik", "Orsik",
                    "Rurik", "Skalf", "Stenrik", "Torbek", "Tordrum", "Ulfgar", "Vondrik", "Yngrim",
                    "Zorik", "Angrim", "Bardrum", "Belgor", "Brumgar", "Durnik", "Faldrum", "Garvik",
                    "Haldrek", "Korgan", "Lodrik", "Marn", "Orgrim", "Ragni", "Skorri", "Thordek",
                    "Umrik", "Varrak", "Yorin", "Zagrim", "Brogdan", "Khardun", "Mandrik", "Odrin",
                    // Halfling and gnomish names.
                    "Albie", "Bamble", "Barnaby", "Bixby", "Bramwell", "Brindle", "Carden", "Cobby",
                    "Cogwin", "Dobbins", "Eldon", "Fennel", "Filbert", "Fizzwick", "Fitzwill", "Gearlo",
                    "Gilly", "Hobbin", "Jory", "Kipwick", "Larkin", "Lindle", "Merrin", "Nibbin",
                    "Nackle", "Pipkin", "Porrin", "Quibble", "Quillby", "Rollo", "Roscoe", "Rufkin",
                    "Samkin", "Sprocket", "Tobble", "Tumble", "Wicket", "Wilby", "Wizzle", "Wobbin",
                    "Boddle", "Crankle", "Dapple", "Dilly", "Fobbin", "Gimble", "Jibbit", "Kettle",
                    "Merriwig", "Noddle", "Pindle", "Rimple", "Tansywick", "Tinker", "Trumble", "Wendel",
                    "Brambleby", "Cobble", "Dindle", "Frizzle", "Puckett", "Tibbins", "Wallywick", "Zook",
                    // Orcish and goblinoid names.
                    "Arghun", "Bagrok", "Brakka", "Brug", "Dargash", "Drubak", "Garok", "Gharzug",
                    "Gorruk", "Grash", "Grimbak", "Groth", "Hargan", "Karguk", "Khord", "Krag",
                    "Krugash", "Lugdak", "Mograk", "Nakgor", "Ogrun", "Rukgar", "Skarn", "Throgg",
                    "Uzgash", "Varguk", "Wargan", "Zog", "Zorbag", "Brugg", "Drokan", "Grolm",
                    "Hruk", "Kraz", "Mugrak", "Ragash", "Skorg", "Thrak", "Urgok", "Vorgash",
                    "Bazgul", "Drok", "Gashnak", "Grukk", "Kharzug", "Murdak", "Nogrum", "Raggor",
                    "Skab", "Torgash", "Ugmar", "Vrakk", "Zurg", "Bogruk", "Draznak", "Gornak",
                    "Krosh", "Lugdush", "Mazgak", "Orzog", "Rugash", "Thurg", "Uglak", "Vrogar",
                    // Nordic and frostborn names.
                    "Aksel", "Arnfinn", "Bjarke", "Eirik", "Eivind", "Frode", "Geir", "Gudmund",
                    "Hallbjorn", "Hakon", "Jorund", "Kjell", "Knut", "Oddvar", "Roald", "Rune",
                    "Sigbjorn", "Stellan", "Stig", "Svend", "Torvald", "Trygve", "Vidar", "Yngvar",
                    "Arne", "Asmund", "Berg", "Brandr", "Egil", "Eskil", "Fenrir", "Finnian",
                    "Gunnar", "Halfdan", "Hemming", "Ingvar", "Jarl", "Ketil", "Njord", "Orvar",
                    "Ragnvald", "Rolf", "Snorri", "Steinar", "Toke", "Torsten", "Ulfred", "Vali",
                    "Viggo", "Vilmund", "Aegir", "Brynjar", "Einar", "Gardar", "Hjalmar", "Isen",
                    "Kolli", "Leik", "Rannulf", "Sigvar", "Thorbjorn", "Valgard", "Vestar", "Winter",
                    // Desert and sunlands names.
                    "Aamir", "Adil", "Akram", "Azhar", "Bashir", "Farid", "Hakim", "Idris",
                    "Javid", "Karim", "Khalid", "Malik", "Nadir", "Nasir", "Qadir", "Rashad",
                    "Rayan", "Samir", "Tariq", "Zahir", "Zayd", "Abasi", "Adnan", "Ammar",
                    "Anwar", "Aziz", "Badr", "Danyal", "Emir", "Faisal", "Fawaz", "Hamid",
                    "Harun", "Ismail", "Jabir", "Jalil", "Kamal", "Kaysan", "Mazin", "Munir",
                    "Nabil", "Nazeem", "Omaran", "Qasim", "Rafiq", "Rami", "Sabir", "Salim",
                    "Shakir", "Tahir", "Waleed", "Yazan", "Zaman", "Ziyad", "Azeem", "Dastan",
                    "Ilyas", "Kaveh", "Mirza", "Navid", "Parviz", "Rostam", "Shahin", "Sorush",
                    // Arcane and celestial names.
                    "Aetheron", "Althazar", "Astrion", "Caelum", "Cosmar", "Ecliptor", "Evandriel", "Galaxion",
                    "Luminar", "Meridian", "Nebrion", "Oracius", "Solarian", "Starion", "Vesperion", "Zenithar",
                    "Astrael", "Aurion", "Calyx", "Comet", "Cygnar", "Etherius", "Helion", "Kepler",
                    "Lunaris", "Magister", "Novarion", "Orionis", "Quasar", "Radiant", "Seraphel", "Sidereus",
                    "Solon", "Stellaris", "Tempus", "Umbriel", "Zodiar", "Aevum", "Arcanis", "Celestian",
                    "Cyrion", "Equinox", "Graviel", "Horolog", "Luxian", "Meteoran", "Nocturn", "Polaris",
                    "Runovar", "Scriptor", "Solstice", "Thaumiel", "Vortigar", "Zephyron", "Altairen", "Cosmon",
                    "Elarcan", "Mystivar", "Orrery", "Parallax", "Sigilar", "Voltaer", "Warden", "Zenthiel",
                    // Shadow and gothic names.
                    "Corvin", "Dacian", "Draven", "Lucivar", "Malverin", "Morcant", "Noctis", "Ravian",
                    "Severin", "Valdemar", "Varick", "Veyron", "Albrecht", "Bastian", "Blackwell", "Cadmus",
                    "Carmine", "Cazimir", "Crowley", "Dorianus", "Ebon", "Evernight", "Graves", "Hadeon",
                    "Lazarus", "Lucien", "Marius", "Mortain", "Nox", "Obsidian", "Poe", "Ravenora",
                    "Requiem", "Silasor", "Thaniel", "Thornan", "Vesper", "Vladan", "Wolfram", "Zarek",
                    "Azrael", "Belial", "Calderon", "Corbett", "Darken", "Edraven", "Fenris", "Gethin",
                    "Harrow", "Iscarn", "Malachor", "Nero", "Omen", "Rook", "Sorenzo", "Tenebris",
                    "Umber", "Vaelor", "Voren", "Wraith", "Xavian", "Zevran", "Mordren", "Nighton",
                    // Nature and wildfolk names.
                    "Alder", "Ash", "Aspen", "Birch", "Briar", "Cedar", "Clay", "Cypress",
                    "Elm", "Finch", "Flint", "Forest", "Hawthorn", "Heathwood", "Linden", "Moss",
                    "Oak", "Rain", "River", "Rowan", "Sage", "Sorrel", "Stone", "Thorn",
                    "Wolf", "Wren", "Acorn", "Badger", "Bracken", "Brook", "Buck", "Canyon",
                    "Cloud", "Creek", "Dune", "Falcon", "Fernald", "Foxen", "Glenwood", "Grove",
                    "Heron", "Juniper", "Lake", "Lark", "Maple", "Meadow", "Oakley", "Pine",
                    "Reef", "Ridge", "Robin", "Spruce", "Storm", "Summit", "Talon", "Timber",
                    "Vale", "Wilder", "Willowby", "Woodrow", "Yarrow", "Zephyr", "Boulder", "Emberwood"
            },
            new String[]{
                    // Modern and common human names.
                    "Abigail", "Ada", "Adelaide", "Adeline", "Aimee", "Alexa", "Alexandra", "Alexis",
                    "Alice", "Alicia", "Alison", "Allison", "Alyssa", "Amelia", "Amelie", "Amy",
                    "Anastasia", "Andrea", "Angela", "Angelica", "Angelina", "Anna", "Annabelle", "Anne",
                    "Annie", "April", "Ariana", "Arianna", "Aria", "Ashley", "Aubrey", "Audrey",
                    "Autumn", "Ava", "Bailey", "Barbara", "Beatrice", "Belinda", "Bella", "Beth",
                    "Bethany", "Bianca", "Brenda", "Brianna", "Bridget", "Britney", "Brooke", "Caitlin",
                    "Camila", "Camille", "Candice", "Cara", "Carla", "Carlie", "Carmen", "Caroline",
                    "Carolyn", "Cassandra", "Catherine", "Cathy", "Cecilia", "Celeste", "Chanel", "Charlotte",
                    "Chelsea", "Chloe", "Christina", "Christine", "Claire", "Clara", "Clarissa", "Courtney",
                    "Crystal", "Cynthia", "Daisy", "Dakota", "Daniella", "Danielle", "Darlene", "Dawn",
                    "Deborah", "Debra", "Delilah", "Diana", "Diane", "Donna", "Dorothy", "Eden",
                    "Edith", "Eileen", "Eleanor", "Elena", "Eliana", "Elinor", "Elisa", "Elise",
                    "Eliza", "Elizabeth", "Ella", "Ellen", "Ellie", "Eloise", "Elsa", "Emily",
                    "Emma", "Erica", "Erin", "Esme", "Estelle", "Esther", "Eva", "Evelyn",
                    "Faith", "Faye", "Felicity", "Fern", "Fiona", "Florence", "Frances", "Francesca",
                    "Freya", "Gabriela", "Gabriella", "Gail", "Georgia", "Georgina", "Gillian", "Gloria",
                    "Grace", "Gwen", "Gwendolyn", "Hailey", "Hannah", "Harper", "Hazel", "Heather",
                    "Heidi", "Helen", "Helena", "Holly", "Hope", "Imogen", "Ingrid", "Irene",
                    "Iris", "Isabel", "Isabella", "Isla", "Ivy", "Jacqueline", "Jade", "Jamie",
                    "Jane", "Janet", "Janice", "Jasmine", "Jean", "Jenna", "Jennifer", "Jessica",
                    "Jillian", "Joan", "Joanna", "Jodie", "Jordan", "Josephine", "Josie", "Joy",
                    "Judith", "Judy", "Julia", "Juliana", "Julie", "Juliet", "June", "Justine",
                    "Karen", "Katherine", "Kathleen", "Katrina", "Kayla", "Keira", "Kelly", "Kelsey",
                    "Kimberly", "Kirsten", "Kristen", "Kristin", "Lacey", "Lana", "Lara", "Laura",
                    "Lauren", "Leah", "Leanne", "Lena", "Lesley", "Lila", "Lillian", "Lily",
                    "Linda", "Lindsey", "Lisa", "Lola", "Loretta", "Lottie", "Louisa", "Louise",
                    "Lucia", "Lucille", "Lucy", "Luna", "Lydia", "Mackenzie", "Macy", "Madeline",
                    "Madison", "Mae", "Maeve", "Maggie", "Maisie", "Mandy", "Margaret", "Margot",
                    "Maria", "Mariah", "Mariam", "Marian", "Marilyn", "Marina", "Martha", "Mary",
                    "Matilda", "Maya", "Megan", "Melanie", "Melissa", "Mia", "Michelle", "Mila",
                    "Molly", "Monica", "Morgan", "Naomi", "Natalia", "Natalie", "Natasha", "Niamh",
                    "Nicole", "Nicola", "Nina", "Noelle", "Nora", "Norah", "Olivia", "Paige",
                    "Pamela", "Patricia", "Paula", "Penelope", "Phoebe", "Poppy", "Priscilla", "Rachel",
                    "Rebecca", "Reese", "Riley", "Rita", "Robyn", "Rosa", "Rosalie", "Rose",
                    "Rosie", "Ruby", "Ruth", "Sabrina", "Samantha", "Sandra", "Sara", "Sarah",
                    "Savannah", "Scarlett", "Selena", "Serena", "Shannon", "Sharon", "Sheila", "Shelby",
                    "Sienna", "Sierra", "Simone", "Sofia", "Sophia", "Sophie", "Stacey", "Stella",
                    "Stephanie", "Summer", "Susan", "Suzanne", "Sydney", "Tara", "Tessa", "Theresa",
                    "Tiffany", "Tracy", "Trinity", "Valentina", "Valerie", "Vanessa", "Vera", "Veronica",
                    "Victoria", "Violet", "Vivian", "Wendy", "Whitney", "Willow", "Yasmin", "Yvonne",
                    "Zara", "Zoe", "Zoey",
                    // Regional and historical human names.
                    "Adriana", "Agatha", "Alana", "Alba", "Alessandra", "Alma", "Amara", "Amira",
                    "Anika", "Annika", "Antonia", "Arabella", "Astrid", "Athena", "Aurelia", "Beatrix",
                    "Berenice", "Bernadette", "Blanca", "Bonnie", "Brielle", "Bruna", "Cadence", "Calliope",
                    "Carina", "Cecily", "Celina", "Charity", "Claudia", "Colette", "Constance", "Cora",
                    "Cordelia", "Cosima", "Dahlia", "Daphne", "Daria", "Davina", "Delia", "Elara",
                    "Elodie", "Elsie", "Ember", "Emilia", "Emmeline", "Enid", "Erika", "Estella",
                    "Etta", "Evangeline", "Fabiola", "Farah", "Fatima", "Flora", "Frida", "Genevieve",
                    "Giselle", "Greta", "Guinevere", "Hattie", "Henrietta", "Iliana", "Ines", "Iona",
                    "Isadora", "Ivana", "Jada", "Janelle", "Jasmin", "Jayla", "Joelle", "Joyce",
                    "Kaia", "Kalina", "Kamila", "Karina", "Karla", "Kendra", "Kiara", "Klara",
                    "Larissa", "Leona", "Liliana", "Lorelei", "Luciana", "Luella", "Mabel", "Marcella",
                    "Maribel", "Marisol", "Marissa", "Marjorie", "Marla", "Marlene", "Maura", "Melody",
                    "Miranda", "Miriam", "Mireille", "Nadia", "Nadine", "Nala", "Nellie", "Nerissa",
                    "Odessa", "Opal", "Ophelia", "Paloma", "Petra", "Ramona", "Regina", "Renata",
                    "Rhea", "Rhiannon", "Roxanne", "Salma", "Selma", "Seraphina", "Sonia", "Soraya",
                    "Tabitha", "Tatiana", "Theodora", "Valeria", "Viola", "Vivienne", "Wanda", "Ximena",
                    "Yvette", "Zelda", "Zinnia",
                    // Medieval and human-fantasy names.
                    "Aeliana", "Aerin", "Aislinn", "Althea", "Amarantha", "Anwen", "Araminta", "Arwen",
                    "Aveline", "Azalea", "Briallen", "Brynhild", "Catriona", "Ceridwen", "Clarimond", "Cressida",
                    "Damaris", "Eirwen", "Elowen", "Eowyn", "Eulalia", "Faelwen", "Fiora", "Ginevra",
                    "Hesperia", "Honora", "Ilyria", "Isolde", "Jessamine", "Kerensa", "Lavinia", "Leocadia",
                    "Liora", "Lyra", "Mabyn", "Melisande", "Morgana", "Nimue", "Ondine", "Oriane",
                    "Rowena", "Sabriel", "Seraphine", "Sigrun", "Sylvara", "Talindra", "Theodosia", "Ursula",
                    "Vespera", "Winifred", "Ysolde", "Zephyra", "Zorina",
                    // Elvish names.
                    "Aelara", "Aeloria", "Aerilwen", "Aerithiel", "Alariel", "Althaea", "Amaryel", "Aranel",
                    "Aravelle", "Caelara", "Caelith", "Caladwen", "Daelira", "Elariel", "Elenara", "Elenwe",
                    "Eliriel", "Faelara", "Faelith", "Galadwen", "Illyria", "Ithilwen", "Kaelara", "Laeriel",
                    "Lethariel", "Liandria", "Lirael", "Lorawen", "Maeriel", "Naelara", "Nymriel", "Raelwen",
                    "Saelara", "Seluniel", "Silvanna", "Sylwen", "Taelara", "Thaelira", "Vaelora", "Valindra",
                    "Yllara", "Zephyriel", "Aerynna", "Belwen", "Celestria", "Daewen", "Elarwyn", "Faenara",
                    "Galaeth", "Halaena", "Isilwen", "Kaelithra", "Loraelis", "Melariel", "Nerawen", "Orielle",
                    "Phaelynn", "Quenara", "Ravaelle", "Selyria", "Talandra", "Vaelith", "Wynara", "Zephiel",
                    // Dwarvish names.
                    "Adrika", "Agna", "Baerna", "Baldrina", "Belda", "Branna", "Brynja", "Dagmara",
                    "Dalra", "Disra", "Dorna", "Drisla", "Durra", "Ebera", "Farra", "Freyda",
                    "Garnet", "Gilda", "Grenda", "Harka", "Helga", "Hilda", "Ingridra", "Kadra",
                    "Kazra", "Kilda", "Korra", "Magna", "Marra", "Norra", "Orsa", "Ragna",
                    "Runa", "Skalda", "Stenna", "Tora", "Ulfra", "Vondra", "Yngra", "Angra",
                    "Bardra", "Belgra", "Brumhilda", "Dagna", "Durna", "Falda", "Garna", "Haldra",
                    "Jorunn", "Korga", "Lodda", "Marnie", "Odria", "Ragnild", "Sigrida", "Thordra",
                    "Umra", "Varna", "Yorra", "Zagda", "Brogna", "Kharda", "Mandria",
                    // Halfling and gnomish names.
                    "Albella", "Babs", "Bellis", "Biddy", "Bramble", "Cally", "Celandine", "Cherry",
                    "Clover", "Daffy", "Dilly", "Dotty", "Effie", "Fennella", "Fidget", "Fizzabelle",
                    "Flossie", "Gertie", "Goldie", "Hattiebell", "Honey", "Jilly", "Kittiwake", "Lolly",
                    "Merry", "Millie", "Nibby", "Pansy", "Peony", "Pippa", "Poppyseed", "Posy",
                    "Prim", "Quilla", "Rosiebee", "Rue", "Tansy", "Tilly", "Trinket", "Twilla",
                    "Willa", "Zuzu", "Bimble", "Button", "Cogsie", "Dapple", "Dewdrop", "Fable",
                    "Figgy", "Gingersnap", "Jumble", "Kettle", "Larkspur", "Mopsy", "Nettle", "Pennywhistle",
                    "Pipette", "Riddle", "Saffy", "Thimble", "Tuppence", "Winkle", "Yarroway",
                    // Orcish and goblinoid names.
                    "Argha", "Bagra", "Brakka", "Draga", "Drubba", "Gharza", "Gorra", "Grasha",
                    "Grimba", "Karga", "Khurza", "Kraga", "Lugra", "Mogra", "Nakra", "Ogra",
                    "Rukka", "Skarna", "Thrakka", "Urga", "Varga", "Zaga", "Zorba", "Brugga",
                    "Drokka", "Grolma", "Hruka", "Kraza", "Mugra", "Ragga", "Skorga", "Thraza",
                    "Vorgra", "Bazga", "Drasha", "Gashna", "Grukka", "Kharza", "Murda", "Nogra",
                    "Raggra", "Skaba", "Torga", "Ugla", "Vrogha", "Zurga", "Borga", "Drazha",
                    "Gorna", "Krosha", "Lugdra", "Mazga", "Orza", "Rugga", "Thurga", "Ugmara",
                    "Vroga", "Zagrukha", "Brumna", "Grizha", "Krazna", "Morgra", "Uzgara",
                    // Nordic and frostborn names.
                    "Ase", "Aslaug", "Birgit", "Bodil", "Dagny", "Eira", "Freydis", "Gudrun",
                    "Gunnhild", "Hallbera", "Hanne", "Hedda", "Helmi", "Inga", "Jorid", "Kari",
                    "Kirsti", "Liv", "Magnhild", "Ragnhild", "Signe", "Sigrid", "Solveig", "Sunniva",
                    "Thora", "Tove", "Tuva", "Yrsa", "Alva", "Astridra", "Bergljot", "Eydis",
                    "Frigg", "Gerda", "Hilde", "Idunn", "Ingeborg", "Katla", "Linnea", "Nanna",
                    "Saga", "Sif", "Skadi", "Thyra", "Ulla", "Vigdis", "Ylva", "Asta",
                    "Eivor", "Embla", "Freja", "Groa", "Hrefna", "Kelda", "Rannveig", "Signy",
                    "Siv", "Torhild", "Vala", "Vilda", "Wintera",
                    // Desert and sunlands names.
                    "Aaliyah", "Adara", "Amina", "Amirah", "Aziza", "Bahira", "Dalia", "Farahna",
                    "Hadiya", "Inaya", "Jamila", "Kalila", "Karima", "Layla", "Malika", "Nadira",
                    "Nasira", "Qadira", "Rania", "Rashida", "Samira", "Tahira", "Yara", "Zahra",
                    "Zaina", "Abira", "Afsana", "Alina", "Anisa", "Arwa", "Basma", "Dalal",
                    "Darya", "Emani", "Farida", "Habiba", "Isra", "Jaleela", "Kamilah", "Kenza",
                    "Laleh", "Maha", "Marwa", "Mina", "Naima", "Nasreen", "Nura", "Parisa",
                    "Rasha", "Shirin", "Sorina", "Tasnim", "Yasira", "Zuleika", "Arezou", "Cyra",
                    "Delara", "Golnar", "Mahin", "Roxana", "Sahar", "Setareh",
                    // Arcane and celestial names.
                    "Aetheria", "Astraea", "Astrielle", "Caeluna", "Celestara", "Cosmia", "Eclipsa", "Ethera",
                    "Galaxia", "Lunara", "Meridianne", "Nebula", "Oracia", "Solara", "Stellara", "Vesperine",
                    "Zenithia", "Auroria", "Calyxa", "Cometa", "Cygnia", "Heliana", "Keplera", "Luxara",
                    "Novella", "Orionna", "Quasara", "Radiella", "Seraphe", "Siderea", "Solenne", "Tempestra",
                    "Umbrielle", "Zodiacra", "Aevia", "Arcanella", "Celesse", "Cyria", "Equinoxa", "Gravielle",
                    "Horologia", "Luminara", "Meteora", "Nocturna", "Polaris", "Runessa", "Scriptora", "Solsticia",
                    "Thaumira", "Vortessa", "Altaira", "Cosmina", "Mystara", "Orreria", "Parallia", "Sigilra",
                    "Voltara", "Wardena", "Zenthia", "Auralis", "Starla", "Moonara", "Comethea",
                    // Shadow and gothic names.
                    "Belladonna", "Carmilla", "Corvina", "Daciana", "Drusilla", "Ebonique", "Lenore", "Lucivara",
                    "Malveria", "Morwenna", "Noctessa", "Ravenna", "Severina", "Valdora", "Varina", "Veyra",
                    "Alberta", "Bastianna", "Cadmira", "Carmine", "Cressara", "Crowna", "Dorianne", "Evernight",
                    "Gravessa", "Hadea", "Lazara", "Lucienne", "Mariella", "Morticia", "Noxa", "Obsidia",
                    "Poesia", "Ravenora", "Requia", "Silvara", "Thana", "Thornia", "Vladena", "Wolfruna",
                    "Azrielle", "Calderia", "Corbetta", "Darka", "Edravenna", "Fenrisa", "Gethra", "Harrowyn",
                    "Iscara", "Malachra", "Omena", "Rooka", "Sorenza", "Tenebra", "Umbria", "Vorenna",
                    "Wraitha", "Xaviera", "Zevrana", "Mordria", "Nightshade",
                    // Nature and wildfolk names.
                    "Alderose", "Apple", "Aspen", "Birchie", "Briar", "Cedar", "Clovera", "Cypress",
                    "Dove", "Juniper", "Lark", "Laurel", "Lilac", "Maple", "Meadow", "Mossy",
                    "Oakley", "Olive", "Petal", "Rain", "River", "Robin", "Rowan", "Sage",
                    "Sorrel", "Sparrow", "Storm", "Sunny", "Thorn", "Wren", "Acacia", "Blossom",
                    "Brook", "Canyon", "Coral", "Dune", "Fawn", "Feather", "Glenna", "Grove",
                    "Heron", "Lake", "Lotus", "Marigold", "Moonflower", "Pine", "Reef", "Ridge",
                    "Rosewood", "Snow", "Starling", "Summerly", "Vale", "Wildera"
            }
    };

    public static final String[] lastNames = new String[]{
            // Modern and common human surnames.
            "Adams", "Ainsworth", "Alexander", "Allen", "Anderson", "Andrews", "Armstrong", "Arnold",
            "Atkins", "Atkinson", "Austin", "Bailey", "Baker", "Ball", "Banks", "Barber",
            "Barker", "Barnes", "Barnett", "Barrett", "Barry", "Bates", "Baxter", "Beck",
            "Bell", "Bennett", "Benson", "Bentley", "Berry", "Black", "Blake", "Booth",
            "Bowen", "Boyd", "Bradley", "Brady", "Brewer", "Bridges", "Briggs", "Brooks",
            "Brown", "Bryant", "Buckley", "Bullock", "Burke", "Burnett", "Burns", "Burton",
            "Bush", "Butler", "Byrne", "Campbell", "Carlson", "Carpenter", "Carr", "Carroll",
            "Carter", "Casey", "Chambers", "Chapman", "Chandler", "Christensen", "Clark", "Clarke",
            "Clayton", "Cobb", "Cohen", "Cole", "Coleman", "Collins", "Conner", "Cook",
            "Cooper", "Curtis", "Cox", "Craig", "Crawford", "Cross", "Cruz", "Cunningham",
            "Dalton", "Daniel", "Daniels", "Davidson", "Davis", "Dawson", "Day", "Dean",
            "Delaney", "Dennis", "Dixon", "Douglas", "Doyle", "Duncan", "Dunn", "Edwards",
            "Elliott", "Ellis", "Erickson", "Eriksen", "Evans", "Farrell", "Ferguson", "Fernandez",
            "Fisher", "Fitzgerald", "Fleming", "Fletcher", "Flores", "Ford", "Foster", "Fowler",
            "Fox", "Francis", "Franklin", "Freeman", "Gallagher", "Gardner", "Garner", "Garcia",
            "Garrison", "George", "Gibbs", "Gibson", "Gilbert", "Gill", "Glover", "Gonzalez",
            "Goodman", "Gordon", "Graham", "Grant", "Graves", "Gray", "Green", "Greene",
            "Gregory", "Griffin", "Griffiths", "Hall", "Hamilton", "Hansen", "Hanson", "Harper",
            "Harris", "Harrison", "Hart", "Harvey", "Hawkins", "Hayes", "Haynes", "Henderson",
            "Henry", "Hernandez", "Hicks", "Hill", "Hines", "Hodges", "Hoffman", "Holland",
            "Holmes", "Holt", "Hopkins", "Horton", "Howard", "Howe", "Hudson", "Hughes",
            "Hunt", "Hunter", "Ingram", "Jackson", "Jacobs", "James", "Jarvis", "Jenkins",
            "Jennings", "Jensen", "Johnson", "Johnston", "Jones", "Jordan", "Kane", "Keller",
            "Kelley", "Kelly", "Kennedy", "Khan", "King", "Kirk", "Klein", "Knight",
            "Lambert", "Lane", "Lang", "Lawrence", "Lawson", "Leach", "Lee", "Lewis",
            "Little", "Lloyd", "Logan", "Long", "Lopez", "Lowe", "Lucas", "Lynch",
            "Lyons", "MacDonald", "Madden", "Manning", "Marks", "Marsh", "Marshall", "Martin",
            "Martinez", "Mason", "Matthews", "Maxwell", "May", "McBride", "McCarthy", "McCormick",
            "McDonald", "McGee", "McGrath", "McGregor", "McKenzie", "McLean", "McMillan", "Medina",
            "Mendez", "Meyer", "Miller", "Mills", "Mitchell", "Moody", "Moore", "Morales",
            "Morgan", "Morris", "Morrison", "Morton", "Moss", "Murphy", "Murray", "Myers",
            "Nelson", "Newman", "Newton", "Nichols", "Nicholson", "Nixon", "Nolan", "Norman",
            "Norris", "O'Brien", "O'Connor", "O'Neill", "Oliver", "Olson", "Ortiz", "Owens",
            "Page", "Palmer", "Parker", "Patel", "Patrick", "Patterson", "Payne", "Pearce",
            "Pearson", "Pena", "Perez", "Perkins", "Perry", "Peters", "Peterson", "Phillips",
            "Pierce", "Poole", "Porter", "Potter", "Powell", "Powers", "Price", "Quinn",
            "Ramirez", "Ramos", "Randall", "Ray", "Reed", "Rees", "Reese", "Reid",
            "Reyes", "Reynolds", "Rhodes", "Rice", "Richards", "Richardson", "Riley", "Rivers",
            "Robbins", "Roberts", "Robertson", "Robinson", "Rodgers", "Rodriguez", "Rogers", "Rose",
            "Ross", "Rowe", "Ruiz", "Russell", "Ryan", "Salazar", "Sanders", "Sanderson",
            "Sandoval", "Santiago", "Saunders", "Schmidt", "Scott", "Sharp", "Shaw", "Sheffield",
            "Shelton", "Short", "Silva", "Simmons", "Simpson", "Singh", "Sloan", "Smith",
            "Snyder", "Spencer", "Stanley", "Stephens", "Stevens", "Stewart", "Stone", "Sullivan",
            "Summers", "Sutton", "Taylor", "Terry", "Thomas", "Thompson", "Thornton", "Todd",
            "Torres", "Townsend", "Tran", "Tucker", "Turner", "Tyler", "Vasquez", "Vaughn",
            "Vazquez", "Wade", "Wagner", "Walker", "Wallace", "Walsh", "Walters", "Ward",
            "Warren", "Washington", "Waters", "Watkins", "Watson", "Watts", "Weaver", "Webb",
            "Weber", "Welch", "Wells", "West", "Wheeler", "White", "Whitaker", "Whitehead",
            "Whitfield", "Williams", "Williamson", "Willis", "Wilson", "Wise", "Wolfe", "Wong",
            "Wood", "Woods", "Wright", "Wyatt", "Young", "Zimmerman",
            // Regional and historical human surnames.
            "Abbott", "Acker", "Aguilar", "Albright", "Aldridge", "Alvarez", "Ambrose", "Andrade",
            "Applegate", "Archer", "Atwood", "Baldwin", "Barlow", "Beasley", "Becker", "Bishop",
            "Blackwell", "Blanchard", "Bolton", "Bonner", "Bourne", "Bradshaw", "Branson", "Bray",
            "Bright", "Browning", "Bruno", "Buchanan", "Burgess", "Calder", "Callahan", "Cannon",
            "Carey", "Carmichael", "Carney", "Chase", "Christie", "Church", "Clancy", "Clay",
            "Cline", "Compton", "Conley", "Conway", "Corbett", "Cortez", "Crosby", "Cullen",
            "Davenport", "Decker", "Delgado", "Dempsey", "Devereux", "Devlin", "Donahue", "Dorsey",
            "Drake", "Draper", "Duffy", "Eaton", "Ellington", "Emerson", "Esposito", "Faulkner",
            "Finch", "Finley", "Fitzpatrick", "Foley", "Forbes", "Foreman", "Franco", "Gallant",
            "Gentry", "Gerard", "Gibbons", "Giles", "Goodwin", "Grady", "Granger", "Greer",
            "Gresham", "Hammond", "Hancock", "Harding", "Harlow", "Harrington", "Hatcher", "Hayward",
            "Heath", "Hensley", "Herrera", "Hester", "Higgins", "Hobbs", "Holloway", "Hooper",
            "Irwin", "Jarrett", "Jefferson", "Kaufman", "Kendrick", "Kincaid", "Kirby", "Lafayette",
            "Larsen", "Latham", "Levine", "Lindsey", "Locke", "Maddox", "Maguire", "Malone",
            "Marlow", "Mercer", "Merritt", "Montague", "Montoya", "Morrissey", "Navarro", "Noble",
            "Norton", "Osborne", "Pace", "Pacheco", "Parks", "Parsons", "Phelps", "Prescott",
            "Quincy", "Rafferty", "Randolph", "Rasmussen", "Roth", "Rowland", "Royce", "Sampson",
            "Savage", "Sawyer", "Schneider", "Serrano", "Sinclair", "Sparks", "Stafford", "Stanton",
            "Stark", "Sterling", "Strickland", "Sweeney", "Tanner", "Tate", "Thayer", "Underwood",
            "Valdez", "Vega", "Vincent", "Waller", "Warner", "Whitmore", "Wilkins", "Winters",
            "Yates", "York", "Zamora",
            // Medieval and general fantasy surnames.
            "Ashborne", "Ashcombe", "Ashdown", "Ashenford", "Blackbriar", "Blackmere", "Blackthorn", "Bloodgood",
            "Brightwater", "Bronzewood", "Cinderfall", "Crowhaven", "Dawnmere", "Dreadmoor", "Dragonbane", "Duskwood",
            "Eaglecrest", "Emberfall", "Fairbairn", "Fairchild", "Fallowmere", "Frostborne", "Goldbranch", "Goldcrest",
            "Gravewell", "Greymantle", "Hallowmere", "Hawkridge", "Ironbark", "Ironhand", "Kingsley", "Knightfall",
            "Longshadow", "Moonbrook", "Mooncrest", "Mournwell", "Oakenshield", "Ravencrest", "Ravenmere", "Redwyne",
            "Rosethorn", "Runebrook", "Silverbranch", "Silverkeep", "Starfall", "Stormvale", "Sunhaven", "Thornfield",
            "Thornwall", "Truehart", "Valebrook", "Valorborn", "Wildermere", "Windrider", "Wintermere", "Wolfhart",
            "Wyrmwood", "Wyvernhall", "Yewshade", "Zephyrfall",
            // Elvish houses and woodland lineages.
            "Amberleaf", "Autumnbough", "Brightbloom", "Brightleaf", "Brightwillow", "Dawnbranch", "Dawnpetal", "Dewsong",
            "Dreamwillow", "Elderbough", "Evenstar", "Faelight", "Fernwhisper", "Frostleaf", "Goldleaf", "Greenbough",
            "Greenmantle", "Greenwillow", "Highgrove", "Ithilwood", "Larkbranch", "Lightbloom", "Lightweaver", "Moonbough",
            "Moonpetal", "Moonwhisper", "Morningdew", "Mossglade", "Nightbloom", "Oakensong", "Rainleaf", "Riverbough",
            "Silverbloom", "Silverdew", "Silverglade", "Silverleaf", "Silversong", "Skybough", "Springvale", "Starbloom",
            "Starleaf", "Starsong", "Sunbranch", "Sunleaf", "Swiftbough", "Thistledown", "Thornbloom", "Valeleaf",
            "Whisperbough", "Whisperleaf", "Wildbloom", "Willowmere", "Windbough", "Windleaf", "Winterbloom", "Woodwhisper",
            "Aerendell", "Amberglen", "Brighthollow", "Caelwood", "Dawnglade", "Elenvale", "Faebrook", "Galewood",
            "Lethariel", "Moonvale", "Rainglade", "Sylvanor", "Vaelwood", "Whisperwind", "Yewbloom", "Zephyrleaf",
            // Dwarven clans and forge families.
            "Anvilborn", "Anvilbreaker", "Ashforge", "Axebearer", "Axeborn", "Battlehammer", "Blackanvil", "Blackforge",
            "Blackiron", "Boulderback", "Boulderborn", "Bronzeanvil", "Bronzebeard", "Bronzehammer", "Coalbraid", "Copperbeard",
            "Copperforge", "Deepdelver", "Deepforge", "Deepmantle", "Emberanvil", "Emberbeard", "Emberforge", "Firebraid",
            "Firehammer", "Flintbeard", "Flintforge", "Forgearm", "Forgeborn", "Forgehammer", "Goldanvil", "Goldbeard",
            "Graniteborn", "Granitefist", "Graybeard", "Hammerfall", "Hammerhand", "Hammerstone", "Hardmantle", "Ironanvil",
            "Ironbraid", "Ironforge", "Ironfist", "Ironmantle", "Ironpick", "Ironsong", "Mithrilborn", "Mountainborn",
            "Oathhammer", "Oreheart", "Redanvil", "Rockbeard", "Rockhammer", "Runeanvil", "Runebeard", "Runeforge",
            "Silveranvil", "Silverbeard", "Steelbraid", "Steelforge", "Stoneanvil", "Stonebeard", "Stoneforge", "Stonefist",
            "Strongarm", "Thunderanvil", "Thunderforge", "Trueanvil", "Underforge", "Vaultkeeper", "Bronzebrow", "Deepstone",
            "Emberpick", "Frostforge", "Goldmantle", "Ironroot", "Runehammer", "Steelmantle", "Stonehelm", "Understone",
            // Halfling and gnomish families.
            "Applebarrel", "Applebottom", "Applebrook", "Bamblefoot", "Barleybun", "Barleywick", "Berrybottle", "Berryhill",
            "Biscuitbottom", "Bramblebutton", "Bramblefoot", "Bramblepot", "Bumblebrook", "Bumblefoot", "Butterburrow", "Buttercup",
            "Buttonberry", "Buttonfoot", "Ciderbrook", "Ciderpot", "Cobblebutton", "Cobblefoot", "Copperkettle", "Crumblecake",
            "Dapplebrook", "Dapplefoot", "Dewberry", "Dimplebottom", "Fiddlewick", "Figbottom", "Fizzlebottle", "Fizzlewick",
            "Gingersnap", "Goldbutton", "Goodbarrel", "Goodberry", "Goodbun", "Greenbottle", "Honeybrook", "Honeybun",
            "Jamjar", "Kettlewhistle", "Littlebarrel", "Littlebutton", "Littlefoot", "Meadowbun", "Merrybrook", "Muddlepot",
            "Nibblewick", "Oatcake", "Pebblefoot", "Pennywhistle", "Picklepot", "Puddlefoot", "Pumpernickel", "Quickbutton",
            "Ramblefoot", "Rumblebelly", "Shortcake", "Smallburrow", "Smallfoot", "Snicklefritz", "Sprigbottom", "Tanglefoot",
            "Teacake", "Thimblewick", "Tumblebrook", "Wafflepot", "Whistlewick", "Wobblefoot", "Acornbottom", "Berrypocket",
            "Bumblebutton", "Cabbagewick", "Dandelionpot", "Fiddlefoot", "Hazelnut", "Muffinbrook", "Nutmegger", "Poppybutton",
            // Orcish and goblinoid clans.
            "Ashfang", "Blackfang", "Blacktusk", "Bloodaxe", "Bloodfang", "Bonebreaker", "Bonechewer", "Bonecrusher",
            "Bonegnaw", "Bonesplitter", "Brimstone", "Brokenhorn", "Chainbreaker", "Darktusk", "Doomfang", "Dreadfang",
            "Dreadtusk", "Emberfang", "Firetusk", "Fleshrender", "Ghostfang", "Grimaxe", "Grimjaw", "Grimtusk",
            "Gutripper", "Hardfang", "Helltusk", "Ironfang", "Ironjaw", "Irontusk", "Jaggedfang", "Jaggedtusk",
            "Marrowgnaw", "Moonfang", "Mudblood", "Mudtusk", "Nightfang", "Nighttusk", "Ragefang", "Redfang",
            "Redspear", "Rocktusk", "Rotfang", "Scarhide", "Skullbreaker", "Skullcrusher", "Skullsplitter", "Smokejaw",
            "Snaggletooth", "Stonefang", "Stonetusk", "Strongjaw", "Thornfang", "Thunderjaw", "Thundertusk", "Toothbreaker",
            "Waraxe", "Warfang", "Warhide", "Warjaw", "Wartusk", "Wolfjaw", "Wolfskull", "Wolfspear",
            "Ashjaw", "Blackmaw", "Bloodmaw", "Bonehide", "Doomjaw", "Grimbone", "Ironhide", "Redmaw",
            "Rotjaw", "Skullgnaw", "Stonejaw", "Stormtusk", "Thickhide", "Thornmaw", "Warbone", "Wolfsbane",
            // Nordic and frostborn clans.
            "Bearmantle", "Bearshield", "Bearson", "Blackfjord", "Coldhammer", "Coldshield", "Dragonfjord", "Eaglehelm",
            "Elkheart", "Everfrost", "Firefjord", "Fjordborn", "Frostbeard", "Frostborn", "Frostbreaker", "Frosthelm",
            "Frostmane", "Frostshield", "Frostwolf", "Graywolf", "Iceblood", "Iceborn", "Icebreaker", "Icehammer",
            "Iceheart", "Icehelm", "Icemantle", "Iceward", "Longwinter", "Northborn", "Northhammer", "Northshield",
            "Oathborn", "Oathkeeper", "Ravenhelm", "Ravenwolf", "Runeborn", "Runeshield", "Seahelm", "Seawolf",
            "Shieldborn", "Shieldbreaker", "Shieldson", "Snowbeard", "Snowborn", "Snowhammer", "Snowhelm", "Snowmantle",
            "Stormborn", "Stormbreaker", "Stormhammer", "Stormhelm", "Stormshield", "Stormwolf", "Thunderborn", "Thunderhelm",
            "Thunderwolf", "Winterborn", "Winterfang", "Winterhammer", "Winterhelm", "Wintershield", "Wolfborn", "Wolfhelm",
            "Wolfsong", "Wyrmhelm", "Ymirson", "Bearclaw", "Coldiron", "Eaglefang", "Frostbrand", "Icebrand",
            "Northwind", "Ravenbrand", "Snowbrand", "Stormbrand", "Thunderbrand", "Winterbrand", "Wolfbrand", "Wyrmbrand",
            // Desert and sunlands houses.
            "Amberdune", "Ashdune", "Brightsand", "Cinderdune", "Copperdune", "Dawnfire", "Dawnscar", "Desertborn",
            "Dunewalker", "Dustborn", "Dustwalker", "Emberdune", "Emberglass", "Fireglass", "Flameborn", "Golddune",
            "Goldenveil", "Heatshimmer", "Highsun", "Mirageborn", "Moonscar", "Oasisborn", "Reddune", "Redsand",
            "Sandborn", "Sandglass", "Sandstrider", "Sandwalker", "Scorchwind", "Sirocco", "Starcaravan", "Sunborn",
            "Sunfire", "Sunglass", "Sunscar", "Sunstrider", "Sunveil", "Sunwalker", "Warmwind", "Whiteflame",
            "Ashcaravan", "Brightdune", "Copperveil", "Dawnveil", "Desertrose", "Duskdune", "Emberveil", "Fireveil",
            "Goldscar", "Moonveil", "Nomadheart", "Oasiswalker", "Redveil", "Sandrose", "Scorchborn", "Silkroad",
            "Sunrose", "Sunshadow", "Sunspire", "Sunstone", "Sunward", "Veilwalker", "Windcaravan", "Zephyrdune",
            "Amberveil", "Cactusborn", "Dunesong", "Dustveil", "Flameveil", "Goldensand", "Miragewalker", "Redglass",
            "Sandspire", "Suncrest", "Sunhammer", "Sunmantle", "Sunwhisper", "Warmstone", "Windsand", "Zephyrveil",
            // Arcane and celestial lineages.
            "Astralborn", "Astralweave", "Aetherborn", "Aetherglass", "Arcaneheart", "Arcanewell", "Brightsigil", "Celestial",
            "Cometborn", "Cometfall", "Constellation", "Cosmosong", "Dawnstar", "Dreamweaver", "Eclipsemantle", "Etherweave",
            "Fatespinner", "Glyphborn", "Glyphweaver", "Mooncipher", "Moonrune", "Nebulaborn", "Nightstar", "Oracleborn",
            "Runecaster", "Runewalker", "Sigilborn", "Sigilkeeper", "Skycipher", "Spellbinder", "Spellborn", "Spellweaver",
            "Starborn", "Starcaster", "Starcipher", "Stardancer", "Stargazer", "Starkeeper", "Starmantle", "Starweaver",
            "Sunrune", "Voidborn", "Voidwalker", "Zodiacborn", "Aethermantle", "Arcanestar", "Aurorawell", "Celestine",
            "Cometweaver", "Cosmaris", "Dreamcipher", "Eclipseborn", "Etherborn", "Fateweaver", "Galaxion", "Glyphkeeper",
            "Lightcipher", "Lumensong", "Mooncaster", "Nebulawell", "Oraclestar", "Runebinder", "Skyweaver", "Solsticeborn",
            "Spellkeeper", "Starbinder", "Starfalling", "Starward", "Voidcipher", "Zodiacweaver", "Auralight", "Moonoracle",
            "Novaheart", "Quasarborn", "Radiantwell", "Solarweave", "Starlumen", "Timekeeper", "Vortexborn", "Zenithstar",
            // Shadow and gothic houses.
            "Ashenveil", "Blackbloom", "Blackgrave", "Blackhollow", "Blackmoor", "Blackveil", "Bleakheart", "Bloodrose",
            "Bonegarden", "Briargrave", "Coldgrave", "Crowmoor", "Darkbloom", "Darkgrave", "Darkhollow", "Darkmantle",
            "Darkveil", "Deadrose", "Dreadgrave", "Dreadveil", "Duskhollow", "Duskmantle", "Duskveil", "Ebonheart",
            "Ebonveil", "Evermourne", "Gloamwood", "Gravebloom", "Graveborn", "Graveheart", "Gravekeeper", "Hollowgrave",
            "Mournbloom", "Mournheart", "Mournshade", "Nightgrave", "Nightmantle", "Nightveil", "Nocturne", "Palegrave",
            "Ravenblood", "Ravenheart", "Ravenhollow", "Ravenmourne", "Ravenshade", "Ravenveil", "Redgrave", "Rosegrave",
            "Shadowbloom", "Shadowgrave", "Shadowhollow", "Shadowmantle", "Shadowveil", "Silentgrave", "Sorrowborn", "Sorrowgrave",
            "Thorngrave", "Thornveil", "Umbergrave", "Veilborn", "Veilkeeper", "Wintergrave", "Wraithborn", "Ashenmourne",
            "Blackrose", "Bloodveil", "Crowgrave", "Darkrose", "Duskmourne", "Ebonmourne", "Gloomheart", "Graveveil",
            "Mourningstar", "Nightrose", "Paleblood", "Ravenrose", "Shadowrose", "Sorrowveil", "Wraithveil", "Yewgrave",
            // Nature and wildfolk families.
            "Alderbrook", "Alderheart", "Applewood", "Aspenbrook", "Aspenheart", "Birchbrook", "Birchheart", "Briarbrook",
            "Briarheart", "Cedarbrook", "Cedarheart", "Cloverfield", "Cypressbrook", "Dawnwood", "Deerheart", "Fernbrook",
            "Fernheart", "Foxglove", "Greenbrook", "Greenheart", "Greenroot", "Greenstone", "Greenwood", "Hazelbrook",
            "Hazelwood", "Hollybrook", "Hollyheart", "Ivybrook", "Juniperbrook", "Larkwood", "Laurelbrook", "Maplebrook",
            "Maplewood", "Meadowbrook", "Meadowheart", "Mossbrook", "Mossheart", "Oakbrook", "Oakheart", "Oakroot",
            "Oakwood", "Pinebrook", "Pineheart", "Rainbrook", "Rainwood", "Riverheart", "Riverroot", "Rosebrook",
            "Roseheart", "Rowanbrook", "Rowanheart", "Sagebrook", "Sageheart", "Sorrelbrook", "Sparrowwood", "Springbrook",
            "Stonebrook", "Stoneheart", "Stormbrook", "Summerbrook", "Sunwood", "Thornbrook", "Thornheart", "Timberbrook",
            "Valeheart", "Wildbrook", "Wildheart", "Willowbrook", "Willowheart", "Windbrook", "Windheart", "Wrenwood",
            "Yarrowbrook", "Acornwood", "Badgerbrook", "Brackenheart", "Brookstone", "Cloudwood", "Creekheart", "Dapplewood",
            "Dovewood", "Featherbrook", "Flintwood", "Foxbrook", "Groveheart", "Heronwood", "Larkbrook", "Moonwood"
    };
}
