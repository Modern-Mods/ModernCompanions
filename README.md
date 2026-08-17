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
- **Bond Command:** Owners and operators can use `/companions bond "NAME"` to set a loaded companion to maximum Bond (Bond V).
- **295 Bundled Skins:** The full bundled male and female skin collection is available for random companion appearances.
- **Personality and Journal:** Companions have traits, backstories, Morale, Bond, age, favorite foods, and persistent journey statistics.
- **Companion Voices:** Five gender-matched voice actors provide greetings, confirmations, refusals, combat callouts, pain, death, idle, and job-completion cues. Voice playback is configurable, duplicate callouts are suppressed, and enemy announcements are rate-limited.
- **Curios and Sophisticated Backpacks:** Optional support adds Curios slots, rendered accessories, backpack storage, and native backpack upgrades/settings.
- **Companion Resources:** Stamina supports sprinting and melee pacing. Magic companions also use Mana.
- **Native Magic Equipment:** Iron's and Ars Nouveau caster items can be used by magic companions, including staffs, wands, spellbooks, scrolls, and magical weapons. A magical companion's dedicated spellbook slot persists the native book and lets its active spells supplement the companion's learned repertoire. Spells face their target during a visible wind-up and use the loaded native cast duration when available. Iron's Max Mana, Mana Regeneration, Spell Power, school power/resistance, cooldown, cast-time, casting-speed, summon-damage, and spell-resistance attributes—and Ars Max Mana, Mana Regeneration, Spell Damage, and Warding—apply from armor, Curios, and held gear.
- **Brewing:** Craft reusable vessels and brew Health, Regeneration, Stamina, Mana, Rejuvenation, and Shield potions.
- **Living Jobs:** Lumberjacks, Farmers, Hunters, Miners, Fishers, and Chefs can search, travel, work, collect, deliver, and resume jobs. Jobs are experimental and hidden by default.
- **Safety Controls:** Villager and PvP protection controls are available per companion and default to safe.

## Recent Updates

- Added configurable Alert exclusions, taming food/resource lists, manual Hunt targets, low-health food thresholds, Stamina costs, voice mode/volume, automatic equipment, and Radius-based teleport leashes.
- Added Epic Fight combat/rendering compatibility, including companion weapon capabilities and TacZ gun pose handoff.
- Added TacZ firearm specialists with native gun, ammunition, reload, and category-specific equipment behavior.
- Fixed spawn loadouts so class weapons enter the live hand slots, expanded offhand support to Totems of Undying and carried lights, and added rare standard-size randomly dyed Sophisticated Backpacks when the optional integration is present.
- Cryomancer now approaches normally and uses its AoE only against targets within five blocks; it never retreats to cast the AoE.
- Added resumable profession goals, delivery chests, job status reporting, safe worker actions, and the Assignment Wand.
- Added cosmetic armor storage and per-slot equipment rendering controls without changing functional armor.

## Worldgen and Spawns

