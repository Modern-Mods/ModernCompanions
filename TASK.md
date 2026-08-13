# Living Tribe Jobs Reliability Revamp

## Deliverable

Implement a robust, resumable companion-job system for Farmer, Lumberjack,
Miner, Fisher, Hunter, and Chef. The result must make companions behave like
reliable members of a settlement rather than isolated AI demonstrations.

Every assigned companion must be able to:

1. Find valid work inside its assigned work contract.
2. Reserve a work unit so another companion cannot invalidate the plan.
3. Travel to the work safely.
4. Perform the profession-specific action through the existing server-side
   safety/action gate.
5. Collect the resulting items or confirm the world-state change.
6. Deliver job output to the assigned chest/container.
7. Return to the saved checkpoint and continue without silently losing work.

Combat, Work being toggled off, owner recall, unloading, a blocked route, a
full inventory, a missing/full/protected chest, a protected target, or another
worker claiming a target must suspend or replan the work without discarding a
valid plan. Only confirmed completion or proven permanent invalidation may
discard a work unit.

This is a focused strengthening of the existing Jobs system. Preserve the
current job enum, concrete goals, inventory/equipment behavior, patrol
center/radius, Assignment Wand, custom fishing hook, synced statistics,
server-authoritative Work toggle, and server-side block-action gate.

Do not introduce a generic behavior-tree framework, a capability-based job
system, a MineColonies dependency, arbitrary terrain destruction, teleport-
to-target recovery, infinite scans, or permanent chunk forcing by default.

## Reference-derived design requirements

The following local source trees were audited before this task was written:

- `workers-main/` — Village Workers.
- `MineColonies/` — MineColonies.
- `SmartBrainLib-1.21.11/` — SmartBrainLib.
- The current ModernCompanions job implementation under `src/`.

The goal is to adopt proven design principles while keeping ModernCompanions'
existing architecture, safety rules, UI, equipment rules, and multiplayer
behavior.

### Village Workers lessons

Village Workers is all-rights-reserved/ARR source. Use it only to discover and
infer behavior; do not copy its source, assets, comments, or implementation.

Apply these concepts independently:

- Treat the assigned patrol center, radius, chest, and job settings as an
  explicit work contract, analogous to a persistent work-area entity.
- Separate profession work into small confirmed operations: planting,
  harvesting, plowing, tree discovery, leaf clearing, stripping, breaking,
  replanting, mining, and fishing.
- Keep supply acquisition and output deposit as explicit job phases rather
  than incidental inventory side effects.
- Use a dedicated storage/delivery state machine with target selection,
  travel, validation, interaction, success, and error states.
- Track profession-specific work objects and queues instead of inferring all
  work from the companion's current position.

Do not reproduce its weaker patterns: boolean ownership in place of typed
reservations, repeated broad scans, direct world edits that bypass the
ModernCompanions action contract, or direct fishing-loot insertion.

### MineColonies lessons

`MineColonies/LICENSE` is GPLv3. This repository may study its architecture,
but this task does not authorize copying MineColonies source or importing its
worker framework. Any future direct source reuse requires a separate legal and
distribution decision, preserved notices, and a GPL-compliance plan. Implement
the following ideas independently:

- Separate persistent job/building/module state from transient AI execution.
- Use one shared execution/state-machine contract for ordered transitions,
  blocking events, delays, exceptions, progress, and diagnostics.
- Treat requests, tools, supplies, dumping, inventory-full, and paused states
  as first-class job conditions.
- Persist deterministic progress and advance it only after confirmed success.
- Persist complete profession work units: field cells, fishing candidates,
  tree components, mine nodes, remaining work, and production batches.
- Track available/in-progress/completed mine work so competing workers do not
  duplicate or invalidate each other's routes.
- Rebuild native navigation paths after reload instead of pretending a path is
  durable world state.

