- 2026-08-10 (Sorcerer targeting and Battlemage combat): Smoke-test Sorcerer Chain Lightning with `friendlyFireCompanions` disabled and enabled around the owner, same-owner companions, other tamed pets, Beastmaster pets, and hostile mobs; confirm players remain safe and invalid chained targets receive no damage/effects.
- 2026-08-10 (Sorcerer targeting and Battlemage combat): With Iron's Spellbooks and Ars Nouveau separately and together, verify Battlemage uses `firebolt`/`fang_strike`, never attempts `spectral_hammer` or `fang_ward`, and closes for melee only while Mana is below its basic spell cost.
- 2026-08-10 (Sorcerer targeting and Battlemage combat): If Battlemage spends too long in melee or the new spells need balance, tune the existing ranged interval/heavy recovery or Mana costs after live combat testing rather than adding another AI layer.

- 2026-08-08: Smoke-test fresh structure and spawn-egg companions with Spawn Armor/Weapon enabled and autoEquip disabled; confirm armor, main-hand, and offhand gear is equipped live, while only intended ammo/consumables remain in cargo, and repeat after relog.
- 2026-08-08: Smoke-test a clean instance with Apothic Enchanting/Apotheosis: place and open both tables, verify the Companion Table keeps its own animated book, particles, and menu, and confirm enchanting-table compatibility changes do not produce a registry crash.
- 2026-08-07: Playtest the Companion Table with primary and secondary trait replacement, tooltip-only 15/5-level costs, red bond warnings at Bond 0/I, insufficient XP/materials, invalid-owner Soul Gems, creative mode, shift-click insertion, JEI recipe visibility, and breaking/replacing the table; consider a dedicated icon or button if right-click secondary selection is not discoverable enough.
- 2026-08-07: In a dev world with Iron's Spellbooks installed, equip each magic companion with an Iron's staff, wand, spellbook/Curios item, scroll, and magical armor set; verify the implement remains equipped, native spells cast, Max Mana/Mana Regeneration/Spell Power/cooldown/cast-time/casting-speed/summon-damage/resistance bonuses change the live behavior, and removing the gear removes only its bonus.
- 2026-08-05: Smoke-test two same-owner companions with one injured, then test simultaneous player/ally/self injuries; verify player-first, most-injured-ally second, self third, ranged fourth, and melee-only when Mana is unavailable.
- 2026-08-04: Smoke-test a Holy Spark's lethal hit against undead and non-undead targets; verify Cleric kill count, XP progress, level-up behavior, and no duplicate XP award.
- 2026-08-04: Smoke-test Cleric owner healing, self-healing, holy sparks, and melee fallback with Mana starting full, partial, and empty; confirm the 6-Mana cost and faster regeneration for magical companions without changing non-magical Stamina pacing.
- 2026-08-04: Smoke-test simultaneous owner and Cleric injuries with Iron's Spellbooks and Ars Nouveau separately; verify owner health rises before Cleric health, then ranged kiting, then melee fallback when Mana is empty.
- 2026-08-04: Smoke-test Cleric holy sparks against a Zombie, Skeleton, and non-undead target with full and depleted Mana; verify sparkle visibility, doubled undead damage, owner-heal priority, and melee fallback.
- 2026-08-04: Add a dev-world Cleric smoke check with a full-health owner and an injured owner; confirm melee kills award Cleric XP, healing pauses offense, and combat resumes after the owner is restored.
- 2026-08-04: Keep config/Explanation.md synchronized with every new ModConfig option; add a runtime smoke step whenever a documented option changes behavior or UI visibility.
- 2026-08-04: Add a dev-world smoke check proving untouched `allFoods` discovers safe modded foods while a customized `allFoods` list excludes unlisted modded foods from healing, favorites, and random recruitment.
- 2026-08-04: Add a dev-world smoke check for a wildcard recruitment rule, a per-companion override, three or more required items, save/reload, and an omitted companion retaining the random default.
- 2026-08-03: Add a dev-world smoke check with one safe standard food from another mod, one modded food with harmful effects, a configured custom consumable, save/reload of a modded recruitment request, and confirmation that vanilla foods still appear.
- 2026-08-01: Add a dev-world dimension-transition smoke check for a nearby Follow companion, a distant Follow companion, Patrol, Guard, ordered-sit, and active-job companions; verify only the eligible nearby Follow companion appears beside the player in the target dimension.
- 2026-08-01: Keep taming-resource and manual-Hunt lists as registry-ID config entries; add tag selectors only if pack authors need broad dynamic item or entity groups.
- 2026-08-01: Keep the one-time Creeper-default migration marker hidden from the config screen; replace it only if NeoForge adds first-class config migrations.
- 2026-07-31: Keep Creeper as the sole editable Alert exclusion by default; add a broader default safety roster only if player feedback establishes a clear need.
- 2026-07-31: Consider an optional entity-ID picker only if the concise registry-ID field and tooltip prove insufficient for modpack players.
- 2026-07-31: Restore the Jobs configuration section only when the profession workflows have a complete player-facing release and smoke coverage.
- 2026-07-31: Add translated Modern Companions configuration labels to the existing Russian, Portuguese, and Polish language files when those locales receive a review pass.
- 2026-07-31: Add a dedicated-server smoke check confirming Mods → Modern Companions → Config shows Alert and persists an exclusion after reconnect.
- 2026-07-31: If non-operators should manage Alert exclusions on dedicated servers, add an owner-scoped companion setting rather than exposing the server-wide safety policy through a client-only mod-menu screen.
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

