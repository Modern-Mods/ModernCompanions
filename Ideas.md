# Ideas

- We need to analyze all magical companions, their spellbooks and combat logic to make sure they are using spells that are meaningful. 

## Bugs
- Armor is not displaying?



- Alchemist needs fixed;
    - Should be able to throw splash potions from their inventory
    - Should default to melee combat if no splash potions are in their inventory, or if their target is too close 
    - Alchemists are not effected by their own negative affect splash potions they throw, nor is their owner. Alchemist is also not negatively affected by any negative splash potions their owner throws
    - The alchemist should use splash potions the following way;
        | Potion                      | Alchemist behavior                                                                                                                                                                                                              |
        | --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
        | ❤️ **Healing**              | Throws at the player when moderately injured. Also targets injured companions. Should prioritize whoever is lowest percentage HP rather than lowest raw HP.                                                                     |
        | ❤️‍🔥 **Healing II**           | Emergency heal. Reserved for critically injured player/companions rather than being casually thrown.                                                                                                                            |
        | 💀 **Harming**              | Bread-and-butter offensive potion. Throws at clustered enemies, especially enemies surrounding the player.                                                                                                                      |
        | ☠️ **Harming II**           | Finisher / emergency burst damage. Prioritize strong enemies, elites, or dense groups.                                                                                                                                          |
        | 💗 **Regeneration**         | Used before or during sustained fights when the player has lost some health but isn't in immediate danger.                                                                                                                      |
        | 💗 **Regeneration II**      | Short emergency sustain during intense combat.                                                                                                                                                                                  |
        | 🧪 **Poison**               | Used against high-health enemies that aren't already poisoned. Great opener against durable mobs.                                                                                                                               |
        | 🧪 **Poison II**            | Used when the Alchemist wants faster damage rather than long attrition.                                                                                                                                                         |
        | 💪 **Strength**             | Buffs the player when melee combat begins, especially if the player is actively attacking enemies nearby.                                                                                                                       |
        | 💪 **Strength II**          | Saved for bosses, raids, dangerous elites, or very large enemy groups.                                                                                                                                                          |
        | 🏃 **Swiftness**            | Used when traveling/combat chasing, or when enemies are keeping distance from the party.                                                                                                                                        |
        | 🏃 **Swiftness II**         | Emergency mobility. Could trigger when fleeing, chasing ranged enemies, or fighting something extremely mobile.                                                                                                                 |
        | 🐇 **Leaping**              | Situational exploration/combat buff. Useful around vertical terrain or enemies above the player.                                                                                                                                |
        | 🐇 **Leaping II**           | Rare. Could trigger when vertical movement is clearly necessary rather than during ordinary combat.                                                                                                                             |
        | 🔥 **Fire Resistance**      | Immediately used if player is burning, falls into lava, or combat includes lots of fire damage. Also excellent preemptively against Blaze-type enemies.                                                                         |
        | 🌊 **Water Breathing**      | Used when player enters deep water and their air begins dropping. Could also proactively throw it while exploring underwater structures.                                                                                        |
        | 🌙 **Night Vision**         | Exploration utility. Throws it in very dark areas, caves, nighttime exploration, or underground structures.                                                                                                                     |
        | 👻 **Invisibility**         | Escape/reset tool. Use when player is critically injured and badly outnumbered. Could also have a stealth-mode command later.                                                                                                   |
        | 🪶 **Slow Falling**         | Emergency rescue potion. If player falls a significant distance, Alchemist tries to splash them mid-fall. Also useful around cliffs or flying enemies.                                                                          |
        | 🐢 **Turtle Master**        | Panic button. Used when player is critically low and surrounded. The Resistance saves them while the Slowness becomes the tradeoff.                                                                                             |
        | 🐢 **Strong Turtle Master** | Absolute “OH GOD” button. Very low-health player + major incoming threat.                                                                                                                                                       |
        | ⚔️ **Weakness**             | Prioritize enemies dealing heavy melee damage. Great against Ravager-style enemies, bosses, armored mobs, etc.                                                                                                                  |
        | 🐌 **Slowness**             | Control potion. Used against enemies chasing the player or attempting to close distance.                                                                                                                                        |
        | 🧊 **Strong Slowness**      | Used against particularly dangerous melee enemies or something the player is actively retreating from.                                                                                                                          |
        | 🌬️ **Wind Charged**         | Throw onto enemies that are likely to die soon, especially enemies surrounded by other mobs. Their death becomes a little explosive crowd-control event.                                                                        |
        | 🕸️ **Weaving**              | Fantastic choke-point potion. Apply to low-health enemies inside groups so their deaths create cobwebs and obstruct the remaining enemies.                                                                                      |
        | 🟢 **Oozing**               | I'd make this a deliberately chaotic offensive tool. Apply to dying enemies when extra Slimes would benefit the party, but avoid using it constantly because it creates additional mobs.                                        |
        | 🪲 **Infested**             | Use against durable enemies that are being hit repeatedly. Since damage can generate Silverfish, it turns one big enemy into battlefield chaos. Best used when enemies significantly outnumber allies or against tanky targets. |
    - The alchemist can brew potions in their inventories. The player can set their active recipe, and when the alchemist has the proper ingredients in their inventory - will create splash potions of the assigned recipe.
        - Create a paper 'recipe' item; combine this paper in a shapeless crafting window with a potion and it will become a new 'recipe' item that contains the potion's crafting recipe. Hand that recipe item to an alchemist and all the required ingredients, and they will brew splash potions right in their inventory.
            - `src\main\resources\assets\modern_companions\textures\item\recipe.png`
            - Item will be called; 'Blank Recipe' and have a white name
            - When combined with a potion, the subsequent item which will have a cyan name and be suffixed with the respective recipe, like; 'Recipe: Harming II'
            - Blank Recipe Item will have a not very difficult recipe including paper




