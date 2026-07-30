# Conditional Magic Companions — Iron’s Spellbooks + Ars Nouveau

## Rule of availability

Every companion in this document is magic-mod gated. It must not be registered, naturally spawned, offered by summon gem, or shown as an available companion unless its required integration mod is loaded. Make this decision before entity and item registration; do not unconditionally register a magic companion and hide it later.

| Gate | Mod ID | Meaning |
|---|---|---|
| Iron’s | `irons_spellbooks` | Companion may use Iron’s registered spells |
| Ars Nouveau | `ars_nouveau` | Companion may use Ars spell glyphs |

A class may use either gate:

- `Iron’s OR Ars`: available if at least one is loaded; its kit uses only the loaded mod’s options.
- `Iron’s only`: unavailable without Iron’s.
- `Ars only`: unavailable without Ars Nouveau.
- Both loaded: one companion entity, with a combined but deliberately small kit—not two duplicate variants.

Each approved cast must invoke the real API of the loaded upstream mod and its registered spell/glyph content. Do not emulate, recreate, or invent spell behavior; add only the matching optional compile/runtime integrations needed for those real APIs.

Under this requirement, the existing Fire Mage, Lightning Mage, Necromancer, and Cleric also become magic-mod-gated rather than appearing in a vanilla-only installation.

## Shared casting limits

Every caster gets:

- One reliable basic cast.
- One cooldown signature cast.
- At most one utility/support cast.
- Strict hostile/owner/allied-target checks before every damaging area effect.
- Temporary and capped summons only.
- No block breaking, placement, fire spread, terrain conversion, uncontrolled teleportation, or player-targeted possession.

The current shared caster AI already supports the basic/signature pattern: it attempts a heavy cast, otherwise performs the normal ranged cast. This fits the design without inventing a large spell framework.

## Existing companion assignments

| Existing class | Availability | Iron’s Spellbooks assignment | Ars Nouveau assignment | Intended role |
|---|---|---|---|---|
| Fire Mage | Iron’s OR Ars | Firebolt → Flaming Barrage or Blaze Storm; Shield/Evasion utility | Projectile + Ignite/Flare; Rune + Ignite; self Bubble | Ranged fire artillery |
| Lightning Mage | Iron’s OR Ars | Lightning Bolt → Chain Lightning or Ball Lightning; Charge utility | Projectile + Lightning; Projectile + Lightning + Split; Gust/Launch control | Precise anti-group striker |
| Necromancer | Iron’s OR Ars | Wither Skull → Raise Dead; Ray of Siphoning utility | Projectile + Wither; Summon Undead; Rune + Fangs | Attrition and capped temporary undead |
| Cleric | Iron’s OR Ars | Heal/Healing Circle → Blessing of Life or Fortify; Cleanse | Heal; Dispel; Bubble; Slowfall | Primary companion healer and protector |

### Fire Mage

Keep it focused: direct fire damage, not a general Mage.

- Iron’s basic: `Firebolt`.
- Iron’s signature: `Flaming Barrage` for a precision burst, or `Blaze Storm` only when hostile targets are safely separated from allies.
- Ars basic: `Projectile + Ignite` or `Projectile + Flare`.
- Ars signature: `Rune + Ignite` or `Projectile + Ignite + Amplify`.
- Utility: Iron’s `Evasion` or Ars `Bubble` self-shield.

Exclude `Magma Bomb`, `Wall of Fire`, `Raise Hell`, and any spell that can alter terrain or leave an uncontrolled fire hazard.

### Lightning Mage

Retain the existing identity: quick single-target strikes and a limited chain burst.

- Iron’s basic: `Lightning Bolt`.
- Iron’s signature: `Chain Lightning`; use `Ball Lightning` only if its behaviour can stay contained and ally-safe.
- Ars basic: `Projectile + Lightning`.
- Ars signature: `Projectile + Lightning + Split`, or a constrained `Rune + Lightning`.
- Utility: Iron’s `Charge`, or Ars `Gust`/`Launch` to create space.