## 2026-07-31 (Epic Fight mage AI ownership)
- Keep the single Epic Fight ownership predicate: mages retain their spell goal while Epic Fight keeps the shared patch and animated renderer. Add a dedicated cast animation only if the current generic spell swing is visibly inadequate.
- With Epic Fight and Iron's Spellbooks and/or Ars Nouveau installed, verify each mage keeps range, casts light and heavy spells, and still displays Epic Fight idle, movement, hit, and cast animations.

## 2026-08-01 (pointed dripstone pathfinding)
- Smoke-test a companion through dripstone caves with both upward stalagmites and downward stalactites, including a one-block-wide route, and confirm it chooses the open route around them without repeated replanning.
- Keep the explicit evaluator guard unless a future Minecraft path evaluator correctly classifies the air node above pointed dripstone for the companion's full bounding box.

## 2026-08-01 (main equipment render toggles)
- Smoke-test each armor slot, main hand, and offhand toggle in the companion inventory, including the 3D preview, world rendering, relog persistence, and an Epic Fight-enabled client.
- If main equipment visibility must work without Curios installed, move the shared eye icon into a Modern Companions-owned texture instead of depending on Curios' existing 8px sprite.

## 2026-08-01 (Epic Fight Curios renderer compatibility)
- With `epicfight_curios_compat` and `efcurioshead` installed, smoke-test body, head, necklace, belt, hand, and feet Curios on companions while idle, walking, attacking, looking, crouching, and using the inventory preview.
- Keep the reflection bridge unless the compatibility mod publishes a stable API; replace it with a direct integration only when that API exists and the dependency remains optional.

## 2026-08-01 (companion cosmetic armor popup)
- Add a dev-world smoke test for opening the popup, placing/removing head/chest/legs/feet armor with left and right clicks, hovering item tooltips, and closing with the green button.
- Verify cosmetic armor replaces functional armor visually in the world and inventory preview while functional armor attributes and equipment-render toggles remain correct.
- Repeat persistence checks through relog, Companion Mover capture/redeploy, Resurrection Scroll revival, and an Epic Fight-enabled client.
## 2026-08-01

- Keep the equipment-panel background and popup visibility state in `CompanionScreen`; this keeps the split texture assets aligned without duplicating menu state.

## 2026-08-01 (automatic gear toggle)

- Add a dev-world check for default-off behavior, manual armor/weapon placement, shift-click fallback to cargo, and job-tool equipping while `autoEquip` is disabled.

## 2026-08-01 (cosmetic slot release routing)

