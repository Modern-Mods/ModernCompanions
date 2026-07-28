# Jobs AI: Safe, Player-Like Workers

## Objective

Make companion jobs behave like competent players: select reachable work sites, traverse safely, react to danger, recover without destroying unrelated terrain, and resume or abandon work predictably.

Implement this as one cohesive Jobs AI batch. Do not copy MineColonies code: it is GPLv3. Use only independently implemented ideas.

## Scope

Improve these existing jobs:

- Miner
- Fisher
- Lumberjack
- Hunter
- Chef
- Courier / chest delivery

Keep existing job UI, inventory, patrol anchor, and configuration unless this task explicitly changes them.

## Non-goals

- No MineColonies dependency, colony system, custom navmesh, work-order framework, or behavior-tree framework.
- No bridge building, pillar building, ladders, torches, or arbitrary terrain placement.
- No prospecting/random digging when no valid ore exists.
- No generic “break blocks when stuck” recovery.
- Do not edit `MineColonies/`.

## Required shared behavior

Create the smallest shared work-site contract needed by multiple jobs:

- A work site consists of a target block/entity and a companion standing position.
- A standing position is valid only when:
  - Its floor is sturdy and non-hazardous.
  - Feet and head spaces are clear.
  - It is not in lava, fire, magma, or an unsafe fluid space.
  - The native navigation path exists and `canReach()` is true.
  - The target is within actual interaction range and visible from the stand.
- Validate an existing site every tick before acting.
- On path failure, retry once; then temporarily blacklist the site/target and choose another.
- If no valid site remains, stop working and return toward the patrol anchor. Do not wander or force-break blocks.

Jobs must own `MOVE` only while active. Guard/patrol return goals must reserve `MOVE` too and must not compete with an active job.

## Job activation and tools

- Assigning any non-`NONE` job must set a patrol anchor at the companion’s current position and enter patrol/work mode.
- Selecting `NONE` restores normal companion behavior.
- Show or retain a clear UI indication that the job is working from its patrol anchor.
- When the job changes during patrol, equip its required tool immediately.
- Every job action must verify the correct tool is actually in the main hand; inventory presence alone is insufficient.
- Preserve and restore the previous combat weapon when work mode ends.

## Miner

Replace the current forced-dig recovery with safe step planning.

- Remove `forceBreakImmediate` and any “mine the current block because we are stuck” behavior.
- Mine only ore targets inside the existing 3D patrol cube.
- Build the route one walkable step at a time:
  - No vertical shafts.
  - Intended feet-height changes are at most one block.
  - Every step has a safe floor and two-block headroom.
  - Before descending, the completed route back toward the patrol anchor remains traversable.
  - Check the destination and nearby cells for lava, fire, magma, unsafe fluids, and dangerous drops before breaking.
- Revalidate the return route after each completed step. If it fails, stop further excavation and return; blacklist that ore.
- Mine only the planned blocks for the next step. Never break blocks outside the current plan to unblock navigation.
- If an ore vanishes externally, remove it from the plan without counting it as mined.
- When no valid ore exists, report/idle at the anchor. Do not tunnel toward arbitrary filler.
- Replace full-cube repeated rescans with a bounded scan budget per tick or an equivalent resumable scan. Multiple miners must not cause server hitches.

## Fisher

Make fishing use a valid shoreline and an actual fishing cycle.

- Find and store `(waterPos, standPos)` pairs.
- Require a reachable dry shoreline stand with headroom, solid floor, line of sight, and a real fishable water area.
- Reject unreachable or invalid shores temporarily instead of repeatedly selecting them.
- Face the selected water before casting.
- Extend `CompanionFishingHook` with a small server-side bite/ready state.
- Reel only when the bobber bites; clear and retry when the hook lands outside valid water, becomes invalid, or times out.
- Keep existing loot-table use, inventory insertion, rendering, and rod durability behavior.

## Lumberjack

- Select a reachable stand for each log, with full 3D interaction-range and line-of-sight checks.
- Never chop logs merely because they are horizontally nearby.
- Retain the bounded connected-tree queue, lower-log priority, and sapling replanting.
- Limit any unstuck clearing to explicitly safe foliage relevant to the selected tree. Do not clear generic terrain.
- Revalidate every next log; abandon/blacklist an unreachable tree rather than hacking through terrain.

## Hunter and danger response

- Select the nearest reachable valid hunt target within the patrol boundary, not the first entity returned by a world query.
- Clear a hunter target when it exits the patrol area, becomes unreachable, or is invalid; return to the anchor.
- Safety and owner-defense targets may interrupt work. After combat, resume the saved job site only if it is still valid.
- Separate the player-controlled Alert setting from temporary creeper avoidance. Creeper escape must not toggle the player’s Alert preference.

## Chef and courier

- Chef must select a reachable, safe heat-source stand.
- Do not directly convert food unless the companion is at a valid heat source and the action respects the intended furnace/campfire contract.
- Courier must use a reachable chest standing position.
- Remove terrain destruction from courier recovery. If the chest is unreachable, report the problem and retry later.
- Courier continuation must stop for combat, invalid chest state, changed job/stance, or unloaded destination.

## Block actions and compatibility

Centralize worker block breaking/placement behind one server-side helper.

It must:

- Validate the approved target and stand before changing the world.
- Respect block-break cancellation/protection integration.
- Use the held tool for harvest, drops, and durability.
- Preserve modded block/drop behavior where possible.
- Never directly `setBlock(AIR)` from a job goal.

## Validation

Add focused automated coverage for the extracted safety predicates and route decisions.

Manually verify in a dev world:

1. Fisher chooses a reachable shore, casts toward water, waits for a bite, and abandons an unreachable pond.
2. Miner reaches ore below via walkable steps and can return to the patrol anchor.
3. Miner stops at lava, unsafe drops, blocked routes, and cube boundaries without forced excavation.
4. Lumberjack does not break high or unreachable logs and replants when supplied a sapling.
5. Hunter does not chase targets outside its patrol boundary.
6. Courier never destroys terrain when a chest is inaccessible.
7. Manual Alert remains enabled/disabled after a creeper avoidance event.
8. Several miners with maximum configured radius do not cause noticeable server tick spikes.

## Repository requirements

- Edit only allowed directories.
- Bump `gradle.properties` version.
- Add concise comments for non-obvious safety/recovery logic.
- Update `README.md` with the shipped job behavior and limitations.
- Add entries to `TRACELOG.md` and `SUGGESTIONS.md`.
- Run `.\gradlew.bat build` successfully before committing.
- Review the final diff for unrelated changes.