### Necromancer

Keep the current temporary-summon rule. It is the right distinction from a permanent pet class.

- Iron’s basic: `Wither Skull`.
- Iron’s signature: `Raise Dead`.
- Iron’s utility: `Ray of Siphoning`; this is self-sustain, not party healing.
- Ars basic: `Projectile + Wither`.
- Ars signature: `Summon Undead`.
- Ars control: `Rune + Fangs` as a short ground-control cast.

Do not use persistent undead, player resurrection, or summon counts that can grow without a hard cap.

### Cleric

Cleric remains the only reliable companion healer.

- Iron’s basic: `Heal`.
- Iron’s signature: `Healing Circle`, `Blessing of Life`, or `Fortify`.
- Iron’s utility: `Cleanse`.
- Ars basic: `Heal`.
- Ars signature: `Bubble` on an endangered ally.
- Ars utility: `Dispel` or `Slowfall`.

Other magical companions may shield, conceal, or protect themselves, but should not receive Cleric-grade recurring healing.

## New companion classes

| Class | Availability | Basic cast | Signature cast | Utility | Identity |
|---|---|---|---|---|---|
| Wizard | Iron’s OR Ars | Magic Missile / Projectile + Harm | Summon Swords / prepared Rune field | Counterspell / Dispel | Deliberate arcane preparation |
| Sorcerer | Iron’s OR Ars | Firebolt, Icicle, Lightning Bolt / elemental Projectile | Chain Lightning, Blizzard, Blaze Storm / amplified elemental spell | Evasion / Bubble | Innate elemental force |
| Warlock | Iron’s OR Ars | Eldritch Blast / Projectile + Wither | Ray of Siphoning or Abyssal Shroud / Hex | Blood Step or self-shield | Void and life-drain pressure |
| Witch | Iron’s OR Ars | Acid Orb or Poison Splash / Hex or Snare | Root or Blight / Rune + Snare | Oakskin / Grow or self-support | Nature, poison, and debuffs |
| Hag | Iron’s OR Ars | Fangs, Slow / Hex, Fangs, Decoy | Sculk Tentacles or Scapegoat / Linger + Hex zone | Invisibility or Shield | Curse, misdirection, and area denial |
| Cryomancer | Iron’s OR Ars | Icicle or Ray of Frost / Freeze | Frostwave, Ice Tomb, Blizzard / Rune + Freeze | Frost Step / Slowfall | Dedicated ice control |
| Druid | Iron’s OR Ars | Firefly Swarm / Grow or Summon Wolves | Root or Oakskin / Summon Wolves + augment | Haste or self-heal-lite | Ally-preserving nature magic |
| Illusionist | Iron’s OR Ars | Magic Arrow or Slow / Decoy or Invisibility | Arcane Shackle / Linger + Decoy | Invisibility / Blink only if destination-safe | Deception and disruption |
| Battlemage | Iron’s OR Ars | Spectral Hammer or Echoing Strikes / Touch + Harm | Shield or Fang Ward / self Bubble + offensive touch spell | Haste | Armoured close-range caster |

## New-class spell detail

### Wizard — arcane control

- Iron’s: `Magic Missile`, `Magic Arrow`, `Arcane Shackle`, `Summon Swords`, `Counterspell`, `Gravity Fissure`.
- Ars: `Projectile + Harm`, `Rune`, `Delay`, `Wall`, `Dispel`, `Gravity`, `Burst`.
- Do not give the Wizard `Portal`, `Recall`, `Pocket Dimension`, or unrestricted `Blink` until companion-safe travel rules exist.

### Sorcerer — elemental specialization

A Sorcerer should select one element at spawn or configuration time; do not combine fire, lightning, ice, and broad arcane control on one companion.

