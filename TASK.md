# Living Tribe Jobs Reliability Revamp

## Outcome

Turn Lumberjack, Miner, Fisher, Hunter, and Chef into reliable, resumable
professions that feel like living members of a settlement rather than isolated
AI demonstrations.

Each companion must visibly find work, travel to it, perform it, collect the
result, deliver outputs to its assigned chest, and resume where it left off.
Combat, unloading, a temporarily blocked path, a full inventory, or another
worker claiming the same target must pause or replan work without silently
discarding it.

This is a focused repair and strengthening of the existing Jobs system. Reuse
the current job enum, goals, inventory, patrol center/radius, Assignment Wand,
custom fishing hook, synced statistics, and server-side block-action gate. Do
not introduce a generic behavior-tree framework, capability-based job system,
or MineColonies dependency.

## Current-source audit: root problems to eliminate

### Shared lifecycle and safety

- Each goal currently owns its own partial search/travel/retry loop. Goal
  preemption calls `stop()`, which often clears the active plan and cannot
  distinguish completion from combat, delivery, unload, or temporary failure.
- `WorkerSite.isValid` conflates destination planning with action validation.
  It asks for line of sight from the worker's current position while evaluating
  a future stand, and it does not centrally require the worker to actually be
  at that stand before editing the world.
- Boolean block actions do not describe why work failed. Some callers remove a
  queued target even when the block was not broken.
- There is no shared target reservation. Multiple workers can select the same
  tree, ore, animal, fishing shore, furnace, or chest stand and invalidate each
  other's plans.
- Large searches are repeated instead of cached or incrementally budgeted.
  Lumberjack can scan hundreds of thousands of blocks; Chef performs path/LOS
  work for ordinary non-heat blocks; Miner can survey millions of positions at
  large configured radii.
- Existing tests cover only two pure worker-safety predicates. They do not
  exercise job state transitions, target retention, navigation failure,
  interruption, collection, delivery, or persistence.

### Delivery and inventory

- Automatic delivery waits for every inventory slot to be occupied or a full
  Minecraft day. Jobs can fail to insert their current output and drop it into
  the world long before that condition is useful.
- Chest stand selection requires initial line of sight from the remote worker,
  so an otherwise reachable chest behind a wall, doorway, hill, or corner can
  be rejected before travel starts.
- Delivery does not preserve and resume an explicit work checkpoint. Only the
  Lumberjack receives an ad hoc rescan callback after success.
- All cooked food is retained as personal food, so Chef output is not delivered.
- Hunters rely on the optional generic three-block Pickup toggle. Ranged kills
  and moving prey can leave job-owned drops outside pickup range.
- Container insertion uses only `Container`; compatible modded item-handler
  containers and their insertion rules are not used.
- Full, missing, protected, unloaded, and temporarily unreachable chests need
  bounded retry/backoff and a stable player-visible reason, not repeated goal
  churn or permanent loss of the work plan.

### Lumberjack

- A “natural tree” is currently any log with nearby leaves. Player structures,
  adjacent trees, or decorative logs can be merged through diagonal log
  adjacency, while giant trees are silently truncated at 96 logs.
- The vertical scan covers only two blocks below and six above the patrol
  center, so slopes and elevation changes are poorly covered.
- Stall recovery can skip an unbroken log. Goal interruption can attempt to
  replant before the tree is actually finished.
- One arbitrary sapling is planted at one stump. Species, multi-trunk/2x2 tree
  footprints, and whether the sapling came from the felled tree are not tracked.
- The extended felling distance is only a distance exemption; it is not tied to
  a reserved, validated tree component. Foliage/branch line of sight can still
  stall the plan.

### Miner

- The route model confuses a walkable feet cell with a block to break. It queues
  the destination floor itself, potentially removing the support the Miner was
  meant to stand on.
- A synthetic straight staircase is planned toward known ore, but every queued
  buried block later requires a currently reachable vanilla stand and line of
  sight. This prevents the first controlled excavation through solid stone.
- Existing caves are not preferred as cheap walkable routes before excavation.
  Caverns and ravines have no explicit ledge, fall, bridge, or alternate-route
  logic.
