# Modern Companions configuration guide

This file explains the common configuration at:

    config/modern_companions-common.toml

The file is shared by the mod's gameplay systems. On a dedicated server, the
server's common config is authoritative for gameplay. Players can edit the
exposed settings through Mods -> Modern Companions -> Config; the Jobs section
is intentionally hidden from that screen while Jobs remain experimental, so
edit those values directly in the TOML file.

Stop the game or server before editing the file, keep a backup of a working
configuration, and restart after changing it. TOML lists use quoted strings,
booleans are true or false, and registry IDs use namespace:path form. Java
class names are not valid registry IDs.

## Important interaction rules

### Food acceptance and the allFoods list

allFoods has two modes:

1. While it exactly matches the shipped default list, the mod keeps its normal
   compatibility behavior and automatically discovers safe food items supplied
   by other mods.
2. After the list is changed, it becomes authoritative. Only listed items are
   added by the configured food pool; automatically discovered safe modded foods
   are no longer added.

This same pool is used for random recruitment food, favorite-food selection,
and ordinary companion healing. The hardcoded unsafe-food blacklist still wins
over configuration. It rejects spider eyes, rotten flesh, poisonous potatoes,
pufferfish, suspicious stew, chorus fruit, raw beef, raw porkchop, raw chicken,
raw mutton, raw rabbit, raw cod, raw salmon, and raw tropical fish.

extraHealConsumables is separate. Its entries are accepted for healing but are
never selected as recruitment requirements. Valid healing or regeneration
potions are also accepted as healing items.

### Recruitment requirements

recruitmentRequirements is optional. An empty list keeps the normal random
behavior: one food is selected from the food pool and one resource is selected
from the resource tiers.

Each row has this format:

    companion_id|item_id|count

The companion ID may be * to create a wildcard rule:

~~~toml
recruitmentRequirements = [
  "*|minecraft:bread|3",
  "*|minecraft:iron_ingot|2"
]
~~~

Every matching row is required. Multiple rows for the same companion are
combined. A specific companion rule replaces the wildcard rule for that
companion; it does not merge with it. Repeat shared rows in the specific rule
if that companion should keep them.

Examples:

~~~toml
# Every companion needs 3 bread.
recruitmentRequirements = [
  "*|minecraft:bread|3"
]

# Archer needs 5 bread and 1 diamond; everyone else needs 3 bread.
recruitmentRequirements = [
  "*|minecraft:bread|3",
  "modern_companions:archer|minecraft:bread|5",
  "modern_companions:archer|minecraft:diamond|1"
]
~~~

Requirement counts must be positive integers up to 100,000. Item IDs are
resolved through the loaded item registry, so modded items work when their mod
is loaded. An exact requirement replaces the random food/resource assignment,
including its normal resource-tier progression gate.

### Resource pools

When no exact recruitmentRequirements rule applies, the resource pools are
sampled with these default chances:

- commonResourceItems: 70%
- uncommonResourceItems: 25%
- rareResourceItems: 5%

The selected resource normally requires 2 to 6 items. If the selected tier has
no currently available item, the mod tries the other configured tiers before
falling back to iron ingots.

The default progression gates remain active for the random resource path:
Nether resources such as glowstone dust, blaze rods, magma cream, and quartz
are held back until the player has reached the Nether. Prismarine shards and
prismarine crystals are held back until the player has reached an ocean biome.
Exact recruitmentRequirements entries bypass this random selection and gate.

## World generation

| Option | Default | Accepted values | Effect |
| --- | ---: | --- | --- |
| averageHouseSeparation | 20 | 11 or higher | Intended average chunk separation between generated companion houses. The option is declared in the current config schema but is not currently consumed by the active runtime placement path; changing it has no confirmed effect until that integration is wired. |

## Companion

