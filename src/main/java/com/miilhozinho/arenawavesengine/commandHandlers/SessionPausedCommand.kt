package com.miilhozinho.arenawavesengine.commandHandlers

import com.hypixel.hytale.server.core.HytaleServer
import com.miilhozinho.arenawavesengine.events.HudHided
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to pause/stop a wave session.
 * Contains all the business logic for session cleanup and stopping.
 */
class SessionPausedCommand(
    private val repository: ArenaWavesEngineRepository,
    private val waveEngine: WaveEngine,
    private val event: SessionPaused
) : BaseWaveCommand<Unit>(priority = CommandPriority.NORMAL) {

    override fun execute(): CommandResult<Unit> {
        return try {
            val sessionsToPause = if (event.sessionId != null) {
                arrayOf(event.sessionId!!)
            } else {
                repository.getActiveSessions().map { it.id }.toTypedArray()
            }

            for (sessionId in sessionsToPause) {
                val session = repository.getSession(sessionId)
                if (session != null) {
                    // Cancel the scheduled task to stop periodic processing
                    session.scheduledTask?.cancel(false)
                    session.scheduledTask = null
                    LogUtil.debug("[SessionPausedCommand] Cancelled scheduled task for session $sessionId")
                }

                // Stop wave processing - delegate to WaveEngine
                waveEngine.stopSession(sessionId, event.despawn)

                // Save configuration
                repository.save(forceSave = true)

                // Dispatch HUD hide event
                HytaleServer.get().eventBus.dispatchFor(HudHided::class.java)
                    .dispatch(HudHided().apply { this.sessionId = sessionId })

                LogUtil.info("[SessionPausedCommand] Session $sessionId paused/stopped")
            }

            CommandResult.Success(Unit)

        } catch (e: Exception) {
            LogUtil.severe("[SessionPausedCommand] Failed to pause session(s): ${e.message}")
            CommandResult.Failure("Failed to pause session: ${e.message}")
        }
    }
}