- Hazard checks are local and incomplete: no stable-floor model, falling-block
  ceiling check, adjacent/above fluid exposure check, world-border/chunk check,
  or complete prevalidated return route.
- A dig queue entry is removed after `mine()` even when the protected, blocked,
  unloaded, or invalid block was not broken.
- Global rescans, movement-stall handling, and repeated abandonment can replace
  an active plan mid-step. Unreachable ore memory has no useful expiry model.
- Dead/duplicated planner paths and the repeated `abandonCurrentOre()` call make
  state transitions harder to reason about and test.

### Fisher

- Shore discovery scans around the worker and is restricted by a small patrol
  radius; it does not efficiently sample river/ocean surface shorelines across
  a larger work area.
- Tool UI accepts `FishingRodItem`, while the goal requires exactly the vanilla
  fishing rod.
- Facing is asynchronous but cast selection uses the current look vector. The
  hook is teleported to water with zero velocity/no gravity, so there is no real
  cast arc or convincing reel-in.
- Bite state is a one-tick random boolean with no durable bite window or splash
  sequence. `fishCooldown` is assigned but does not control the hook.
- Successful loot is inserted directly into inventory; no fish/item travels
  visually from the bobber to the worker.
- Rejected-water entries are not pruned, and any interruption clears the useful
  shoreline plan instead of suspending it.

### Hunter

- `HuntGoal` and `HunterJobGoal` independently select targets. The legacy target
  goal is enabled by the Hunter job but does not enforce the job planner's patrol
  boundary/reservation contract.
- Hunt targets are hard-coded to six classes rather than all eligible animals or
  data-driven modded animals.
- The Hunter job only assigns a target and assumes every companion subclass has
  a compatible attack goal for the held sword, axe, bow, or crossbow.
- There is no explicit pursue/attack/confirm-kill/collect-loot sequence. Target
  movement beyond the work area, ranged-kill drops, pickup disabled, inventory
  pressure, and kill interruption are not handled as job states.

### Chef

- Cooking is hard-coded to seven vanilla raw foods instead of using Minecraft's
  cooking recipes and data tags. Modded meats and future recipes are invisible.
- The Chef searches for a stand before checking whether a scanned block is a
  heat source, causing unnecessary path/LOS work.
- The goal can travel to a workstation with no cookable input and idle there.
- Normal campfires convert food directly while soul campfires are recognized but
  do nothing. Furnace/smoker behavior can remove unrelated cooked output and
  does not represent ownership of inserted work.
- There is no shared-chest supply loop. Hunters may deposit raw meat, but Chefs
  cannot withdraw that meat, cook it, and deposit the result.

## Required shared job contract

### 1. Resumable job lifecycle

Keep one small shared lifecycle used by every concrete job goal:

`SEARCHING -> TRAVELLING -> WORKING -> COLLECTING -> DELIVERING -> RETURNING`

`PAUSED` and `WAITING` are explicit side states, not completion.

- Centralize common active-job gates, bounded movement progress, target
  reservation, retry/backoff, delivery requests, and owner-visible status in a
  narrow shared base/coordinator. Keep job-specific discovery and actions in the
  existing concrete goal classes.
- Distinguish `COMPLETED`, `SUSPENDED`, `RETRYABLE`, and `ABANDONED` exits.
  Combat, eating, delivery, following an emergency owner recall, or unload must
  suspend a valid plan. Only successful completion or proven invalidation may
  discard it.
- Persist only durable checkpoints: job/work center, phase, current target or
  workstation, current tree/ore/death/shore identity, delivery chest, and return
  position. Rebuild volatile native paths and scan cursors after load.
- A companion must defend itself, then return to the suspended job when combat
  ends and it remains inside the work contract.
- When a companion has any job other than `NONE`, replace the existing `Guard`
  button's label and action with `Work`. `Work` is the single explicit gate for
  profession execution: jobs may search, travel, act, collect, deliver, or
  return only while it is toggled on and rendered green. Turning it off enters
  `PAUSED`, stops starting new job actions, and preserves the resumable
  checkpoint; it must not masquerade as completion or silently clear the plan.
  Patrol/Guard state must not independently start job work.
