# ArenaWavesEngine – GitHub Issues

> Generated backlog issues for the ArenaWavesEngine mod.

> Each issue includes mandatory rules requested.


## How to use

- Copy each issue into GitHub as an Issue title + body.

- Suggested labels are included per issue.


---


## Issue 01 — Bootstrap plugin: ArenaWavesEngine skeleton + lifecycle hooks

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:chore, area:core


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create the base plugin project for ArenaWavesEngine and wire the plugin lifecycle.

**Scope**
- Create main plugin class and required lifecycle methods (setup/start/shutdown) following Hytale Java Plugins API.
- Add structured logging prefix `[ArenaWavesEngine]`.
- Ensure no background threads; use server APIs for scheduled work.


### Acceptance Criteria

- [ ] Plugin loads successfully in server `mods/` without errors.
- [ ] Logs show setup/start/shutdown messages once.
- [ ] No runtime exceptions on enable/disable.


---


## Issue 02 — Add manifest.json and plugin identifier conventions

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:chore, area:core


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add `manifest.json` with Group/Name/Version/Main/Description and ensure the plugin identifier conventions are consistent across logs and config paths.

**Scope**
- Define plugin group (e.g., `com.tmaia`) and name `ArenaWavesEngine`.
- Validate that the server recognizes the plugin metadata.


### Dependencies

- #01


### Acceptance Criteria

- [ ] `manifest.json` is present and valid.
- [ ] Server recognizes the mod metadata (name/version).
- [ ] Identifier is consistent across logs and config.


---


## Issue 03 — Config system using CODEC (plugin withConfig) + default settings

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:config, codec


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create the plugin configuration model and load it via Hytale's `withConfig(BuilderCodec<T>)`.

**Fields (initial)**
- `defaultWaveCount`
- `defaultMobsPerWave`
- `defaultSpawnIntervalSeconds`
- `defaultSpawnRadius`
- `maxConcurrentMobsPerSession`
- `maxConcurrentSessionsGlobal`
- `cleanupTimeoutSeconds`
- `debugLoggingEnabled`

**Scope**
- Fail-fast validation with clear error messages.


### Dependencies

- #01
- #02


### Acceptance Criteria

- [ ] Config loads automatically on plugin preLoad/load.
- [ ] Invalid config produces clear validation error logs and refuses to start.
- [ ] Defaults work without a config file.


---


## Issue 04 — Domain models: ArenaSession, WaveDefinition, WaveMap (runtime-only for MVP)

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:domain


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create clean domain models for the wave engine.

**Models**
- `ArenaSessionId`
- `ArenaSession` (owner, center, state, currentWave, aliveEntityIds)
- `WaveDefinition` (count, interval, enemy selection)
- `WaveState` enum (IDLE/RUNNING/SPAWNING/WAITING_CLEAR/COMPLETED/STOPPED/FAILED)

**Scope**
- Keep MVP models in-memory only.


### Dependencies

- #01
- #03


### Acceptance Criteria

- [ ] Domain models compile and are used by engine services.
- [ ] Models are immutable where possible; state transitions go through a single service.


---


## Issue 05 — WaveScheduler service using TaskRegistry (one task per session)

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:engine, performance


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement a scheduler that drives a session's wave logic using server task facilities.

**Rules**
- One scheduled loop per session (avoid per-mob timers).
- Respect spawn throttling to avoid burst lag.
- Cleanly cancel tasks on stop/shutdown.


### Dependencies

- #01
- #04


### Acceptance Criteria

- [ ] Starting a session creates exactly one repeating task.
- [ ] Stopping a session cancels the task and prevents further spawns.
- [ ] Shutdown cancels all session tasks safely.


---


## Issue 06 — WaveSpawner abstraction + spawn rules (radius, caps, throttle)

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:engine, performance


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create `WaveSpawner` responsible for spawning enemies for a session.

**Scope**
- Spawn within radius around session center.
- Enforce `maxConcurrentMobsPerSession`.
- Throttle spawns per tick/cycle.
- Track spawned entity IDs for lifecycle management.


### Dependencies

- #04
- #05


### Acceptance Criteria

- [ ] Spawns happen within configured radius.
- [ ] Never exceeds max concurrent mobs per session.
- [ ] All spawned entity IDs are tracked.


---


## Issue 07 — Entity tracking + clear detection (event-driven + reconciliation failsafe)

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:engine, stability


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement clear detection so a wave completes only when all tracked enemies are dead/removed.

