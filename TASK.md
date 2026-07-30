# Companion Stamina, Mana, and Potions

## Outcome

Give every companion a persistent, server-authoritative Stamina pool and every
magic companion a persistent Mana pool. Add six player- and companion-usable
potion items, their empty vessels, brewing recipes, data-driven loot entries,
and Jade tooltip bars for the two resources.

This is a new feature. Do not modify the supplied `Lootr-mdg-1.21.1/`,
`kubejs-2101/`, or `Mekanism/` sources; they are reference material only.

## Resource rules

- Stamina applies to all companions. It is spent only on a successful melee
  attack and while the companion is actually sprinting; an enabled sprint toggle
  alone costs nothing.
- At zero Stamina, a companion stops sprinting. It may still defend itself, but
  its shared melee attack cadence is slowed rather than disabled. Resume
  sprinting only after a small recovery threshold so it cannot flicker on and
  off at zero.
- Mana applies only to magic companions. It is spent only after a basic, heavy,
  or utility spell successfully casts. A spell with insufficient Mana is not
  attempted; the companion waits for Mana to recover.
- Define costs by the companion spell kit: basic = low, utility = medium, heavy
  = high. Keep the costs companion-local; do not attempt to mirror or consume
  Iron's Spellbooks' or Ars Nouveau's player resources.
- Both pools regenerate naturally. Regeneration is slower while a valid combat
  target is present and faster after a short out-of-combat grace period.
- Active regeneration/rejuvenation potion effects boost resource recovery.
- Persist and synchronize current/max Stamina and Mana. Values must survive
  unload/reload, death/revival handling, and multiplayer tracking.

## Potions

All six potions can be consumed by a player or a companion. A companion should
select and drink one from its inventory only when its effect is useful, never
waste it at full resource/health, and return the matching empty vessel.

| Potion | Vessel / animation sheet | Effect | Companion use condition |
| --- | --- | --- | --- |
| Health | red round | Immediate health restoration | Missing health |
| Regeneration | blue round | Health restored over time | Sustained missing health |
| Stamina | green rectangle | Immediate Stamina restoration | Stamina is too low for sprint/attack demand |
| Mana | pink pyramid | Immediate Mana restoration | A spell is ready but Mana is insufficient |
| Rejuvenation | orange hexagon | Health, Stamina, and Mana restored over time | Multiple resources are depleted or recovery is urgently needed |
| Shield | white droplet | Temporary armor bonus that fades when its effect expires | In combat and unshielded |

- Assets live in `src/main/resources/assets/modern_companions/textures/potions/`.
  Each supplied 256x16 PNG is a sixteen-frame, 16x16 left-to-right loop. Use
  the matching frames as a looping animated item texture; do not stretch the
  sheet into a static icon.
- Reuse the supplied empty round, rectangle, pyramid, hexagon, and droplet
  vessels. Health and Regeneration share the empty round vessel.
- The five empty-vessel recipes use glass as their base material and have their
  own shaped crafting recipes matching the visible vessel geometry. They are
  reusable outputs of drinking, not consumed components after use.
- Potion recipes use a custom-vessel brewing-stand progression: fill the empty
  vessel with water, make it awkward with nether wart, then apply its reagent.
  This preserves the custom vessel shape while retaining the normal Minecraft
  potion flow. Keep the reagents familiar and thematic: glistering melon
  (Health), ghast tear (Regeneration), sugar and rabbit's foot (Stamina),
  amethyst and lapis (Mana), ghast tear plus amethyst (Rejuvenation), and turtle
  scute plus iron (Shield). If the NeoForge brewing API cannot transform custom
  vessels directly, add only the narrow custom brewing recipe required--not a
  generic brewing framework. Final recipe data must be valid in a clean game
  and visible in recipe viewers.
- Health and Stamina restore immediately; Regeneration and Rejuvenation use
  bounded timed effects; Shield applies a temporary armor attribute modifier
  that is removed reliably on expiry, death, and unload.

## Loot and optional-mod support

- Add the potions to suitable vanilla chest loot through data-driven global
  loot modifiers, with conservative rarity and stronger potions in stronger
  structures. Do not inject duplicate loot on reload.
- Lootr support must use its intended generated-container path so the same
  data-driven loot works for Lootr containers and stays per-player where Lootr
  provides that behavior.
- KubeJS support requires no hard dependency: expose stable item tags for all
  companion potions, empty vessels, and each potion family, and keep recipes
  and loot data in normal datapack formats KubeJS can replace or extend.
- Inspect the supplied Lootr and KubeJS sources before implementation; use only
  their supported public extension points and document the verified behavior.

## Jade integration

- When Jade is installed, hovering a companion shows compact Stamina and Mana
  bars alongside its existing entity data. Mana is shown only for magic
  companions.
- The bars show current/max values and use a compact filled-bar presentation
  comparable to Mekanism's energy/fluid tooltip bars, not Mekanism code or its
  capability system.
- Treat Jade as optional and keep the common companion entity free of a Jade
  hard dependency. Inspect the included Mekanism source for the narrow display
  pattern, retain required license attribution for any copied material, and
  port only what is necessary.

## Implementation order and acceptance checks

1. Inventory the shared attack, sprint, mage-casting, inventory-consumption,
   NBT, sync, and Jade compatibility seams. Keep resource ownership in the
   shared companion entity rather than adding a parallel capability system.
2. Implement authoritative resources, regeneration, combat detection, NBT,
   synchronization, UI, and the smallest runnable checks for resource spending
   and regeneration.
3. Wire Stamina into the shared melee/sprint paths and Mana into successful
   basic/heavy/utility casts. Verify exhaustion never leaves a companion stuck
   sprinting, unable to defend, or repeatedly attempting an unaffordable spell.
4. Register the empty vessels and six consumable potions; implement their
   player effects and companion inventory decision rules.
5. Add animated item models/textures, shaped empty-vessel recipes, brewing
   recipes, tags, loot modifiers, optional Lootr/KubeJS support, and Jade bars.
6. Bump the mod version; update README, TRACELOG, and SUGGESTIONS; build before
   committing. Validate in a dev world with a melee companion and each mage
   integration: drink every potion as player and companion, confirm vessel
   return, persistence, tooltip values, recipes/JEI, vanilla and Lootr loot,
   and KubeJS overrides.

## Open balance values

Choose the initial max pools, regeneration rates, combat grace duration,
attack/sprint drain, sprint-resume threshold, spell costs, potion values,
effect durations, shield armor, loot weights, and recipe quantities during
implementation. Put player-tunable values in the existing config only when a
fixed value demonstrably needs tuning; do not add per-spell config boilerplate
without a balancing need.
