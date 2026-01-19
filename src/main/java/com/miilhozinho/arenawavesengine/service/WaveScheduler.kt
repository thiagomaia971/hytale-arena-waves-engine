package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.Universe
import com.miilhozinho.arenawavesengine.ArenaWavesEngine.Companion.configState
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.apply

class WaveScheduler(private val waveEngine: WaveEngine) {

    private val activeTasks = ConcurrentHashMap<UUID, ScheduledFuture<*>>()

    fun startSession(event: SessionStarted): Boolean {
        val sessionId = UUID.randomUUID() // Ensure unique ID for this instance
        LogUtil.debug("[WaveScheduler] Attempting to start session: $sessionId for map: ${event.waveMapId}")

        if (activeTasks.containsKey(sessionId)) {
            LogUtil.warn("[WaveScheduler] Session $sessionId is already being tracked.")
            return false
        }

        val session = ArenaSession().apply {
            this.id = sessionId
            this.waveMapId = event.waveMapId
            this.state = WaveState.RUNNING
            this.owner = event.playerRef.uuid
        }

        // Initialize session in config
        persistNewSession(session)

        // Schedule the task and store it atomically
        val task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay({
            runTick(sessionId, event)
        }, 0L, 1L, TimeUnit.SECONDS)

        activeTasks[sessionId] = task

        // Register for Hytale's automatic cleanup
        waveEngine.plugin.taskRegistry.registerTask(task as ScheduledFuture<Void>)

        LogUtil.info("[WaveScheduler] Successfully started wave task for session $sessionId")
        return true
    }

    private fun runTick(sessionId: UUID, event: SessionStarted) {
        val worldName = event.world.name
        val world = Universe.get().worlds.get(worldName) ?: return

        // Hytale World Thread Context
        world.execute {
            try {
                val currentConfig = configState?.get() ?: return@execute
                val session = currentConfig.sessions.find { it.id == sessionId }

                if (session == null) {
                    LogUtil.debug("[WaveScheduler] Session $sessionId no longer exists in config. Stopping task.")
                    pauseSession(SessionPaused().apply { this.sessionId = sessionId})
                    return@execute
                }

                // If session finished naturally, clean up the scheduler task
                if (session.state.isInactive()) {
                    LogUtil.debug("[WaveScheduler] Session $sessionId reached terminal state ${session.state}. Cleaning up task.")
                    pauseSession(SessionPaused().apply { this.sessionId = sessionId})
                    return@execute
                }

                LogUtil.debug("[WaveScheduler] Processing tick for $sessionId in world $worldName")
                waveEngine.processTick(sessionId, currentConfig, event)

            } catch (e: Exception) {
                LogUtil.severe("[WaveScheduler] Critical error in tick for $sessionId: ${e.stackTraceToString()}")
                pauseSession(SessionPaused().apply { this.sessionId = sessionId})
            }
        }
    }

    private fun persistNewSession(session: ArenaSession) {
        configState?.apply {
            val config = get() ?: return@apply
            // Thread-safe update to the session list
            config.sessions = config.sessions + session
            save()
            LogUtil.debug("[WaveScheduler] Session ${session.id} persisted to config.")
        }
    }

    fun pauseSession(event: SessionPaused): Boolean {
        val task = activeTasks.remove(event.sessionId)
        return if (task != null) {
            val cancelled = task.cancel(false)
            LogUtil.info("[WaveScheduler] Task for ${event.sessionId} cancelled: $cancelled")

            // Optional: Tell engine to clean up entities
            waveEngine.stopSession(event.sessionId)
            true
        } else {
            LogUtil.debug("[WaveScheduler] No active task found for ${event.sessionId} during stop request.")
            false
        }
    }

    fun shutdown() {
        LogUtil.debug("[WaveScheduler] Shutting down. Cleaning up ${activeTasks.size} tasks.")
        activeTasks.keys.forEach { pauseSession(SessionPaused().apply { this.sessionId = it}) }
        activeTasks.clear()
    }

    // Helper visibility methods
    fun isSessionActive(sessionId: UUID) = activeTasks.containsKey(sessionId)
    fun getActiveTaskCount() = activeTasks.size
}