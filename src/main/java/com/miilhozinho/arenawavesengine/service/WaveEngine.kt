package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo
import com.hypixel.hytale.server.npc.commands.NPCCommand.NPC_ROLE
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.miilhozinho.arenawavesengine.config.*
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*

class WaveEngine(val arenaWavesEngineRepository: ArenaWavesEngineRepository) {

    private val npcSpawn = NpcSpawn()

    fun processTick(sessionId: String, session: ArenaSession, event: SessionStarted) {
        if (session.state.isInactive()) {
            LogUtil.debug("[WaveEngine] Tick skipped: Session $sessionId is in inactive state ${session.state}")
            return
        }
        val arenaMapDefinition = arenaWavesEngineRepository.findArenaMapDefinition(session.waveMapId)

        when (session.state) {
            WaveState.RUNNING         -> prepareWave(session, arenaMapDefinition)
            WaveState.SPAWNING        -> handleSpawning(session, arenaMapDefinition, event)
            WaveState.WAITING_CLEAR   -> checkWaveCleared(session)
            WaveState.WAITING_INTERVAL -> checkIntervalElapsed(session, arenaMapDefinition)
            WaveState.COMPLETED        -> checkCompleted(session)
            else -> LogUtil.warn("[WaveEngine] Unhandled state ${session.state} for $sessionId")
        }
    }

