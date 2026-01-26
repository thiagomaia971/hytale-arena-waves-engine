package com.miilhozinho.arenawavesengine.commandHandlers

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.Universe
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionUpdated
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to process a single wave tick for a session.
 * Orchestrates wave processing through WaveEngine methods.
 */
class WaveTickCommand(
    private val waveEngine: WaveEngine,
    private val repository: ArenaWavesEngineRepository,
    private val sessionId: String,
    private val event: SessionStarted
) : BaseWaveCommand<Unit>(priority = CommandPriority.NORMAL) {

    override fun execute(): CommandResult<Unit> {
//        val worldName = event.world.name
//        val world = Universe.get().worlds.get(worldName) ?: return CommandResult.Success(Unit)

        return try {
            repository.loadConfig()
            val session = repository.getSession(sessionId)

            if (session == null) {
                LogUtil.debug("[WaveTickCommand] Session $sessionId no longer exists. Skipping tick.")
                return CommandResult.Success(Unit)
            }

            if (session.state.isInactive()) {
                LogUtil.debug("[WaveTickCommand] Tick skipped: Session $sessionId is in inactive state ${session.state}")
                session.scheduledTask?.cancel(true)
                session.scheduledTask = null
                return CommandResult.Success(Unit)
            }

            val arenaMapDefinition = repository.findArenaMapDefinition(session.waveMapId)

            when (session.state) {
                WaveState.RUNNING         -> waveEngine.prepareWave(session, arenaMapDefinition)
                WaveState.SPAWNING        -> waveEngine.handleSpawning(session, arenaMapDefinition, event)
                WaveState.WAITING_CLEAR   -> waveEngine.checkWaveCleared(session)
                WaveState.WAITING_INTERVAL -> waveEngine.checkIntervalElapsed(session, arenaMapDefinition)
                WaveState.COMPLETED        -> waveEngine.checkCompleted(session)
                else -> LogUtil.warn("[WaveTickCommand] Unhandled state ${session.state} for $sessionId")
            }

            if (repository.save())
                HytaleServer.get().eventBus.dispatchFor(SessionUpdated::class.java)
                    .dispatch(SessionUpdated(session))

            CommandResult.Success(Unit)

        } catch (e: Exception) {
            LogUtil.severe("[WaveTickCommand] Failed to process wave tick for session $sessionId: ${e.message}")
            CommandResult.Failure("Failed to process wave tick: ${e.message}")
        }
    }
}

private fun WaveState.isInactive() = this in listOf(WaveState.IDLE, WaveState.STOPPED, WaveState.COMPLETED, WaveState.FAILED)