- Add a visual smoke check for removing and replacing each cosmetic armor piece with empty and occupied cursors.

## 2026-08-01 (functional equipment panel refresh)

- Add a smoke check that confirms the cosmetic popup, standard equipment panel, and world model show their respective functional/cosmetic views without requiring an inventory reopen.

## 2026-08-01 (optional Radius-relative teleport leash)

- Smoke-test the default-off setting, then enable it in Mods → Modern Companions → Config → Companion and verify teleport starts at Radius + 5 for small and large Radius values.
- If teleport frequency needs further tuning, expose the 5-block buffer only after live following tests show that a fixed buffer is insufficient.

## 2026-08-01 (cosmetic armor popup alignment)

- Add a visual dev-world check for empty armor silhouettes, each cosmetic slot independently, and the popup preview against the standard equipment preview.

## 2026-08-01 (companion voice pools)

- Smoke-test actor persistence through relog, Companion Mover capture/redeploy, and Resurrection Scroll revival; add per-cue volume controls only if live playtesting shows the shared voice level needs tuning.

## 2026-08-01 (single companion enemy callout)

- Smoke-test two or more owned companions acquiring the same hostile target together and confirm only one `Enemy Spotted` voice plays while all companions still attack normally.
- If callouts should also be globally serialized across different simultaneous enemies, add an owner-level cooldown after live testing shows target-specific suppression is insufficient.

## 2026-08-01 (closest companion enemy callout)

- Smoke-test companions at different distances from the player and confirm the closest one is the audible speaker while every eligible companion retains combat behavior.

## 2026-08-01 (taming voice cue routing)

- Smoke-test a new untamed companion with empty hand, wrong food, desired food, repeated desired food, and rapid clicks; verify one greeting, refusals only for rejected food, confirmation for accepted food, and no overlapping cues.

## 2026-08-03 (companion health persistence)

- Smoke-test a leveled companion through save, relog, and chunk unload/reload while full and while injured; also add/remove a max-health source and confirm full companions refill once while injured companions retain their saved health.
- If runtime behavior differs, capture the companion's level, max health, current health, and source of the extra max health before and after the reload.

## 2026-08-03 (survival weapon enchanting)

- Smoke-test one representative dagger, hammer, club, spear, quarterstaff, and glaive in an enchanting table and anvil with Sharpness, Unbreaking, and Mending; repeat with bronze loaded and absent.
## 2026-08-03 (Beastmaster pet safety)

- Smoke-test an Ocelot and each supported pet family beside its Beastmaster and player owner, then repeat with `friendlyFireCompanions` enabled to confirm the documented opt-in behavior.
- Add a pet-specific avoidance rule only if a future pet needs to retain one particular vanilla avoidance behavior after live testing.

## 2026-08-03 (recruitment food status rendering)

- Smoke-test two untamed companions in sequence and verify vanilla, modded, wrong, and partially fulfilled requirements render item names in chat after the localization change.
- If a display-only fix does not resolve the issue, capture the interaction log from a build that actually reports that version before changing recruitment logic.

## 2026-08-04 (server-side recruitment item names)

- Smoke-test the new build in an integrated server and confirm the client resolves both vanilla and Farmer’s Delight item descriptions in the recruitment chat.
- Keep server-side recruitment messages as translatable components; resolving item names with `getString()` before sending can produce empty text because the server has no client language table.

## 2026-08-06 (Soul Reforging)

- Keep the catalyst-to-trait pools data-driven if modpack authors later request custom personality themes or additional catalysts.
- Add a dedicated visual ritual effect only if the native Enchanting Table screen and particles do not provide enough feedback in live play.

## 2026-08-04 (Epic Fight recruitment chat compatibility)

