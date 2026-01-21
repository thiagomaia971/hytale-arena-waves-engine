package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.Universe
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.EntityKilled
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class WaveScheduler(
    private val arenaWavesEngineRepository: ArenaWavesEngineRepository,
    private val waveEngine: WaveEngine) {

    private val activeTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun startSession(event: SessionStarted): Boolean {
        val sessionId = UUID.randomUUID().toString()
        LogUtil.debug("[WaveScheduler] Attempting to start session: $sessionId for map: ${event.waveMapId}")

        if (activeTasks.containsKey(sessionId)) {
            LogUtil.warn("[WaveScheduler] Session $sessionId is already being tracked.")
            return false
        }

        val session = ArenaSession().apply {
            this.id = sessionId
            this.waveMapId = event.waveMapId
            this.state = WaveState.RUNNING
            this.owner = event.playerId
            this.spawnPosition = event.spawnPosition
            this.world = event.world.name
        }

        persistNewSession(session)
        startTask(sessionId, event)
        return true
    }

    fun startTask(sessionId: String, event: SessionStarted) {
        val task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay({
            runTick(sessionId, event)
        }, 0L, 1L, TimeUnit.SECONDS)

        activeTasks[sessionId] = task

        // Register for Hytale's automatic cleanup
        Universe.get().taskRegistry.registerTask(task as ScheduledFuture<Void>)

        LogUtil.info("[WaveScheduler] Successfully started wave task for session $sessionId")
    }

    private fun runTick(sessionId: String, event: SessionStarted) {
        val worldName = event.world.name
        val world = Universe.get().worlds.get(worldName) ?: return

        // Hytale World Thread Context
        world.execute {
            try {
                arenaWavesEngineRepository.loadConfig()
                val session = arenaWavesEngineRepository.getSession(sessionId)

                if (session == null) {
                    LogUtil.debug("[WaveScheduler] Session $sessionId no longer exists in config. Stopping task.")
                    pauseSession(SessionPaused().apply { this.sessionId = sessionId})
                    return@execute
                }

                if (session.state.isInactive()) {
                    LogUtil.debug("[WaveScheduler] Session $sessionId reached terminal state ${session.state}. Cleaning up task.")
                    pauseSession(SessionPaused().apply { this.sessionId = sessionId})
                    return@execute
                }

                waveEngine.processTick(sessionId, session, event)
                arenaWavesEngineRepository.save()

            } catch (e: Exception) {
                LogUtil.severe("[WaveScheduler] Critical error in tick for $sessionId: ${e.stackTraceToString()}")
                pauseSession(SessionPaused().apply { this.sessionId = sessionId})
            }
        }
    }

    private fun persistNewSession(session: ArenaSession) {
        arenaWavesEngineRepository.addSession(session)
        arenaWavesEngineRepository.save()
    }

    fun pauseSession(event: SessionPaused) {
        var sessions = arrayOf<String>()
        if (event.sessionId != null)
            sessions += event.sessionId!!
        else
            sessions = activeTasks.keys.toTypedArray()

        for (sessionId in sessions) {
            val task = activeTasks.remove(sessionId)
            if (task != null) {
                val cancelled = task.cancel(false)
                LogUtil.info("[WaveScheduler] Task for ${sessionId} cancelled: $cancelled")

                waveEngine.stopSession(sessionId, event.despawn)
            } else {
                LogUtil.debug("[WaveScheduler] No active task found for ${sessionId} during stop request.")
            }
        }
    }

    fun onEntityDeath(event: EntityKilled){
        waveEngine.onEntityDeath(event.entityId)
    }

    fun shutdown() {
        LogUtil.debug("[WaveScheduler] Shutting down. Cleaning up ${activeTasks.size} tasks.")
        activeTasks.keys.forEach { pauseSession(SessionPaused().apply { this.sessionId = it; this.despawn = false } ) }
        activeTasks.clear()
    }

    // Helper visibility methods
    fun isSessionActive(sessionId: String) = activeTasks.containsKey(sessionId)
    fun getActiveTaskCount() = activeTasks.size
}