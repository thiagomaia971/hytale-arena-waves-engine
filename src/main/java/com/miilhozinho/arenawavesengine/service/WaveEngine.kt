package com.miilhozinho.arenawavesengine.service

import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.ArenaMapDefinition
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.config.EnemyDefinition
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * WaveEngine handles the core wave logic and spawn throttling for arena sessions.
 *
 * This service manages:
 * - Wave state transitions (IDLE → RUNNING → SPAWNING → WAITING_CLEAR → COMPLETED)
 * - Spawn throttling to prevent server lag
 * - Entity tracking per session
 * - Session state persistence
 */
class WaveEngine(
    public val plugin: ArenaWavesEngine
) {

    // Track entities spawned per session for cleanup
    private val sessionEntities = ConcurrentHashMap<UUID, MutableSet<UUID>>()

    /**
     * Processes one tick of wave logic for a session.
     * Called every second by WaveScheduler.
     */
    fun processTick(sessionId: UUID) {
        val config = plugin.config?.get() ?: return
        val session = config.sessions.find { it.id == sessionId } ?: return

        // Skip processing if session is not in an active state
        if (session.state == WaveState.IDLE || session.state == WaveState.STOPPED ||
            session.state == WaveState.COMPLETED || session.state == WaveState.FAILED) {
            return
        }

        when (session.state) {
            WaveState.RUNNING -> processRunningState(session, config)
            WaveState.SPAWNING -> processSpawningState(session, config)
            WaveState.WAITING_CLEAR -> processWaitingClearState(session, config)
            else -> {
                LogUtil.warn("[WaveEngine] Unexpected state ${session.state} for session $sessionId")
            }
        }
    }

    /**
     * Starts a new wave session.
     */
    fun startSession(session: ArenaSession, mapDefinition: ArenaMapDefinition): ArenaSession {
        // Initialize entity tracking for this session
        sessionEntities[session.id] = ConcurrentHashMap.newKeySet()

        // Update session with map info and start state
        val updatedSession = ArenaSession().apply {
            id = session.id
            owner = session.owner
            center = session.center
            state = WaveState.RUNNING
            currentWave = 0 // Start at wave 0
            aliveEntityIds = emptySet()
            waveMapId = mapDefinition.id
            startTime = System.currentTimeMillis()
        }

        LogUtil.info("[WaveEngine] Started session ${session.id} with map ${mapDefinition.id}")
        return updatedSession
    }

    /**
     * Stops a wave session and cleans up entities.
     */
    fun stopSession(sessionId: UUID) {
        // Clean up entity tracking
        sessionEntities.remove(sessionId)

        // TODO: Implement entity cleanup/removal logic
        LogUtil.info("[WaveEngine] Stopped session $sessionId")
    }

    private fun processRunningState(session: ArenaSession, config: com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig) {
        // Find the map definition
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId }
        if (mapDef == null) {
            LogUtil.severe("[WaveEngine] Map definition ${session.waveMapId} not found for session ${session.id}")
            updateSessionState(session.id, WaveState.FAILED)
            return
        }

        // Check if we have waves left
        if (session.currentWave >= mapDef.waves.size) {
            // All waves completed
            updateSessionState(session.id, WaveState.COMPLETED)
            return
        }

        // Start spawning current wave
        updateSessionState(session.id, WaveState.SPAWNING)
    }

    private fun processSpawningState(session: ArenaSession, config: ArenaWavesEngineConfig) {
        val mapDef = config.arenaMaps.find { it.id == session.waveMapId } ?: return
        val currentWaveDef = mapDef.waves.getOrNull(session.currentWave) ?: return

        // Check spawn throttling (max concurrent mobs per session)
        val currentAliveCount = sessionEntities[session.id]?.size ?: 0
        val maxConcurrent = config.maxConcurrentMobsPerSession

        if (currentAliveCount >= maxConcurrent) {
            // Too many mobs alive, wait for some to die
            return
        }

        // Spawn enemies for this wave (throttled)
        val enemiesToSpawn = currentWaveDef.enemies.flatMap { enemyDef ->
            // Calculate how many of this enemy type to spawn this tick
            val remainingToSpawn = calculateRemainingSpawns(session.id, enemyDef)
            val canSpawnThisTick = (maxConcurrent - currentAliveCount).coerceAtMost(remainingToSpawn)

            if (canSpawnThisTick > 0) {
                spawnEnemies(enemyDef, canSpawnThisTick, session)
            }
            emptyList() // We'll handle spawning in spawnEnemies
        }

        // Check if wave is complete (all enemies spawned and waiting to clear)
        if (isWaveComplete(session.id, currentWaveDef)) {
            updateSessionState(session.id, WaveState.WAITING_CLEAR)
        }
    }

    private fun processWaitingClearState(session: ArenaSession, config: com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig) {
        // Check if all enemies are dead
        val aliveEntities = sessionEntities[session.id] ?: emptySet()
        if (aliveEntities.isEmpty()) {
            // Wave cleared, advance to next wave
            val nextWave = session.currentWave + 1
            updateSessionCurrentWave(session.id, nextWave)

            // Check if this was the last wave
            val mapDef = config.arenaMaps.find { it.id == session.waveMapId }
            if (mapDef != null && nextWave >= mapDef.waves.size) {
                updateSessionState(session.id, WaveState.COMPLETED)
            } else {
                updateSessionState(session.id, WaveState.RUNNING)
            }
        }
    }

    private fun calculateRemainingSpawns(sessionId: UUID, enemyDef: EnemyDefinition): Int {
        // TODO: Track spawned count per enemy type per session
        // For now, assume we need to spawn all
        return enemyDef.count
    }

    private fun spawnEnemies(enemyDef: EnemyDefinition, count: Int, session: ArenaSession): List<UUID> {
        val spawnedIds = mutableListOf<UUID>()

        // TODO: Implement actual spawning logic using NpcSpawn
        // This would need access to CommandContext, Store, Ref, PlayerRef, World
        // For now, just simulate spawning by adding to tracking

        for (i in 0 until count) {
            val fakeEntityId = UUID.randomUUID()
            spawnedIds.add(fakeEntityId)

            // Track spawned entity
            sessionEntities[session.id]?.add(fakeEntityId)
        }

        LogUtil.info("[WaveEngine] Spawned $count ${enemyDef.enemyType} for session ${session.id}")
        return spawnedIds
    }

    private fun isWaveComplete(sessionId: UUID, waveDef: com.miilhozinho.arenawavesengine.config.WaveDefinition): Boolean {
        // TODO: Check if all enemies for this wave have been spawned
        // For now, assume waves complete immediately
        return true
    }

    /**
     * Updates session state in the configuration.
     */
    private fun updateSessionState(sessionId: UUID, newState: WaveState) {
        // This would need to update the config and persist it
        // For now, just log the state change
        LogUtil.info("[WaveEngine] Session $sessionId state changed to $newState")
    }

    /**
     * Updates current wave for a session.
     */
    private fun updateSessionCurrentWave(sessionId: UUID, newWave: Int) {
        LogUtil.info("[WaveEngine] Session $sessionId advanced to wave $newWave")
    }

    /**
     * Called when an entity dies to update tracking.
     */
    fun onEntityDeath(sessionId: UUID, entityId: UUID) {
        sessionEntities[sessionId]?.remove(entityId)
    }

    /**
     * Gets the count of alive entities for a session.
     */
    fun getAliveEntityCount(sessionId: UUID): Int {
        return sessionEntities[sessionId]?.size ?: 0
    }
}
