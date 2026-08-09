# Ideas

## Scratch
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
    - Essence can act as currency, or as catalysts for crating recipes
    - Essence stack to 64
    - `essence.png`

- Have companions able to get onto mounts and ride along with the player
    - Player mounts up, Companion searches for nearby mountable mob owned by the player, mounts it and re-engages follow logic. 
    - Companion dismounts when the player dismounts.
- Wand to assign companions to mounts?
    - When assigned, mount is then attached to respective companion via lead
    - Lead vanishes when the companion mounts his mount, and returns once they dismount
    - When companion is instructed to sit with a mount on a lead, they will place down a fence with the mounts lead attached
        - Instructing the companion to move again makes them re-lead their mount

- Currencies
    - Currencies can be toggle OFF in config, ON by default.
    - Added to vanilla loot tables; dungeons, caves, villages, etc.
    - Uses `currency.png` which is a 128x16 sprite SHEET with 16x16 sprites
        - Left to right; Tin, Copper, Silver, Gold, Dollar, Stack, Credit Card, Stack of Gold Coins
    - Allow modpack developers and players to tune knobs for currency such as loot disperse, currency values, etc.
    - Currencies will not have recipes for crafting, but will have the ability to show trading recipes in JEI, configured by players/modpack developers (for villager trades, etc)

- Health Pack
    - Inserted into loot tables; dungeons, caves, villages, etc.
    - NOT Craftable.
    - Instantly heals to full when consumed
    - When 'used' on another player or companions - instantly heals them to full
    - Has a cooldown, cannot spam usage

- Ranks
    - Companions increase their 'rank' as they level and gain combat experience. 
    - Uses `ranks.png` which is a 80x16 sprite SHEET with 16x16 sprites
        - Left to Right; Private → Corporal → Sergeant → Captain → Commander
    - Higher rank companions gain access to more features
        - Additional equipment slots?
        - Access to higher tier equipment?
        - Boost to overall stats?

- `Better Combat` support in tandem with `Epic Fight`

- Bard Class
    - Uses instruments as weapons to buff allies and debuff enemies?