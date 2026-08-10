# Current Spell kits and origins 

| Companion | Iron’s Spellbooks kit | Ars Nouveau kit | Current logic |
|---|---|---|---|
| Fire Mage | `firebolt` / `flaming_barrage` / `evasion` | Projectile + Ignite / Projectile + Ignite + Amplify / Self + Bubble | Standard ranged caster |
| Lightning Mage | `lightning_bolt` / `chain_lightning` / `charge` | Projectile + Lightning / Projectile + Lightning + Split / Self + Launch | Standard ranged caster |
| Necromancer | `wither_skull` / `raise_dead` / `ray_of_siphoning` | Projectile + Wither / Projectile + Summon Undead / Self + Heal | Ranged damage and undead summons |
| Cleric | `heal` / `healing_circle` / `cleanse` | Projectile + Heal / Self + Bubble / Self + Dispel | Custom Holy Spark offense; direct owner/ally healing; heavy and utility disabled |
| Wizard | `magic_missile` / `summon_swords` / `counterspell` | Projectile + Harm / Harm Rune / Self + Dispel | Heavy spell cannot create another sword batch while owned swords remain |
| Sorcerer | `firebolt` / `chain_lightning` / `evasion` | Projectile + Ignite / Projectile + Lightning + Amplify / Self + Bubble | Hostile-target kit; initial targets are restricted to monsters |
| Warlock | `eldritch_blast` / `abyssal_shroud` / `blood_step` | Projectile + Wither / Projectile + Hex / Self + Bubble | Standard ranged caster |
| Witch | `poison_splash` / `root` / `oakskin` | Projectile + Hex / Snare Rune / Self + Grow | Poison and control |
| Hag | `fang_strike` / `sculk_tentacles` / `invisibility` | Projectile + Fangs / Linger + Hex / Self + Invisibility | Curse and area denial |
| Cryomancer | `icicle` / `frostwave` / `frost_step` | Projectile + Freeze / Freeze Rune / Self + Slowfall | Ice control |
| Druid | `firefly_swarm` / `root` / `haste` | Projectile + Grow / Projectile + Summon Wolves / Self + Slowfall | Nature effects and wolf summons |
| Illusionist | `magic_arrow` / `arcane_shackle` / `invisibility` | Projectile + Hex / Linger + Summon Decoy / Self + Invisibility | Hexes, decoys, and disruption |
| Battlemage | `firebolt` / `fang_strike` / `haste` | Projectile + Harm / Projectile + Harm / Self + Bubble | Uses ranged magic while Mana is available, then melee; `spectral_hammer` and `fang_ward` are no longer used |



# Current casting and mod-presence logic

| Installed mods / situation | Actual behavior |
|---|---|
| Iron’s + Ars Nouveau | Each built-in cast uses one provider. Iron’s is attempted first or selected randomly; Ars is tried as fallback; Iron’s may be retried afterward. They do not cast both versions simultaneously. |
| Iron’s only | Uses only Iron’s registry spells. Failed casts do not fall back to vanilla or Ars. |
| Ars Nouveau only | Uses only dynamically assembled Ars glyph spells. Failed casts do not fall back to vanilla or Iron’s. |
| Neither mod | The 13 mage entities and their summon gems are not registered. There is no vanilla spell fallback. |
| Compatible held spell item | For basic attacks only, a stored Iron’s spell is tried first, then an Ars caster item. Scrolls are consumed only after a successful cast. |
| Basic attack | Normally costs 10 Mana and runs through the held-item path, then the built-in kit. Cleric uses 6 Mana. |
| Heavy attack | Costs 35 Mana, uses the built-in kit, and has a base 160-tick cooldown. |
| Utility spell | Costs 20 Mana, triggers below half health, and has a 200-tick cooldown. Cleric disables this path. |
| Sorcerer targeting | Initial targets must be monsters; players are excluded, and companion/pet damage follows the shared friendly-fire policy. |
| Battlemage without enough Mana | Its ranged goal stops being usable and its melee goal takes over. |
| Stormcaller | Always available independently of Iron’s/Ars; uses vanilla `LIGHTNING_BOLT`, vanilla lightning damage, and vanilla Strength. |