**Scope**
- Prefer event-driven removal (entity death/despawn events).
- Add periodic reconciliation to handle edge cases (chunk unload, forced removal).
- Avoid scanning large areas every tick.


### Dependencies

- #05
- #06


### Acceptance Criteria

- [ ] Wave completes when all tracked entity IDs are gone.
- [ ] No soft-lock if entities despawn or unload unexpectedly.
- [ ] Reconciliation runs at low frequency and is bounded.


---


## Issue 08 — Admin commands: /awe start|stop|status (+ flags)

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:commands


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add command interface for MVP control.

**Commands**
- `/awe start [--waves N] [--count N] [--interval S] [--radius R] [--enemy ID|tag]`
- `/awe stop`
- `/awe status`

**Scope**
- Validate inputs.
- Only allow authorized users (operator/admin).
- Provide concise feedback in chat + logs.


### Dependencies

- #01
- #03
- #05
- #07


### Acceptance Criteria

- [ ] Commands register and work on server.
- [ ] `start` creates a session and begins wave loop.
- [ ] `stop` stops session and cleans spawned mobs.
- [ ] `status` shows session state and wave progress.


---


## Issue 09 — SessionManager: prevent duplicate sessions and enforce global caps

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:engine, safety


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement `SessionManager` to own all sessions.

**Scope**
- Enforce `maxConcurrentSessionsGlobal`.
- Prevent multiple sessions overlapping same arena center (configurable).
- Provide query APIs for `/awe status`.


### Dependencies

- #03
- #04
- #08


### Acceptance Criteria

- [ ] Cannot exceed global max sessions.
- [ ] Overlapping sessions are blocked unless explicitly forced (optional flag).
- [ ] SessionManager exposes consistent state.


---


## Issue 10 — Cleanup guarantees: stop cleans entities; timeout if no players nearby

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:feature, area:stability, performance


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement session cleanup policies.

**Scope**
- On stop/complete: despawn/cleanup tracked entities according to safe rules.
- If owner disconnects: optionally pause or stop (config).
- Timeout cleanup if no players nearby for `cleanupTimeoutSeconds`.


### Dependencies

- #03
- #07
- #09


### Acceptance Criteria

- [ ] No entity leaks after stop or completion.
- [ ] Disconnect policy is applied consistently.
- [ ] Timeout cleanup triggers and clears the session.


---


## Issue 11 — Observability: structured logs + debug toggle + counters

**Phase:** Phase 1 (MVP)


**Suggested labels:** phase:1, type:chore, area:observability


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add observability for operations and debugging.

**Scope**
- Structured logs per sessionId (start/wave/spawn/stop).
- Debug toggle from config.
- Basic counters: spawned, alive, killed, session duration.


### Dependencies

- #03
- #09


### Acceptance Criteria

- [ ] Logs include sessionId and wave number.
- [ ] Debug logs can be enabled/disabled by config.
- [ ] Counters are available via `/awe status` output.


---


## Issue 12 — WaveMapDefinition (CODEC) with fixed or random wave sources

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:data, codec


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create `WaveMapDefinition` and load it using CODEC.

**Requirements**
- Supports **fixed** waves: explicit `waves: [WaveDefinition...]`
- Supports **random** rules: enemy pool + min/max + scaling
- Include display fields: name, description/lore
- Validation errors must point to mapId + field path

**Storage**
- Load from config folder (e.g., `configs/ArenaWavesEngine/wavemaps/*.json`).


### Dependencies

- #03
- #04


### Acceptance Criteria

- [ ] WaveMaps load at startup with validation.
- [ ] Both fixed and random modes work.
- [ ] Invalid WaveMaps produce clear errors and are skipped or fail-fast (configurable).


---


## Issue 13 — WaveMap item: encode mapId in item metadata + display lore

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:items, codec


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create an item representation for WaveMap.

**Scope**
- When created/given, item contains `mapId`.
- Item shows readable description/lore derived from WaveMapDefinition.
- Ensure server-side validation: unknown mapId cannot be used.


### Dependencies

- #12


### Acceptance Criteria

- [ ] WaveMap item can be created and recognized reliably.
- [ ] mapId is retrievable from item metadata.
- [ ] Unknown mapId is rejected with clear message.


---


## Issue 14 — Admin command: /awe wavemap give <player> <mapId>

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:commands


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add admin command to give WaveMap items to players.