- Smoke-test 3.52 with Epic Fight enabled, then repeat with Epic Fight disabled, using a second companion and both vanilla and Farmer’s Delight foods.
- The refreshed 3.51 logs fail during unrelated mod loading before recruitment; retain a clean successful runtime log if the 3.52 chat still renders blank.
- 2026-08-07: Smoke-test `/companions bond "NAME"` as the owner and as an operator, including names with spaces, companions in another loaded dimension, relog persistence, and the Bond-disabled config path; verify the Journal shows Bond V / 2,000 XP.
- 2026-08-07: Playtest the Companion Table block and item from multiple angles and lighting conditions; verify the custom top, side, bottom, and particle textures render correctly while the animated book remains aligned.
- 2026-08-07: Switch between English, Polish, Brazilian Portuguese, and Russian, then verify the Companion Table name in inventory/JEI, its container title, trait tooltips, Soul Reforging feedback, and Bond requirement warning.
## 2026-08-07 (Beastmaster's Wand and Soul Orbs)

- Smoke-test cow, wolf, villager, water, and modded non-hostile mobs for NBT/UUID preservation, cyan orb naming, inventory-full drops, and rejection of hostile mobs.
- Smoke-test owner-only Beastmaster swaps in both directions, pet combat/follow behavior after swapping, ground release, relog, dimension movement, and Companion Mover storage/redeployment.

## 2026-08-07 (Beastmaster pet resurrection persistence)

- Smoke-test a Beastmaster with each pet family through combat death, Resurrection Scroll activation, relog, and Companion Mover storage; verify the same pet UUID, name, taming state, equipment/data, owner link, and follow behavior return without a duplicate pet.

## 2026-08-07 (Soul Orb companion interaction)

- Smoke-test Soul Orb use on Beastmasters and ordinary companions, confirming Beastmaster swapping runs before any inventory GUI opens and ordinary companion right-click behavior remains unchanged for non-orb items.

## 2026-08-07 (Beastmaster Soul Orb pet names)

- Smoke-test swapping a nameless animal Soul Orb onto a Beastmaster and confirm it receives a stock pet name; repeat with a custom-named orb and confirm the stored name is unchanged.

## 2026-08-07 (Curios and Ars magic equipment)

- Smoke-test a stock mage and an optional Ars mage with Iron's and Ars armor, Curios rings/amulets, spellbooks, staffs, scrolls, enchanted weapons, bows, and crossbows; verify attribute changes apply and each stored spell casts.
- Repeat with Curios absent, each magic mod absent, and both magic mods installed; add a compatibility-specific adapter only if an installed addon does not implement the documented native caster interfaces.

## 2026-08-08 (Steve/Alex companion model toggle)

- Smoke-test the Bio edit button on an owned companion, confirm both model shapes in-world and in the inventory preview, then relog and verify persistence.
- Repeat once with Epic Fight enabled; add separate slim-arm armor geometry only if live Alex-model armor visibly misaligns.

## 2026-08-08 (visible current Steve/Alex model state)

- Verify the Bio edit Model button reads `Steve` or `Alex` before and after toggling, then reopen the screen and confirm the synchronized state is still correct.

## 2026-08-08 (Alex armor geometry)

- Verify Alex-arm thickness with no armor and with each armor slot equipped; repeat with Steve to confirm the wide armor path is unchanged.

## 2026-08-08 (Epic Fight Alex mesh selection)

- With Epic Fight installed, toggle both models in-world and verify the arm width changes while attack animations and armor remain aligned.

## 2026-08-08 (legendary items)

- Smoke-test each legendary item's tooltip/model, durability, mining action, native mace smash, trident throw/return, arrow firing, companion equipment, and Epic Fight animation; also open representative common, epic, and mythic structure chests to verify the global modifiers without relying on a biome or dimension-specific rule.

## 2026-08-08 (legendary creative-tab visibility)

- Open the dedicated Modern Companions and vanilla Combat tabs in-game and verify all 34 legendary items appear, including the three arrow variants.

## 2026-08-08 (currencies)

- Smoke-test currency items in the dedicated tab, each sprite/tooltip value, vanilla chest loot across dungeons, mineshafts, temples, and villages, the `enabled` toggle, and configured JEI trade rows.
- If a pack needs denomination-specific loot weighting, add a weighted currency list to `[currencies]`; the shipped distribution currently favors lower denominations with a fixed lightweight table.

