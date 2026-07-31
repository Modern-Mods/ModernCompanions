- 2026-07-30: Add a Curios dev-world smoke check for each firearm specialist gem to confirm the shared specialist type opens slots and renders equipped curios.
- 2026-07-30: Add a small dev-world regression checklist for equipment: replace worn armor with shift-click, extract it from both cargo/equipment views, capture/redeploy a pistol-only companion, and verify no pistol enters an armor slot.
- 2025-11-18: Next, port original Human Companions content into the new NeoForge 1.21.1 scaffolding—migrate registries (entities/items/structures), networking, configs, and assets under `modern_companions`, then add README and run a full Gradle build once the code is in place.
- 2025-11-18 (later): Finish porting gameplay logic (entities/AI, networking, GUIs, structure placement) onto the new registries; add data-driven assets under the new namespace once binary inclusion is allowed, then verify with a Gradle build.
- 2025-11-18 (further): Next still to-do—port GUI toggle controls and networking for patrol/alert/hunt/stationary flags, migrate companion house worldgen data/logic, and run a full Gradle build/validation once core behaviors are complete.
- 2025-11-18 (network prep): Wire upcoming GUI buttons to the new toggle-flag packet so player interactions sync follow/guard/hunt/alert/stationery states server-side; then run a Gradle build to catch regressions.
- 2025-11-19: Keep going—port remaining worldgen/structure data (textual JSON where possible, noting binaries blocked), add README with build/run instructions, then execute Gradle build to verify.
- 2025-11-19 (next): When binary inclusion is allowed, import companion house NBTs under `data/modern_companions/structures/` and wire structure JSON to the new namespace; then run `./gradlew build` to validate.
- 2025-11-19 (future): After binaries are in place, migrate textures/models/lang/sounds, register entity renderers, and perform full runtime testing.
- 2025-11-19 (current): Finish refitting the codebase to the NeoForge 1.21.1 API (ModLoadingContext usage, DeferredHolder registries, SynchedEntityData builder, Animal#isFood overrides, ResourceLocation factories, networking replacements) so `./gradlew build` succeeds.
- 2025-11-19 (up next): Hook up actual entity/client renderers + layers, flesh out AI/GUI actions (release, clear target, patrol radius), and verify functionality in-game now that the build passes.
- 2025-11-19 (next focus): Playtest companions in a dev world to validate patrol/guard/hunt/alert/stationary behaviors, tune ranged attack AI to newer crossbow/bow mechanics (charging, accuracy, strafing), and consider surfacing XP/level stats plus remaining config toggles in the GUI.
- 2025-11-20: During playtest, confirm the new GUI health/level readouts track correctly, and consider adding patrol radius adjustment + food requirement display. Also evaluate replacing the arbalist's bow-based goal with the 1.21 crossbow behavior (charge/cooldown) for closer parity.
- 2025-11-20 (next): Playtest arbalist crossbow AI for accuracy/charge timing; if too passive, raise attack radius slightly or bias pathfinding while guarding. Add config/GUI control for patrol radius and show current food requirements to help players fulfill taming quickly.
- 2025-11-20 (future polish): Add a slider/text field to choose patrol radius directly and expose a "reset to default" button. Consider adding XP bar + next-level preview in the UI, and a toggle to enable/disable creeper warning per companion.
- 2025-11-20 (following): Now that XP percentage is shown, add a small progress bar graphic and the exact XP needed for next level; also allow exporting/importing companion data via commands for testing.
- 2025-11-20 (later): Consider recoloring the XP bar to match companion class (melee vs ranged) and add tooltip showing total XP and kills; optional toggle to hide the bar for minimal UI.
- 2025-11-20 (inventory stats panel): Fine-tune the new stats panel by adding compact icons/labels for armor, damage, and resistances, and align the sidebar buttons visually with any callouts baked into `inventory_stats.png`; if space allows, add hover tooltips for each stat line.
- 2025-11-21: If the inventory texture ever exceeds 256px in height/width again, centralize texture size constants and add a tiny helper to blit GUIs with explicit atlas dimensions to avoid future wrap issues.
- 2025-11-21 (stats alignment): Add a small helper to render stats with padding derived from the texture so any future layout tweaks only touch constants; consider centering the text block vertically within the stats panel for balance.
- 2025-11-21 (XP sync): If we add more client-side displays (kills, total XP), consider syncing a small stats packet on open to avoid relying on data parameters for everything and to reduce bandwidth.
- 2025-11-21 (inventory growth): Consider adding slot-locking or equipment-only rows to preserve balance now that inventory doubled; or gate extra rows behind higher levels/config to keep early-game companions modest.
- 2025-11-21 (UI polish): If we tweak the background again, consider driving the 1px offsets from constants so different texture revisions can be tested quickly without changing logic.
- 2025-11-21 (pickup toggle): Expose pickup radius/behavior in config (owner-only vs global, magnet strength) and add a quick visual indicator near the button so players know when auto-loot is active.
- 2025-11-21 (RPG stats UI): Surface STR/DEX/INT/END in the companion screen with icons + tooltips, and show any rolled “specialist” bonus so players can see their companion’s build.
- 2025-11-21 (HUD overlays): Add configurable toggle and color-coding for the Jade/WTHIT attribute line (e.g., specialist color), and consider exposing health/XP alongside stats for parity with the inventory panel.
- 2025-11-21 (new classes): Playtest the seven new roles to tune numbers—e.g., cap Vanguard projectile DR, scale Berserker cleave with weapon tier, give Beastmaster pet sit/unsit commands, and add cooldown HUD hints for Stormcaller/Alchemist effects so players can anticipate bursts.
- 2025-11-21 (weapons port next): Add Better Combat `weapon_attributes` entries and final textures/models for the new arsenal; revisit bronze gating/repair materials if the external bronze mod exposes proper tags.
- 2025-11-21 (spawn eggs): Now that eggs are registered for the new roles, validate they show in the Spawn Eggs tab and consider a dedicated “Companions” creative tab for easier discovery.
- 2025-11-21 (weapon assets): After pulling in BasicWeapons textures/models and bettercombat attributes, playtest with Better Combat enabled to ensure reach/animation feel match upstream; consider adding repair ingredients (bronze tag) once the dependency API is confirmed.
- 2025-11-21 (taming variety): Add per-class taming preference weights (e.g., Vanguard likes ingots more, Alchemist prefers gems) and surface current taming progress in the companion tooltip or GUI so players know how many items remain.
- 2025-11-21 (egg art): If we keep gem-style eggs, consider theming colors to match class UI tinting (e.g., tank blues, healer golds) or swap to class weapon icons later; add a simple glow/outline variant for higher-tier spawn items if desired.
- 2025-11-21 (legacy egg gems): Confirm the four original classes render with their new Gem_7–Gem_10 icons and adjust hues if they clash with the new roles; a single palette map in docs would help keep future assignments consistent.
- 2025-11-21 (kill tracker): Consider surfacing total kills alongside XP in Jade/WTHIT overlays and adding milestones (e.g., cosmetic badges) at certain thresholds to reward long-lived companions.
- 2025-11-21 (beastmaster pets): Add a HUD/icon in the companion screen to show pet status/cooldown and a manual "recall pet" action so players can resync or dismiss the wolf without waiting on the automatic respawn timer.
- 2025-11-21 (pet variety): Expose a config to tune Beastmaster pet weights (e.g., turn off hoglin/polar bear rolls, boost cats/foxes) and surface the current pet type/rarity in the GUI tooltip.
- 2025-11-21 (pet follow): Consider showing a small “following” indicator on the pet and let owners toggle sit/follow like wolves (even for non-tamable mobs) via a right-click or GUI button tied to the Beastmaster.
- 2025-11-21 (pet defense): Add a brief on-hit VFX/SFX when a pet switches to defend the Beastmaster/owner, and expose a config toggle for whether pets may retaliate against neutral mobs the player accidentally hits.
- 2025-11-21 (pet anti-rubberband): Make the follow/teleport grace configurable (hold time + start distance) so pack makers can tune how long pets stay engaged before snapping back.
- 2025-11-21 (pet damage): Consider scaling pet damage by Beastmaster level or weapon tier and surfacing a small damage number/crit indicator to confirm hits from passive mobs.
- 2025-11-21 (pet ownership UI): Add a compact owner/pet status widget with a manual "Bind/Recall Pet" button so players can reattach or retame a Beastmaster pet without waiting for automatic respawn.
- 2025-11-21 (pet spawn FX): Add a brief summon particle/SFX when the Beastmaster’s starting pet appears so players notice the companion instantly and can confirm it spawned correctly.
- 2025-11-21 (pet roster config): Expose a data/config toggle to adjust Beastmaster pet weights (or disable specific mobs) so pack makers can curate allowed pets without code edits.
- 2025-11-21 (pet type UI): Show the locked-in pet type in the Beastmaster’s UI/Jade line and add a manual “respec” token/config gate in case players want to reroll a companion’s pet species.
- 2025-11-21 (respawn diagnostics): Add a small debug/config toggle to log pet respawn failures and the chosen type id to help catch future edge cases without needing code changes.
- 2025-11-21 (pet-specific tuning): Add per-pet-type movement/attack tuning (config or data-driven) so pandas/camels/etc. can be balanced independently without hardcoding values.
- 2025-11-21 (pet lifecycle polish): Consider a small VFX/SFX on Beastmaster death pet-despawn and pet-respawn to signal the lifecycle changes to players.
- 2025-11-21 (spawn init auditing): Add a small helper/log to confirm finalizeSpawn results per pet type (genes/attributes) to quickly diagnose any future mob-specific spawn quirks.
- 2025-11-21 (lost pet UX): After the grace period, consider showing a brief "recalling pet" toast so players know a new pet is on the way instead of assuming it is gone forever.
- 2025-11-21 (untamed handling): Add a config toggle to allow/disable pet spawning for untamed companions in case some packs prefer pets only after hiring.
- 2025-11-21 (camel tuning): Consider a small stamina/step-height tweak for camels so their larger model doesn’t snag on terrain when keeping pace.
- 2025-11-21 (camel pacing): If camels still feel fast/slow, expose their speed multiplier in config alongside pandas to let pack makers tune individually.
- 2025-11-21 (wander tuning): Consider a small configurable idle-wander radius per pet type instead of fully removing wander, to keep them lively without rubber-banding.
- 2025-11-21 (friendly fire config): If desired, expose a toggle allowing friendly-fire for specific pet types (for pack scenarios where shared damage interactions are needed).
- 2025-11-21 (name pool config): Consider moving pet name list to JSON/config so pack makers can theme names or localize more easily.
- 2025-11-21 (nameplate toggle): Add a config to choose always-visible vs hover-only pet nameplates for players who prefer constant labels.
- 2025-11-21 (kill credit UX): Surface pet-kill contributions in the GUI/Jade overlay so players can see which kills came from their beasts.
- 2025-11-21 (scaling tuning): Expose per-attribute multipliers (attack/health/speed) in config so pack makers can tune how strongly pets scale with STR/DEX/END.
- 2025-11-22 (XP tuning): Expose the new MMO-style XP curve constants (base, scale, exponent) in config and surface next-level XP + total XP earned in the companion UI so pack makers can rebalance progression without code edits.
- 2025-11-22 (resurrection flow): Gate activated scrolls behind an advancement/recipe unlock, add an owner-check when reviving to prevent griefing, and make the activation cost configurable (extra reagents, durability loss, or cooldown) while keeping inventory retention opt-in.
- 2025-11-22 (smithing UX): Add a custom smithing template item so players aren’t forced to burn any template for scroll activation, and expose a recipe unlock advancement to teach the template requirement in-game.
- 2025-11-22 (pack format guard): Add a small build check to keep `pack.mcmeta` pack_format aligned with the target Minecraft version so data tags can’t silently be ignored.
- 2025-11-22 (commands): Add a permission-configurable shortcut command (or datapack tag) for locating companion houses, and surface it in README/DESCRIPTION so players know they can `/locatecompanionhouse`.
- 2025-11-23 (structures): Replace the placeholder house-based NBTs for Berserker/Alchemist/Beastmaster/Cleric/Scout/Stormcaller/Vanguard with bespoke builds, and fix `gradlew` line endings so data-only build validations can run in WSL.
- 2025-11-23 (biome tuning): After playtesting, split biome lists by theme (e.g., keep sandstone/terracotta to arid, spruce to cold, windmills to windswept/meadow) instead of the unified temperate spread applied today, and consider separate structure sets per climate for finer control.
- 2025-11-23 (next refinement): Consider reintroducing biome tags (e.g., `#modern_companions:arid_structures`, `#modern_companions:cold_structures`) to reduce duplication and make future tuning data-driven; add exclusion lists for class houses if they should avoid arid/cold zones.
- 2025-11-23 (spawn QA): Add a lightweight debug config to log companion spawns per structure (or toggle the tracker) to confirm no duplicate spawning once gradlew is runnable again.
- 2025-11-24: Add a compact Jade/WTHIT line for Beastmaster pets that shows their master’s name and pet status/cooldown so players can quickly tell which companion a pet belongs to.
- 2025-11-24 (asset lint): Add a small build-time check (e.g., Gradle task) that fails if any asset paths contain uppercase letters or spaces so texture/model names stay pack-format compliant.
- 2025-11-24 (gui assets): Deduplicate GUI button textures (root vs `textures/gui`) and settle on one path convention to avoid silent fallbacks or black squares when casing diverges.
- 2025-11-24 (new casters): Playtest Fire/Lightning Mage and Necromancer damage numbers, AoE safety, and summon lifetime; consider lightweight HUD indicators for heavy-cooldown readiness to telegraph burst windows to players.
- 2025-11-24 (caster UX): Add a visible cooldown bar or particle cue for mage heavy spells and a timer UI for necromancer summons so players can predict downtime and despawns.
- 2025-11-24 (summon polish): Consider letting Necromancer summons ignore Peaceful despawn rules only while tamed/owned, and add a faint timer ring or fade-out particles as their lifetime expires for clearer feedback.
- 2025-11-24 (Vanguard stance): Add a stance toggle (aggressive vs defensive) that tunes shield-raise distance and aura radius, plus a small shield icon when blocking so owners can read the posture at a glance.
- 2025-11-24 (teleport tuning): Add a config slider for the companion recall distance (default 35 blocks) plus an optional "recall now" hotkey/button in the companion UI to let players manually snap companions back when crossing portals or long gaps.
- 2025-11-24 (sprint toggle polish): Consider a proper sprint icon + tooltip that explains speed vs hunger costs (if any), and optionally allow per-role sprint defaults or a config cap on sprint duration to avoid constant sprinting in combat.
- 2025-11-24 (consumable tuning): Add a config for which potion effects are allowed (or a whitelist tag) and a health-threshold slider so companions don't waste regen/heal potions when barely scratched; consider logging/debug overlays to show what item they'll consume next.
- 2025-11-25 (potion UX): Add a small HUD/icon when a companion is under an active potion effect and surface the remaining duration in the companion screen/Jade line so players know the drink actually applied.
- 2025-11-25 (summon affinity UI): Consider a tooltip or small icon near the Necromancer that indicates how many summons are active and that other companions will ignore them, reducing confusion about whether minions are friendly.
- 2025-11-25 (gem theming): Now that the spare gem textures are mapped to the mage and necromancer summon items, consider recoloring or outlining them to reflect class hues (fire, lightning, necrotic) and add a compact palette guide in docs for future eggs.
- 2025-11-25 (companion storage polish): Add per-class stored-companion models/textures (or a tint based on entity type) plus an optional owner-lock tooltip/advancement to make stored items clearly distinguishable from spawn gems and to teach the mover recipe in-game.
- 2025-11-25 (summoning wand UX): Add a short cooldown bar or on-use particle ring when the wand fires, and consider a config tag/whitelist so datapacks can block certain companion classes or pets from being recalled if desired.
- 2025-11-25 (enchantment tuning): Add a config/data cap for how many attribute enchant levels can stack across armor pieces (or introduce diminishing returns) and surface gear-contributed attribute bonuses directly in the companion UI for clarity.
- 2025-11-25 (creative tab polish): Consider grouping the new enchant books under a subheader or ordering them near armor items, and add a JEI info page describing their attribute bonuses and intended companion-only use.
- 2025-11-25 (data pack docs): Document the new data-driven enchant JSONs so pack makers can tweak costs/weights/slots via datapacks without code edits.
- 2025-11-25 (docs follow-up): Add an in-game guide/JEI info page for the sprint toggle, teleport recall distance, Companion Mover, Summoning Wand, and the four attribute enchants (what they boost and loot/creative sources) so players learn the new systems without leaving the game.
- 2025-11-25 (party UI): Consider an optional UI hint/config that shows current companion count vs a soft recommended cap, since the docs now highlight unlimited party size.
- 2025-11-26: Add a small `/companionskin list` or GUI toggle that surfaces which companions have custom URLs applied, plus a quick button to revert to bundled skins so players can manage downloads easily.
- 2025-11-26 (combat feel): Consider adding configurable bow draw/cooldown scaling by DEX or weapon tier and hook melee swings into Better Combat animations when that mod is present, so companion attacks visually match their stats and modded weapon styles.
- 2025-11-26 (net sync): If swing packets still drop under heavy latency, add a tiny debug toggle to log/trace attack hand events or send an explicit `ClientboundAnimatePacket` from custom damage sources as a fallback.
- 2025-11-26 (future-proof): If Better Combat is present, consider routing `forceSwingAnimation` through its API so companions use modded swing animations instead of vanilla packets, keeping visuals consistent with player swings.
- 2025-11-26 (pose polish): Add shield-block arm pose handling in `CompanionRenderer.armPose` so Vanguards block visibly; right now shields render as generic ITEM pose when raised.
- 2025-11-26 (fallback swing): If swings ever get suppressed again, consider setting `attackAnim` directly on the model in a renderer mixin as a last resort, but prefer the packet-based approach we now use.
- 2025-11-26 (data sync): Keep `LAST_SWING_TICK` gated behind a debug flag if logs get noisy; we can also expose it to a `/companions debug swings` command to help players verify animations are firing.
- 2025-11-26 (renderer guard): If animations still fail, add a minimal client renderer hook that sets `attackAnim = 1.0F` for one frame when `LAST_SWING_TICK` changes—last resort to override any animation suppression.
- 2025-11-26 (debug command): Add `/companions debug swings` to print the last swing tick per nearby companion and whether the forced fallback fired, to help players confirm the sync is working.
- 2025-11-26 (idle pose): If arms ever stick again, consider also zeroing `attackAnim` when no swing ticks have changed for >1s to hard-reset the pose without touching combat swings.
- 2025-11-26 (crossbow cooldowns): If arbalists feel too bursty now that they fire correctly, consider raising `attackDelay` (post-charge) slightly or scaling it with DEX so late-game arbalists gain smoother DPS without stalling charges.
- 2025-11-26 (scroll visibility): Add a subtle particle ring or glint hue around dropped Resurrection Scrolls so players can spot their now-indestructible drops quickly in cluttered battlefields.
- 2025-11-29 (Curios polish): Add a compact legend/tooltip in the companion Curios screen that labels each slot (Head/Shoulder/Back/etc.) and indicates which tags they accept; consider exposing cosmetic slot toggles and syncing render states so players can hide/show visuals per slot.
- 2025-11-29 (Curios UX follow-up): Add a small tab toggle so players can swap between Inventory and Curios without closing/reopening (two tabs sharing the same stats pane), and surface slot render toggles directly in the companion Curios UI.
- 2025-11-29 (Curios absence UX): When Curios isn’t installed, show a short tooltip or disabled-state hint near the missing Curios button so players know the feature is optional and how to enable it.
- 2025-11-29 (metadata guard): Add a small CI check (or Gradle verification task) that inspects the packaged `neoforge.mods.toml` for the current version and optional Curios flag, failing the build if they drift from `gradle.properties`.
- 2025-11-29 (schema drift): Audit other optional dependencies (Jade/WTHIT) and migrate their metadata blocks to `type = optional` to prevent future breakage if NeoForge drops support for `mandatory` entirely.
- 2025-11-29 (weapon fallback tuning): Consider a config-driven priority list per class so companions still prefer their weapon archetypes but avoid equipping consumables when no ideal gear is available; optional filter for “usable in combat” tags could keep food/potions out of main hand.
- 2025-11-29 (shield tag hardening): Add a tiny helper for tag creation that rejects malformed paths at dev time, preventing crashes if a tag name accidentally contains a namespace delimiter.
- 2025-11-30: Hook Bond/Morale events (time-with-owner ticks, feeding, resurrection, near-death) to award XP and adjust morale using config multipliers; add minor stat nudges per morale/trait and a simple Memory Journal panel to read total kills/resurrections.
- 2025-11-30 (follow-up): Implement trait-specific stat/AI nudges and morale floors from Bond levels, and expand the Memory Journal with major kills/distance traveled once those metrics are tracked.
- 2025-12-01: Refine trait hooks (Guardian target weighting, Reckless chase radius, Lucky loot bumps) and add configurable thresholds; surface major kills separately and show distance traveled in more readable units (km) with formatting.
- 2025-12-01 (next): Tune Lucky drop bonus to roll an extra loot-table pass instead of duplicating an existing drop; add target weighting for Guardian (prefer mobs targeting owner) and a modest chase radius bump for Reckless, all behind config toggles.
- 2025-12-03: Add a lightweight debug overlay/toggle for miners that renders their current surveyed cube and ore waypoints, plus a config to throttle rescan frequency for large patrol radii to keep server load predictable.
- 2025-12-03 (courier follow-up): Surface the assigned drop-off chest in the companion GUI (coords + dimension) with a "deliver now" button, and add a tiny status icon showing when a delivery run is active or blocked by chunk loading.
- 2025-12-03 (wand UX): Add a subtle actionbar hint when the Assignment Wand is held explaining the two-step flow (select companion, then shift-right-click chest), and show the currently stored companion name in the wand tooltip.
- 2025-12-03 (selection persistence): Optionally mirror the wand’s stored companion in the tooltip/actionbar by reading the player-persistent cache so players know the selection survived hand swaps/logouts.
- 2025-12-04 (wand + logging toggle): Add a small client config to suppress the companion GUI when the Assignment Wand is held (for players who prefer the old behavior) and a server config/logging toggle to keep lumberjack trace logs at INFO or drop back to DEBUG once the issue is solved, avoiding noisy production logs.
- 2025-12-04 (lumberjack UX): Expose a config knob for the new stall watchdog (idle seconds before repath/skip) and surface a brief actionbar/WTHIT hint when a lumberjack recovers from a stall so players know why it jumped targets.
- 2025-12-04 (lumberjack stance): Add a config for ground-stand search radius/offset and an optional client hint that shows the chosen stand tile, so players can adjust behavior on uneven terrain without changing code.
- 2025-12-06: For Fishers, consider caching the last successful stand/water pair and probing outward from it (with a cap) before doing a full scan; this keeps "nearest water" pathing snappy without lifting the scan throttle.
## 2026-07-27
- Add an integration test world with a protection mod and manually validate that worker block changes respect its cancellation hooks; companion actions do not have a player-break context.
## 2026-07-27
- Add a small screenshot-based GUI smoke test if future sidebar layout changes become frequent.
## 2026-07-29
- Add visible on/off state or tooltips to the text sidebar buttons only if players need feedback beyond their resulting companion behavior.
## 2026-07-29
- Add a dedicated selected-button sprite only if the standard Minecraft focused appearance is not distinct enough during in-game playtesting.
## 2026-07-29
- Add configurable companion spacing only if playtesting shows parties still overlap; the saved radius already controls the follow leash and idle area.
## 2026-07-29
- Add a small Sophisticated Backpacks runtime smoke world only if future upstream GUI/context changes require a repeatable compatibility check.
## 2026-07-29
- Add a focused TacZ dev-world smoke check only if a future TacZ release changes its entity item-handler API; the standard companion inventory capability now covers native reload and ammo consumption.

## 2026-07-29 (magic companion runtime smoke)
- Add a small two-mod dev-world smoke only when Iron's Spellbooks or Ars Nouveau changes its public casting API; reflection keeps both integrations optional, so live casts, summon lifetime, and ally-safe AoE need verification against installed versions.

## 2026-07-29 (conditional worldgen regression)
- Keep one resource-load smoke with neither magic mod installed whenever adding a gated entity; static structure JSON cannot refer to an entity omitted from the registry.
- Keep required empty codec fields such as `spawn_overrides: {}` when removing static entries; valid JSON alone does not prove Minecraft's structure codec accepts it.

## 2026-07-29 (conditional registry regression)
- Keep a no-magic-mod creative-tab/JEI and Curios data-load smoke whenever adding gated companion content; Curios 9.5.1 accepts only direct registered entity IDs here, not entity tags.

## 2026-07-29 (magic metadata regression)
- Keep an installed-Iron's/Ars launch smoke when changing optional dependency metadata; NeoForge compares full mod version strings such as `1.21.1-3.16.2`.

## 2026-07-29 (magic targeting smoke)
- Keep a live clear-LOS and blocked-LOS cast smoke for every upstream spell API update; direct projectiles use the caster's look vector while entity resolvers target directly.

## 2026-07-29 (magic ally-safety smoke)
- Keep a compact Prism test pen: caster owner, same-owner companion, same-owner summon, villager, enemy player, and enemy-player companion. Verify summoned swords never target friendly entries, then verify PvP/villager toggles only unlock their intended enemy categories and Intelligence produces a measurable spell-damage increase.

## 2026-07-29 (Wizard batch-cap smoke)
- Recheck the three-weapon entity IDs only when upgrading Iron's Spellbooks; they are the narrow live-batch contract that lets Wizards resummon after every prior weapon has actually gone.

## 2026-07-29 (magic gem visuals)
- Add new gem art only when a reused role-fit gem no longer gives a new class a readable identity; the current nine model files deliberately reuse proven, packaged assets.

## 2026-07-29 (resource balance)
- Add config values only after live play shows 100-point pools, 10/20/35 spell costs, or 5-second combat grace need tuning; fixed shared values avoid per-spell config noise now.

## 2026-07-29 (brewing regression)
- Keep one launch smoke whenever moving a NeoForge listener; compile success cannot verify the event's owning bus.

## 2026-07-29 (resource visuals)
- Keep one Prism hover smoke at empty, partial, and full Stamina/Mana plus one creative-tab reload after changing Jade rendering or the item atlas; Gradle validates resources, but game UI/atlas stitching is runtime behavior.

## 2026-07-29 (potion-effect icons)
- Keep one inventory and HUD smoke for every custom potion icon after NeoForge updates; the client extension API controls both render paths and cannot be fully validated by Gradle.

## 2026-07-29 (expanded companion inventory)
- Keep one Prism smoke for equipment placement/removal, unload/reload persistence, the 3D preview, and green/off versus dark-red/on safety switches; Gradle cannot verify texture alignment or live entity rendering.

## 2026-07-30 (equipment follow-up)
- Add support for modded weapons or shields only when a concrete item family needs automatic shift-equip; vanilla armor, swords, and shield-tagged items now cover the requested flow without a generic item scoring framework.

## 2026-07-30 (effect icon regression)
- Keep one inventory and HUD smoke after replacing effect-icon art; the 32px inventory cell and centered 18px HUD icon are separate render paths.

## 2026-07-30 (inventory effect alignment)
- Recheck only if NeoForge changes the inventory-extension coordinates; 32px art is intentionally offset seven pixels from the normal 18px icon origin to share its center.

## 2026-07-30 (hand equipment rules)
- Add a modded weapon/tool tag only when a concrete item family needs automatic companion use; the current native item checks and existing sword/firearm support cover the shipped equipment paths.

## 2026-07-30 (JEI brewing visibility)
- Keep the JEI adapter limited to brewing steps defined by `CompanionBrewing`; add a custom JEI category only if the brewing stand can no longer represent a future potion workflow.

## 2026-07-30 (lumberjack foliage recovery)
- Keep a Prism smoke for oak, dark oak, and leaf-walled trees with mob griefing both enabled and disabled; pathfinding and protection outcomes need live-world validation.

## 2026-07-30 (lumberjack full-tree felling)
- Keep a Prism smoke for tall birch, spruce, and dark-oak trunks; the lumberjack now retains a single stump stand and must clear every connected log before replanting.

## 2026-07-30 (living jobs follow-up)
- Add durable per-job target checkpoint serialization and bounded incremental tree/miner scans after live smoke identifies a concrete remaining unload or large-area stall; current shared Work, reservation, action, and delivery contracts are intentionally small and avoid a behavior-tree framework.

## 2026-07-30 (Work radius smoke)
- Keep a two-chest dev-world smoke: bind chest A, enable Work, verify Follow/Patrol unpress and job stays inside Radius around A; then rebind chest B and verify future searches move to B's radius without stale patrol-center work.

## 2026-07-30 (job route and supply smoke)
- Test one Miner with an exposed ore and a solid-wall ore: verify it never breaks the floor below a planned feet cell and leaves blocked/protected ore queued. Test one Chef with only tagged raw meat in its assigned chest: verify travel to chest, one-item withdrawal at chest stand, cooking, deposit, and resume. Add wider route/replant persistence only after this baseline live path is verified.

## 2026-07-30 (job path regression smoke)
- Keep a leaf-wall tree, an unobstructed chest, a one-block-wide dirt tunnel, and a pond shore in the dev test pen. Verify no chest spam, Lumberjack leaf clearing/full-log progress, Miner digs from present feet, and Fisher bobber always begins at farther surface water.

## 2026-07-30 (tree and return-target smoke)
- In the same pen, confirm a reachable trunk leaves its canopy intact while an actually blocked approach removes only enough leaves to proceed. Confirm a Fisher visibly turns toward its far-water bobber and a Miner retains a clear flat return route to the chest-side stand.

## 2026-07-30 (Lumberjack navigation smoke)
- Add one normal tree and one leaf-walled tree at several Radius distances. Confirm a Lumberjack's path advances without repeated restart, reaches normal trees without leaf damage, and clears only the minimum blocked leaf approach.

## 2026-07-30 (job reliability smoke)
- Keep one compact two-worker pen with an oak, tall spruce, acacia, solid dirt/stone ore tunnel, nearby cave ore, pond, and shared chest. Verify reservations, complete log removal, tunnel return, one-second minimum recasts, retained food/potions, two-minute bulk unload, and dusk unload before expanding the planners further.

## 2026-07-30 (128-radius search smoke)
- Bind a chest at surface level, set Radius to 128, and place known mature trees and exposed/buried ores at roughly 8, 32, 64, and 120 blocks. Confirm center-out discovery prefers nearer targets and eventually reaches the outer ring without a long server tick.

## 2026-07-30 (first excavation smoke)
- Put known ore below a grass/dirt surface and watch the first three descending steps: each must visibly remove its upper/lower tunnel blocks, preserve floor support, walk forward, and leave the same opening usable on return.

## 2026-07-30 (Jobs button layout smoke)
- Open companion inventory with `showJobsButton` false and true, both with and without Curios/Pack. Confirm the stack remains contiguous and the bottom control stays inside the supplied texture.

## 2026-07-30 (job inventory panel smoke)
- Open the inventory before and after assigning each job. Confirm `newinventory_nojob.png` shows no Currently/State panel and `newinventory.png` plus live job/status text return immediately after assignment.

## 2026-07-30 (journal editing smoke)
- In a multiplayer-capable dev world, rename a companion, set a long Bio, and paste an HTTPS skin URL through the journal. Confirm each Enter submission survives relog, the edit sprite uses all three states, non-owner requests are ignored, and the skin renderer refreshes for tracking clients.

## 2026-07-30 (journal edit navigation smoke)
- Confirm the Back button below Skin and Escape both return to the journal at every supported GUI scale.

## 2026-07-30 (journal age editing smoke)
- Set an Age at each boundary (1 and 120) and reject empty, non-numeric, zero, and 121 values; confirm valid ages update the journal and survive relog.

## 2026-07-30 (TacZ firearm specialists)
- Keep the TacZ category mapping tied to `CommonGunIndex.getType()`; add a data/config override only if a future TacZ pack exposes a category that does not identify itself through that index.
- Keep specialist loadout diversity and spawn weights fixed until a live-world pass shows a balance problem; the current Heavy 1% and Sniper 4% specialty weights satisfy the requested rarity without adding config noise.

## 2026-07-30 (TacZ specialist summon gems)
- Keep the seven specialist gems on the shared `gem_9` art until a player-facing visual pass shows that category-specific art materially improves recognition; separate texture assets are unnecessary for the current request.

## 2026-07-30 (TacZ specialist display names)
- Keep the labels tied to the specialty enum until localization is requested; add translated display keys only when the project standardizes these class labels across languages.

## 2026-07-30 (TacZ firearm capture restore)
- Keep the load-time preservation guard tied to TacZ classification availability; add explicit serialized firearm migration only if a future TacZ version changes its item-stack schema rather than merely delaying its resource index.

## 2026-07-30 (journal local skin removal)
- Keep skin editing HTTP(S)-only; revisit local skins only with an explicit upload and synchronization design.

## 2026-07-30 (death effect cleanup)
- Keep the death/resurrection smoke test covering a vanilla harmful effect and Mekanism radiation; add other optional
  capability-specific cleanup only when a mod demonstrates death-persistent harmful state.

## 2026-07-30 (health threshold and name pools)
- Keep the default health threshold at 0.5 until gameplay smoke shows that companions request food too early or too late; add per-companion thresholds only if global tuning proves insufficient.

## 2026-07-30 (medieval and fantasy name expansion)
- Keep names as static data tables; move them to datapack-driven content only if players need server-specific naming themes or localization.

## 2026-07-31 (complete bundled skin pools)
- Keep future skin filenames lowercase with only letters, digits, hyphens, underscores, and periods so they can be registered directly as Minecraft resource paths; add a build-time asset linter if contributors continue adding large skin batches.

## 2026-07-31 (female skin pool refresh)
- Keep new skin PNGs at 64x64 before adding them; add the suggested asset-to-pool linter if future batches continue to arrive after code updates.

## 2026-07-31 (pre-tame empty-hand dialogue)
- Keep empty-hand dialogue in `CompanionData.notTamed`; move it to localization only if dialogue translation becomes a supported feature.

## 2026-07-31 (progression-gated taming resources)
- Keep progression flags on the player’s persistent data; move resource tiers to datapack/config content only if server-specific taming rules are requested.

## 2026-07-31 (player-facing localization)
- Keep new UI, dialogue, job-status, command, and optional-integration copy in the language files; add translated locale values when translation support is expanded beyond the English fallback.

## 2026-07-31 (configurable stamina costs and toggle)
- Keep the three values in the common companion config; add per-class or per-attack stamina tuning only if live balancing shows the shared sprint/melee costs are insufficient.

## 2026-07-31 (upstream summon targeting smoke)
- Keep the shared 200-tick combat-assist memory fixed until live play shows stale owner-hit targets or a summon needs a longer pursuit window; add a config only if that boundary proves player-visible.
- In a dev world with Iron's Spellbooks and/or Ars Nouveau, verify Necromancer summons attack visible hostile mobs, assist the Necromancer/owner's active fight, clear targets behind walls, leave passive mobs alone, and obey the existing PvP/villager safety toggles. Repeat the clear-LOS case for Wizard summons.

## 2026-07-31 (Epic Fight compatibility smoke)
- Keep the shared humanoid patch for all companions; split class-specific Epic Fight combos only if a live balance pass shows that a role needs a distinct moveset.
- In a dev world with Epic Fight, verify melee stances/combo hits, bow and crossbow draw/release, shield blocking, spell and firearm fallback behavior, role effects and stamina, companion safety toggles, and normal vanilla behavior with Epic Fight removed.

## 2026-07-31 (Epic Fight capability maintenance)
- Keep bundled weapon capabilities as item data rather than changing the existing weapon class hierarchy; add a new capability JSON whenever a material or weapon family is registered.
- If a future Epic Fight release provides a renderer compatible with the companion player model and dynamic skins, evaluate it in a dev world before re-enabling animated companion meshes.

## 2026-07-31 (TacZ pose integration)
- Keep the MIT-derived mixin companion-only; add player combat-mode changes only if player-facing Epic Fight/TacZ compatibility becomes an explicit feature.
- Recheck the reflected TacZ third-person method against the installed TacZ version whenever that mod updates its client animation API.

## 2026-07-31 (Epic Fight renderer split)
- Keep the renderer fallback tied only to a currently held TacZ gun; cargo guns must not disable Epic Fight movement or melee animations.

## 2026-07-31 (stable automatic weapon selection)
- Keep the held class-valid weapon as the selector's first choice; add explicit equipment-ranking only if a future feature needs companions to automatically replace an already valid weapon with a better one.
- In a dev world, give each companion two compatible weapons, verify the hand stops changing after the initial selection, then confirm an intentional player equipment change still selects the newly held valid weapon exactly once.

## 2026-07-31 (Epic Fight AI ownership)
- Keep the split by held weapon: Epic Fight owns melee, native goals own bows/crossbows and TacZ guns. Add custom Epic Fight ranged AI only if the upstream ranged animation hooks stop supporting a future game version.
- Smoke-test a melee companion with a TacZ gun only in cargo, then with that gun equipped, to ensure cargo does not change melee animation/pathing and an equipped gun still uses native TacZ behavior.

## 2026-07-31 (Epic Fight weapon-swap goal repair)
- Keep the post-swap repair conditional on a missing animated/chase pair; add a general selector rebuild only if a future Epic Fight release shows another reproducible lifecycle that loses both goals.
- Verify a sword, axe, custom club, spear, and glaive each immediately restore chase and an animated hit after a companion was previously unarmed.

## 2026-07-31 (Epic Fight authoritative hand capability)
- Keep melee-goal reconstruction tied to the equipment event's replacement capability; only revisit the general goal lifecycle if a future Epic Fight version changes that event contract.
- Verify each bundled weapon family after a direct player handoff and after the companion selects it from cargo.

## 2026-07-31 (Epic Fight companion animator ownership)
- Keep the companion patch on `MobPatch`; only return to `HumanoidMobPatch` if Epic Fight exposes a held-item motion hook that preserves the companion renderer's base living motions.
- Verify melee behavior after changing a held item, after loading a saved companion, and after a tracking client joins.

## 2026-07-31 (Epic Fight companion attack range)
- Keep the companion-specific range gate unless Epic Fight exposes a target predicate that accounts for companion dimensions and stance height.
- Validate hit reach against small, standard, and tall hostile targets before changing the per-category ranges.

## 2026-07-31 (Epic Fight weapon animation timing)
- Keep the positive companion attack-speed floor because companion base attributes are intentionally lower than player base speed while held weapon penalties are player-calibrated. Revisit it only if companion base attack speed is raised to absorb every supported weapon penalty.

## 2026-07-31 (structure insertion smoke)
- Keep structure residents on the bounded code-spawn path; only raise the one-per-tick budget if profiling shows the queue cannot clear during deliberate pregeneration. Test `/place structure` and a Chunky-style pregeneration run with timings enabled before changing it.