ModernCompanions must retain its own protection hooks, drop handling,
`mobGriefing` behavior, item-handler/container integration, equipment policy,
and assigned-chest contract. MineColonies' colony, warehouse, building-level,
request, and skill systems are not to be introduced.

### SmartBrainLib lessons

`SmartBrainLib-1.21.11/LICENSE` is MPL-2.0. Do not add it as a dependency or
copy its source for this task. Use its behavior as a design reference for
narrow mechanisms only:

- Throttle sensors and expensive scans with configurable scan intervals.
- Bound behavior runtime and add cooldown/backoff rather than retrying every
  tick forever.
- Use explicit start/stop predicates and cleanup for temporary execution.
- Model ordered, repeated, delayed, and held actions explicitly.
- Track meaningful unreachable-target state rather than assuming that a path
  object means progress.
- Rebuild or expire observations/memories when their world data is stale.
- Use gradual work progress and cleanup presentation without weakening the
  server-side action/drop/protection contract.

Do not replace the current Goals with a generic behavior-tree or brain system.
The shared job coordinator below is the smaller project-specific abstraction
required here.

## Current-source audit: problems to eliminate

### Shared lifecycle and safety

- Each goal owns its own partial search/travel/retry loop. Goal `stop()` often
  clears the active plan and cannot distinguish combat, delivery, unload,
  temporary failure, and completion.
- `JobLifecycle` and `ResumableJobGoal` currently provide a status/retry bridge,
  not authoritative ownership of the active work plan.
- `WorkerSite.isValid` conflates destination planning with action validation.
  Future stands must not require line of sight from the worker's current
  position.
- World actions have inconsistent failure semantics. A queue entry can be
  removed even when the target was protected, unloaded, invalid, or not
  actually changed.
- Reservations are string-keyed TTL claims without typed target ownership and
  complete death/removal/dimension cleanup.
- Large searches and path/LOS checks are repeated instead of incrementally
  budgeted and cached.
- Existing tests do not adequately cover transitions, persistence, target
  retention, navigation failure, collection, delivery, or contention.

### Delivery and inventory

- Delivery waits too long, often until every inventory slot is occupied or a
  long idle period has elapsed.
- Chest stand selection can require remote line of sight before travel,
  rejecting reachable chests behind walls, doors, hills, or corners.
- Delivery does not consistently preserve a pre-delivery phase, target, and
  return checkpoint.
- Cooked Chef output can be retained as personal food instead of delivered
  job output.
- Hunter collection depends on the optional generic Pickup toggle and does not
  own a complete ranged-kill drop flow.
- Container insertion must support NeoForge item handlers first and `Container`
  fallback, including sided rules and compatible modded storage.
- Full, missing, protected, unloaded, and temporarily unreachable chests need
  bounded retry/backoff and a stable player-visible reason.

### Lumberjack

- A natural tree must not be inferred from a nearby log/leaves heuristic that
  merges adjacent trees, decorative structures, or diagonal-only connections.
- Search must cover the full horizontal work radius and relevant terrain
  heights without silently truncating oversized trees.
- Stall recovery must not skip an unbroken log or replant before the reserved
  tree is completely felled.
- Replanting must preserve species, sapling family, stump footprint, and 1x1
  versus 2x2 layout.
- Extended felling reach is allowed only for the reserved validated tree
  component, never as a generic remote-break exemption.

### Miner

- A walkable feet cell, its supporting floor, its two-block body clearance,
  and a block to break are different things and must never be conflated.
- A synthetic route must not require a currently reachable vanilla stand for
  every buried excavation step when the controlled route planner approved the
  excavation.
- Existing caves should be preferred before paying to excavate solid stone.
- Caverns and ravines need explicit ledge, fall, bridge, fluid, hazard,
  alternate-route, and return-route handling.
- A failed/protected/unloaded/invalid dig must retain the target and route
  operation instead of advancing the queue.
- Rescans and movement stalls must not replace an active plan mid-step.

### Fisher

