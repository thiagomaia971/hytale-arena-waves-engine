package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo
import com.hypixel.hytale.server.npc.commands.NPCCommand.NPC_ROLE
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.config.EnemyDefinition
import com.miilhozinho.arenawavesengine.config.WaveDefinition
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.UUID

class WaveEngine(public val plugin: ArenaWavesEngine) {

    private val npcSpawn = NpcSpawn()

    fun processTick(sessionId: String, config: ArenaWavesEngineConfig, event: SessionStarted) {
        val session = config.sessions.find { it.id == sessionId } ?: run {
            LogUtil.debug("[WaveEngine] Tick skipped: Session $sessionId not found in config")
            return
        }

        if (session.state.isInactive()) {
            LogUtil.debug("[WaveEngine] Tick skipped: Session $sessionId is in inactive state ${session.state}")
            return
        }

        when (session.state) {
            WaveState.RUNNING         -> prepareWave(session, config)
            WaveState.SPAWNING        -> handleSpawning(session, config, event)
            WaveState.WAITING_CLEAR   -> checkWaveCleared(session, config, event)
            WaveState.WAITING_INTERVAL -> checkIntervalElapsed(session, config, event)
            else -> LogUtil.warn("[WaveEngine] Unhandled state ${session.state} for $sessionId")
        }
    }

    private fun prepareWave(session: ArenaSession, config: ArenaWavesEngineConfig) {
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId }

        if (mapDef == null) {
            LogUtil.severe("[WaveEngine] Failed to prepare wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED)
            return
        }

        if (session.currentWave >= mapDef.waves.size) {
            LogUtil.debug("[WaveEngine] All waves (${mapDef.waves.size}) completed for session ${session.id}")
            transitionTo(session, WaveState.COMPLETED)
            return
        }

        LogUtil.debug("[WaveEngine] Preparing wave ${session.currentWave} for session ${session.id}")
        session.currentWaveSpawnProgress.clear()
        transitionTo(session, WaveState.SPAWNING)
    }

    private fun handleSpawning(session: ArenaSession, config: ArenaWavesEngineConfig, event: SessionStarted) {
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId } ?: return
        val waveDef = mapDef.waves.getOrNull(session.currentWave) ?: return

        val aliveCount = getAliveEntityCount(session.id)
        val maxConcurrent = config.maxConcurrentMobsPerSession
        val availableSlots = maxConcurrent - aliveCount

        LogUtil.debug("[WaveEngine] Spawning check: Alive=$aliveCount, Max=$maxConcurrent, Available=$availableSlots")

        if (availableSlots <= 0) {
            LogUtil.debug("[WaveEngine] Spawn throttled: Session ${session.id} at max capacity")
            return
        }

        var spawnedThisTick = 0

        for (enemy in waveDef.enemies) {
            val spawnedSoFar = session.currentWaveSpawnProgress.getOrDefault(enemy.enemyType, 0)
            val remainingForThisEnemy = enemy.count - spawnedSoFar

            if (remainingForThisEnemy <= 0) continue

            val toSpawn = minOf(availableSlots - spawnedThisTick, remainingForThisEnemy)

            if (toSpawn > 0) {
                LogUtil.debug("[WaveEngine] Spawning $toSpawn x ${enemy.enemyType} (Progress: ${spawnedSoFar + toSpawn}/${enemy.count})")
                executeSpawn(session, enemy, toSpawn, event)
                session.currentWaveSpawnProgress[enemy.enemyType] = spawnedSoFar + toSpawn
                spawnedThisTick += toSpawn
            }

            if (spawnedThisTick >= availableSlots) {
                LogUtil.debug("[WaveEngine] Tick spawn limit reached for session ${session.id}")
                break
            }
        }