- Fire: `Firebolt` → `Flaming Barrage` or Ars `Projectile + Ignite`.
- Lightning: `Lightning Bolt` → `Chain Lightning` or Ars `Projectile + Lightning`.
- Ice: `Icicle`/`Ray of Frost` → `Frostwave` or Ars `Projectile + Freeze`.

This makes the existing Fire Mage and Lightning Mage natural Sorcerer specializations without forcing a rename.

### Warlock — blood and void

- Iron’s: `Eldritch Blast`, `Abyssal Shroud`, `Ray of Siphoning`, `Blood Needles`, `Blood Step`, `Shadow Slash`.
- Ars: `Projectile + Wither`, `Hex`, `Snare`, `Fangs`, and a limited `Summon Undead`.
- The Warlock’s sustain is self-only. It should never compete with the Cleric for group-healer identity.

Exclude `Sacrifice`, `Heartstop`, `Pocket Dimension`, and other effects whose player-facing cost or control contract does not translate safely to autonomous companions.

### Witch — poison and natural control

- Iron’s: `Acid Orb`, `Poison Splash`, `Poison Breath`, `Blight`, `Root`, `Oakskin`, `Firefly Swarm`.
- Ars: `Hex`, `Snare`, `Grow`, `Summon Wolves`, `Conjure Water`, and `Rune` for stationary traps.
- The Witch’s support is mitigation and preparation: Oakskin, roots, soft crowd control—not large heals.

### Hag — curse and deception

The Hag should not simply be another poison Witch.

- Iron’s: `Fang Strike`, `Fang Swirl`, `Slow`, `Scapegoat`, `Sculk Tentacles`, `Invisibility`.
- Ars: `Hex`, `Fangs`, `Decoy`, `Linger`, `Invisibility`, `Rune`, `Snare`.
- Signature pattern: a short-lived hostile-only cursed zone such as `Rune + Hex` or `Linger + Snare`; it must expire and respect ally checks.

### Cryomancer — a worthwhile focused specialist

- Iron’s: `Icicle`, `Ray of Frost`, `Frostwave`, `Ice Tomb`, `Blizzard`.
- Ars: `Projectile + Freeze`, `Rune + Freeze`, `Cold Snap`.
- Avoid permanent ice placement and terrain obstruction. The class controls enemies, not the map.

### Druid — nature support without replacing Cleric

- Iron’s: `Root`, `Oakskin`, `Firefly Swarm`, `Haste`, and a constrained `Summon Polar Bear`.
- Ars: `Grow`, `Summon Wolves`, `Conjure Water`, `Slowfall`.
- Its summon should be temporary and capped; its direct healing remains intentionally weaker than Cleric healing.

## Required roster

Implement every listed class, with a distinct role:

1. Existing Fire Mage, Lightning Mage, Necromancer, and Cleric—with conditional Iron’s/Ars kits.
2. Wizard: arcane control.
3. Sorcerer: elemental specialization.
4. Warlock: void/blood self-sustain.
5. Witch: poison and roots.
6. Hag: curses and deception.
7. Cryomancer: focused ice control.
8. Druid: nature support.
9. Illusionist: deception and disruption.
10. Battlemage: armoured close-range casting.

Distinct roles remain required: do not collapse Mage, Wizard, Sorcerer, and Warlock into the same projectile with different names.

The source basis for this document is Iron’s registered spell catalogue and Ars Nouveau’s current glyph catalogue, plus the existing companion caster implementation: [Iron’s spell registry](/R:/Users/Zach/Documents/GitHub/ModernCompanions/irons-spells-n-spellbooks-1.21/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java), [Ars glyphs](/R:/Users/Zach/Documents/GitHub/ModernCompanions/Ars-Nouveau-main/src/main/java/com/hollingsworth/arsnouveau/common/lib/GlyphLib.java), and [current shared caster AI](/R:/Users/Zach/Documents/GitHub/ModernCompanions/src/main/java/com/majorbonghits/moderncompanions/entity/magic/AbstractMageCompanion.java).