- Shore discovery must sample loaded surface/heightmap positions throughout
  the work radius and reject puddles or unsafe/unreachable shores.
- The plan must accept safe compatible fishing rods rather than accidentally
  requiring only one concrete vanilla item type.
- Casting must visibly travel from rod to reserved water; a hook teleported to
  water with zero velocity is not a complete fishing interaction.
- Bite needs a durable response window and explicit splash/bob state.
- Fishing loot must use native fishing conditions and be collected through a
  guaranteed insertion/delivery path, not direct unexplained insertion.
- Rejected shore candidates must expire, and orphan hooks must be cleaned on
  job change, death, dimension change, unload, and entity removal.

### Hunter

- `HuntGoal` and `HunterJobGoal` must not independently select authoritative
  job targets.
- Target eligibility must be data-driven rather than a hard-coded six-class
  allowlist: adult, alive, non-allied animals are eligible by default, with
  owned/tamed/protected exclusions and configurable allow/deny tags.
- The job needs explicit acquire, pursue, attack, kill confirmation, owned
  drop collection, delivery, and return states.
- Melee and ranged kills must work with the companion's actual held weapon and
  must not lose drops when prey moves, leaves the work area, or dies at range.

### Chef

- Replace hard-coded food mappings with recipe-manager lookup plus a
  data-driven raw-meat tag, including valid modded recipes.
- Search only when cookable input exists or can be obtained from the assigned
  chest; filter heat sources before path/LOS work.
- Reserve the workstation and track the Chef's inserted batch/output. Never
  steal unrelated player output.
- Use native furnace, smoker, normal campfire, and soul-campfire behavior,
  including fuel, cook time, visible food, and output capacity.
- Support the Hunter-to-Chef shared-chest supply loop.

## Required shared job contract

### 1. Authoritative resumable lifecycle

Use one narrow shared lifecycle for every concrete goal:

```text
SEARCHING -> TRAVELLING -> WORKING -> COLLECTING -> DELIVERING -> RETURNING
```

`PAUSED` and `WAITING` are explicit side states, not completion. Distinguish
at least these exits:

- `COMPLETED` — the current work unit was confirmed complete.
- `SUSPENDED` — combat, Work OFF, recall, delivery, unload, or another
  temporary preemption preserved the valid plan.
- `RETRYABLE` — the target or route remains valid but needs bounded backoff or
  replanning.
- `ABANDONED` — the target was proven permanently invalid or explicitly
  cancelled by a job change/player action.

The shared coordinator must own active-job gates, phase transitions, target
reservation, navigation progress, retry/backoff, delivery requests, status,
and cleanup. Concrete goals keep profession-specific discovery and actions.

### 2. Server-authoritative Work toggle

- A non-`NONE` job changes the existing Guard control to `Work`.
- Work ON is the single explicit gate for job searching, travel, action,
  collection, delivery, and return.
- Work OFF enters `PAUSED`, stops starting new profession actions, preserves
  the durable checkpoint, and does not masquerade as completion.
- Work state and pressed/green UI state are synchronized from the server.
- Removing the job restores the original Guard label, appearance, and behavior.
- Changing jobs releases incompatible reservations and clears incompatible
  active checkpoints. Lumberjack replant debt remains durable unless the site
  is proven permanently invalid or the owner explicitly clears it.

### 3. Durable plan and checkpoint model

Add a versioned, server-owned plan payload appropriate to the existing entity
NBT boundary. It must be sufficient to rebuild execution after goal stop,
combat, Work OFF, entity/chunk unload, dimension reload, and server restart.

Persist:

- Job, Work state, work center, patrol radius, and assigned delivery chest.
- Phase, waiting reason, plan schema/version, and current work-unit identity.
- Current target block/entity/workstation/shore where applicable.
- Approved stand or route checkpoint when it is durable and meaningful.
- Pre-delivery phase/target and return position.
- Profession-specific durable payload: field cell, tree, ore route/node,
  fishing shore/cast plan, Hunter target/drop claim, Chef batch, and Lumberjack
  replant debt.