- Persist and synchronize the Work toggle so its green pressed state is derived
  from server-authoritative companion state, not client click focus. When the
  companion has no assigned job, the same control returns to its existing
  `Guard` label, appearance, and guarding behavior.
- Switching jobs or selecting `NONE` intentionally releases active reservations
  and clears incompatible active checkpoints. A Lumberjack's durable replant
  backlog is retained as completed-work debt so it can be resumed if that
  companion is assigned Lumberjack again, unless the site is proven permanently
  invalid or the owner explicitly clears that memory.

### 2. Separate planning from action validation

Refactor the shared worker-site API into explicit contracts:

- Destination discovery checks safe floor, two-block body clearance, hazards,
  work bounds, loaded chunks, and whether a native path can reach the future
  stand. It must not require current line of sight to the work target.
- Action validation requires the companion to be physically within tolerance of
  the approved stand, the target to be loaded/in bounds, current line of sight
  where appropriate, correct held tool/action, and valid interaction distance.
- Excavation planning may approve a blocked future feet cell only through the
  Miner route planner; ordinary jobs may not treat unreachable terrain as a
  world-edit instruction.
- Central block actions return a reasoned result such as `SUCCESS`,
  `RETRYABLE_BLOCKED`, `INVALID_TARGET`, `PROTECTED`, `INVENTORY_FULL`, or
  `TOOL_MISSING`. A queue advances only on `SUCCESS` or confirmed external
  completion.
- Breaking and placement remain server-authoritative and must respect loaded
  chunks, world border, unbreakable blocks, `mobGriefing`, correct drops and
  durability, NeoForge entity-destroy/protection hooks, and compatible claim
  protection behavior. No direct `setBlock(AIR)` recovery path.

### 3. Reservations, bounded scans, and status

- Add one lightweight per-server reservation registry for block positions,
  entity UUIDs, and workstations. Reservations expire, are released on job
  change/death/removal, and are never durable world data.
- Budget large scans across ticks and cache positive/negative results until the
  nearby world changes or a short expiry elapses. Do not rescan a whole work
  volume every goal evaluation.
- Track progress by decreasing distance, changed navigation nodes, successful
  actions, or collected outputs—not merely “navigation exists.” Retry the same
  plan a bounded number of times, then replan with a temporary target backoff.
- Sync a compact job status and waiting reason to the Jobs screen: searching,
  travelling, chopping/mining/fishing/hunting/cooking, collecting, delivering,
  returning, no tool, no work, inventory full, chest full/missing/unreachable,
  target protected, or route unsafe.

## Job-specific deliverables

### Lumberjack: bounded deforestation and correct replanting

- Incrementally discover mature tree candidates throughout the full horizontal
  work radius and terrain-height range, using loaded chunks only.
- Validate a tree component before reservation: lowest logs stand on natural
  growable ground, the component has a plausible trunk/canopy relationship, and
  connected logs/leaves stay within a bounded tree envelope. Avoid merging
  neighboring trees or touching structures through diagonal-only adjacency.
- Record the complete connected log set, canopy set, lowest-log footprint, log
  family, and candidate sapling drops before work starts. Support single-stump
  and 2x2 multi-trunk trees. Do not silently truncate a tree; reject an
  over-limit component with a visible reason.
- Reserve the whole tree, choose a reachable stump-side stand, clear only leaves
  that block the approved approach/action, face the active log, and break logs
  bottom-up. A tree-authorized felling reach may cover its reserved connected
  component, but must never become a generic remote block-break exemption.
- Retain every unbroken log until success. Combat, delivery, unload, foliage, or
  temporary LOS failure may not mark the tree complete.
- Enter a collection/replant phase after every reserved log is gone. Collect
  canopy drops, prefer the sapling produced by that tree's leaves, and plant at
  the recorded stump footprint only after the site is clear and valid. Plant a
  correct 2x2 pattern for 2x2 trees. If no compatible sapling is available,
  wait/back off and report `needs sapling`; do not plant a random species.