![Worldgen](https://i.imgur.com/ERYQEPk.jpeg)

- Companion buildings generate throughout the Overworld.
- Each generated building produces exactly one resident.
- Cleric houses, churches, and mage towers with Cleric, Fire Mage, Lightning Mage, or Necromancer residents generate whether or not Iron's Spellbooks or Ars Nouveau is installed.
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
- **Cleric:** Uses strong, mana-costing single-target owner/ally/self heals with a cast wind-up, mana-costing holy sparks, and a mana-gated nearby blessing before falling back to melee.
- **Alchemist:** Throws useful beneficial splash potions at allies and harmful splash potions only at visible valid enemies, with melee fallback at close range.
- **Stormcaller:** Trident fighter who calls lightning and gains strength after striking.
- **Fire Mage:** Uses Iron's/Ars spells when available, otherwise precise non-igniting fireballs and heavier blast attacks.
- **Lightning Mage:** Uses Iron's/Ars spells when available, otherwise single-target lightning and storm-enhanced chain attacks.
- **Necromancer:** Uses Iron's/Ars spells when available, otherwise fires wither skulls and summons temporary allied wither skeletons.

### Optional Magic Companions

Optional magic companions use the loaded mod’s actual spell systems rather than replacing them with custom fallback attacks.

- **Wizard:** Summons magical weapons and controls their active lifetime.
- **Sorcerer:** Elemental offensive caster.
- **Warlock:** Dark magic specialist.
- **Witch:** Hexes, curses, and battlefield control.
- **Hag:** Powerful debuff and damage caster.
- **Cryomancer:** Ice-themed control and damage magic; approaches targets normally and only casts its AoE within five blocks without retreating to cast.
- **Druid:** Nature-themed magical support and offense.
- **Illusionist:** Deception and ranged spell specialist.
- **Battlemage:** Close-range fighter with magical attacks.

The nine optional magic companions, their summon gems, and their spell-dependent content remain unavailable when neither optional magic mod is installed. Cleric, Fire Mage, Lightning Mage, Necromancer, their summon eggs, and their houses/towers remain available and use their bundled bespoke combat kits without those mods.

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
- Magical companions also have a dedicated persisted spellbook slot; its native active spells are used alongside their learned spells.
- A 3D inventory preview.
- Automatic armor and weapon selection.
- Optional automatic gear equip from the companion inventory (disabled by default).
- Separate cosmetic armor slots with per-slot visibility toggles; cosmetic items change appearance without replacing functional equipment.
- Persistent equipment through relogging, capture, and redeployment.
- Owner-only villager and PvP safety controls.

Equipment rules keep companions from grabbing unsuitable items:

- Main hand: tools and weapons.
- Offhand: Totems of Undying, shields, torches, and lanterns.
- Spawn weapons are placed directly in the live main-hand slot, so they render and function immediately instead of remaining cargo.
- Torches and lanterns use the normal offhand renderer and emit temporary light when the companion has an open space above it and normal mob-griefing/owner protection permits the temporary block; the light follows the companion and is removed when the item or companion is removed.
- Manually equipped items remain protected from automatic replacement.

## Companion Resources and Potions

Every companion has 100 Stamina by default. Sprinting and successful melee attacks consume Stamina. At zero Stamina, sprinting pauses and melee attacks use a slower cadence.

Magic companions also have 100 Mana, which regenerates slightly faster during and after combat. Spell costs are applied only after a spell successfully casts. Cleric support spells and holy sparks cost 6 Mana; legacy fallback mages use the same shared Mana pool for their projectiles and summons.

Five reusable vessel shapes support six potion types:

- **Health:** Restores health immediately.
- **Regeneration:** Heals over time.
- **Stamina:** Restores companion Stamina.
- **Mana:** Restores companion Mana.
- **Rejuvenation:** Restores health, Stamina, and Mana over time.
- **Shield:** Grants temporary armor.

Brewing recipes are visible in JEI, and used potions return their matching empty vessels.

### Health Packs

Health Packs are rare, non-craftable chest loot found in dungeons, mineshafts, villages, temples, strongholds, ships, and other structures. Use one on yourself, another player, or a Modern Companions companion to restore that target to full health instantly. Each successful use consumes one pack and starts a 30-second cooldown.

### Potion Recipes

Craft one of the five reusable empty vessels, fill it with a Water Bottle, and brew it with Nether Wart to create the matching Empty Vessel. Add the listed ingredients in a Brewing Stand to finish the potion.

![Empty vessel crafting recipes](https://i.imgur.com/tw4koGa.gif)

![Complete companion potion guide](https://i.imgur.com/5AGBSZR.gif)

Each potion returns its matching empty vessel after use. Health restores immediately, Regeneration heals over time, Stamina and Mana restore their matching companion resource, Rejuvenation restores all three over time, and Shield grants temporary armor.

### Alchemist Recipes

Craft a Blank Recipe from paper and a glass bottle, then combine it shapelessly with a supported vanilla potion. The cyan Recipe item records that potion's vanilla ingredients and displays them in its tooltip. The Alchemist itself does not consume glass bottles: it creates splash potions directly, taking 200 ticks (half a Brewing Stand's normal time) per brew. Potions and splash potions stack to 64 only inside the Alchemist's inventory. The first configured recipe in the inventory is active; the Alchemist brews splash potions automatically and uses them for emergency healing, party buffs, crowd control, and offensive support before falling back to melee range.

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

- With Sophisticated Backpacks and Curios installed, a newly spawned companion has a deliberately rare chance to receive a standard-size backpack with two random dye colors.
- Picked-up items are inserted into the backpack before the companion’s normal inventory.
- Placement Wand captures insert Soul Gems into the player’s equipped backpack before falling back to empty player-inventory slots, and targeted use reads them back from that equipped backpack first; backpacks merely carried in inventory are ignored.
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
- Model (Steve or Alex)

Name, Age, Bio, and model updates are owner-checked and persistent. Skin editing uses HTTP(S) URLs.

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

Modern Companions adds vanilla-style recipes for every custom weapon and material combination. Bronze variants appear when a compatible bronze mod is installed. All custom weapons accept the standard sword-compatible combat, durability, and repair enchantments in survival.

![Weapons](https://i.imgur.com/vJeU7FG.png)

![Weapons](https://i.imgur.com/wuyhvYn.png)

![Weapons](https://i.imgur.com/yfRNaFM.png)

### Companion Mover

The owner-only Companion Mover stores a companion as a glinting item while preserving its identity, UUID, inventory, equipment, stats, and personality.

![Companion Mover](https://i.imgur.com/wKsYkiP.png)

### Soul Gems

Soul Gems preserve a companion’s soul through the Companion Mover and allow later redeployment.

### Beastmaster's Wand and Soul Orbs

Craft the Beastmaster's Wand to capture any non-hostile mob. Captured mobs retain their entity data and UUID in a cyan-named Soul Orb. Use an orb on its owner's Beastmaster to swap pets—the replaced pet becomes the returned Soul Orb—or right-click the ground to release the stored mob. Soul Orbs are consumed when used. Beastmasters also preserve their pet's full state through Companion Mover storage and Resurrection Scroll revival.

Upgrade the Beastmaster's Wand in a smithing table with a Netherite Upgrade Smithing Template and Ancient Debris to create the Empowered Beastmaster's Wand. It captures any hostile `Enemy` mob except entities in NeoForge's boss tag, including future tagged bosses. The capture uses the mob's vanilla XP reward as its difficulty-scaled cost, requires an empty inventory slot, and removes neither the mob nor XP when the capture cannot complete. Apply the resulting hostile Soul Orb to the owner's Beastmaster at level 20 or higher; the transfer is atomic and preserves the hostile mob's UUID, full NBT, variant, equipment, custom name, and inventory. Hostile Soul Orbs cannot be released as free-world mobs.

### Soul Reforging

Reforge an owned companion's traits without losing its bond, backstory, equipment, or memories:

1. Capture the companion with the Companion Mover.
2. Craft and place a Companion Table, then open it.
3. Place the Soul Gem in the top-left slot, Lapis Lazuli in the top-right slot, an Echo Shard in the lower-left slot, and a trait catalyst in the lower-right slot.
4. Left-click one of the three rolled traits to replace the primary trait, or right-click it to replace the secondary trait.

The ritual consumes one Lapis Lazuli, one Echo Shard, the catalyst, and XP levels. Secondary traits require Bond I and 5 levels; primary traits require Bond II and 15 levels. Catalysts follow Minecraft themes: Blaze Rods favor Brave/Reckless, Turtle Scutes favor Cautious/Stalwart, Rabbit's Feet favor Quickstep/Lucky, Phantom Membranes favor Night Owl, Glowstone favors Sun-Blessed, Prismarine favors Guardian/Devoted, Cake favors Glutton/Jokester, and Soul Soil favors Melancholic.

The Companion Table uses the enchanting table's animated book, block textures, and particle effects. Its recipe is the enchanting-table pattern with an Echo Shard replacing the center Obsidian: Diamond–Book–Diamond, Obsidian–Echo Shard–Obsidian, Obsidian–Obsidian–Obsidian.

![Soul Gem](https://i.imgur.com/1FrL94k.png)

### Summoning Wand

The Summoning Wand recalls all living companions and Beastmaster pets in the current dimension to a safe location near the owner.

![Summoning Wand](https://i.imgur.com/OClm2Fj.png)

### Placement Wand

The Placement Wand deploys every Soul Gem in the equipped Sophisticated Backpack first, then the player's inventory, around a targeted block. Sneak-use it in the air to store nearby owned companions as Soul Gems; the equipped backpack is filled first, then empty player-inventory slots, and any excess companions remain in the world. Backpacks merely carried in player inventory are ignored.

### Assignment Wand

The Assignment Wand links an owned companion to a delivery container or a saddled mount. Right-click the companion with the wand, then use it on a container or on the owned mount. The companion delivers job output there and can withdraw raw inputs when a job supports it.

When the owner is actively riding, a following companion mounts its assigned eligible mount, or the nearest other owned eligible mount when none is assigned. An explicitly assigned active horse may share its seat with the player; otherwise the companion rides a separate mount. A separate mount follows the owner's vehicle with its native movement and jump attributes, and a bounded speed multiplier prevents a low-stat horse from crawling behind a faster owner mount; shared-seat horses remain vanilla-controlled. The companion dismounts when the owner dismounts. An assigned mount is led to the companion while traveling or standing. If the companion is ordered to sit, it uses a safe nearby fence when available, placing an oak fence only where normal player placement/protection checks allow it; moving again restores the companion-held lead and removes only an unchanged fence that Modern Companions placed, without dropping it.

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

## Jobs

Jobs are disabled in the player-facing screen by default while the system remains experimental. Set `showJobsButton = true` under `[jobs]` in `config/modern_companions-common.toml` to expose the Jobs button. The Jobs category is intentionally hidden from the native config screen for now.

Available jobs:

- **Lumberjack:** Finds mature natural trees, chops them with an axe, collects logs, and replants when possible.
- **Farmer:** Harvests mature crops, replants the matching seed on valid farmland or soul sand, and can use carried bone meal when enabled.
- **Hunter:** Tracks configured hunt targets with a sword, axe, bow, or crossbow and collects the results.
- **Miner:** Surveys the work area and safely tunnels to configured ore targets with a pickaxe.
- **Fisher:** Finds water, fishes with a fishing rod, and collects catches.
- **Chef:** Uses raw food, cooking recipes, and nearby campfires, soul campfires, furnaces, or smokers. Furnaces and smokers need fuel.

To use a job, assign it in the Jobs screen, give the companion the required tool, and bind a delivery container with the Assignment Wand. The **Work** control starts or pauses the job. Job phases and waiting reasons are shown in the worker panel, and the Jobs screen shows profession counters including Hunter kills and Chef meals cooked; combat, blocked routes, full inventories, and unavailable chests preserve the job checkpoint for later resumption.

The worker plan is server-owned and versioned. It preserves the active field cell, tree/log queue and species-correct replant debt, ore target and ordered tunnel operations, fishing shore/catch state, Hunter target/drop claims, or Chef workstation batch across goal interruption and entity reload. Miner routes distinguish walkable feet cells from blocks approved for controlled excavation, so buried ore can be reached without mining the support floor or digging straight down. Delivery keeps the pre-delivery work unit, releases the chest claim after each attempt, and returns the companion to its saved checkpoint before the profession resumes. Protection denials, unsafe or unloaded work, occupied replant spots, missing fuel/tools, and full storage remain visible waiting states instead of silently advancing the plan.

## Configuration

Open **Mods → Modern Companions → Config** for the player-facing settings. Dedicated servers use the common server config. Values below are the shipped defaults and accepted ranges.

### Worldgen

| Key | Default | Description |
| --- | ---: | --- |
| `averageHouseSeparation` | `20` | Average chunk separation between companion houses; minimum `11`. |

### Companion

| Key | Default | Description |
| --- | ---: | --- |
| `friendlyFireCompanions` | `false` | Allow companions and Beastmaster pets to damage each other. |
| `friendlyFirePlayer` | `true` | Allow a companion to damage its owner. |
| `fallDamage` | `true` | Allow fall damage. |
| `spawnArmor` | `true` | Give newly spawned companions random armor. |
| `spawnWeapon` | `true` | Give newly spawned companions a weapon. |
| `autoEquip` | `false` | Automatically equip suitable gear from the companion inventory. |
| `teleportLeash` | `false` | Teleport a following companion to a safe spot after it exceeds its selected Radius by 5 blocks and fails to close distance. |
| `teleportDelayTicks` | `20` | Ticks a companion tries to close distance before emergency teleporting; `20` ticks is 1 second. |
| `teleportCooldownTicks` | `40` | Minimum ticks between emergency teleports; `40` ticks is 2 seconds. |
| `baseHealth` | `20` | Base health before spawn variance; minimum `5`. |
| `lowHealthFood` | `true` | Let companions eat and ask for food when low on health. |
| `lowHealthFoodThreshold` | `0.5` | Health fraction for low-health food behavior; `0.0`–`1.0`. |
| `staminaEnabled` | `true` | Enable the Stamina system. |
| `sprintStaminaCost` | `1` | Stamina per sprinting tick; `0`–`100`, where `0` disables sprint drain. |
| `meleeStaminaCost` | `8` | Stamina per successful melee hit; `0`–`100`, where `0` disables melee drain. |
| `creeperWarning` | `true` | Warn about and avoid nearby Creepers. |
| `voiceMode` | `FULL` | `FULL` plays all cues, `LIMITED` keeps pain/death/idle cues, and `OFF` disables custom companion sounds. |
| `voiceVolume` | `80` | Custom voice volume as a percentage; `0`–`100`. |

### Taming, hunting, and Alert

Lists use registry IDs. Item lists accept IDs such as `minecraft:bread`; entity lists accept IDs such as `minecraft:goat`. Java class names are not valid.

| Key | Default | Description |
| --- | --- | --- |
| `allFoods` | See defaults below | Configured foods companions may request, select as favorites, and eat for healing. Safe standard foods from other mods are included automatically until this list is changed; then this list is authoritative. |
| `recruitmentRequirements` | Empty | Optional exact rows formatted `companion_id\|item_id\|count`; add multiple rows for multiple required items. `*` applies to every companion without an exact row. |
| `extraHealConsumables` | See defaults below | Healing items companions may eat but never request for taming; may be empty. |
| `commonResourceItems` | See defaults below | Common taming-resource pool. |
| `uncommonResourceItems` | See defaults below | Uncommon taming-resource pool. |
| `rareResourceItems` | See defaults below | Rare taming-resource pool. |
| `huntMobs` | See defaults below | Entity IDs targeted by the manual Hunt control; may be empty. |
| `excludedMobs` | `minecraft:creeper` | Entity IDs excluded from Alert targeting; may be empty. Alert otherwise recognizes registered Monster entities. Existing configs receive the Creeper default once. |

`creeperDefaultMigrated` is an internal migration marker and should not be edited.

Default lists:

```text
allFoods = minecraft:cookie, minecraft:bread, minecraft:melon_slice, minecraft:apple, minecraft:sweet_berries, minecraft:carrot, minecraft:baked_potato, minecraft:cooked_salmon, minecraft:cooked_cod, minecraft:cooked_mutton, minecraft:cooked_porkchop, minecraft:cooked_beef, minecraft:cooked_chicken, minecraft:pumpkin_pie, minecraft:glow_berries, minecraft:potato, minecraft:beetroot, minecraft:dried_kelp, minecraft:cooked_rabbit
recruitmentRequirements = "modern_companions:archer|minecraft:bread|3", "modern_companions:archer|minecraft:iron_ingot|2"
extraHealConsumables = minecraft:golden_apple, minecraft:enchanted_golden_apple, minecraft:golden_carrot, minecraft:honey_bottle, minecraft:mushroom_stew, minecraft:beetroot_soup, minecraft:rabbit_stew
commonResourceItems = minecraft:coal, minecraft:charcoal, minecraft:copper_ingot, minecraft:iron_ingot, minecraft:redstone, minecraft:lapis_lazuli, minecraft:flint, minecraft:clay_ball, minecraft:string, minecraft:leather, minecraft:bone, minecraft:feather
uncommonResourceItems = minecraft:gold_ingot, minecraft:amethyst_shard, minecraft:slime_ball, minecraft:gunpowder, minecraft:glowstone_dust, minecraft:prismarine_shard, minecraft:prismarine_crystals, minecraft:ender_pearl, minecraft:obsidian
rareResourceItems = minecraft:diamond, minecraft:emerald, minecraft:blaze_rod, minecraft:magma_cream
huntMobs = minecraft:chicken, minecraft:cow, minecraft:pig, minecraft:rabbit, minecraft:sheep, minecraft:goat
```

### Currencies

Currencies are enabled by default, appear in vanilla chest loot such as dungeons, mineshafts, temples, and villages, and are listed in the dedicated Modern Companions creative tab. Set `enabled = false` under `[currencies]` to disable their loot, creative-tab entries, interactions, conversions, and JEI trade display.

| Key | Default | Range / description |
| --- | ---: | --- |
| `lootDisperse` | `25` | Percent chance for each vanilla chest loot table to receive currency; `0`-`100`. |
| `lootRolls` | `1` | Currency stacks added when a chest receives currency; `1`-`5`. |
| `lootMinCount` / `lootMaxCount` | `1` / `3` | Per-stack item-count range; `1`-`64`. |
| `cardLootChance` | `2` | Percent chance for one rare Credit Card in each vanilla chest loot table; `0`-`100`. Its starting balance is randomized from `5` to `7,500`. |
| `tinValue` through `goldStackValue` | `1`, `5`, `10`, `25`, `100`, `500`, `5000` | Player-visible physical-denomination values; each accepts `0` or greater. |
| `tradeRecipes` | Empty | JEI-only rows formatted `first_id|first_count|second_id|second_count|output_id|output_count`; use `-|0` for no second input. Unknown modded item IDs are skipped until that mod is loaded. |

Credit Cards are non-stackable wallets with a persistent UUID and `long` balance. Click physical currency onto a card to deposit it, shift-click physical currency to deposit into the highest-balance card in the player's inventory, or click one card onto another to transfer and consume the source card. The server-side `CurrencyService.pay` API selects the lowest-balance card that can cover a cost; the card remains and only the cost is deducted. Credit Cards have no crafting recipe.

Physical conversion recipes use the configured values from the same service and preserve value exactly, including multi-output conversions such as `5 Silver -> 2 Gold`. A recipe requiring more than the nine slots in a vanilla crafting grid is intentionally unavailable. For example: `minecraft:emerald|1|-|0|modern_companions:dollar|1` displays an emerald-for-dollar trade in JEI. These rows describe display recipes; they do not inject trades into villagers.

Alert can also be extended by pack authors through `data/modern_companions/tags/entity_type/alert_unsafe.json`. A higher-priority datapack can use `"replace": true` to provide the complete safety policy before `/reload`.

### Personality

| Key | Default | Range / description |
| --- | ---: | --- |
| `traitsEnabled` | `true` | Enable primary and secondary birth traits. |
| `secondaryTraitChance` | `40` | Percent chance of a secondary trait; `0`–`100`. |
| `bondEnabled` | `true` | Enable Bond/Loyalty. |
| `moraleEnabled` | `true` | Enable Morale and its small performance effects. |
| `bondTickInterval` | `1200` | Ticks between passive Bond XP awards near the owner; minimum `20`. |
| `bondTimeXp` | `5` | Bond XP per passive interval; `0`–`10000`. |
| `bondFeedXp` | `15` | Bond XP when fed; `0`–`10000`. |
| `bondResurrectXp` | `80` | Bond XP when resurrected; `0`–`100000`. |
| `moraleFeedDelta` | `0.05` | Morale change when fed; `-1.0`–`1.0`. |
| `moraleNearDeathDelta` | `-0.07` | Morale change after nearly dying; `-1.0`–`1.0`. |
| `moraleResurrectDelta` | `-0.1` | Morale change after resurrection; `-1.0`–`1.0`. |
| `moraleBondLevelDelta` | `0.05` | Morale change when Bond levels up; `-1.0`–`1.0`. |
| `luckyExtraDropChance` | `0.05` | Lucky trait extra-drop chance; `0.0`–`1.0` (`5%` by default). |

### Jobs

The job settings are available in the common TOML but hidden from the native editor while Jobs are experimental.

Assigned-chest workers keep their active work plan while they collect outputs. A Lumberjack now chains mature trees within its assigned work radius, then returns to the linked chest when the bounded scan is exhausted or inventory delivery is due; successful delivery resets the scan so the job resumes without losing a partially felled tree.

| Key | Default | Range / description |
| --- | ---: | --- |
| `lumberjackEnabled` | `true` | Enable Lumberjack behavior. |
| `lumberjackRadius` | `10` | Minimum search radius; `4`–`64`. Companion Radius can expand work up to `128` blocks. |
| `lumberjackGroundBlocks` | `#minecraft:dirt`, `minecraft:moss_block` | Block IDs or `#tag` IDs accepted beneath natural trees. |
| `lumberjackBreakTimeMultiplier` | `2.0` | Multiplies tool-based per-log felling time; `0.25`–`8.0`. |
| `farmerEnabled` | `true` | Enable Farmer behavior. |
| `farmerRadius` | `12` | Minimum crop scan radius; `4`–`64`. Companion Radius can expand work up to `128` blocks. |
| `farmerBoneMealEnabled` | `true` | Let Farmers use carried bone meal on immature crops. |
| `hunterEnabled` | `true` | Enable Hunter behavior. |
| `hunterRadius` | `20` | Hunt scan radius; `6`–`64`. |
| `minerEnabled` | `true` | Enable Miner behavior. |
| `minerRadius` | `8` | Minimum ore search radius; `4`–`32`. Companion Radius can expand work up to `128` blocks. |
| `minerAllowBlocks` | `[]` | Optional block-ID whitelist; empty uses the default ore tags. |
| `minerDenyBlocks` | `minecraft:chest`, `minecraft:spawner` | Block-ID blacklist. |
| `fisherEnabled` | `true` | Enable Fisher behavior. |
| `fisherRadius` | `10` | Water search radius; `4`–`32`. |
| `chefEnabled` | `true` | Enable Chef behavior. |
| `chefRadius` | `8` | Heat-source search radius; `3`–`24`. |
| `assignedChestsChunkload` | `false` | Keep assigned delivery chests chunk-loaded. |
| `showJobsButton` | `false` | Show the Jobs button in the companion screen. |

## Optional Mod Compatibility

All integrations are optional unless listed under Requirements. Content that depends on an absent mod is not registered.

| Mod | Integration |
| --- | --- |
| **Iron's Spellbooks** | Adds native spell paths for Fire Mage, Lightning Mage, Necromancer, and the nine optional magic roles. Cleric keeps its bundled support kit. Magic companions accept Iron's casting implements, stored-spell weapons, and scrolls; Iron's equipment attributes apply from armor, Curios, and held gear. |
| **Ars Nouveau** | Adds native spell paths for Fire Mage, Lightning Mage, Necromancer, and the nine optional magic roles. Cleric keeps its bundled support kit. Ars spellbooks, tomes, parchment, bows, crossbows, and enchanted weapons can be used; Ars equipment attributes apply from armor and Curios. Both providers can be used together. |
| **TacZ** | Adds firearm specialists, category-matched guns and ammunition, native firing/reload behavior, and matching summon gems. |
| **Curios** | Adds companion Curios slots, accessory rendering, and per-slot render toggles. |
| **Sophisticated Backpacks** | Adds a native backpack screen, upgrades, settings, and pickup insertion for a backpack equipped in the companion's Curios back slot. Requires Curios. |
| **Epic Fight** | Adds the armature renderer, movement, melee timing, hit logic, weapon movesets, and equipped armor rendering while retaining companion roles, Stamina, equipment, and safety rules. Bundled weapon families include matching Epic Fight capabilities. TacZ guns temporarily use TacZ's native pose; a gun stored in cargo does not disable Epic Fight melee. |
| **Jade** | Shows companion attributes and Stamina/Mana bars in the HUD. |
| **WTHIT** | Shows companion data in WTHIT tooltips. |
| **JEI** | Adds the custom-vessel brewing steps to JEI's brewing category. Modern Companions does not ship a dedicated REI plugin. |
| **Better Combat** | Prevents duplicate custom reach handling for Modern Companions weapons. |
| **Bronze weapon providers** | Registers bronze dagger, club, hammer, spear, quarterstaff, and glaive variants when the `bronze` mod ID is present. |
| **Mekanism** | Clears Mekanism entity radiation when a companion is resurrected. |

Epic Fight's optional `Epic Fight x Curios Compat` layer is also detected when present so Curios accessories can remain attached to Epic Fight companion renderers.

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