| Option | Default | Accepted values | Effect |
| --- | ---: | --- | --- |
| friendlyFireCompanions | false | true or false | If true, companions and Beastmaster pets can damage one another. If false, shared companion protection blocks that damage. |
| friendlyFirePlayer | true | true or false | If true, a companion may damage its owning player. Set false to block owner damage. |
| fallDamage | true | true or false | If false, companion fall damage is ignored. |
| spawnArmor | true | true or false | Gives newly spawned companions their class-appropriate randomized starting armor. |
| spawnWeapon | true | true or false | Gives newly spawned companions their normal class weapon or loadout. |
| autoEquip | false | true or false | Allows automatic equipment selection from the companion inventory. Manual equipment remains protected from automatic replacement. |
| teleportLeash | false | true or false | Enables the optional safe teleport leash for nearby companions actively ordered to Follow after they exceed their saved Radius by 5 blocks. It does not transfer Patrol, Guard, work, sit, or distant companions. |
| baseHealth | 20 | 5 or higher | Base health used for companion attributes before the mod's spawn variance and class adjustments. |
| lowHealthFood | true | true or false | Allows companions to eat from their inventory and request food when low on health. |
| lowHealthFoodThreshold | 0.5 | 0.0 to 1.0 | Health fraction used by low-health food behavior. 0.5 means half of maximum health. |
| staminaEnabled | true | true or false | Enables the Stamina system. When false, stamina is restored instead of limiting sprint/melee behavior. |
| sprintStaminaCost | 1 | 0 to 100 | Stamina spent per sprinting tick. Zero disables sprint drain. |
| meleeStaminaCost | 8 | 0 to 100 | Stamina spent after a successful melee attack. Zero disables melee drain. |
| creeperWarning | true | true or false | Enables the Creeper warning/avoidance behavior. Creeper Alert exclusion settings also affect whether companions treat Creepers as alert targets. |
| voiceMode | FULL | FULL, LIMITED, or OFF | FULL plays all custom companion cues. LIMITED keeps pain, death, and ambient cues while suppressing interaction/combat callouts. OFF disables custom companion sounds. |
| voiceVolume | 80 | 0 to 100 | Custom companion sound volume as a percentage. 80 means 80%, not a raw game-volume multiplier. |

## Taming and supplies

### allFoods

allFoods is a list of item registry IDs. The shipped default list is:

~~~toml
allFoods = [
  "minecraft:cookie",
  "minecraft:bread",
  "minecraft:melon_slice",
  "minecraft:apple",
  "minecraft:sweet_berries",
  "minecraft:carrot",
  "minecraft:baked_potato",
  "minecraft:cooked_salmon",
  "minecraft:cooked_cod",
  "minecraft:cooked_mutton",
  "minecraft:cooked_porkchop",
  "minecraft:cooked_beef",
  "minecraft:cooked_chicken",
  "minecraft:pumpkin_pie",
  "minecraft:glow_berries",
  "minecraft:potato",
  "minecraft:beetroot",
  "minecraft:dried_kelp",
  "minecraft:cooked_rabbit"
]
~~~

Add items to create a custom random pool:

~~~toml
allFoods = [
  "minecraft:bread",
  "minecraft:apple",
  "farmersdelight:tomato",
  "farmersdelight:cabbage",
  "farmersdelight:vegetable_soup"
]
~~~

Because this list is now customized, only these configured items are used for
the configured food pool. This does not make a companion require every item;
the recruitment code selects one item at random when no exact recruitment rule
exists.

### recruitmentRequirements

See Important interaction rules above. The default is:

~~~toml
recruitmentRequirements = []
~~~

Use this setting when a pack needs exact per-companion requirements, exact
counts, multiple required items, or a shared wildcard rule.

### extraHealConsumables

This is a separate list of item IDs that companions may consume for healing but
will never randomly request for recruitment. It may be empty:

~~~toml
extraHealConsumables = [
  "minecraft:golden_apple",
  "minecraft:enchanted_golden_apple",
  "minecraft:golden_carrot",
  "minecraft:honey_bottle",
  "minecraft:mushroom_stew",
  "minecraft:beetroot_soup",
  "minecraft:rabbit_stew"
]
~~~

### commonResourceItems, uncommonResourceItems, and rareResourceItems

Each is a list of item registry IDs used by the random recruitment resource
selector. Edit the lists to change the possible items in each tier. The tier
probabilities and normal count range are described above. These lists do not
override an exact recruitmentRequirements row.

## Hunting

| Option | Default | Accepted values | Effect |
| --- | --- | --- | --- |
| huntMobs | chicken, cow, pig, rabbit, sheep, goat | Entity registry IDs; may be empty | Controls targets selected by the manual Hunt order. An empty list disables configured manual Hunt targets. This list is separate from the Hunter profession's data-driven target rules. |

Example:

~~~toml
huntMobs = [
  "minecraft:zombie",
  "minecraft:skeleton",
  "minecraft:spider"
]
~~~

Use entity registry IDs such as minecraft:zombie or a modded ID such as
examplemod:custom_animal.

## Alert

| Option | Default | Accepted values | Effect |
| --- | --- | --- | --- |
| excludedMobs | minecraft:creeper | Entity registry IDs; may be empty | Removes listed entities from Alert targeting. Alert otherwise recognizes registered Monster-category entities, subject to the alert_unsafe entity-type datapack tag. |
| creeperDefaultMigrated | false | Internal boolean | Hidden migration marker. Do not edit it. The first config migration inserts the Creeper exclusion for older empty configs, then marks the migration complete so a later intentional Creeper removal is preserved. |

To let companions Alert against Creepers, remove minecraft:creeper from
excludedMobs after the migration marker is true. A datapack can extend the
unsafe-target safety boundary through:

    data/modern_companions/tags/entity_type/alert_unsafe.json

## Personality and progression