- Before leaving a completely felled tree, add its exact stump footprint, work
  area/dimension, required sapling family, and 1x1 or 2x2 layout to a deduplicated
  durable replant backlog. This memory survives combat, delivery, Work being
  toggled off, job interruption, and entity/chunk unload. When compatible
  saplings become available, the Lumberjack revisits reachable remembered sites
  and replants them even if it had to continue felling other trees first. Remove
  an entry only after every required sapling is successfully planted or the
  location is conclusively and permanently invalid; an unloaded, protected,
  occupied, or temporarily unreachable site remains pending with a visible
  waiting reason and bounded retry/backoff.
- Continue tree-by-tree until no eligible mature tree remains in the work area,
  then service remembered replant sites before reporting the area complete and
  waiting for growth/world changes.

Acceptance examples: ordinary oak/birch/spruce, branching acacia, 2x2 dark oak,
two adjacent canopies, a tree against a hill, approach-blocking leaves, a
protected trunk, combat interruption, unload/reload, inventory-triggered
delivery, and successful same-footprint replanting.

### Miner: safe cave traversal plus controlled excavation

- Replace the straight synthetic staircase with one bounded, testable 3D route
  planner over feet cells. A route step is `WALK`, `BREAK`, or `PLACE`; the feet
  cell, two-block clearance, and supporting floor are distinct.
- Prefer existing walkable cave/cavern routes using native navigation before
  paying to excavate solid stone. Use controlled excavation only when no safe
  walk route reaches a valid ore-side stand.
- Score routes to favor existing air, stable floors, short tunnels, gentle
  stairs, and low hardness. Reject fluids, lava/fire/magma exposure, falling
  blocks above an opened cell, unbreakable/protected blocks, unloaded chunks,
  world-border exits, unsupported drops, and steps outside the assigned volume.
- Never mine the support floor under the planned feet position. Never dig
  straight down or open a fluid/falling-block cell into the worker.
- Caverns and ravines must be handled deliberately: route around or descend on
  existing safe terrain first; when supplied with approved filler blocks, allow
  only a bounded, prevalidated bridge/stair placement whose complete outward and
  return route is safe. Without supplies, reject that crossing and choose
  another ore.
- Require a complete return route to the patrol center or assigned chest before
  the first irreversible excavation/placement. Revalidate after world changes.
- Execute one operation at a time, physically move into the opened route, and
  advance only after confirmed success. On failure, preserve the ore target and
  replan from the actual current position; back off only when the route is
  proven unsafe/protected.
- Survey ores incrementally in the bounded work volume, reserve the selected ore
  vein, and mine the connected vein once safely reached. Avoid duplicate plans
  between Miners. Persist the active ore/checkpoint but rebuild route nodes after
  load.
- Optionally place supplied torches in newly excavated dark tunnels using a
  small fixed spacing; do not create a lighting framework or require torches to
  traverse existing safe caves.
- Collect all drops, request delivery before output capacity is exhausted,
  deliver, then return to the saved route checkpoint and continue.

Acceptance examples: exposed cave ore, ore around a cave corner, ore across a
small supplied-block ravine crossing, an unsafe wide ravine that is rejected,
solid-stone staircase, gravel ceiling, water pocket, lava-adjacent ore,
protected block, competing Miner, combat interruption, unload/reload, full
inventory delivery, and a verified return to the patrol center.

### Fisher: reliable shoreline discovery and visible fishing

- Discover water by incrementally sampling loaded surface/heightmap positions
  throughout the work radius, then validate contiguous river/ocean-quality
  surface water and a dry reachable shoreline stand. Avoid full-volume block
  scans and one-block puddles.
- Cache/reject shoreline candidates with expiry and reserve the selected stand
  and cast sector so multiple Fishers spread out.
- Accept compatible `FishingRodItem` implementations rather than only the
  vanilla item, while retaining explicit exclusions if a modded rod cannot use
  vanilla durability semantics.
- Turn toward a preselected water target before casting. Launch the custom hook
  with a visible server-authoritative arc, validate that it lands in the
  reserved water, and show the line from rod to bobber.
