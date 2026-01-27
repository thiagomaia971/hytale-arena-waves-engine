package com.miilhozinho.arenawavesengine.service

import com.google.gson.Gson
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.ParseResult
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo
import com.hypixel.hytale.server.npc.commands.NPCCommand.NPC_ROLE
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.ArenaWavesEngine.Companion.eventRepository
import com.miilhozinho.arenawavesengine.commandHandlers.SessionPausedCommand
import com.miilhozinho.arenawavesengine.commandHandlers.WaveTickCommand
import com.miilhozinho.arenawavesengine.components.EnemyComponent
import com.miilhozinho.arenawavesengine.config.*
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionUpdated
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*

class WaveEngine(
    val arenaWavesEngineRepository: ArenaWavesEngineRepository,
    val sessionRepository: ArenaSessionRepository,) {

    private val npcSpawn = NpcSpawn()

    fun prepareWave(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to prepare wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED, WaveTickCommand::class.simpleName!!)
            return
        }

        if (session.currentWave >= arenaMapDefinition.waves.size) {
            LogUtil.debug("[WaveEngine] All waves (${arenaMapDefinition.waves.size}) completed for session ${session.id}")
            transitionTo(session, WaveState.COMPLETED, WaveTickCommand::class.simpleName!!)
            return
        }

        LogUtil.debug("[WaveEngine] Preparing wave ${session.currentWave} for session ${session.id}")
        session.currentWaveSpawnProgress.clear()

        val waveData = session.getOrCreateCurrentWave()
        waveData.startTime = System.currentTimeMillis()
        transitionTo(session, WaveState.SPAWNING, WaveTickCommand::class.simpleName!!)
    }

    fun handleSpawning(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?, event: SessionStarted) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to spawn wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED, WaveTickCommand::class.simpleName!!)
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
            transitionTo(session, WaveState.WAITING_CLEAR, WaveTickCommand::class.simpleName!!)
        }
    }

    fun checkWaveCleared(session: ArenaSession) {
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

            transitionTo(session, WaveState.WAITING_INTERVAL, WaveTickCommand::class.simpleName!!)
        }
    }

    fun checkIntervalElapsed(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to check interval: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED, WaveTickCommand::class.simpleName!!)
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

    private fun markToNextWave(session: ArenaSession, arenaMapDefinition: ArenaMapDefinition?) {
        if (arenaMapDefinition == null) {
            LogUtil.severe("[WaveEngine] Failed to mark to next wave: Map ${session.waveMapId} not found")
            transitionTo(session, WaveState.FAILED, WaveTickCommand::class.simpleName!!)
            return
        }

        if (session.currentWave + 1 < arenaMapDefinition.waves.size) {
            session.currentWave++
            transitionTo(session, WaveState.RUNNING, WaveTickCommand::class.simpleName!!)
        } else {
            transitionTo(session, WaveState.COMPLETED, WaveTickCommand::class.simpleName!!)
        }
    }

    fun checkCompleted(session: ArenaSession) {
        session.currentWaveSpawnProgress.clear()
        arenaWavesEngineRepository.markToSave()
    }

    private fun isWaveFullySpawned(session: ArenaSession, waveDef: WaveDefinition): Boolean {
        val isComplete = waveDef.enemies.all { (session.currentWaveSpawnProgress[it.enemyType] ?: 0) >= it.count }
        LogUtil.debug("[WaveEngine] Wave completion check for ${session.id}: $isComplete")
        return isComplete
    }

    private fun executeSpawn(session: ArenaSession, enemy: EnemyDefinition, count: Int, event: SessionStarted) {
        repeat(count) {
            event.world.execute {
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


                spawnReturn.npc.reference!!.store.addComponent(spawnReturn.npc.reference!!, ArenaWavesEngine.enemyComponentType,
                    EnemyComponent().apply {
                        this.entityRoleName = enemy.enemyType
                        this.entityId =  npcUuidComponent.uuid.toString()
                        this.sessionId = session.id
                        this.world = session.world
                    }
                )

                spawnReturn.npc.reference!!.store.saveAllResources()

                session.activeEntities += npcUuidComponent.uuid.toString()
                LogUtil.debug("[WaveEngine] Entity ${npcUuidComponent.uuid} tracked for session ${session.id}")
            }
        }
    }

    fun getAliveEntityCount(session: ArenaSession): Int {
        return session.activeEntities.size
    }

    /**
     * Stops a wave session and cleans up all tracked entities.
     */
    fun stopSession(sessionId: String, despawn: Boolean) {
        LogUtil.debug("[WaveEngine] Initiating soft despawn for session $sessionId")

        val session = sessionRepository.getSession(sessionId)
        if (session == null) {
            LogUtil.warn("[WaveEngine] Error stop Session ${sessionId} not found")
            return
        }

        val activeEntities = session.activeEntities
        session.currentWaveSpawnProgress.clear()

        if (despawn) {
            LogUtil.info("[WaveEngine] Sending despawn signal to ${activeEntities.size} NPCs.")
            val world = Universe.get().getWorld(session.world) ?: return
            activeEntities.forEach { entityId ->
                world.execute {
                    try {
                        val entityRef = world.getEntityRef(UUID.fromString(entityId))
                        val store = entityRef?.store!!
                        val npc = store.getComponent<NPCEntity>(entityRef, NPCEntity.getComponentType()!!) as NPCEntity

                        if (npc.wasRemoved())
                            return@execute

                        npc.setToDespawn()
                        LogUtil.debug("[WaveEngine] SetDespawning(true) for NPC: ${npc.roleName} - $entityId")
                    } catch (e: Exception) {
                        LogUtil.warn("[WaveEngine] Error despawning NPC $entityId: ${e.message}")
                    }
                }
            }
        }

        session.activeEntities = emptyArray()
        session.currentWaveSpawnProgress.clear()
        transitionTo(session, WaveState.STOPPED, SessionPausedCommand::class.simpleName!!)
    }

    private fun transitionTo(session: ArenaSession, newState: WaveState, eventType: String) {
        if (session.state == newState) return
        val oldState = session.state
        session.state = newState
        LogUtil.info("[WaveEngine] Session ${session.id} state transition: $oldState -> $newState")
        eventRepository.addLog(session.id, EventDataLog().apply {
            this.event = eventType
            this.oldState = oldState.toString()
            this.newState = newState.toString()
            this.newSession = Gson().toJson(session)
        })
        sessionRepository.markToSave()
    }

    fun onEntityDeath(sessionId: String, entityId: String, entityRoleName: String) {
        val session = sessionRepository.getSession(sessionId)

        if (session == null) {
            LogUtil.warn("[WaveEngine] Error save entity death on Session ${sessionId} not found")
            return
        }

        session.activeEntities = session.activeEntities.filter { it != entityId }.toTypedArray()

        val world = Universe.get().worlds[session.world] ?: return
        world.execute {
            val waveData = session.getOrCreateCurrentWave()
            waveData.increaseKilledEnemy(entityRoleName, entityId)

            sessionRepository.markToSave()
            sessionRepository.saveSession(session)

            HytaleServer.get().eventBus.dispatchFor(SessionUpdated::class.java)
                .dispatch(SessionUpdated(session))

            LogUtil.debug("[WaveEngine] Entity $entityId removed from tracking for session $sessionId. Remaining: ${getAliveEntityCount(session)}")
        }
    }

    fun onDamageDealt(victimId: String, attackerId: String, damage: Float) {
//        val sessionId = arenaWavesEngineRepository.get().entityToSessionMap[victimId] ?: return
//        val session = arenaWavesEngineRepository.getSession(sessionId) ?: return
//
//        val currentWave = session.currentWave
//        val waveData = session.wavesData.getOrPut(currentWave) { WaveCurrentData() }
//        val currentTotal = waveData.damage.getOrDefault(attackerId, 0.0) as Float
//        waveData.damage[attackerId] = currentTotal + damage

        LogUtil.debug("[WaveEngine] Recorded $damage damage for player $attackerId in wave")
    }
}

fun WaveState.isInactive() = this in listOf(WaveState.IDLE, WaveState.STOPPED, WaveState.COMPLETED, WaveState.FAILED)
