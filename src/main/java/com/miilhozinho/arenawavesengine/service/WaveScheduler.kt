package com.miilhozinho.arenawavesengine.service

import com.miilhozinho.arenawavesengine.commandHandlers.*
import com.miilhozinho.arenawavesengine.events.DamageDealt
import com.miilhozinho.arenawavesengine.events.EntityKilled
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

/**
 * WaveScheduler now acts as a coordinator that routes events to the command processor.
 * The actual business logic has been moved into command objects for better separation of concerns.
 */
class WaveScheduler(
    private val repository: ArenaWavesEngineRepository,
    private val sessionRepository: ArenaSessionRepository,
    private val waveEngine: WaveEngine
) {
    private val commandProcessor = WaveCommandProcessor(repository, sessionRepository, waveEngine)
    private val activeTasks = ConcurrentHashMap<String, ScheduledFuture<Void>>()

    /**
     * Handle session started event by queuing and processing it asynchronously
     */
    fun handleSessionStarted(event: SessionStarted) {
        commandProcessor.queueEvent(event)
        commandProcessor.processCommandsAsync().thenAccept { results ->
            handleCommandResults("SessionStarted", results)
        }
    }

    /**
     * Handle session paused event by queuing and processing it asynchronously
     */
    fun handleSessionPaused(event: SessionPaused) {
        commandProcessor.queueEvent(event)
        commandProcessor.processCommandsAsync().thenAccept { results ->
            handleCommandResults("SessionPaused", results)
        }
    }

    /**
     * Handle entity killed event by queuing and processing it asynchronously
     */
    fun handleEntityKilled(event: EntityKilled) {
        commandProcessor.queueEvent(event)
        commandProcessor.processCommandsAsync().thenAccept { results ->
            handleCommandResults("EntityKilled", results)
        }
    }

    /**
     * Handle damage dealt event by queuing and processing it asynchronously
     */
    fun handleDamageDealt(event: DamageDealt) {
        commandProcessor.queueEvent(event)
        commandProcessor.processCommandsAsync().thenAccept { results ->
            handleCommandResults("DamageDealt", results)
        }
    }

    /**
     * Handle command execution results
     */
    private fun handleCommandResults(eventType: String, results: List<CommandResult<*>>) {
        results.forEach { result ->
            when (result) {
                is CommandResult.Success -> {
                    LogUtil.debug("[$eventType] Command executed successfully")
                }
                is CommandResult.Failure -> {
                    LogUtil.severe("[$eventType] Command failed: ${result.error}")
                    // TODO: Could add retry logic, notifications, or error recovery here
                }
            }
        }
    }

    /**
     * Process all pending commands synchronously
     */
    fun processPendingCommands(): List<CommandResult<*>> {
        return commandProcessor.processCommands()
    }

    /**
     * Process commands asynchronously
     */
    fun processPendingCommandsAsync() = commandProcessor.processCommandsAsync()

    /**
     * Get the number of pending commands
     */
    fun getPendingCommandCount(): Int = commandProcessor.getQueueSize()

    /**
     * Check if there are high-priority commands pending
     */
    fun hasHighPriorityCommands(): Boolean = commandProcessor.hasHighPriorityCommands()

    /**
     * Clear all pending commands
     */
    fun clearPendingCommands() = commandProcessor.clearQueue()

    fun shutdown() {
        LogUtil.debug("[WaveScheduler] Shutting down. Clearing ${commandProcessor.getQueueSize()} pending commands.")
        commandProcessor.clearQueue()
        activeTasks.clear() // Clear any remaining task references
    }

    // Helper visibility methods
    fun isSessionActive(sessionId: String) = activeTasks.containsKey(sessionId)
    fun getActiveTaskCount() = activeTasks.size
}