- Give the hook explicit waiting, bite-window, hooked, and reeled states. Use
  splash/sound/bob motion and a bite window long enough for the goal to respond;
  remove the unused competing cooldown behavior.
- On reel, visibly move the caught item from the bobber toward the companion,
  then guarantee insertion/collection or trigger delivery if capacity is
  insufficient. Use the vanilla fishing loot table, rod components, and Luck.
- Suspend and restore the shoreline/hook plan correctly around combat or
  delivery. Clean up orphaned hooks on job change, death, dimension change, or
  removal.

Acceptance examples: riverbank, ocean beach, irregular shore, elevation above
water, blocked/unsafe shore, distant water near the edge of the work radius,
two Fishers, combat interruption, unload/reload cleanup, visible cast/bite/reel,
rod durability, vanilla loot, and full-inventory delivery.

### Hunter: complete target, kill, loot, and return loop

- Remove the competing Hunter target-selection paths. One Hunter job planner is
  authoritative; defensive/owner-protection targets may preempt it and then
  return control.
- Treat every adult, alive, non-allied `Animal` inside the work radius as
  eligible by default, excluding owned/tamed animals and entity types in a
  data-driven protected/deny tag. Provide allow/deny entity-type tags so packs
  can add modded animals or protect species without code changes.
- Reserve one animal, pursue it while it remains inside the work boundary, and
  choose only an attack mode the companion can actually execute with its held
  weapon. Supply a reliable Hunter melee path for sword/axe users and reuse the
  companion's compatible bow/crossbow path for ranged users.
- Maintain `ACQUIRE -> PURSUE -> ATTACK -> CONFIRM_KILL -> COLLECT_LOOT ->
  DELIVER/RETURN`. Moving prey, temporary LOS loss, another attacker, or combat
  defense must not cause target thrashing.
- Attribute direct and projectile kills to the Hunter. Mark the resulting
  vanilla drops as short-lived job-owned loot, walk to the death/drop position,
  and collect every remaining drop even when the generic Pickup toggle is off.
  Do not steal another worker's claimed drops.
- If inventory capacity is insufficient, preserve the claimed drops briefly,
  deliver existing outputs, return, and collect before expiry where possible.
- Continue until every eligible animal in the assigned area has been hunted,
  then wait for new eligible animals. Existing tame/allied/player/villager
  safety rules remain authoritative.

Acceptance examples: every vanilla `Animal` subtype represented by the default
predicate, babies excluded, owned/tamed animals excluded, modded allow/deny tag,
melee and ranged Hunters, fleeing prey, prey leaving bounds, competing Hunters,
defensive combat interruption, all vanilla drops collected, Pickup toggle off,
full inventory delivery, and return to the work center.

### Chef: data-driven cooking and hunter-to-chef supply chain

- Replace the hard-coded raw/cooked map with Minecraft's recipe manager plus a
  data-driven raw-meat item tag. Ship vanilla meat coverage and accept modded
  tagged meats with valid smelting/smoking/campfire recipes.
- Search only when the Chef has cookable input or can obtain it from the
  assigned chest. Filter for heat-source blocks before performing stand/path
  checks.
- Treat the assigned chest as a shared settlement stock point for Chef only:
  when personal raw-meat input is empty, withdraw only cookable raw meat; after
  cooking, deposit finished meat and unrelated outputs. Other jobs remain
  deposit-only.
- Use actual furnace/smoker/campfire block-entity and recipe behavior. Insert
  only compatible raw meat, require existing fuel or explicitly supplied fuel,
  wait for native cooking completion, and collect only output attributable to
  the Chef's inserted batch. Do not steal unrelated player output.
- Support normal and soul campfires through the native campfire recipe/slot
  path, with visible placed food and normal cook timing. Remove direct instant
  conversion.
- Reserve the workstation while a batch is active, release it between batches,
  and persist enough batch identity to recover safely after unload without
  duplicating or stealing items.