**Scope**
- Validate player exists and mapId exists.
- Provide feedback to admin and player.


### Dependencies

- #13


### Acceptance Criteria

- [ ] Command gives item with correct mapId.
- [ ] Invalid mapId returns error + list suggestions (optional).


---


## Issue 15 — Arena Block: register block + persistent state (inserted mapId, status)

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:block, persistence


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create an Arena controller block.

**State**
- `insertedWaveMapId`
- `owner/lockMode`
- `status` (IDLE/RUNNING/COOLDOWN)

**Persistence**
- Block state must persist across server restarts.


### Dependencies

- #12
- #13


### Acceptance Criteria

- [ ] Block can be placed and interacted with.
- [ ] Inserted mapId is saved and restored after restart.
- [ ] Status updates correctly.


---


## Issue 16 — Block interaction: insert/eject WaveMap item + start wave session

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:block, area:engine


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement player interaction with the Arena Block.

**Flow**
- Insert WaveMap item into block (store mapId)
- Activate block to start waves (create session at block location)
- Prevent eject/removal while RUNNING (or safe stop + eject)

**Compatibility**
- Reuse Phase 1 Wave Engine; input comes from WaveMapDefinition.


### Dependencies

- #15
- #09
- #10


### Acceptance Criteria

- [ ] Player can insert a WaveMap item into the block.
- [ ] Activation starts a session using the selected WaveMap.
- [ ] Eject/removal rules are enforced safely.


---


## Issue 17 — Arena boundary rules (Phase 2): soft boundary + anti-exploit

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:gameplay, safety


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add basic boundary handling for arenas started from the block.

**Options (pick MVP)**
- Soft boundary: warning + teleport back to center
- Optional: apply slow/damage if repeatedly leaving

**Scope**
- Keep it server-side cheap; avoid constant position checks (use periodic checks / events).


### Dependencies

- #16


### Acceptance Criteria

- [ ] Players are prevented from kiting enemies infinitely far away.
- [ ] Boundary checks are bounded and do not run every tick for every player.


---


## Issue 18 — WaveMap-driven waves: random pool + scaling curve (server-side deterministic)

**Phase:** Phase 2


**Suggested labels:** phase:2, type:feature, area:engine, codec


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement random wave generation from WaveMap rules.

**Requirements**
- Deterministic per session (seed from sessionId) so it’s reproducible for debugging.
- Scaling curve affects counts and/or enemy tier.
- Validate enemy ids/tags exist at load-time (if possible).


### Dependencies

- #12
- #06
- #09


### Acceptance Criteria

- [ ] Random WaveMaps generate waves deterministically for a given session.
- [ ] Scaling curve increases difficulty over waves.
- [ ] Missing enemies are handled gracefully.


---


## Issue 19 — InstanceAllocator: reserve arena space (grid strategy MVP)

**Phase:** Phase 3


**Suggested labels:** phase:3, type:feature, area:instances, performance


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create a minimal instancing system.

**MVP strategy**
- Reserve coordinates in a grid far from main world spawn.
- Each instance gets `InstanceId` + center + bounds.

**Scope**
- No collision between concurrent instances.
- Release reservation on session end.
- Configurable base coords + spacing.


### Dependencies

- #09
- #12


### Acceptance Criteria

- [ ] Two simultaneous sessions allocate different instance locations.
- [ ] Instance reservation is released after completion/stop.
- [ ] Allocator survives restarts if needed (optional persistent registry).


---


## Issue 20 — Instanced session start: using WaveMap on block creates private instance + teleport owner

**Phase:** Phase 3


**Suggested labels:** phase:3, type:feature, area:instances, area:teleport


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

When a WaveMap is activated in the block, create a new instance and teleport the activating player to it.

**Flow**
- Allocate instance
- Teleport player to instance center
- Start session bound to instance id
- Track players in session for cleanup rules


### Dependencies

- #19
- #16
- #10


### Acceptance Criteria

- [ ] Owner is teleported into instance reliably.
- [ ] Session is started within instance bounds.
- [ ] Stop/completion teleports players back (configurable return point).


---


## Issue 21 — Party system (minimal): invite/join/leave + leader

**Phase:** Phase 3


**Suggested labels:** phase:3, type:feature, area:party


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Implement minimal party mechanics for ArenaWavesEngine (if no native API exists).

**Commands**
- `/awe party invite <player>`
- `/awe party accept <leader>`
- `/awe party leave`
- `/awe party kick <player>` (optional)