## Maybe

- I want to add a new option to the mod; Companions show chat bubbles show over their heard when speaking, instead of chat.
    - This should be a toggle between chat speak and bubble speak, or both so the player can choose.
- Companions will interact with eachother
    - Chat amongst eachother
    - Call out enemies to eachother
    - Mourn companion deaths
    - Trash talk enemies
    - Ask other companions for food
        - Companions will be able to share edible food between themselves

- Optional Curios
    - Rings:
    - Amulets:
    - Hats:
        - Crowns
        - Strawhat
        - Tophats
    - Foods: 
        - Donuts
        - Ice Creams
        - Pies / Pastries
    - Instruments:
        - Ocarina
        - Harp
        - Trumpet
        - Hand Drum
        - Violin
        - Saxaphone
        - Guitar
    - Music Discs:
        - CD
        - Vinyl
    - Misc:
        - Cross: Give thes companion a spare life (similar to the totem, but does not need to be held in off-hand)
        - Lanterns: Gives companion large light radius
        - Cigarette: Companion periodically has small smoke particles emit from their mouth
        - Umbrella: Slowfall 
        - Magnet: Increase pick-up radius
        - Megaphone: Makes the companion's voice lines damage nearby hostiles
        - Crystal Ball: 
        - CPU: Always keeps whatever chunk the companion is in loaded
        - Eyeballs: 
            - Monster Eyes

- Essence
    - Companions periodically collect essence from defeated enemies
        - Essence does not drop like traditional items, rather instantly transported to the companions inventory
    - Essence can act as catalysts for crating recipes
    - Essence stack to 64 per type
    - `essence.png`
        - 48x16 sheet, 16x16 sprites
            - Left to right; Red, Blue, Green
- Ranks
    - Companions increase their 'rank' as they level and gain combat experience. 
    - Uses `ranks.png` which is a 80x16 sprite SHEET with 16x16 sprites
        - Left to Right; Private → Corporal → Sergeant → Captain → Commander
    - Higher rank companions gain access to more features
        - Additional equipment slots?
        - Access to higher tier equipment?
        - Boost to overall stats?