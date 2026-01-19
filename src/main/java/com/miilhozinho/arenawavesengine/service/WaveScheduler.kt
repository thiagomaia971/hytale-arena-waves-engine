package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.Universe
import com.miilhozinho.arenawavesengine.ArenaWavesEngine.Companion.configState
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * WaveScheduler manages scheduled tasks for arena wave sessions using Hytale's TaskRegistry.
 *
 * This service ensures:
 * - One scheduled task per active session (avoiding per-mob timers)
 * - Clean task cancellation on session stop/shutdown
 * - TaskRegistry integration for automatic cleanup
 */
class WaveScheduler(
    private val waveEngine: WaveEngine
) {

    // Track session UUID -> ScheduledFuture mapping
    private val activeTasks = ConcurrentHashMap<UUID, ScheduledFuture<*>>()

    /**
     * Starts a wave session by creating exactly one scheduled task.
     * The task runs every second and processes wave logic for the session.
     */
    fun startSession(event: SessionStarted): Boolean {
        val session = ArenaSession().apply {
            this.waveMapId = event.waveMapId
            this.state = WaveState.RUNNING
        }
        val sessionId = session.id
        LogUtil.debug("Session started: $sessionId")

        // Check if session already has an active task
        if (activeTasks.containsKey(sessionId)) {
            LogUtil.warn("[WaveScheduler] Session $sessionId already has an active task")
            return false
        }

        saveSession(session)

        // Create repeating task (runs every 1 second)
        val task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay({
            val world = Universe.get().worlds.get(event.world.name)
            world!!.execute {
                try {
                    LogUtil.debug("[WaveScheduler] WaveScheduler started for ${event.world.name}")
                    waveEngine.processTick(sessionId, event)
                } catch (e: Exception) {
                    LogUtil.severe("[WaveScheduler] Error processing tick for session $sessionId: ${e.message}")
                    // Stop the session on critical errors
                    stopSession(sessionId)
                }
            }
        }, 0L, 1L, TimeUnit.SECONDS)

        // Register with TaskRegistry for automatic cleanup on shutdown
        waveEngine.plugin.taskRegistry.registerTask(task as ScheduledFuture<Void>)
        LogUtil.debug("WaveScheduler started for session $sessionId")

        // Track the task
        activeTasks[sessionId] = task

        LogUtil.info("[WaveScheduler] Started wave task for session $sessionId")
        return true
    }

    private fun saveSession(session: ArenaSession) {
        var config = configState?.get()
        config?.sessions += session
        configState?.save()
    }

    /**
     * Stops a wave session by cancelling its scheduled task.
     */
    fun stopSession(sessionId: UUID): Boolean {
        val task = activeTasks.remove(sessionId)
        return if (task != null) {
            task.cancel(false) // Cancel but don't interrupt if running
            LogUtil.info("[WaveScheduler] Stopped wave task for session $sessionId")
            true
        } else {
            LogUtil.warn("[WaveScheduler] No active task found for session $sessionId")
            false
        }
    }

    /**
     * Checks if a session has an active scheduled task.
     */
    fun isSessionActive(sessionId: UUID): Boolean {
        return activeTasks.containsKey(sessionId)
    }

    /**
     * Gets all currently active session IDs.
     */
    fun getActiveSessionIds(): Set<UUID> {
        return activeTasks.keys.toSet()
    }

    /**
     * Cancels all active tasks. Called during plugin shutdown.
     * Note: TaskRegistry handles automatic cancellation, but this provides explicit control.
     */
    fun shutdown() {
        val sessionCount = activeTasks.size
        activeTasks.values.forEach { task ->
            task.cancel(false)
        }
        activeTasks.clear()

        if (sessionCount > 0) {
            LogUtil.info("[WaveScheduler] Shutdown completed, cancelled $sessionCount wave tasks")
        }
    }

    /**
     * Gets the number of currently active tasks.
     */
    fun getActiveTaskCount(): Int {
        return activeTasks.size
    }
}
