package com.miilhozinho.arenawavesengine.commandHandlers

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.Universe
import java.util.concurrent.TimeUnit
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.WaveCurrentData
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionUpdated
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.LogType
import com.miilhozinho.arenawavesengine.service.PlayerMessageManager
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to start a new wave session.
 * Contains all the business logic for session initialization.
 */
class SessionStartedCommand(
    private val repository: ArenaWavesEngineRepository,
    private val waveEngine: WaveEngine,
    private val event: SessionStarted
) : BaseWaveCommand<Boolean>(priority = CommandPriority.HIGH) {

    override fun execute(): CommandResult<Boolean> {
        return try {
            LogUtil.debug("[SessionStartedCommand] Attempting to start session: ${event.sessionId} for map: ${event.waveMapId}")

            // Validate map exists
            if (repository.getMapDefition(event.waveMapId) == null) {
                PlayerMessageManager.sendMessage(
                    event.playerId,
                    Message.raw("[SessionStartedCommand] Wave Map ${event.waveMapId} not found."),
                    LogType.WARN
                )
                return CommandResult.Success(false)
            }

            // Check if session already exists (server restart scenario)
            val existingSession = repository.getSession(event.sessionId)
            val session = if (existingSession != null) {
                LogUtil.debug("[SessionStartedCommand] Restarting existing session ${event.sessionId}")
                existingSession
            } else {
                // Create new session
                ArenaSession().apply {
                    id = event.sessionId
                    waveMapId = event.waveMapId
                    state = WaveState.RUNNING
                    owner = event.playerId
                    spawnPosition = event.spawnPosition
                    world = event.world.name
                    activePlayers = arrayOf(event.playerId)
                    startTime = System.currentTimeMillis()
                }.also { newSession ->
                    // Initialize wave data for new sessions
                    newSession.createWaveData()
                    // Persist new session
                    repository.addSession(newSession)
                    repository.save(forceSave = true)
                }
            }

            // Start the scheduled task
            startScheduledTask(session)

            // Dispatch session updated event
            HytaleServer.get().eventBus.dispatchFor(SessionUpdated::class.java)
                .dispatch(SessionUpdated(session))

            LogUtil.info("[SessionStartedCommand] Successfully started session ${event.sessionId}")
            CommandResult.Success(true)

        } catch (e: Exception) {
            LogUtil.severe("[SessionStartedCommand] Failed to start session ${event.sessionId}: ${e.message}")
            CommandResult.Failure("Failed to start session: ${e.message}")
        }
    }

    private fun startScheduledTask(session: ArenaSession) {
        @Suppress("UNCHECKED_CAST")
        val task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay({
            runWaveTick(session)
        }, 0L, 1L, java.util.concurrent.TimeUnit.SECONDS) as java.util.concurrent.ScheduledFuture<Void>

        // Store task reference for cancellation when session is paused/stopped
        session.scheduledTask = task

        // Register with Hytale's task registry for automatic cleanup
        Universe.get().taskRegistry.registerTask(task)

        LogUtil.info("[SessionStartedCommand] Started wave task for session ${session.id}")
    }

    private fun runWaveTick(session: ArenaSession) {
        try {
            // Execute wave processing (all logic is now encapsulated in WaveTickCommand)
            val waveTickCommand = WaveTickCommand(waveEngine, repository, session.id, event)
            val result = waveTickCommand.execute()

            if (result is CommandResult.Failure) {
                LogUtil.severe("[SessionStartedCommand] Wave tick failed for session ${session.id}: ${result.error}")
            }

        } catch (e: Exception) {
            LogUtil.severe("[SessionStartedCommand] Critical error in tick for ${session.id}: ${e.stackTraceToString()}")
            // Task will be cancelled by Hytale's cleanup system
        }
    }
}