- Completed/session counters and player-visible status data.

Do not persist native navigation paths, rebuildable scan cursors, short-lived
retry counters, or a temporary hook entity as if it were the job plan. Rebuild
those safely after load. A checkpoint call must not overwrite the actual return
position merely because the current phase changed.

### 4. Separate planning from action validation

Destination discovery must check:

- Safe floor, two-block body clearance, hazards, bounds, loaded chunks, and a
  path to the future stand where possible.
- Work-area/dimension ownership and reservation availability.
- No remote line-of-sight requirement for a future stand.

Action validation must check at execution time:

- Companion is physically within tolerance of the approved stand.
- Target is loaded, in bounds, unchanged/valid, and still reserved.
- Current line of sight where the action requires it.
- Correct tool, item, workstation, interaction range, and job gate.

Excavation may approve a blocked future feet cell only through the Miner route
planner. Ordinary jobs may not treat unreachable terrain as a world-edit
instruction.

### 5. Reasoned, transactional actions

Shared block/item actions must return a reasoned result, for example:

```text
SUCCESS
RETRYABLE_BLOCKED
INVALID_TARGET
PROTECTED
INVENTORY_FULL
TOOL_MISSING
UNLOADED
UNSAFE
```

Advance a queue only after `SUCCESS` or confirmed external completion. Breaking
must preserve correct drops, durability, `mobGriefing`, loaded-world checks,
NeoForge destroy/protection hooks, claim protection, and inventory safety.
Placement must validate and consume the item through a reasoned result; no
direct `setBlock` bypass may be used as recovery.

### 6. Reservations, scans, progress, and status

Implement one lightweight per-server reservation registry for:

- Block positions and connected work components.
- Entity UUIDs and owned drops.
- Workstations, shore stands/cast sectors, and chest interaction stands.

Every reservation has typed ownership, purpose, expiry/renewal, and cleanup on
success, cancellation, job change, death, removal, dimension change, and
server-level cleanup. Reservations are transient and are not durable world
data.

Budget large scans across ticks. Cache positive and negative results until a
short expiry or relevant world change. Do not rescan an entire work volume on
every goal evaluation.

Measure progress by decreasing distance, changed navigation nodes, successful
actions, collected outputs, or confirmed world changes. A merely existing path
is not progress. Retry the same plan a bounded number of times, then apply a
temporary target backoff and replan.

Synchronize a compact job status and waiting reason to the Jobs screen and the
bottom-left `Currently` panel. Include searching, travelling, profession
action, collecting, delivering, returning, paused, no tool, no work,
inventory full, chest full/missing/unreachable, target protected, and route
unsafe.

## Profession deliverables

### Farmer: confirmed field progression

- Persist field cell and crop identity.
- Discover and process cells incrementally inside the work contract.
- Use native planting, harvesting, plowing, and item behavior.
- Advance the cell only after confirmed success.
- Preserve the cell on missing seed/tool, protected farmland, unloaded chunks,
  blocked stands, or inventory pressure.
- Collect and deliver outputs using the shared contract.

### Lumberjack: bounded deforestation and correct replanting

- Incrementally discover mature tree candidates across the complete horizontal
  work radius and relevant terrain-height range using loaded chunks only.
- Validate a tree component before reservation: plausible trunk/canopy
  relationship, natural growable ground, bounded envelope, and no accidental
  merge through neighboring or diagonal-only logs.
- Record every connected log and relevant leaf, the lowest-log footprint, log
  family, compatible sapling family, and single-stump versus 2x2 layout.
- Reject oversized/ambiguous components with a visible reason; never silently
  truncate them.
- Reserve the whole tree, select a reachable stump-side stand, clear only
  approach-blocking foliage, face the active log, and break bottom-up.
- Retain every unbroken log through combat, delivery, foliage, LOS failure,
  unload, or Work OFF.