    private fun prepareWave(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to prepare wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED)
            return
        }

        if (session.currentWave >= arenaMapDefinition.waves.size) {
            LogUtil.debug("[WaveEngine] All waves (${arenaMapDefinition.waves.size}) completed for session ${session.id}")
            transitionTo(session, WaveState.COMPLETED)
            return
        }

        LogUtil.debug("[WaveEngine] Preparing wave ${session.currentWave} for session ${session.id}")
        session.currentWaveSpawnProgress.clear()
        val waveData = session.wavesData.getOrPut(session.currentWave) { WaveCurrentData() }
        waveData.startTime = System.currentTimeMillis()
        transitionTo(session, WaveState.SPAWNING)
    }

    private fun handleSpawning(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?, event: SessionStarted) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to spawn wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED)
            return
        }

        val waveDef = arenaMapDefinition.waves.getOrNull(session.currentWave) ?: return

        val aliveCount = getAliveEntityCount(session)
        val maxConcurrent = arenaWavesEngineRepository.get().maxConcurrentMobsPerSession
        val availableSlots = maxConcurrent - aliveCount

        LogUtil.debug("[WaveEngine] Spawning check for wave ${session.currentWave}: Alive=$aliveCount, Max=$maxConcurrent, Available=$availableSlots")

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

        if (isWaveFullySpawned(session, waveDef)) {
            LogUtil.debug("[WaveEngine] Wave ${session.currentWave} fully spawned for ${session.id}. Moving to clear phase.")
            transitionTo(session, WaveState.WAITING_CLEAR)
        }
    }

    private fun checkWaveCleared(
        session: ArenaSession
    ) {
        if (session.activeEntities.isEmpty()) {
            val now = System.currentTimeMillis()
            LogUtil.debug("[WaveEngine] Wave ${session.currentWave} cleared for session ${session.id}. Starting interval wait.")

            val waveData = session.wavesData[session.currentWave]
            if (waveData != null) {
                waveData.clearTime = now
                val durationSeconds = ((now - waveData.startTime) / 1000).toInt()
                waveData.duration = durationSeconds
                LogUtil.info("[WaveEngine] Wave ${session.currentWave} cleared in $durationSeconds seconds.")
            }

            transitionTo(session, WaveState.WAITING_INTERVAL)
        }
    }

    private fun checkIntervalElapsed(
        session: ArenaSession,
        arenaMapDefinition: ArenaMapDefinition?
    ) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to check interval: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED)
            return
        }

        val waveDef = arenaMapDefinition.waves.getOrNull(session.currentWave) ?: return

        val waveData = session.wavesData[session.currentWave]
        val waveClearTime = waveData?.clearTime ?: 0L
        val elapsedTimeMs = System.currentTimeMillis() - waveClearTime
        val requiredIntervalMs = waveDef.interval * 1000L

        LogUtil.debug("[WaveEngine] Interval check: Elapsed=${elapsedTimeMs}ms, Required=${requiredIntervalMs}ms for session ${session.id}")

        if (elapsedTimeMs >= requiredIntervalMs) {
            LogUtil.debug("[WaveEngine] Interval elapsed for wave ${session.currentWave} in session ${session.id}. Starting next wave.")
            markToNextWave(session, arenaMapDefinition)
        }
    }

    private fun markToNextWave(
        session: ArenaSession,
        arenaMapDefinition: ArenaMapDefinition?
    ) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to mark to next wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED)
            return
        }

        if (session.currentWave <= arenaMapDefinition.waves.size) {
            session.currentWave++
            transitionTo(session, WaveState.RUNNING)
        } else {
            transitionTo(session, WaveState.COMPLETED)
        }
    }

    private fun checkCompleted(session: ArenaSession) {
        session.currentWaveSpawnProgress.clear()
    }

    private fun isWaveFullySpawned(session: ArenaSession, waveDef: WaveDefinition): Boolean {
        val isComplete = waveDef.enemies.all { (session.currentWaveSpawnProgress[it.enemyType] ?: 0) >= it.count }
        LogUtil.debug("[WaveEngine] Wave completion check for ${session.id}: $isComplete")
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
            arenaWavesEngineRepository.get().entityToSessionMap[npcUuidComponent.uuid.toString()] = session.id
            LogUtil.debug("[WaveEngine] Entity ${npcUuidComponent.uuid} tracked for session ${session.id}")
        }
    }

    private fun transitionTo(session: ArenaSession, newState: WaveState) {
        if (session.state == newState) return
        val oldState = session.state
        session.state = newState
        LogUtil.info("[WaveEngine] Session ${session.id} state transition: $oldState -> $newState")
    }


    fun getAliveEntityCount(session: ArenaSession): Int {
        return session.activeEntities.size
    }

    /**
     * Stops a wave session and cleans up all tracked entities.
     */
    fun stopSession(sessionId: String, despawn: Boolean) {
        LogUtil.debug("[WaveEngine] Initiating soft despawn for session $sessionId")

        val session = arenaWavesEngineRepository.getSession(sessionId)
        if (session == null) {
            LogUtil.warn("[WaveEngine] Error stop Session ${sessionId} not found")
            return
        }

        val activeEntities = session.activeEntities
        session.currentWaveSpawnProgress.clear()

        if (despawn) {
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
                    arenaWavesEngineRepository.get().entityToSessionMap.remove(entityId)
                    LogUtil.debug("[WaveEngine] SetDespawning(true) for NPC: ${npc.roleName} - $entityId")
                } catch (e: Exception) {
                    LogUtil.warn("[WaveEngine] Error despawning NPC ${npc.roleName} - $entityId: ${e.message}")
                }
            }
        }

        session.activeEntities = emptyArray()
        session.currentWaveSpawnProgress.clear()
        transitionTo(session, WaveState.STOPPED)
    }

    fun onEntityDeath(entityId: String) {
        val sessionId = arenaWavesEngineRepository.get().entityToSessionMap[entityId] ?: return
        val session = arenaWavesEngineRepository.getSession(sessionId)

        if (session == null) {
            LogUtil.warn("[WaveEngine] Error save entity death on Session ${sessionId} not found")
            return
        }

        session.activeEntities = session.activeEntities.filter { it != entityId }.toTypedArray()
        arenaWavesEngineRepository.get().entityToSessionMap.remove(entityId)
        arenaWavesEngineRepository.save()
        LogUtil.debug("[WaveEngine] Entity $entityId removed from tracking for session $sessionId. Remaining: ${getAliveEntityCount(session)}")
        if (getAliveEntityCount(session) == 0)
            transitionTo(session, WaveState.WAITING_INTERVAL)
    }

    fun onDamageDealt(victimId: String, attackerId: String, damage: Float) {
        val sessionId = arenaWavesEngineRepository.get().entityToSessionMap[victimId] ?: return
        val session = arenaWavesEngineRepository.getSession(sessionId) ?: return

        val currentWave = session.currentWave
        val waveData = session.wavesData.getOrPut(currentWave) { WaveCurrentData() }
        val currentTotal = waveData.damage.getOrDefault(attackerId, 0.0) as Float
        waveData.damage[attackerId] = currentTotal + damage

        LogUtil.debug("[WaveEngine] Recorded $damage damage for player $attackerId in wave $currentWave (Session: $sessionId)")
    }
}

fun WaveState.isInactive() = this in listOf(WaveState.IDLE, WaveState.STOPPED, WaveState.COMPLETED, WaveState.FAILED)