**Rules**
- Party leader owns the arena session.
- Store party state in-memory (Phase 3).


### Dependencies

- #08


### Acceptance Criteria

- [ ] Players can form a party with leader/invites.
- [ ] Party membership is validated for arena joins.
- [ ] Leader leaving ends or transfers leadership (choose MVP behavior).


---


## Issue 22 — Party join to instance: allies can join active arena (with join window rules)

**Phase:** Phase 3


**Suggested labels:** phase:3, type:feature, area:party, area:instances


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Allow party members to join the leader’s active arena instance.

**Scope**
- Configurable join window (e.g., only before wave 2 starts).
- Teleport joining member to instance.
- Update session player list for cleanup/end conditions.


### Dependencies

- #20
- #21


### Acceptance Criteria

- [ ] Party members can join during allowed window.
- [ ] Joining does not break wave tracking.
- [ ] Leaving/disconnect is handled safely.


---


## Issue 23 — Reward pipeline interfaces + session events for future integrations

**Phase:** Phase 3


**Suggested labels:** phase:3, type:feature, area:rewards, extensibility


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Create foundation hooks (not full RPG yet).

**Deliverables**
- `RewardResolver` + `RewardApplier` interfaces
- Internal events:
  - `ArenaSessionStarted`
  - `WaveCompleted`
  - `ArenaSessionCompleted`
- Default implementation: none / placeholder


### Dependencies

- #09
- #10


### Acceptance Criteria

- [ ] Events are emitted at correct times.
- [ ] Reward interfaces exist and can be implemented later without breaking API.


---


## Issue 24 — HUD integration placeholder: prepare arrow count UI hook using Common.ui (no gameplay dependency)

**Phase:** Cross-cutting


**Suggested labels:** type:chore, area:ui, phase:later


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Prepare a minimal UI hook that can display Arena session info (e.g., current wave, mobs alive) using shared UI assets.

**Scope**
- Reference shared UI from `Common.ui` (path already mounted in repo tooling).
- No hard dependency on waves running; just establish the UI service structure.
- Keep client UI optional; server-side remains authoritative.


### Acceptance Criteria

- [ ] UI hook compiles and can be toggled by config.
- [ ] No runtime errors if UI is disabled or missing.
- [ ] Does not affect server performance.


---


## Issue 25 — Automated tests: session state transitions + spawn throttling (unit tests)

**Phase:** Cross-cutting


**Suggested labels:** type:test, area:engine


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Add unit tests for core wave logic.

**Scope**
- State transitions: start -> spawning -> waiting_clear -> completed
- Stop logic cancels tasks and cleans entities (mocked)
- Throttling respects max concurrent mobs


### Dependencies

- #04
- #05
- #07
- #09


### Acceptance Criteria

- [ ] Tests cover critical state transitions and edge cases.
- [ ] CI can run tests headlessly.
- [ ] No flaky timing tests; use deterministic scheduler abstractions.


---


## Issue 26 — Docs: README with commands, config, WaveMap format, and admin guide

**Phase:** Cross-cutting


**Suggested labels:** type:docs, area:docs


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Write documentation for server admins and contributors.

**Include**
- Installation steps
- Commands (Phase 1-3)
- Config fields
- WaveMap JSON examples (fixed/random)
- Arena Block usage
- Known limitations and performance tips


### Dependencies

- #03
- #08
- #12
- #16


### Acceptance Criteria

- [ ] README is clear and copy-paste friendly.
- [ ] WaveMap examples validate against codec model.
- [ ] Docs match actual commands and defaults.


---


## Issue 27 — Build pipeline: shadowJar + reproducible release artifact

**Phase:** Cross-cutting


**Suggested labels:** type:chore, area:build


**Rules (must be included in EVERY issue):**
- Use hytale-doc.md cline rule
- Use hytale-ui-common-ui.md cline rule
- Always use SequencialThink MCP to get the best response


### Description

Configure Gradle build for reliable server deployment.

**Scope**
- Ensure Kotlin compiles for Java 25 target
- Create shaded jar as needed
- Keep Hytale Server API as compileOnly
- Produce reproducible artifact naming: `ArenaWavesEngine-<version>.jar`


### Dependencies

- #01
- #02


### Acceptance Criteria

- [ ] `./gradlew build` produces a deployable jar.
- [ ] No server API classes are bundled incorrectly.
- [ ] Artifact name and versioning are consistent.


---