- Enter collection/replant only after every reserved log is confirmed gone.
- Collect canopy drops and prefer saplings produced by the felled tree.
- Plant the correct species at every recorded footprint; support 2x2 patterns.
- Add exact stump footprints, dimension/work area, sapling family, and layout
  to a deduplicated durable replant backlog before leaving a fully felled tree.
- Revisit pending sites with bounded retry/backoff. Remove an entry only after
  every required sapling is planted or the location is conclusively permanent
  invalid. Unloaded, protected, occupied, and temporarily unreachable sites
  remain pending with a visible reason.
- Service replant backlog before reporting the work area complete.

Acceptance examples: oak, birch, spruce, acacia, 2x2 dark oak, adjacent
canopies, a hill, approach-blocking leaves, protected trunk, combat
interruption, unload/reload, full-inventory delivery, and same-footprint
species-correct replanting.

### Miner: safe cave traversal and controlled excavation

- Replace a straight synthetic staircase with a bounded 3D feet-cell route
  planner whose operations are explicitly `WALK`, `BREAK`, or `PLACE`.
- Keep feet cell, support floor, two-block clearance, target block, and route
  operation distinct.
- Prefer existing walkable caves/caverns before excavating solid stone.
- Score routes for existing air, stable floors, short tunnels, gentle stairs,
  and low hardness.
- Reject fluids, lava/fire/magma exposure, falling blocks above an opened cell,
  unbreakable/protected blocks, unloaded chunks, world-border exits,
  unsupported drops, and out-of-contract steps.
- Never mine the support floor beneath planned feet. Never dig straight down.
- Handle ravines and ledges deliberately. Allow only bounded, prevalidated
  supplied-block bridges/stairs, with a complete safe outward and return route.
- Require a validated return route before the first irreversible excavation or
  placement, then revalidate after world changes.
- Survey ores incrementally, reserve the selected ore vein, and persist the
  active ore/checkpoint while rebuilding route nodes after load.
- Execute one operation at a time and advance only after confirmed success.
- Preserve the ore target on failure; replan from the actual current position.
- Optionally place supplied torches at fixed spacing, without adding a general
  lighting framework or requiring torches in existing safe caves.
- Collect all drops, deliver before capacity is exhausted, and resume at the
  saved route checkpoint.

Acceptance examples: exposed cave ore, ore around a corner, supplied ravine
crossing, unsafe ravine rejection, solid-stone route, gravel ceiling, water,
lava-adjacent ore, protected block, competing Miners, combat, unload/reload,
full-inventory delivery, and return to the work center.

### Fisher: reliable shoreline discovery and visible fishing

- Incrementally sample loaded surface/heightmap positions throughout the work
  radius and validate contiguous river/ocean-quality surface water plus a dry
  reachable shore stand.
- Cache/reject candidates with expiry and reserve the selected stand and cast
  sector so multiple Fishers spread out.
- Accept compatible `FishingRodItem` implementations when their durability and
  vanilla fishing semantics are safe.
- Face the preselected water target before casting.
- Launch the custom hook with a visible server-authoritative arc, validate its
  reserved landing water, and render the line from rod to bobber.
- Implement explicit waiting, bite-window, hooked, reeled, and collection
  states, with splash/bob motion and a response window long enough to act.
- Use native fishing loot conditions, rod components, durability, and Luck.
- Visibly move caught item presentation toward the companion, then guarantee
  insertion or trigger delivery when capacity is insufficient.
- Suspend and restore shore/hook plan around combat and delivery.
- Clean orphaned hooks on job change, death, dimension change, unload, and
  entity removal.

Acceptance examples: riverbank, ocean beach, irregular shore, high shore,
blocked shore, distant water near the radius edge, two Fishers, combat,
unload/reload, visible cast/line/bite/reel, rod durability, vanilla loot, and
full-inventory delivery.

### Hunter: complete target, kill, loot, and return loop

- Remove competing authoritative target-selection paths. Defensive/owner
  protection targets may preempt the job and then return control.