## 2026-08-09 (credit card wallets)

- Smoke-test Credit Card UUID/balance persistence through relogging, death, containers, and multiplayer; cursor deposits, highest-balance shift-click deposits, card combining, overflow rejection, and unrelated shift-click behavior.
- Verify rare loot cards roll balances from `5` through `7,500`, Credit Cards remain uncraftable, exact-value conversions work in a 3x3 grid, and `CurrencyService.pay` deducts only the requested amount from the smallest sufficient card.

## 2026-08-09 (loot modifier gating)

- Smoke-test breaking stone, ores, and custom blocks alongside representative structure chests; confirm potion, enchanted-book, and legendary loot stays chest-only while intended chest additions still roll.

## 2026-08-09 (currency validation and localization)

- Smoke-test the currency config screen in Polish, Brazilian Portuguese, and Russian; verify malformed JEI trade entries such as `-|garbage` are rejected while the documented `-|0` form remains accepted.

## 2026-08-09 (loot condition and resource-path correction)

- Install 3.92 in the active Testing instance, reload the world, and break stone, ores, and modded blocks; verify no Modern Companions potion, enchanted-book, or legendary loot appears from block drops.
- Open representative dungeon, mineshaft, temple, mansion, bastion, and End City chests and verify potion, enchanted-book, and legendary additions load after changing the predicate, serializer, and loot-table paths.

## 2026-08-09 (Health Pack)

- Smoke-test self-use, another-player use, owned and unowned companion use, full-health no-op behavior, item consumption, and the 30-second cooldown in a dev world.
- Open representative dungeon, mineshaft, village, temple, stronghold, ship, and late-game structure chests to confirm the Health Pack modifier resolves only through chest loot.
- If the pack is too frequent or too scarce in a modpack, tune the single `0.04` random chance in `health_pack.json`; add a config option only if pack-specific balancing is needed.

## 2026-08-09 (Mekanism companion armor rendering)

- Smoke-test MekaSuit helmet, bodyarmor, pants, and boots on both Steve- and Alex-model companions with Mekanism enabled; verify modules, energy tint, and normal armor remain aligned.
- Repeat the same world with Mekanism absent to confirm Modern Companions still starts and vanilla armor rendering is unchanged.

## 2026-08-10 (Companion mount travel)

- Smoke-test an owned saddled horse and camel with an actively Following companion: automatic mount selection, assigned-mount selection, shared-seat behavior where supported, owner dismount, switching to Patrol/Guard, relog persistence, mount death/removal, and unassigning with the same Wand target.
- Test sit anchoring beside an existing fence, safe oak-fence placement on normal terrain, protected/replaceable-block rejection, broken fence recovery, and multiplayer ownership/claim protection.
- Add a compatibility predicate for a custom mount only if a real modded mount is found that is owned, saddled, and leash-capable without implementing the vanilla `OwnableEntity`, `Saddleable`, and `Leashable` contracts.

## 2026-08-10 (Mounted rider alignment and fence cleanup)

- Smoke-test companion feet against the saddle on adult and baby horse variants, then compare mounted speed, jumping, step-up, and terrain handling with the owner riding alone.
- Verify an assigned companion-only mount stays led until the owner mounts an eligible vehicle; it should then ride beside the owner and use its own persisted horse movement/jump stats.
- Verify unsitting removes the companion-created oak fence with no fence drop, while an existing fence, a player-replaced block, and a changed fence state remain untouched.

## 2026-08-10 (Assigned mount relog recovery)

- Smoke-test relogging while the owner is mounted with both an assigned separate horse and an explicitly assigned shared horse; confirm the lead remains until the companion mounts, separate horses follow at their own stats, and owner dismount cleanly restores the lead.

## 2026-08-10 (Responsive follow recovery)

- Smoke-test a companion walking, sprinting, riding a horse, and using a Speed potion while Follow is enabled; confirm it begins catching up promptly, keeps moving after a path completes, and does not repeat teleporting every few ticks.
- Test blocked terrain and unloaded/chunk-edge routes; confirm the companion walks when navigation works, teleports only after failing to close distance, and immediately resumes navigation after a safe recall.
- If the 20-tick grace or 40-tick cooldown feels too short or long in live play, tune those local constants only after testing normal walking and high-speed owner movement together.

