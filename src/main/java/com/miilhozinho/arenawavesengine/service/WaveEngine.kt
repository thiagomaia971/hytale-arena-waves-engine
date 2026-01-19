package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo
import com.hypixel.hytale.server.npc.commands.NPCCommand.NPC_ROLE
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.*
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class WaveEngine(public val plugin: ArenaWavesEngine) {

    private val sessionEntities = ConcurrentHashMap<UUID, MutableSet<NPCEntity>>()
    private val spawnTracker = ConcurrentHashMap<UUID, MutableMap<String, Int>>()
    private val npcSpawn = NpcSpawn()

    fun processTick(sessionId: UUID, config: ArenaWavesEngineConfig, event: SessionStarted) {
        val session = config.sessions.find { it.id == sessionId } ?: run {
            LogUtil.debug("[WaveEngine] Tick skipped: Session $sessionId not found in config")
            return
        }

        if (session.state.isInactive()) {
            LogUtil.debug("[WaveEngine] Tick skipped: Session $sessionId is in inactive state ${session.state}")
            return
        }

        LogUtil.debug("[WaveEngine] Processing tick for session $sessionId (State: ${session.state}, Wave: ${session.currentWave})")

        when (session.state) {
            WaveState.RUNNING      -> prepareWave(session, config)
            WaveState.SPAWNING     -> handleSpawning(session, config, event)
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
        spawnTracker[session.id] = mutableMapOf()
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
            val tracker = spawnTracker.getOrPut(session.id) { mutableMapOf() }
            val spawnedSoFar = tracker.getOrDefault(enemy.enemyType, 0)
            val remainingForThisEnemy = enemy.count - spawnedSoFar

            if (remainingForThisEnemy <= 0) continue

            val toSpawn = minOf(availableSlots - spawnedThisTick, remainingForThisEnemy)

            if (toSpawn > 0) {
                LogUtil.debug("[WaveEngine] Spawning $toSpawn x ${enemy.enemyType} (Progress: ${spawnedSoFar + toSpawn}/${enemy.count})")
                executeSpawn(session, enemy, toSpawn, event)
                tracker[enemy.enemyType] = spawnedSoFar + toSpawn
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

    private fun isWaveFullySpawned(sessionId: UUID, waveDef: WaveDefinition): Boolean {
        val tracker = spawnTracker[sessionId] ?: return false
        val isComplete = waveDef.enemies.all { (tracker[it.enemyType] ?: 0) >= it.count }
        LogUtil.debug("[WaveEngine] Wave completion check for $sessionId: $isComplete")
        return isComplete
    }

    private fun executeSpawn(session: ArenaSession, enemy: EnemyDefinition, count: Int, event: SessionStarted) {
        repeat(count) {
            val spawnReturn = npcSpawn.execute(
                event.context,
                event.store,
                event.ref,
                event.playerRef,
                event.world,
                NPC_ROLE.parse(enemy.enemyType, ParseResult()) as BuilderInfo,
                1
            )

            val npcUuidComponent = checkNotNull(
                event.store.getComponent<UUIDComponent?>(
                    spawnReturn.npcRef,
                    UUIDComponent.getComponentType()
                )
            )

            sessionEntities.getOrPut(session.id) { ConcurrentHashMap.newKeySet() }.add(spawnReturn.npc)
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

    fun onEntityDeath(sessionId: UUID, entity: NPCEntity) {
        val removed = sessionEntities[sessionId]?.remove(entity) ?: false
//        if (removed) {
//            LogUtil.debug("[WaveEngine] Entity $entityId removed from tracking for session $sessionId. Remaining: ${getAliveEntityCount(sessionId)}")
//        }
    }

    fun getAliveEntityCount(sessionId: UUID) = sessionEntities[sessionId]?.size ?: 0

    /**
     * Stops a wave session and cleans up all tracked entities.
     */
    fun stopSession(sessionId: UUID) {
        LogUtil.debug("[WaveEngine] Initiating soft despawn for session $sessionId")

        val npcs = sessionEntities.remove(sessionId)
        if (npcs == null || npcs.isEmpty()) {
            spawnTracker.remove(sessionId)
            return
        }

        LogUtil.info("[WaveEngine] Sending despawn signal to ${npcs.size} NPCs.")

        npcs.forEach { npc ->
            if (npc.wasRemoved())
                return@forEach

            val npcUuidComponent = checkNotNull(
                npc.reference?.store?.getComponent<UUIDComponent?>(
                    npc.reference!!,
                    UUIDComponent.getComponentType()
                )
            )
            try {
                npc.setDespawning(true) // The "Clean" Hytale way to remove NPCs
                LogUtil.debug("[WaveEngine] SetDespawning(true) for NPC: ${npc.roleName} - ${npcUuidComponent.uuid}")
            } catch (e: Exception) {
                LogUtil.warn("[WaveEngine] Error despawning NPC ${npc.roleName} - ${npcUuidComponent.uuid}: ${e.message}")
            }
        }

        spawnTracker.remove(sessionId)
    }
}

fun WaveState.isInactive() = this in listOf(WaveState.IDLE, WaveState.STOPPED, WaveState.COMPLETED, WaveState.FAILED)