## World discovery, summon items, and companion parity

Every magic companion—existing or new—must be a full Modern Companions class, not a combat-only entity.

When its Iron’s/Ars availability gate passes, each class must:

- Be registered as a companion entity with normal attributes, renderer, localization, inventory, taming, gear, leveling, traits, commands, safety rules, jobs, and UI behavior.
- Receive its own summon item, matching the current `DeferredSpawnEggItem`/“Summon Gem” pattern.
- Be added to the appropriate structure spawn pool alongside existing companions, so players can discover it naturally.
- Have the same spawn-duplication protection as every existing structure companion.
- Be omitted entirely from the entity list, spawn pool, summon items, natural discovery, and UI when neither required mod is present.

If both mods are loaded, there is still only one entity type and one summon item per class; it receives the combined approved spell options. Do not create an Iron’s Wizard and an Ars Wizard as separate companions.

### Conditional worldgen pools

| Structure pool | Existing companions | Add when eligible |
|---|---|---|
| `tower1` | Fire Mage, Lightning Mage | Wizard, Sorcerer, Cryomancer, Illusionist, Battlemage |
| `tower2` | Necromancer | Warlock, Hag |
| `alchemist_house` | Alchemist | Witch, Druid |
| `cleric_house`, `church` | Cleric | Keep Cleric gated by Iron’s or Ars; do not add a second healer class initially |

Each entry is individually filtered by mod presence before random selection. For example:

- If only Ars Nouveau is loaded, Ars-capable Wizard/Sorcerer/Witch/Hag/etc. remain valid choices.
- If only Iron’s is loaded, Iron’s-capable versions remain valid choices.
- If neither is loaded, magic entries are removed from their structure pools; a tower must not attempt to spawn a missing magic companion or mark that structure as serviced.
- Existing Fire Mage, Lightning Mage, Necromancer, and Cleric follow the same rule.

### Required summon items

Every added class receives a counterpart to the existing summon gems:

- Wizard Summon Gem
- Sorcerer Summon Gem
- Warlock Summon Gem
- Witch Summon Gem
- Hag Summon Gem
- Cryomancer Summon Gem
- Druid Summon Gem
- Illusionist Summon Gem
- Battlemage Summon Gem

The summon item is only registered and available when that companion’s compatibility gate passes. It uses the same creative-tab placement, localization, spawn behavior, and visual presentation as the current Fire Mage, Lightning Mage, Necromancer, and Cleric summon gems.

You can reuse existing gem assets.

### Class parity requirement

A discovered or summoned magical companion must behave identically to other companion classes outside its spell kit:

- Can be tamed, named, equipped, healed, revived, stored, moved, commanded, and managed through the existing UI.
- Uses the existing inventory, armor, weapon preference, attribute progression, morale/trait system, owner protection, friendly-fire controls, and job system.
- Participates in structure-spawn tracking exactly once per generated structure.
- Uses the same owner/allied safety rules as existing companions before any spell is cast.

This makes the new classes discoverable members of the normal companion roster, rather than optional creatures that only exist through commands or test spawn items.

The current placement system supports this directly: `tower1` already selects Fire Mage or Lightning Mage, `tower2` selects Necromancer, and `alchemist_house`/church-related pools already establish the appropriate discovery patterns. [Structure companion spawning](/R:/Users/Zach/Documents/GitHub/ModernCompanions/src/main/java/com/majorbonghits/moderncompanions/world/StructureCompanionSpawner.java), [existing summon gems](/R:/Users/Zach/Documents/GitHub/ModernCompanions/src/main/java/com/majorbonghits/moderncompanions/core/ModItems.java), and [entity registrations](/R:/Users/Zach/Documents/GitHub/ModernCompanions/src/main/java/com/majorbonghits/moderncompanions/core/ModEntityTypes.java) are the parity model. No files were changed.