- Keep only a small personal food ration required by companion survival; cooked
  job output must be deliverable. Continue withdrawing/cooking/depositing until
  no tagged raw meat remains in inventory or the shared chest.

Acceptance examples: every vanilla raw meat, furnace, smoker, normal campfire,
soul campfire, no fuel, incompatible/busy output, player-owned unrelated batch,
modded tagged meat recipe, Hunter depositing raw meat to a shared chest, Chef
withdrawing/cooking/depositing it, combat interruption, unload/reload, and full
output chest.

## Shared assigned-chest delivery contract

- Every non-`NONE` job can use one Assignment-Wand-linked chest/container.
- Select a reachable chest-side destination without requiring remote line of
  sight; require proximity and line of sight only when interacting.
- Prefer NeoForge item-handler insertion/extraction when available, with vanilla
  `Container` fallback. Respect container validity, sided insertion/extraction,
  protected claims, loaded dimensions/chunks, stack components, and double
  chests.
- Trigger delivery when the next output cannot fit, when a completed work unit
  reaches a bounded batch threshold, on a short idle timer with cargo, or on
  manual Deposit—not only when every slot is occupied or after one game day.
- Reserve active tools, armor, potions, a small personal food ration, required
  Lumberjack saplings, and approved Miner bridge/torch supplies. Deposit job
  outputs, including cooked food. Make retention job-aware and test it.
- Save the pre-delivery phase/target/return point, deliver, then return and resume
  without rescanning valid completed work. A chest failure pauses delivery and
  reports a reason; it does not erase cargo, assignment, or the job plan.
- Clear forced-delivery state on terminal success/cancel and back off full or
  unreachable chests. Do not spam the owner or pathfind every tick.

## Living presentation

- During work, continuously face the relevant log, ore block, bobber, animal,
  furnace, campfire, chest, or collected drop.
- Reuse existing swing, held-tool, fishing-line, sound, particle, eating,
  combat, and navigation systems. Add only the narrow missing cast/reel/cook
  presentation required by the job.
- Use short deterministic work pauses and status changes for readability; do
  not add random delays that reduce reliability.
- Expose current phase, target summary, completed/session counts, and waiting
  reason on the Jobs screen. The player should be able to tell whether a worker
  is searching, travelling, working, interrupted, full, missing supplies, or
  genuinely finished.
- Also render the server-synchronized current job and compact current
  task/state inside the bottom-left `Currently` panel already baked into
  `newinventory.png`. Draw only dynamic text beneath the existing `Currently`
  label--do not duplicate the label or modify the texture. Keep all text clipped
  to that panel and prefer a short job line plus action/waiting line, for example
  `Lumberjack` / `Chopping oak`, `Delivering`, `Paused`, or `Needs sapling`.
  Update it promptly when Work is toggled, a phase changes, or a waiting reason
  changes, and show a clear idle state when no job is assigned.

## Implementation order

1. Inventory the current goal scheduling, state persistence, item pickup,
   block/protection hooks, container capability, recipe-manager, kill/drop, and
   fishing-render seams. Delete dead duplicate job logic only after callers are
   accounted for.
2. Implement the shared resumable lifecycle, server-authoritative Work toggle,
   reasoned action results, split planning/action site checks, reservations,
   bounded retry/backoff, status sync, and delivery pause/return contract.
3. Repair assigned-chest capability handling and job-aware retention/delivery;
   prove every job can deliver and resume before expanding profession behavior.
4. Rebuild Lumberjack around complete reserved tree plans and post-felling
   collection/replanting.
5. Rebuild Miner around the bounded feet-cell route planner and execute
   walk/break/place operations with a prevalidated return route.
6. Consolidate Hunter targeting/combat ownership and add guaranteed job-loot
   collection.
7. Rebuild Fisher shoreline selection and visible hook state/cast/reel flow.
8. Replace Chef's hard-coded conversion with native recipes/workstations and the
   shared-chest hunter-to-chef pipeline.
9. Add status UI, statistics where missing, player-facing documentation, config
   descriptions, `TRACELOG.md`, `SUGGESTIONS.md`, and the required version bump.
10. Run the complete automated and manual validation matrix before shipping.