- Treat adult, alive, non-allied animals inside the work radius as eligible by
  default; exclude owned/tamed/protected types through data-driven tags.
- Reserve one target and keep it inside the work boundary while pursuing.
- Select only an attack mode the companion can execute with its held weapon.
- Provide reliable melee sword/axe behavior and reuse compatible bow/crossbow
  behavior for ranged Hunters.
- Implement:

  ```text
  ACQUIRE -> PURSUE -> ATTACK -> CONFIRM_KILL -> COLLECT_OWNED_DROPS
  -> DELIVER/RETURN
  ```

- Attribute direct and projectile kills to the Hunter. Mark resulting vanilla
  drops as short-lived job-owned loot and collect all remaining owned drops,
  even when generic Pickup is disabled.
- Preserve claimed drops through delivery and temporary inventory pressure
  where possible; never steal another worker's claimed drops.
- Wait for new eligible animals after the assigned area is exhausted.
- Preserve existing tame/allied/player/villager safety rules.

Acceptance examples: every vanilla Animal subtype covered by the predicate,
babies excluded, owned/tamed excluded, modded allow/deny tags, melee/ranged
Hunters, fleeing prey, prey leaving bounds, competing Hunters, defensive
interruption, all drops collected, Pickup OFF, full delivery, and return.

### Chef: native recipes and Hunter-to-Chef supply chain

- Use Minecraft's recipe manager plus a data-driven raw-meat tag. Cover vanilla
  meats and valid modded tagged meats without hard-coded item maps.
- Search only when cookable input exists locally or can be withdrawn from the
  assigned chest. Filter heat sources before stand/path/LOS checks.
- Withdraw only cookable raw meat from the assigned shared chest when personal
  input is empty; remain deposit-only for other jobs.
- Reserve the workstation during an active batch.
- Insert only compatible input, require existing or explicitly supplied fuel,
  wait for native cooking completion, and collect only output attributable to
  the Chef's batch.
- Support furnace, smoker, normal campfire, and soul campfire using native
  block-entity/recipe behavior and visible placed food. Do not perform instant
  direct conversion.
- Keep only a small personal survival ration; cooked job output is deliverable.
- Persist enough batch identity to recover after unload without duplicating or
  stealing items.
- Continue until no tagged raw meat remains in inventory or assigned stock.

Acceptance examples: every vanilla raw meat, furnace, smoker, normal/soul
campfire, no fuel, incompatible/busy output, unrelated player batch, modded
tagged meat, Hunter deposit, Chef withdrawal/cook/deposit, combat,
unload/reload, and full output chest.

## Shared assigned-chest delivery contract

- Every non-`NONE` job can use the Assignment Wand-linked chest/container.
- Select a reachable chest-side destination without requiring remote LOS.
  Require proximity and LOS only while interacting.
- Prefer NeoForge item-handler insertion/extraction, with vanilla `Container`
  fallback. Respect sided insertion/extraction, container validity, stack
  components, double chests, protected claims, loaded dimensions/chunks, and
  existing Sophisticated Backpacks support.
- Trigger delivery when the next output cannot fit, a bounded work batch is
  complete, cargo has been idle for a short interval, capacity pressure is
  approaching, or the player requests Deposit. Do not wait for every slot.
- Retain tools, armor, potions, a small personal-food ration, required
  Lumberjack saplings, and approved Miner bridge/torch supplies. Deposit job
  outputs, including cooked food.
- Save pre-delivery phase, target, plan identity, and return point. Deliver,
  then return and resume without rescanning valid completed work.
- A full/missing/protected/unreachable chest pauses delivery with a reason; it
  never deletes cargo, assignment, reservation, or work plan.
- Clear forced-delivery state only after terminal success or cancellation, and
  back off without pathfinding or attempting interaction every tick.

## Living presentation and UI

- Continuously face the relevant log, ore, bobber, animal, furnace, campfire,
  chest, or collected drop during work.
