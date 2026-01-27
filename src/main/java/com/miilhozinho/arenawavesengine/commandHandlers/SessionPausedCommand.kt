package com.miilhozinho.arenawavesengine.commandHandlers

import com.hypixel.hytale.server.core.HytaleServer
import com.miilhozinho.arenawavesengine.events.HudHided
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to pause/stop a wave session.
 * Contains all the business logic for session cleanup and stopping.
 */
class SessionPausedCommand(
    private val repository: ArenaWavesEngineRepository,
    private val sessionRepository: ArenaSessionRepository,
    private val waveEngine: WaveEngine,
    private val event: SessionPaused
) : BaseWaveCommand<Unit>(priority = CommandPriority.NORMAL) {

    override fun execute(): CommandResult<Unit> {
        return try {
            val sessionsToPause = if (event.sessionId != null) {
                arrayOf(event.sessionId!!)
            } else {
                sessionRepository.getActiveSessions().map { it.id }.toTypedArray()
            }

            for (sessionId in sessionsToPause) {
                val session = sessionRepository.getSession(sessionId) ?: continue

                session.scheduledTask?.cancel(false)
                session.scheduledTask = null
                LogUtil.debug("[SessionPausedCommand] Cancelled scheduled task for session $sessionId")

                // Stop wave processing - delegate to WaveEngine
                waveEngine.stopSession(sessionId, event.despawn)

                // Save configuration
                sessionRepository.saveSession(session, SessionPaused::class.simpleName)

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