        if (isWaveFullySpawned(session.id, waveDef)) {
            LogUtil.debug("[WaveEngine] Wave ${session.currentWave} fully spawned for ${session.id}. Moving to clear phase.")
            transitionTo(session, WaveState.WAITING_CLEAR)
        }
    }

    private fun checkWaveCleared(
        session: ArenaSession,
        config: ArenaWavesEngineConfig,
        event: SessionStarted
    ) {
        if (session.activeEntities.size == 0){
            LogUtil.debug("[WaveEngine] Wave ${session.currentWave} cleared for session ${session.id}. Starting interval wait.")
            session.waveClearTime = System.currentTimeMillis()
            transitionTo(session, WaveState.WAITING_INTERVAL)
        }
    }

    private fun checkIntervalElapsed(
        session: ArenaSession,
        config: ArenaWavesEngineConfig,
        event: SessionStarted
    ) {
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId } ?: return
        val waveDef = mapDef.waves.getOrNull(session.currentWave) ?: return

        val elapsedTimeMs = System.currentTimeMillis() - session.waveClearTime
        val requiredIntervalMs = waveDef.interval * 1000L

        LogUtil.debug("[WaveEngine] Interval check: Elapsed=${elapsedTimeMs}ms, Required=${requiredIntervalMs}ms for session ${session.id}")

        if (elapsedTimeMs >= requiredIntervalMs) {
            LogUtil.debug("[WaveEngine] Interval elapsed for wave ${session.currentWave} in session ${session.id}. Starting next wave.")
            markToNextWave(session, config, event)
        }
    }

    private fun markToNextWave(
        session: ArenaSession,
        config: ArenaWavesEngineConfig,
        event: SessionStarted
    ) {
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId } ?: return
        if (session.currentWave <= mapDef.waves.size) {
            session.currentWave++
            transitionTo(session, WaveState.RUNNING)
        } else {
            transitionTo(session, WaveState.COMPLETED)
        }
    }

    private fun isWaveFullySpawned(sessionId: String, waveDef: WaveDefinition): Boolean {
        val session = ArenaWavesEngine.config.sessions.find { it.id == sessionId } ?: return false
        val isComplete = waveDef.enemies.all { (session.currentWaveSpawnProgress[it.enemyType] ?: 0) >= it.count }
        LogUtil.debug("[WaveEngine] Wave completion check for $sessionId: $isComplete")
        return isComplete
    }

    private fun executeSpawn(session: ArenaSession, enemy: EnemyDefinition, count: Int, event: SessionStarted) {
        repeat(count) {
            val spawnReturn = npcSpawn.execute(
                event.store,
                event.playerPosition,
                event.playerHeadRotation,
                event.playerBoundingBox,
                event.world,
                NPC_ROLE.parse(enemy.enemyType, ParseResult()) as BuilderInfo,
                1,
                event.radius,
                event.flagsString,
                event.speedArg,
                event.nonRandom,
                event.posOffset,
                event.headRotation,
                event.bodyRotation,
                event.randomRotationArg,
                event.facingRotation,
                event.flockSize,
                event.frozen,
                event.randomModel,
                event.scaleArg,
                event.bypassScaleLimitsArg,
                event.test,
                event.spawnPosition,
                event.spawnOnGround
            )

            val npcUuidComponent = checkNotNull(
                spawnReturn.npcRef.store.getComponent<UUIDComponent?>(
                    spawnReturn.npcRef,
                    UUIDComponent.getComponentType()
                )
            )

            session.activeEntities += npcUuidComponent.uuid.toString()
            ArenaWavesEngine.config.entityToSessionMap[npcUuidComponent.uuid.toString()] = session.id
            LogUtil.debug("[WaveEngine] Entity ${npcUuidComponent.uuid} tracked for session ${session.id}")
        }
    }

    private fun transitionTo(session: ArenaSession, newState: WaveState) {
        if (session.state == newState) return
        val oldState = session.state
        session.state = newState
        ArenaWavesEngine.configState?.save()
        LogUtil.info("[WaveEngine] Session ${session.id} state transition: $oldState -> $newState")
    }


    fun getAliveEntityCount(sessionId: String): Int {
        val session = ArenaWavesEngine.config.sessions.find { it.id == sessionId } ?: return 0
        return session.activeEntities.size
    }

    /**
     * Stops a wave session and cleans up all tracked entities.
     */
    fun stopSession(sessionId: String) {
        LogUtil.debug("[WaveEngine] Initiating soft despawn for session $sessionId")

        val session = ArenaWavesEngine.config.sessions.find { it.id == sessionId } ?: return
        val activeEntities = session.activeEntities
        if (activeEntities.isEmpty()) {
            session.currentWaveSpawnProgress.clear()
            return
        }

        LogUtil.info("[WaveEngine] Sending despawn signal to ${activeEntities.size} NPCs.")
        val world = Universe.get().getWorld(session.world)

        activeEntities.forEach { entityId ->
            val entityRef = world?.getEntityRef(UUID.fromString(entityId))
            val store = entityRef?.store!!
            val npc = store.getComponent<NPCEntity>(entityRef, NPCEntity.getComponentType()!!) as NPCEntity

            if (npc.wasRemoved())
                return@forEach

            try {
                npc.setToDespawn()
                ArenaWavesEngine.config.entityToSessionMap.remove(entityId)
                LogUtil.debug("[WaveEngine] SetDespawning(true) for NPC: ${npc.roleName} - $entityId")
            } catch (e: Exception) {
                LogUtil.warn("[WaveEngine] Error despawning NPC ${npc.roleName} - $entityId: ${e.message}")
            }
        }

        session.activeEntities = emptyArray()
        session.currentWaveSpawnProgress.clear()
        transitionTo(session, WaveState.STOPPED)
    }

    fun onEntityDeath(entityId: String) {
        val sessionId = ArenaWavesEngine.config.entityToSessionMap[entityId] ?: return
        val session = ArenaWavesEngine.config.sessions.find { it.id == sessionId } ?: return

        session.activeEntities = session.activeEntities.filter { it != entityId}.toTypedArray()
        ArenaWavesEngine.config.entityToSessionMap.remove(entityId.toString())
        ArenaWavesEngine.configState?.save()
        LogUtil.debug("[WaveEngine] Entity $entityId removed from tracking for session $sessionId. Remaining: ${getAliveEntityCount(sessionId)}")
    }
}

fun WaveState.isInactive() = this in listOf(WaveState.IDLE, WaveState.STOPPED, WaveState.COMPLETED, WaveState.FAILED)