## 2026-08-10 (Configurable follow teleport timing)

- Smoke-test `teleportDelayTicks = 0`, `20`, and a larger value with Teleport Leash enabled; verify companions still attempt navigation and only recall after the configured no-progress delay.
- Smoke-test `teleportCooldownTicks = 0`, `40`, and a larger value during continuous sprinting; verify the setting controls repeat recalls without reintroducing the post-teleport standing-still loop.
- Confirm invalid or out-of-range values are rejected by the common config range and the English config screen shows both settings in ticks.

## 2026-08-10 (Mounted companion speed matching)

- Smoke-test a slow and fast owned horse/camel as separate companion mounts while the owner rides; confirm the companion mount keeps pace without changing its saved movement or jump attributes.
- Compare shared-seat riding with the owner alone to confirm vanilla horse control remains unchanged, then repeat after relog and owner dismount.

## 2026-08-10 (Mounted horse ridden-speed compensation)

- Smoke-test a separate companion horse on grass and ice beside the owner’s ridden horse; compare measured travel distance over 10 seconds and confirm it no longer uses the slow AI-walk pace.
- Verify horse/camel step-up and jump behavior, shared-seat riding, owner dismount, relog, and multiplayer after the speed compensation.

## 2026-08-11 (magical companion spellbooks)

- Open a mana-bearing companion's inventory with Iron's Spellbooks or Ars Nouveau enabled; verify the magical panel replaces the standard panel, the spellbook slot is at the supplied position, the cosmetic button remains clickable below it, and ordinary companions still show the standard panel.
- Place a native spellbook in the slot, relog, capture/redeploy, and remove it; confirm its active-spell components persist, the companion mixes native book casts with its learned kit, and the book is rejected for non-magical companions.
- Repeat with the optional magic mod absent to confirm the base mod still starts and non-magical inventory menus retain their original slot layout.

## 2026-08-11 (magical spellbook follow-up)

- In a live dev world, equip a book containing multiple active spells and confirm the companion can cast more than the first active slot, including when an earlier slot is locked or fails its pre-cast conditions.
- Verify the empty magical slot shows only the supplied panel art without a missing-texture square, and repeat the visual check at supported GUI scales.

## 2026-08-11 (companion combat and persistence follow-up)

- In a live world with Iron's Spellbooks and Ars Nouveau, verify every magical class visibly turns toward its target, holds the cast wind-up, and cancels safely when line of sight is lost; test native spellbooks and both optional-mod combinations.
- Test Battlemage with full Mana at arms reach and with zero Mana at range; confirm melee starts in both cases and resumes ranged casting only after it is safely outside melee reach.
- Test Cleric owner, ally, and self healing at low health; confirm each heal is single-target and strong, no healing-circle/regeneration aura occurs, and the Cleric still fights when no support target needs healing.
- Patrol a companion through obstructed terrain for several minutes and verify it remains bounded and resumes after returning to its patrol center. Test owner Elytra and hang-glider flight to confirm follow teleport falls back to navigation and never places the companion in mid-air.
- Put cosmetic armor in all four slots, then relog, capture/redeploy, and resurrect; verify every cosmetic stack and component survives independently of functional armor.

## 2026-08-11 (cosmetic armor slot-index migration)

- Test sparse cosmetic sets, especially leggings plus boots, through relog, soul-gem capture/redeployment, and resurrection; confirm leggings remain in legs and boots remain in feet.
- Test old 4.06 captures containing sparse cosmetic armor once, then save them again and repeat redeployment to confirm the legacy migration is rewritten in the indexed format.

## 2026-08-11 (long held spear and quarterstaff models)