## Automated checks

Keep planners/rules pure where possible and leave the smallest deterministic
checks that fail on the known regressions:

- Shared lifecycle: suspend/resume, terminal completion, retry/backoff,
  reservation expiry, no queue advancement on failed action, delivery/return,
  job switch cleanup, reload checkpoint restoration, Work-off inactivity,
  Work-on checkpoint resumption, and restoration of Guard behavior for `NONE`.
- Work sites/actions: future destination without remote LOS, action requires
  actual stand proximity, hazards/headroom/range, protection denial, inventory
  full result, correct drops/durability, and no direct world-edit bypass.
- Tree planner: adjacent trees remain separate, full connected log retention,
  bottom-up ordering, 2x2 footprint, interruption without early replant, and
  compatible-sapling placement. Persist and deduplicate a missing-sapling
  replant entry through unload and Work-off pause, then remove it only after a
  later species-correct 1x1 or 2x2 planting succeeds.
- Miner planner on a small synthetic grid: existing cave preferred, solid
  tunnel, stable floor retained, staircase, bounded supplied-block crossing,
  unsafe ravine rejected, falling block/fluid/lava/protected cell rejected, and
  complete return route.
- Fishing rules: surface-water/shore validation, reservation, compatible rod,
  durable bite window, orphan-hook cleanup, and loot insertion/delivery result.
- Hunter rules: full eligible-animal predicate, exclusions/tags, single target
  authority, direct/projectile kill attribution, claimed-drop collection, and
  competing Hunter behavior.
- Cooking/delivery: recipe/tag lookup, workstation batch ownership, no unrelated
  output theft, normal/soul campfire flow, Chef raw withdrawal/cooked deposit,
  job-aware retention, capability container insertion, and full/missing chest
  backoff.

Use NeoForge GameTests for world/navigation/block-entity flows if supported by
the existing ModDevGradle setup; otherwise add a narrow server-side test harness
for those cases. Do not claim pure planner tests validate live mob navigation or
rendering.

## Manual dev-world acceptance

- Run every per-job acceptance example above with one worker, then with two
  workers sharing an area and chest.
- Interrupt each job with hostile combat, manual Deposit, owner recall,
  unload/reload, full inventory, broken tool, target removal, and blocked/full
  chest; confirm the worker reports the reason and safely resumes or replans.
- Verify visible facing, held tools, swings, block progress cadence, fishing
  cast/line/bite/reel, animal pursuit/kill/loot, workstation food rendering, and
  chest deposit/return.
- For every job, verify Work OFF prevents profession execution without losing
  the checkpoint, Work ON is visibly green and resumes it, and removing the job
  restores the original Guard control and behavior.
- Verify the inventory's bottom-left `Currently` panel shows the correct job and
  live action/waiting state for every lifecycle phase, remains inside the panel
  at all supported GUI scales, and does not redraw the baked-in label.
- Test `mobGriefing` disabled and at least one protection/claim environment.
- Test vanilla inventory plus the existing Sophisticated Backpacks path.
- Separate automated evidence from remaining visual/runtime smoke gaps in the
  handoff.

## Boundaries and non-goals

- Edit only allowed project directories. Treat supplied Minecraft, NeoForge,
  OriginalCompanions, ModDevGradle, and other upstream/reference sources as
  read-only.
- MineColonies may be consulted only as design research under its license; do
  not copy its source or import its worker framework.
- No arbitrary terrain destruction, teleport-to-target recovery, direct
  `setBlock(AIR)`, infinite scan, permanent chunk forcing by default, generic
  behavior tree, or speculative settlement economy.
- Do not make all jobs share identical discovery/action code. Share lifecycle,
  safety, reservation, collection, and delivery contracts; keep each
  profession's planner small and explicit.
- Preserve existing combat safety, equipment/inventory, stamina/mana, potion,
  patrol, optional-mod, and multiplayer behavior.
- Build with Java 21 before committing. Update the version, `README.md`,
  `TRACELOG.md`, and `SUGGESTIONS.md` with shipped facts and clearly list manual
  validation still required.