| Option | Default | Accepted values | Effect |
| --- | ---: | --- | --- |
| traitsEnabled | true | true or false | Enables randomly assigned primary and secondary companion traits. |
| secondaryTraitChance | 40 | 0 to 100 | Percent chance that a spawned companion receives a secondary trait. |
| bondEnabled | true | true or false | Enables Bond/Loyalty progression and bond XP awards. |
| moraleEnabled | true | true or false | Enables morale changes and morale-related effects. |
| bondTickInterval | 1200 | 20 or higher | Ticks between passive bond XP awards while a companion is alive near its owner. 20 ticks is approximately 1 second. |
| bondTimeXp | 5 | 0 to 10,000 | Bond XP awarded at each passive time interval. |
| bondFeedXp | 15 | 0 to 10,000 | Bond XP awarded when the owner successfully feeds a tamed companion. Favorite food receives the existing feed multiplier. |
| bondResurrectXp | 80 | 0 to 100,000 | Bond XP awarded when resurrecting a companion. |
| moraleFeedDelta | 0.05 | -1.0 to 1.0 | Morale change from owner feeding. |
| moraleNearDeathDelta | -0.07 | -1.0 to 1.0 | Morale change when damage brings a companion near death. |
| moraleResurrectDelta | -0.1 | -1.0 to 1.0 | Morale change after resurrection. |
| moraleBondLevelDelta | 0.05 | -1.0 to 1.0 | Morale change when the companion gains a Bond level. |
| luckyExtraDropChance | 0.05 | 0.0 to 1.0 | Chance for the Lucky trait to duplicate one kill drop. 0.05 means 5%. |

Morale deltas are changes, not percentages. Positive values increase morale;
negative values reduce it. The runtime clamps morale to its valid range.

## Jobs

The Jobs category is hidden from the native config screen while Jobs are
experimental, but all of these options are valid in the TOML.

| Option | Default | Accepted values | Effect |
| --- | ---: | --- | --- |
| lumberjackEnabled | true | true or false | Enables Lumberjack goal installation. |
| lumberjackRadius | 10 | 4 to 64 | Minimum Lumberjack search radius. The companion's saved Radius can expand the work contract up to the worker-system limit. |
| hunterEnabled | true | true or false | Enables Hunter profession behavior. |
| hunterRadius | 20 | 6 to 64 | Hunter target-search radius. |
| minerEnabled | true | true or false | Enables Miner goal installation. |
| minerRadius | 8 | 4 to 32 | Minimum Miner search radius. The companion's saved Radius can expand the work contract up to the worker-system limit. |
| minerAllowBlocks | [] | Block ID strings | Optional whitelist. Empty uses the normal ore/tag rules. When non-empty, only listed blocks are eligible before other safety checks. |
| minerDenyBlocks | chest, spawner | Block ID strings | Blocks the Miner must never break. Deny entries win over allow entries. |
| fisherEnabled | true | true or false | Enables Fisher profession behavior. |
| fisherRadius | 10 | 4 to 32 | Fisher water/shore search radius. |
| chefEnabled | true | true or false | Enables Chef profession behavior. |
| chefRadius | 8 | 3 to 24 | Chef heat-source search radius. |
| assignedChestsChunkload | false | true or false | Keeps assigned delivery chests chunk-loaded when enabled. This can increase server chunk-loading work. |
| showJobsButton | false | true or false | Shows the Jobs button in the companion inventory screen. It is hidden by default because Jobs are experimental. |

Example Miner restrictions:

~~~toml
minerAllowBlocks = [
  "minecraft:coal_ore",
  "minecraft:iron_ore",
  "minecraft:copper_ore"
]

minerDenyBlocks = [
  "minecraft:chest",
  "minecraft:spawner",
  "minecraft:ancient_debris"
]
~~~

## Practical recipes

### Normal behavior with more possible foods

Keep recruitmentRequirements empty and add desired IDs to allFoods. Because
allFoods is changed, include every food you want in the authoritative list.

### One shared recruitment rule

~~~toml
recruitmentRequirements = [
  "*|minecraft:bread|3"
]
~~~

### Exact class requirements

~~~toml
recruitmentRequirements = [
  "modern_companions:archer|minecraft:bread|5",
  "modern_companions:archer|minecraft:iron_ingot|2",
  "modern_companions:beastmaster|minecraft:golden_carrot|4"
]
~~~

### Custom resource tiers

~~~toml
commonResourceItems = [
  "minecraft:iron_ingot",
  "minecraft:copper_ingot",
  "farmersdelight:canvas"
]

uncommonResourceItems = [
  "minecraft:gold_ingot",
  "minecraft:amethyst_shard"
]

rareResourceItems = [
  "minecraft:diamond",
  "minecraft:emerald"
]
~~~

If a registry ID does not exist in the loaded game, it cannot be resolved as a
valid configured item. Check the mod that provides a modded ID is installed
before adding it.