- In first person, equip each material spear and quarterstaff and confirm the held model is visibly longer while the inventory icon remains unchanged.
- View the same weapons on a normal companion in third person; repeat with Epic Fight installed and with optional bronze loaded/absent.
- Verify dropped, ground, GUI, legendary spear, attack reach, damage, and weapon animation behavior did not change.

## 2026-08-12 (Lumberjack multi-tree batches)

- Smoke-test two or more mature trees within one assigned radius and confirm the Lumberjack fells each tree in sequence instead of idling after the first.
- Confirm a full inventory preempts the active tree safely, deposits only deliverable cargo, and resumes the remaining tree plan after a successful chest trip.
- Confirm an exhausted work-radius scan requests immediate delivery, then starts a fresh bounded scan after delivery; repeat with a full or unreachable chest and with Work toggled off/on.

## 2026-08-11 (Placement Wand)

- In a dev world, put several Soul Gems in the player inventory and use the Placement Wand on open ground, a wall, and a crowded target; verify every gem deploys to separate safe spots and blocked spots remain as gems.
- Sneak-use the wand in air with owned companions nearby; verify companions become Soul Gems, Beastmaster pet state survives capture/redeployment, non-owned companions are ignored, and companions outside the nearby range remain in the world.
- Fill all but four inventory slots with five nearby owned companions; verify exactly four are gemmed and the fifth remains present, including after relog and with JEI/recipe discovery.

## 2026-08-11 (conditional magical worldgen)

- Generate fresh worlds with neither optional magic mod, Iron's Spellbooks only, and Ars Nouveau only; confirm magical buildings appear only when their companion integration is available and ordinary house generation remains present.
- Generate enough terrain around each setup to confirm no `humancompanions:companions` Lithostitched warnings remain, then use `/locate structure #modern_companions:companion_houses` to confirm generated residents match the selected building.
- Smoke-test `/place structure` separately because it is an explicit operator action and is not the natural-generation gate.

## 2026-08-11 (long held glaive models)

- In first person, equip each material glaive and confirm it matches the spear/quarterstaff held length while its inventory icon remains unchanged.
- View wooden through netherite glaives on a companion; repeat with Epic Fight installed and optional bronze loaded/absent.
- Verify dropped, ground, GUI, legendary glaive, reach, damage, and attack-animation behavior remains unchanged.

## 2026-08-11 (airtight structure residents)

- With Iron's Spellbooks only and Ars Nouveau only, generate church, cleric-house, tower1, and tower2 placements and confirm each has exactly one compatible magical companion.
- Obstruct the structure center with blocks or entities, then load/reload the surrounding chunks; confirm the companion waits for a valid interior floor and is not duplicated.
- Repeat with neither magic mod installed to confirm no magical natural-generation placement occurs, then test `/place structure` separately as the explicit operator path.

## 2026-08-11 (resumable jobs and Farmer workflow)

- Assign a Farmer to a chest-linked field and verify wheat, carrots, potatoes, beetroot, melon/pumpkin stems, nether wart, torchflower, and pitcher crops harvest and replant with the matching carried item.
- Toggle Work off/on and unload/reload during travel, harvesting, planting, and bone-meal use; confirm the worker keeps its checkpoint, restores the right tool, and does not duplicate or lose crops.
- Verify `mobGriefing` off, a protected farm, a full inventory, missing seeds, missing bone meal, a full delivery chest, and compatible modded fishing rods all produce a bounded status/backoff instead of repeated world edits.
- Confirm Fisher loot changes with the active bobber biome/position, the line renders for a custom rod exposing `FISHING_ROD_CAST`, and orphan hooks disappear after job changes and relog.

## 2026-08-12 (airtight structure resident follow-up)

- Upgrade a world containing pre-4.14 structure-spawn SavedData; load each magical building with all intersecting chunks, confirm an existing resident is not duplicated, and confirm an empty legacy record receives a resident.
- Generate a structure across a chunk boundary, obstruct its center, unload/reload one edge chunk, and confirm the retry uses another safe interior position without a duplicate.
- Exercise a cancelled entity-join event and deliberate pregeneration while watching server TPS; confirm the request remains pending and the one-resident-per-tick budget is preserved.