- Reuse existing swings, held tools, fishing line, sounds, particles, eating,
  combat, and navigation systems.
- Add only the narrow missing cast/reel/cook presentation required by the
  specific job. Avoid random delays that reduce reliability.
- Show current phase, target summary, completed/session counts, and waiting
  reason on the Jobs screen.
- In the bottom-left `Currently` panel already baked into `newinventory.png`,
  draw only dynamic text beneath the existing `Currently` label. Do not
  duplicate the label or modify the texture. Keep text clipped to the panel,
  for example `Lumberjack` / `Chopping oak`, `Delivering`, `Paused`, or
  `Needs sapling`.
- Update promptly when Work toggles, phases change, or waiting reasons change.
  Show a clear idle state for `NONE`.

## Implementation order

1. Inventory the current goal scheduling, state persistence, equipment/tool
   policy, item pickup, block/protection hooks, container capabilities,
   recipe-manager seams, kill/drop attribution, and fishing-render seams.
2. Implement the shared durable plan/coordinator, server-authoritative Work
   toggle, lifecycle exits, typed reservations, progress/stuck detection,
   bounded retry/backoff, status synchronization, and cleanup.
3. Split destination planning from action validation and make block/item
   actions transactional with reasoned results.
4. Repair assigned-chest capability handling, job-aware retention, delivery
   triggers, and delivery pause/return behavior. Prove every job can deliver
   and resume before expanding profession behavior.
5. Repair Farmer's confirmed field-cell progression.
6. Rebuild Lumberjack around complete reserved tree plans and durable replant
   debt.
7. Rebuild Miner around the bounded feet-cell route planner and safe
   walk/break/place execution.
8. Consolidate Hunter target ownership, combat confirmation, and owned-drop
   collection.
9. Rebuild Fisher shoreline selection, hook states, visible cast/reel, and
   orphan cleanup.
10. Replace Chef's remaining hard-coded conversion with native recipes,
    workstation ownership, batch attribution, and the shared-chest supply
    chain.
11. Add status UI, statistics where missing, player documentation, config
    descriptions, `TRACELOG.md`, and `SUGGESTIONS.md` with shipped facts.
12. Run the complete automated and manual acceptance matrix before claiming
    completion or shipping.

## Automated acceptance checks

Keep planners and rules pure where possible. Add deterministic tests for:

### Lifecycle and persistence

- Suspend/resume for combat, Work OFF/ON, delivery, recall, and unload.
- Terminal completion versus suspended, retryable, and abandoned exits.
- Retry/backoff and bounded stuck detection.
- Reservation claim, typed ownership, expiry, release, job switch, death,
  removal, dimension change, and competing workers.
- No queue advancement after a failed action.
- Serialization/reload restoration of phase, active unit, return checkpoint,
  delivery target, and profession payload.
- Guard restoration when the job becomes `NONE`.

### Work sites and actions

- Future destination can be planned without remote LOS.
- Action requires actual stand proximity and current validation.
- Floor/headroom/range/hazard/bounds/chunk checks.
- Protection denial, `mobGriefing`, claim hooks, correct drops/durability,
  inventory-full results, and no direct world-edit bypass.
- Item-handler insertion with `Container` fallback and sided behavior.

### Profession planners

- Farmer cell confirmation and failed-action retention.
- Tree separation, full connected-log retention, bottom-up order, 2x2
  footprint, interruption without early replanting, species-correct planting,
  and durable/deduplicated missing-sapling debt.
- Miner cave preference, solid tunnel, stable floor, stairs, supplied bridge,
  unsafe ravine, falling block/fluid/lava/protected rejection, and return route.
- Fisher surface-water/shore validation, reservation, compatible rod, durable
  bite window, hook cleanup, and loot insertion/delivery result.
- Hunter full eligibility predicate, exclusions/tags, single target authority,
  direct/projectile kill attribution, claimed-drop collection, and competing
  Hunter behavior.
