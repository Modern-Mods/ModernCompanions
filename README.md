# Modern Companions (NeoForge 1.21.1)

![Header](https://i.imgur.com/V29Cq8E.jpeg)

Modern Companions is a NeoForge 1.21.1 port and rebrand of [Human Companions](https://www.curseforge.com/minecraft/mc-mods/human-companions), with new branding, Soul Gems, a Summoning Wand, custom weapons, optional magic companions, firearm specialists, and deeper companion progression.

Recruit human followers, equip them, shape their personalities, and take your own growing party into the world.

## What You Get

![Inventory/Curio](https://i.imgur.com/WLBY5hc.gif) 

- **Fourteen Core Roles:** Knight, Vanguard, Axeguard, Berserker, Scout, Archer, Arbalist, Beastmaster, Cleric, Alchemist, Stormcaller, Fire Mage, Lightning Mage, and Necromancer.
- **Optional Magic Roles:** When Iron’s Spellbooks or Ars Nouveau is installed, nine additional companions become available: Wizard, Sorcerer, Warlock, Witch, Hag, Cryomancer, Druid, Illusionist, and Battlemage.
- **TacZ Firearm Specialists:** Rare optional firearm companions specialize in Pistols, SMGs, Rifles, Shotguns, Snipers, Machine Guns, or Heavy weapons.
- **Spawn Gems:** All companion spawn eggs use class-colored gem artwork while retaining normal spawn-egg behavior.
- **Soul Gems:** Use the Companion Mover to preserve a companion’s identity, stats, equipment, and inventory while storing them as an item.
- **Custom Weapons:** Craft daggers, clubs, hammers, spears, quarterstaves, and glaives in vanilla materials, with optional bronze variants when supported.
- **RPG Stats and Leveling:** Strength, Dexterity, Intelligence, and Endurance affect combat, speed, XP gain, health, defense, and knockback resistance.
- **No Level Cap:** Companions can continue leveling indefinitely, with no hard party-size limit beyond practical server performance.
- **Custom Names and Skins:** Companions use expanded male and female name pools, including medieval and fantasy names. Use `/companionskin "NAME" URL` to assign an HTTP(S) skin.
- **295 Bundled Skins:** The full bundled male and female skin collection is available for random companion appearances.
- **Personality and Journal:** Companions have traits, backstories, Morale, Bond, age, favorite foods, and persistent journey statistics.
- **Curios and Sophisticated Backpacks:** Optional support adds Curios slots, rendered accessories, backpack storage, and native backpack upgrades/settings.
- **Companion Resources:** Stamina supports sprinting and melee pacing. Magic companions also use Mana.
- **Brewing:** Craft reusable vessels and brew Health, Regeneration, Stamina, Mana, Rejuvenation, and Shield potions.
- **Safety Controls:** Villager and PvP protection controls are available per companion and default to safe.

## Worldgen and Spawns

![Worldgen](https://i.imgur.com/ERYQEPk.jpeg)

- Companion buildings generate throughout the Overworld.
- Each generated building produces exactly one resident.
- Structure placement is spread across the world and can be configured.
- Use `/locate structure #modern_companions:companion_houses` to find the nearest companion structure.
- Use `/place structure modern_companions:<id>` to place a specific structure.

## Class Details

![Classes](https://i.imgur.com/1tquTk1.png)

- **Knight:** Balanced melee fighter using swords, clubs, and spears.
- **Vanguard:** Shielded tank with projectile protection, resistance support, increased health, and monster taunts.
- **Axeguard:** Heavy axe fighter built for powerful close-range attacks.
- **Berserker:** Gains offensive power as health drops and can cleave nearby enemies.
- **Scout:** Fast skirmisher with improved movement, jumping, fall protection, and backstab damage.
- **Archer:** Ranged bow specialist that automatically equips bows and arrows.
- **Arbalist:** Crossbow specialist using 1.21 charge, cooldown, and line-of-sight behavior.
- **Beastmaster:** Ranged fighter with a scaling animal companion.
- **Cleric:** Support fighter that heals allies and deals extra damage to undead.
- **Alchemist:** Uses beneficial potions on allies and harmful effects against enemies.
- **Stormcaller:** Trident fighter who calls lightning and gains strength after striking.
- **Fire Mage:** Uses precise, non-igniting fireballs and heavier blast attacks.
- **Lightning Mage:** Uses single-target lightning and storm-enhanced chain attacks.
- **Necromancer:** Fires wither skulls and summons temporary allied wither skeletons.

### Optional Magic Companions

Optional magic companions use the loaded mod’s actual spell systems rather than replacing them with custom fallback attacks.

- **Wizard:** Summons magical weapons and controls their active lifetime.
- **Sorcerer:** Elemental offensive caster.
- **Warlock:** Dark magic specialist.
- **Witch:** Hexes, curses, and battlefield control.
- **Hag:** Powerful debuff and damage caster.
- **Cryomancer:** Ice-themed control and damage magic.
- **Druid:** Nature-themed magical support and offense.
- **Illusionist:** Deception and ranged spell specialist.
- **Battlemage:** Close-range fighter with magical attacks.

Magic companions, summon gems, structures, and related content remain unavailable when the required optional mods are not installed.

### TacZ Specialists

TacZ firearm specialists are permanently assigned to one firearm category and only equip compatible guns.

Supported specialties:

- Pistol
- SMG
- Rifle
- Shotgun
- Sniper
- Machine Gun
- Heavy

Specialists use TacZ’s native shooting, ammunition, reload, and weapon behavior. Matching summon gems are available only when TacZ is installed.

## How It Plays

1. **Find:** Locate a companion structure in the Overworld.
2. **Hire:** Right-click an untamed companion with the exact requested food and resource items.
3. **Tame:** Fulfill both requested stacks to unlock the companion and its inventory.
4. **Command:** Use the companion screen to select Follow, Patrol, Guard, Alert, Hunting, Sprint, Pickup, Clear, Release, and Radius controls.
5. **Equip:** Give companions armor, weapons, tools, shields, torches, or lanterns through their inventory.
6. **Progress:** Companions gain XP from kills and improve as they level.
7. **Recover:** Feed injured companions, use beneficial potions, or revive fallen tamed companions with Resurrection Scrolls.
8. **Regroup:** Use the Summoning Wand to recall all living companions and Beastmaster pets in the current dimension.

Following companions use their saved Radius for wandering and recall. Radius values range from 2 to 128 blocks.

## Taming and Upkeep

Each untamed companion requests two specific stacks of food or resources.

- Resource tiers are weighted as 70% common, 25% uncommon, and 5% rare.
- Nether and ocean resources remain unavailable until the player has reached those areas.
- The final resource requirement is resolved on the first interaction and then saved.
- Empty-hand interactions produce companion dialogue.
- Wrong food produces a different response from the companion.
- Companions can eat cooked food, vegetables, fruit, honey, enchanted golden foods, and beneficial potions.
- Companions remember their favorite food and gain improved Bond and Morale rewards when fed it.
- Set `companion.lowHealthFoodThreshold` from `0.0` to `1.0` to control when companions eat or ask for food. The default is `0.5`.

## Inventory and Equipment

![Inventory](https://i.imgur.com/6iCmeTL.png)

Each companion has:

- A 7×9 personal inventory.
- Dedicated helmet, chestplate, leggings, boots, main-hand, and offhand slots.
- A 3D inventory preview.
- Automatic armor and weapon selection.
- Persistent equipment through relogging, capture, and redeployment.
- Owner-only villager and PvP safety controls.

Equipment rules keep companions from grabbing unsuitable items:

- Main hand: tools and weapons.
- Offhand: shields, torches, and lanterns.
- Manually equipped items remain protected from automatic replacement.

## Companion Resources and Potions

Every companion has 100 Stamina by default. Sprinting and successful melee attacks consume Stamina. At zero Stamina, sprinting pauses and melee attacks use a slower cadence.

Magic companions also have 100 Mana. Spell costs are applied only after a spell successfully casts.

Five reusable vessel shapes support six potion types:

- **Health:** Restores health immediately.
- **Regeneration:** Heals over time.
- **Stamina:** Restores companion Stamina.
- **Mana:** Restores companion Mana.
- **Rejuvenation:** Restores health, Stamina, and Mana over time.
- **Shield:** Grants temporary armor.

Brewing recipes are visible in JEI, and used potions return their matching empty vessels.

### Potion Recipes

Craft one of the five reusable empty vessels, fill it with a Water Bottle, and brew it with Nether Wart to create the matching Empty Vessel. Add the listed ingredients in a Brewing Stand to finish the potion.

![Empty vessel crafting recipes](https://i.imgur.com/tw4koGa.gif)

![Complete companion potion guide](https://i.imgur.com/5AGBSZR.gif)

Each potion returns its matching empty vessel after use. Health restores immediately, Regeneration heals over time, Stamina and Mana restore their matching companion resource, Rejuvenation restores all three over time, and Shield grants temporary armor.

### Stamina Configuration

- `companion.staminaEnabled`: Set to `false` to disable the Stamina system completely.
- `companion.sprintStaminaCost`: Stamina spent per sprinting tick. Default: `1`.
- `companion.meleeStaminaCost`: Stamina spent per successful melee hit. Default: `8`.

Both cost settings accept values from `0` to `100`. A value of `0` disables that individual drain.

When Stamina is disabled, companions keep a full Stamina pool, continue sprinting and attacking normally, and the Jade Stamina bar is hidden. Mana remains active for magic companions.

## Curios and Sophisticated Backpacks

Both integrations are optional.

### Curios

- Companions expose Curios slots when Curios is installed.
- Curio rendering can be toggled per companion.
- All TacZ firearm specialists support the same Curios integration.

### Sophisticated Backpacks

Equip a Sophisticated Backpack in a companion’s Curios back slot.

- Picked-up items are inserted into the backpack before the companion’s normal inventory.
- The Pack button opens Sophisticated Backpacks’ native storage screen.
- Backpack upgrades and settings remain available.
- Backpack equipment persists when companions are captured and redeployed.

## Personality, Morale, Bond, and Journal

![Journal](https://i.imgur.com/F8KB9kT.png)

The Journal displays:

- Traits and their effects.
- Backstory.
- Morale descriptor.
- Bond level and XP.
- Kills and major kills.
- Resurrections.
- Distance traveled with the owner.
- First hired day.
- Companion age.
- Favorite food.

Companions normally begin between 18 and 35 years old and age visually over time. Legacy companions receive missing personality data once without being repeatedly rerolled.

The Journal edit menu supports:

- Name
- Age
- Bio
- Skin URL

Name, Age, and Bio updates are owner-checked and persistent. Skin editing uses HTTP(S) URLs.

### Trait Effects

- **Brave:** More damage and closer following.
- **Cautious:** Greater following distance and slower movement.
- **Guardian:** Increased armor.
- **Reckless:** Increased movement speed and closer following.
- **Stalwart:** Knockback resistance.
- **Quickstep:** Increased movement and following speed.
- **Glutton:** Increased Bond XP from feeding.
- **Disciplined:** Increased XP gain and reduced Morale loss.
- **Lucky:** Chance to duplicate one kill drop.
- **Night Owl:** Damage and speed bonuses at night.
- **Sun-Blessed:** Damage and speed bonuses during the day.
- **Jokester:** Reduced Morale loss.
- **Melancholic:** Minor damage penalty at low Morale.
- **Devoted:** Increased armor and Bond XP.

## Items and Crafting

### Weapons

Modern Companions adds vanilla-style recipes for every custom weapon and material combination. Bronze variants appear when a compatible bronze mod is installed.

![Weapons](https://i.imgur.com/vJeU7FG.png)

![Weapons](https://i.imgur.com/wuyhvYn.png)

![Weapons](https://i.imgur.com/yfRNaFM.png)

### Companion Mover

The owner-only Companion Mover stores a companion as a glinting item while preserving its identity, UUID, inventory, equipment, stats, and personality.

![Companion Mover](https://i.imgur.com/wKsYkiP.png)

### Soul Gems

Soul Gems preserve a companion’s soul through the Companion Mover and allow later redeployment.

![Soul Gem](https://i.imgur.com/1FrL94k.png)

### Summoning Wand

The Summoning Wand recalls all living companions and Beastmaster pets in the current dimension to a safe location near the owner.

![Summoning Wand](https://i.imgur.com/OClm2Fj.png)

### Spawn Gems

All companion spawn eggs use class-colored gem artwork and are available on the Modern Companions creative tab. Survival acquisition is left to modpack makers and datapacks.

![Spawn Gems](https://i.imgur.com/nHlP3mX.png)

![Spawn Gems](https://i.imgur.com/Ddy3yEk.png)

### Resurrection Scroll

Tamed companions drop Resurrection Scrolls containing their saved stats, equipment, inventory, and personality data.

Activate a scroll with a Nether Star in the offhand, then use it on a block or fluid face to respawn the companion at that location.

![Resurrection Scroll](https://i.imgur.com/NV1urK6.png)

![Resurrection Scroll](https://i.imgur.com/K8Zl7ka.png)

Harmful effects and optional radiation are cleared from the resurrection data so a revived companion does not immediately repeat the same fatal condition.

## Attribute Enchantments

**Empower, Nimbility, Enlightenment, and Vitality** add Strength, Dexterity, Intelligence, and Endurance bonuses through companion armor.

Levels I–III are available.

![Attribute Enchantments](https://i.imgur.com/NDqdrXP.png)

Books can appear in dungeon, mineshaft, stronghold library, temple, buried treasure, and shipwreck loot.

## Configuration and Compatibility

Configurable companion behaviors include:

- Friendly fire.
- Player damage.
- Villager damage.
- Fall damage.
- Spawn armor.
- Spawn weapons.
- Low-health food behavior.
- Low-health food threshold.
- Stamina enablement and costs.
- Alert targeting: Alert automatically recognizes every entity registered as a Monster. `minecraft:creeper` is the one default entry in `excludedMobs`, so companions avoid Creepers out of the box; existing configurations receive this entry once on startup, and removing it lets companions fight Creepers. In single-player or as the host, open Mods → Modern Companions → Config → Alert to add registry entity IDs such as `minecraft:ender_dragon` or `example:dangerous_mob`; Java class names such as `EnderDragon.class` are not valid. Dedicated-server operators use the same server config.
- Taming and manual hunting: Mods → Modern Companions → Config now exposes the default food, healing-consumable, common/uncommon/rare taming-resource, and manual Hunt-mob lists as editable registry IDs. The in-game lists display the current vanilla values by default and accept registered mod items or entity types.
- House spacing.
- Trait, Bond, and Morale systems.

Optional compatibility includes:

- Iron’s Spellbooks.
- Ars Nouveau.
- TacZ.
- Curios.
- Sophisticated Backpacks.
- Jade.
- JEI/REI.
- Bronze weapon materials.
- Better Combat reach handling.
- Epic Fight: companions use its armature renderer, movement, melee timing, hit logic, and weapon movesets while retaining their roles, stamina, equipment, and safety rules. Class-valid held weapons remain equipped instead of being swapped with cargo every tick, and equipping a melee weapon restores Epic Fight's animated attack/chase pair. A companion holding a TacZ gun temporarily uses TacZ's native pose and firearm logic, then returns to Epic Fight animations when it holds another item; a gun in cargo does not disable Epic Fight melee. Every bundled weapon family has an Epic Fight capability category for its matching moveset.

Pack authors can extend `data/modern_companions/tags/entity_type/alert_unsafe.json` with unsafe entity ids. A higher-priority datapack can set `"replace": true` to supply the complete safety policy before `/reload`.

TacZ companion gun poses include a companion-only adaptation of [Epic Fight x TacZ Compat](https://github.com/Ardelhite/epic-tacz) by ImperialArchitects, licensed under the MIT License. Copyright (c) 2026 ImperialArchitects. Permission is hereby granted, free of charge, to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies, subject to including that copyright notice and permission notice. The software is provided “AS IS”, without warranty of any kind.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.219 or newer
- Java 21

## Credits

- [Human Companions — justinwon777](https://www.curseforge.com/minecraft/mc-mods/human-companions)
- [Basic Weapons — Khazoda](https://www.curseforge.com/minecraft/mc-mods/basicweapons)

**Take a squad with you, keep them fed, and bring your companions into every adventure.**
