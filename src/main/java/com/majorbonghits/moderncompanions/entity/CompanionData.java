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
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;

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
            Items.COOKED_RABBIT
    };

    /** Higher-tier foods/drinks companions can consume for healing but will not request while taming. */
    public static final Item[] EXTRA_HEAL_CONSUMABLES = new Item[]{
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_CARROT,
            Items.HONEY_BOTTLE
    };

    /** Non-food resources companions might demand during taming. */
    public static final Item[] RESOURCE_ITEMS = new Item[] {
            Items.COAL,
            Items.CHARCOAL,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.COPPER_INGOT,
            Items.DIAMOND,
            Items.EMERALD,
            Items.LAPIS_LAZULI,
            Items.REDSTONE,
            Items.QUARTZ,
            Items.AMETHYST_SHARD
    };

    private static final Set<Item> DISALLOWED_FOODS = Set.of(
            Items.SPIDER_EYE,
            Items.ROTTEN_FLESH,
            Items.BEEF,
            Items.PORKCHOP,
            Items.CHICKEN,
            Items.MUTTON,
            Items.RABBIT,
            Items.COD,
            Items.SALMON
    );

     public static final MutableComponent[] tameFail = new MutableComponent[]{
            Component.literal("I need more food."),
            Component.literal("Is that all you got?"),
            Component.literal("I'm still hungry."),
            Component.literal("Can I have some more?"),
            Component.literal("I'm going to need a bit more."),
            Component.literal("That's not enough."),
            // extra lines
            Component.literal("You call that a meal?"),
            Component.literal("My stomach didn't even notice that."),
            Component.literal("Nope. Still hungry."),
            Component.literal("I'm going to pretend that never happened. Try again."),
            Component.literal("Nice start. Now add about ten more of those."),
            Component.literal("I appreciate the effort, not the portion size."),
            Component.literal("You're going to have to commit harder than that."),
            Component.literal("That was a snack, not a meal."),
            Component.literal("I'm going to need a lot more if you want my loyalty."),
            Component.literal("My hunger bar barely moved.")
    };

    public static final MutableComponent[] notTamed = new MutableComponent[]{
            Component.literal("Do you have any food?"),
            Component.literal("I'm hungry."),
            Component.literal("Have you seen any food around here?"),
            Component.literal("I could use some food."),
            Component.literal("I wish I had some food."),
            Component.literal("I'm starving."),
            // extra lines
            Component.literal("Got any snacks on you? Asking for a friend. I'm the friend."),
            Component.literal("We could be best friends... if you had food."),
            Component.literal("I'll listen when the food starts talking."),
            Component.literal("You look like someone who carries snacks. Prove me right."),
            Component.literal("No food, no deal."),
            Component.literal("We can talk taming after we talk feeding."),
            Component.literal("Is there a delivery service around here? Preferably you."),
            Component.literal("I'm interviewing humans. Requirement: must bring food."),
            Component.literal("If you had food, this conversation would be going better."),
            Component.literal("Step one: food. Step two: maybe I'll like you.")
    };

    public static final MutableComponent[] WRONG_FOOD = new MutableComponent[]{
            Component.literal("That's not what I asked for."),
            Component.literal("I didn't ask for that."),
            Component.literal("Looks like you didn't understand my request."),
            Component.literal("Did you forget what I asked for?"),
            Component.literal("I don't remember asking for that"),
            // extra lines
            Component.literal("That's… boldly incorrect."),
            Component.literal("Are you even listening to me?"),
            Component.literal("Points for effort, not for accuracy."),
            Component.literal("Close. But also not close at all."),
            Component.literal("This is the opposite of what I wanted."),
            Component.literal("Creative choice. Still wrong, though."),
            Component.literal("Did your inventory slip or was that on purpose?"),
            Component.literal("I'm picky, not desperate."),
            Component.literal("I asked for food, not whatever that is."),
            Component.literal("Try again, but this time use your memory.")
    };

    public static final MutableComponent[] ENOUGH_FOOD = new MutableComponent[]{
            Component.literal("I have enough of that."),
            Component.literal("I don't want that anymore."),
            Component.literal("I want something else now."),
            // extra lines
            Component.literal("If I eat one more of those, I'll explode."),
            Component.literal("Variety would be nice, you know."),
            Component.literal("I am officially bored of that flavor."),
            Component.literal("No more of that, please. My taste buds are on strike."),
            Component.literal("I'm full on that. Emotionally and physically."),
            Component.literal("Do you have literally anything else?"),
            Component.literal("Thanks, but I'm good on those for the next century."),
            Component.literal("My stomach says no. My soul also says no."),
            Component.literal("I get it, you like that item. I don't anymore."),
            Component.literal("Try something new. Surprise me—in a good way.")
    };

    public static final Class<?>[] alertMobs = new Class<?>[]{
            Blaze.class,
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
            Raider.class
    };

    public static final Class<?>[] huntMobs = new Class<?>[]{
        Chicken.class,
        Cow.class,
        MushroomCow.class,
        Pig.class,
        Rabbit.class,
        Sheep.class
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
        Map<Item, Integer> food = new HashMap<>();
        Item foodItem = pickAllowedFood(random);
        Item resourceItem = pickResource(random);
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

    private static Item pickResource(Random random) {
        return RESOURCE_ITEMS[random.nextInt(RESOURCE_ITEMS.length)];
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModernCompanions.MOD_ID, path);
    }

    // English names (American/British)
    // male=0, female=1
    public static final String[][] firstNames = new String[][]{
            new String[]{
                    "Aaron", "Abel", "Abraham", "Adam", "Adrian", "Aidan", "Aiden", "Albert",
                    "Alfred", "Andrew", "Anthony", "Arthur", "Asher", "Austin", "Barrett", "Barry",
                    "Beau", "Benjamin", "Blake", "Bobby", "Brad", "Bradley", "Brandon", "Brent",
                    "Brett", "Brian", "Brody", "Bryan", "Caleb", "Calvin", "Cameron", "Carl",
                    "Carlos", "Casey", "Carter", "Cedric", "Chad", "Charles", "Charlie", "Christian",
                    "Christopher", "Clark", "Clayton", "Clifford", "Cody", "Colby", "Cole", "Colin",
                    "Collin", "Connor", "Conrad", "Corey", "Craig", "Damian", "Damien", "Damon",
                    "Daniel", "Darren", "Darryl", "David", "Dean", "Declan", "Dennis", "Derek",
                    "Derrick", "Desmond", "Devin", "Diego", "Dominic", "Donald", "Donovan", "Douglas",
                    "Drew", "Dustin", "Dylan", "Edward", "Edwin", "Eli", "Elias", "Elijah", "Elliot",
                    "Elliott", "Ethan", "Eugene", "Evan", "Everett", "Felix", "Fernando", "Finley",
                    "Finn", "Francis", "Francisco", "Frank", "Franklin", "Gabriel", "Gage", "Gareth",
                    "Gavin", "George", "Gerald", "Gilbert", "Glen", "Glenn", "Gordon", "Graham",
                    "Grant", "Grayson", "Greg", "Gregory", "Harley", "Harold", "Harrison", "Harry",
                    "Harvey", "Hayden", "Heath", "Hector", "Henry", "Hudson", "Hugh", "Hugo",
                    "Hunter", "Ian", "Isaac", "Isaiah", "Israel", "Jack", "Jackson", "Jacob",
                    "Jaden", "Jake", "James", "Jamie", "Jared", "Jason", "Jasper", "Javier",
                    "Jeff", "Jeffrey", "Jeremiah", "Jeremy", "Jerome", "Jesse", "Jesus", "Joel",
                    "John", "Johnny", "Jonah", "Jonathan", "Jordan", "Jorge", "Jose", "Joseph",
                    "Joshua", "Josiah", "Juan", "Jude", "Julian", "Julio", "Justin", "Kaden",
                    "Kai", "Kaleb", "Karl", "Kayden", "Keith", "Kelvin", "Kenneth", "Kevin",
                    "Kieran", "Kyle", "Landon", "Larry", "Lawrence", "Lee", "Leo", "Leon",
                    "Leonard", "Leroy", "Liam", "Logan", "Lonnie", "Louis", "Luca", "Lucas",
                    "Luis", "Luke", "Malcolm", "Manuel", "Marcus", "Mario", "Mark", "Marshall",
                    "Martin", "Mason", "Mateo", "Matthew", "Maurice", "Max", "Maximilian", "Maxwell",
                    "Micah", "Michael", "Miguel", "Miles", "Mitchell", "Morgan", "Nate", "Nathan",
                    "Nathaniel", "Neil", "Nelson", "Nicholas", "Nico", "Nolan", "Noah", "Norman",
                    "Oliver", "Omar", "Oscar", "Owen", "Parker", "Patrick", "Paul", "Peter",
                    "Philip", "Phillip", "Preston", "Quentin", "Quinn", "Rafael", "Ralph", "Ramon",
                    "Randall", "Randy", "Raphael", "Ray", "Raymond", "Reece", "Reed", "Reid",
                    "Rhys", "Ricardo", "Richard", "Rick", "Ricky", "Riley", "Roberto", "Robert",
                    "Rodney", "Roger", "Roland", "Roman", "Ronald", "Ronnie", "Ross", "Roy",
                    "Russell", "Ryan", "Samuel", "Scott", "Sean", "Sebastian", "Sergio", "Seth",
                    "Shane", "Shaun", "Shawn", "Silas", "Simon", "Spencer", "Stanley", "Stephen",
                    "Steven", "Stuart", "Terrence", "Theodore", "Thomas", "Timothy", "Todd", "Tom",
                    "Travis", "Trevor", "Tristan", "Troy", "Tyler", "Tyrone", "Victor", "Vincent",
                    "Warren", "Wayne", "Wesley", "Weston", "Wilfred", "Will", "William", "Wyatt",
                    "Xavier", "Zach", "Zachariah", "Zachary",
                    // Expanded male pool: common, regional, and historical names.
                    "Abner", "Adolfo", "Alaric", "Alberto", "Alden", "Alec", "Alejandro", "Alessandro",
                    "Alistair", "Amir", "Anders", "Andrei", "Angelo", "Ansel", "Anton", "Archibald",
                    "Archer", "Armand", "Armando", "Arno", "August", "Augustus", "Basil", "Benedict",
                    "Bennett", "Berkley", "Bernard", "Blaise", "Bo", "Boris", "Bruno", "Bryce",
                    "Callum", "Casimir", "Cedric", "Chester", "Cillian", "Clement", "Clive", "Coby",
                    "Colson", "Constantine", "Cullen", "Curtis", "Dante", "Darius", "Darwin", "Dashiell",
                    "Dawson", "Dorian", "Drake", "Duncan", "Edgar", "Edmund", "Eduardo", "Emanuel",
                    "Emilio", "Emmett", "Enrique", "Ernest", "Esteban", "Ezekiel", "Fabian", "Felipe",
                    "Frederick", "Gael", "Gideon", "Giovanni", "Griffin", "Hank", "Harlan", "Harris",
                    "Hendrik", "Holden", "Homer", "Horatio", "Igor", "Immanuel", "Ivan", "Jamal",
                    "Jared", "Joaquin", "Jonas", "Jovan", "Khalil", "Kirby", "Klaus", "Lars",
                    "Leander", "Leif", "Lionel", "Lucian", "Magnus", "Malachi", "Marcel", "Marco",
                    "Matthias", "Mauricio", "Merrick", "Milo", "Mohamed", "Nikolai", "Orlando", "Otto",
                    "Pascal", "Percival", "Pierce", "Rainer", "Roderick", "Santino", "Saul", "Simeon",
                    "Stefan", "Sullivan", "Thaddeus", "Valentin", "Vance", "Vaughan", "Waldo", "Wesley",
                    "Yannis", "Yusuf", "Zane", "Zavier",
                    // Medieval and fantasy-flavored additions keep the roster varied beyond modern names.
                    "Aelfric", "Aldous", "Alwyn", "Anselm", "Arcturus", "Arlen", "Arvid", "Athelstan",
                    "Balian", "Bartholomew", "Beorn", "Bran", "Brannon", "Cadoc", "Caelan", "Cahir",
                    "Cerdic", "Cian", "Cormac", "Dagobert", "Darragh", "Eadric", "Eamon", "Eldric",
                    "Elric", "Emeric", "Erec", "Everard", "Faelan", "Fenric", "Fintan", "Galahad",
                    "Garrick", "Godfrey", "Hadrian", "Halvard", "Hawthorne", "Ivar", "Jareth", "Kael",
                    "Kendric", "Leofric", "Loric", "Lothar", "Lucan", "Mordecai", "Nereus", "Oberon",
                    "Osric", "Ragnar", "Rhydderch", "Riven", "Roland", "Sable", "Sigurd", "Soren",
                    "Taran", "Tiberius", "Torin", "Tristram", "Ulric", "Valerian", "Varek", "Wulfric",
                    "Yorick", "Zorion"
            },
            new String[]{
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
                    // Expanded female pool: common, regional, and historical names.
                    "Adriana", "Agatha", "Alana", "Alba", "Alessandra", "Alma", "Amara", "Amira",
                    "Anika", "Annika", "Antonia", "Arabella", "Astrid", "Athena", "Aurelia", "Beatrix",
                    "Berenice", "Bernadette", "Blanca", "Bonnie", "Brielle", "Bruna", "Cadence", "Calliope",
                    "Carina", "Carmen", "Cecily", "Celina", "Charity", "Claudia", "Colette", "Constance",
                    "Cora", "Cordelia", "Cosima", "Dahlia", "Daphne", "Daria", "Davina", "Delia",
                    "Elara", "Elodie", "Elsie", "Ember", "Emilia", "Emmeline", "Enid", "Erika",
                    "Estella", "Etta", "Evangeline", "Fabiola", "Farah", "Fatima", "Flora", "Frida",
                    "Genevieve", "Giselle", "Greta", "Guinevere", "Hattie", "Henrietta", "Iliana", "Ines",
                    "Iona", "Isadora", "Ivana", "Jada", "Janelle", "Jasmin", "Jayla", "Joelle",
                    "Joyce", "Kaia", "Kalina", "Kamila", "Karina", "Karla", "Kendra", "Kiara",
                    "Klara", "Lana", "Larissa", "Leona", "Liliana", "Lorelei", "Luciana", "Luella",
                    "Mabel", "Marcella", "Maribel", "Marisol", "Marissa", "Marjorie", "Marla", "Marlene",
                    "Maura", "Melody", "Miranda", "Miriam", "Mireille", "Nadia", "Nadine", "Nala",
                    "Nellie", "Nerissa", "Odessa", "Opal", "Ophelia", "Paloma", "Penelope", "Petra",
                    "Ramona", "Regina", "Renata", "Rhea", "Rhiannon", "Roxanne", "Salma", "Selma",
                    "Seraphina", "Sonia", "Soraya", "Tabitha", "Tatiana", "Theodora", "Valeria", "Viola",
                    "Vivienne", "Wanda", "Ximena", "Yvette", "Zelda", "Zinnia",
                    // Medieval and fantasy-flavored additions keep the roster varied beyond modern names.
                    "Aeliana", "Aerin", "Aislinn", "Althea", "Amarantha", "Anwen", "Araminta", "Arwen",
                    "Aveline", "Azalea", "Briallen", "Brynhild", "Catriona", "Ceridwen", "Clarimond", "Cressida",
                    "Damaris", "Eirwen", "Elara", "Elowen", "Eowyn", "Eulalia", "Faelwen", "Fiora",
                    "Ginevra", "Guinevere", "Hesperia", "Honora", "Ilyria", "Isolde", "Jessamine", "Kerensa",
                    "Lavinia", "Leocadia", "Liora", "Lorelei", "Lyra", "Mabyn", "Melisande", "Morgana",
                    "Nerissa", "Nimue", "Ondine", "Oriane", "Rowena", "Sabriel", "Seraphine", "Sigrun",
                    "Sylvara", "Talindra", "Theodosia", "Ursula", "Valeria", "Vespera", "Winifred", "Ysolde",
                    "Zephyra", "Zorina"
            }
    };

    public static final String[] lastNames = new String[]{
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
            "Curtis", "Dalton", "Daniel", "Daniels", "Davidson", "Davis", "Dawson", "Day",
            "Dean", "Delaney", "Dennis", "Dixon", "Douglas", "Doyle", "Duncan", "Dunn",
            "Edwards", "Elliott", "Ellis", "Erickson", "Eriksen", "Evans", "Farrell", "Ferguson",
            "Fernandez", "Fisher", "Fitzgerald", "Fleming", "Fletcher", "Flores", "Ford", "Foster",
            "Fowler", "Fox", "Francis", "Franklin", "Freeman", "Gallagher", "Gardner", "Garner",
            "Garcia", "Garrison", "George", "Gibbs", "Gibson", "Gilbert", "Gill", "Glover",
            "Gonzalez", "Goodman", "Gordon", "Graham", "Grant", "Graves", "Gray", "Green",
            "Greene", "Gregory", "Griffin", "Griffiths", "Hall", "Hamilton", "Hansen", "Hanson",
            "Harper", "Harris", "Harrison", "Hart", "Harvey", "Hawkins", "Hayes", "Haynes",
            "Henderson", "Henry", "Hernandez", "Hicks", "Hill", "Hines", "Hodges", "Hoffman",
            "Holland", "Holmes", "Holt", "Hopkins", "Horton", "Howard", "Howe", "Hudson",
            "Hughes", "Hunt", "Hunter", "Ingram", "Jackson", "Jacobs", "James", "Jarvis",
            "Jenkins", "Jennings", "Jensen", "Johnson", "Johnston", "Jones", "Jordan", "Kane",
            "Keller", "Kelley", "Kelly", "Kennedy", "Khan", "King", "Kirk", "Klein",
            "Knight", "Lambert", "Lane", "Lang", "Lawrence", "Lawson", "Leach", "Lee",
            "Lewis", "Little", "Lloyd", "Logan", "Long", "Lopez", "Lowe", "Lucas",
            "Lynch", "Lyons", "MacDonald", "Madden", "Manning", "Marks", "Marsh", "Marshall",
            "Martin", "Martinez", "Mason", "Matthews", "Maxwell", "May", "McBride", "McCarthy",
            "McCormick", "McDonald", "McGee", "McGrath", "McGregor", "McKenzie", "McLean", "McMillan",
            "Medina", "Mendez", "Meyer", "Miller", "Mills", "Mitchell", "Moody", "Moore",
            "Morales", "Morgan", "Morris", "Morrison", "Morton", "Moss", "Murphy", "Murray",
            "Myers", "Nelson", "Newman", "Newton", "Nichols", "Nicholson", "Nixon", "Nolan",
            "Norman", "Norris", "O'Brien", "O'Connor", "O'Neill", "Oliver", "Olson", "Ortiz",
            "Owens", " Page", "Palmer", "Parker", "Patel", "Patrick", "Patterson", "Payne",
            "Pearce", "Pearson", "Pena", "Perez", "Perkins", "Perry", "Peters", "Peterson",
            "Phillips", "Pierce", "Poole", "Porter", "Potter", "Powell", "Powers", "Price",
            "Quinn", "Ramirez", "Ramos", "Randall", "Ray", "Reed", "Rees", "Reese",
            "Reid", "Reyes", "Reynolds", "Rhodes", "Rice", "Richards", "Richardson", "Riley",
            "Rivers", "Robbins", "Roberts", "Robertson", "Robinson", "Rodgers", "Rodriguez", "Rogers",
            "Rose", "Ross", "Rowe", "Ruiz", "Russell", "Ryan", "Salazar", "Sanders",
            "Sanderson", "Sandoval", "Santiago", "Saunders", "Schmidt", "Scott", "Sharp", "Shaw",
            "Sheffield", "Shelton", "Short", "Silva", "Simmons", "Simpson", "Singh", "Sloan",
            "Smith", "Snyder", "Spencer", "Stanley", "Stephens", "Stevens", "Stewart", "Stone",
            "Sullivan", "Summers", "Sutton", "Taylor", "Terry", "Thomas", "Thompson", "Thornton",
            "Todd", "Torres", "Townsend", "Tran", "Tucker", "Turner", "Tyler", "Vasquez",
            "Vaughn", "Vazquez", "Wade", "Wagner", "Walker", "Wallace", "Walsh", "Walters",
            "Ward", "Warren", "Washington", "Waters", "Watkins", "Watson", "Watts", "Weaver",
            "Webb", "Weber", "Welch", "Wells", "West", "Wheeler", "White", "Whitaker",
            "Whitehead", "Whitfield", "Williams", "Williamson", "Willis", "Wilson", "Wise", "Wolfe",
            "Wong", "Wood", "Woods", "Wright", "Wyatt", "Young", "Zimmerman",
            // Expanded surname pool keeps new companions from repeating family names quickly.
            "Abbott", "Acker", "Aguilar", "Albright", "Aldridge", "Alvarez", "Ambrose", "Andrade",
            "Applegate", "Archer", "Atwood", "Baldwin", "Barlow", "Beasley", "Becker", "Bishop",
            "Blackwell", "Blanchard", "Bolton", "Bonner", "Bourne", "Bradshaw", "Branson", "Bray",
            "Bright", "Browning", "Bruno", "Buchanan", "Burgess", "Burton", "Calder", "Callahan",
            "Cannon", "Carey", "Carmichael", "Carney", "Chase", "Christie", "Church", "Clancy",
            "Clay", "Cline", "Compton", "Conley", "Conway", "Corbett", "Cortez", "Crawford",
            "Crosby", "Cullen", "Davenport", "Decker", "Delgado", "Dempsey", "Devereux", "Devlin",
            "Donahue", "Dorsey", "Drake", "Draper", "Duffy", "Eaton", "Ellington", "Emerson",
            "Esposito", "Faulkner", "Finch", "Finley", "Fitzpatrick", "Foley", "Forbes", "Foreman",
            "Franco", "Gallant", "Gentry", "Gerard", "Gibbons", "Giles", "Goodwin", "Grady",
            "Granger", "Greer", "Gresham", "Hammond", "Hancock", "Harding", "Harlow", "Harrington",
            "Hatcher", "Hayward", "Heath", "Hensley", "Herrera", "Hester", "Higgins", "Hobbs",
            "Holloway", "Hooper", "Irwin", "Jarrett", "Jefferson", "Kaufman", "Kendrick", "Kincaid",
            "Kirby", "Lafayette", "Larsen", "Latham", "Levine", "Lindsey", "Locke", "Maddox",
            "Maguire", "Malone", "Marlow", "Mercer", "Merritt", "Montague", "Montoya", "Morrissey",
            "Navarro", "Noble", "Norton", "Osborne", "Pace", "Pacheco", "Parks", "Parsons",
            "Pena", "Phelps", "Prescott", "Quincy", "Rafferty", "Randolph", "Rasmussen", "Roth",
            "Rowland", "Royce", "Sampson", "Savage", "Sawyer", "Schneider", "Serrano", "Sinclair",
            "Sparks", "Stafford", "Stanton", "Stark", "Sterling", "Strickland", "Sweeney", "Tanner",
            "Tate", "Thayer", "Underwood", "Valdez", "Vega", "Vincent", "Waller", "Warner",
            "Whitmore", "Wilkins", "Winters", "Wolfe", "Yates", "York", "Zamora",
            // Medieval and fantasy-flavored surnames support stronger settlement and adventurer themes.
            "Ashborne", "Ashcombe", "Ashdown", "Ashenford", "Blackbriar", "Blackmere", "Blackthorn", "Bloodgood",
            "Brightwater", "Bronzewood", "Cinderfall", "Crowhaven", "Dawnmere", "Dreadmoor", "Dragonbane", "Duskwood",
            "Eaglecrest", "Emberfall", "Fairbairn", "Fairchild", "Fallowmere", "Frostborne", "Goldbranch", "Goldcrest",
            "Gravewell", "Greymantle", "Hallowmere", "Hawkridge", "Ironbark", "Ironhand", "Kingsley", "Knightfall",
            "Longshadow", "Moonbrook", "Mooncrest", "Mournwell", "Oakenshield", "Ravencrest", "Ravenmere", "Redwyne",
            "Rosethorn", "Runebrook", "Silverbranch", "Silverkeep", "Starfall", "Stormvale", "Sunhaven", "Thornfield",
            "Thornwall", "Truehart", "Valebrook", "Valorborn", "Wildermere", "Windrider", "Wintermere", "Wolfhart",
            "Wyrmwood", "Wyvernhall", "Yewshade", "Zephyrfall"
    };
}