- Chef recipe/tag lookup, workstation batch ownership, no unrelated output
  theft, normal/soul campfire flow, Hunter deposit/Chef withdrawal, and
  cooked-output delivery.

Use NeoForge GameTests for world, navigation, block-entity, and entity flows
where supported. Otherwise add a narrow server-side harness. Pure tests do not
prove live navigation, rendering, multiplayer authority, or mod integrations.

## Manual dev-world acceptance

Run every profession's acceptance examples with one worker and with two
workers sharing an area and chest. For each job, interrupt work with:

- Hostile combat and defensive combat.
- Work OFF/ON.
- Owner recall and manual Deposit.
- Full inventory, broken/missing tool, target removal, protected target.
- Unloaded/reloaded chunks or entity restart.
- Blocked route and missing/full/protected/unreachable chest.

Confirm the companion reports the reason and safely resumes, replans, waits,
or completes without lost items or duplicate world actions.

Also verify:

- Navigation around walls, doors, foliage, slopes, caves, ravines, fluids,
  unloaded chunks, and world borders.
- Fishing cast/line/bite/reel visuals and hook cleanup.
- Melee/ranged hunting, kill confirmation, and drop collection with Pickup
  disabled.
- Native furnace, smoker, normal campfire, and soul campfire behavior.
- Multiplayer/server authority and client reconnect/reload behavior.
- At least one protection/claim environment and `mobGriefing` disabled.
- Vanilla inventory, double chests, item handlers, and Sophisticated Backpacks.
- Work UI green pressed state, phase/status updates, `Currently` panel
  clipping at supported GUI scales, and restoration of Guard for `NONE`.

Separate automated evidence from remaining visual/runtime/protection smoke.
Build and tests are necessary but are not sufficient proof of gameplay.

## Boundaries and non-goals

- Edit only allowed project directories except this explicitly authorized
  `TASK.md` deliverable. Treat Minecraft, NeoForge, OriginalCompanions,
  ModDevGradle, and the supplied reference trees as read-only.
- Village Workers is ARR/all-rights-reserved: infer behavior only.
- MineColonies is GPLv3: use architecture as research; do not copy source or
  import its framework without a separate licensing decision and compliance
  plan.
- SmartBrainLib is MPL-2.0: use narrow concepts as research; do not add it as
  a dependency or copy its source into this mod without an explicit license
  review and notice plan.
- No arbitrary terrain destruction, teleport-to-target recovery, direct
  `setBlock(AIR)` recovery, infinite scans, permanent chunk forcing by default,
  generic behavior tree, speculative settlement economy, or wholesale rewrite
  of existing combat/equipment/inventory behavior.
- Do not make all jobs share identical discovery/action code. Share lifecycle,
  safety, reservation, collection, delivery, status, and persistence contracts;
  keep each profession's planner and action semantics explicit.
- Preserve existing combat safety, owner protection, equipment restrictions,
  food/stamina/mana behavior, patrol behavior, optional-mod compatibility, and
  multiplayer/server authority.
- Before implementation is considered complete, build with Java 21, update
  relevant player-facing documentation/configuration, update `TRACELOG.md` and
  `SUGGESTIONS.md` with shipped facts, and clearly list manual validation still
  required.

## Completion definition

This task is complete only when:

- Every listed job has the shared durable lifecycle and delivery contract.
- A valid active work unit survives interruption, unload/reload, and delivery.
- Failed/protected/unsafe actions preserve work and report a reason.
- Competing workers cannot duplicate active targets or workstations.
- Inventory pressure never silently loses job output or required supplies.
- Lumberjack, Miner, Fisher, Hunter, Chef, and Farmer pass their automated
  checks and manual acceptance examples.
- Work OFF/ON and job removal have the specified authoritative UI and behavior.
- Java 21 build/check succeeds, project changes remain within policy, and the
  final handoff separates automated evidence from remaining runtime smoke.
