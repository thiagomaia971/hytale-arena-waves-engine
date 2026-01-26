package com.miilhozinho.arenawavesengine.commandHandlers

import com.miilhozinho.arenawavesengine.events.*
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.PriorityBlockingQueue

/**
 * Processes wave-related commands with priority ordering.
 * Commands are queued and executed asynchronously based on priority.
 */
class WaveCommandProcessor(
    private val repository: ArenaWavesEngineRepository,
    private val waveEngine: WaveEngine
) {

    private val commandQueue = PriorityBlockingQueue<WaveCommand<*>>(
        11, // initial capacity
        compareBy<WaveCommand<*>> { it.priority }.thenBy { it.timestamp }
    )

    /**
     * Queue a command for execution
     */
    fun queueCommand(command: WaveCommand<*>) {
        commandQueue.add(command)
        LogUtil.debug("[WaveCommandProcessor] Queued command: ${command::class.simpleName} (priority: ${command.priority})")
    }

    /**
     * Queue an event by creating the appropriate command
     */
    fun queueEvent(event: Any) {
        val command = when (event) {
            is SessionStarted -> SessionStartedCommand(repository, waveEngine, event)
            is SessionPaused -> SessionPausedCommand(repository, waveEngine, event)
            is EntityKilled -> EntityKilledCommand(waveEngine, event)
            is DamageDealt -> DamageDealtCommand(waveEngine, event)
            else -> {
                LogUtil.warn("[WaveCommandProcessor] Unknown event type: ${event::class}")
                return
            }
        }
        queueCommand(command)
    }

    /**
     * Process all pending commands synchronously
     */
    fun processCommands(): List<CommandResult<*>> {
        val results = mutableListOf<CommandResult<*>>()
        var processedCount = 0

        while (commandQueue.isNotEmpty()) {
            val command = commandQueue.poll()
            try {
                val result = command.execute()
                results.add(result)
                processedCount++

                // Log failures
                if (result is CommandResult.Failure) {
                    LogUtil.warn("[WaveCommandProcessor] Command failed: ${result.error}")
                }
            } catch (e: Exception) {
                LogUtil.severe("[WaveCommandProcessor] Unexpected error executing command: ${e.message}")
                results.add(CommandResult.Failure<Any>("Unexpected error: ${e.message}"))
            }
        }

        return results
    }

    /**
     * Process commands asynchronously
     */
    fun processCommandsAsync(): CompletableFuture<List<CommandResult<*>>> {
        return CompletableFuture.supplyAsync { processCommands() }
    }

    /**
     * Get the current queue size
     */
    fun getQueueSize(): Int = commandQueue.size

    /**
     * Clear all pending commands
     */
    fun clearQueue() {
        val clearedCount = commandQueue.size
        commandQueue.clear()
        if (clearedCount > 0) {
            LogUtil.info("[WaveCommandProcessor] Cleared $clearedCount pending commands")
        }
    }

    /**
     * Check if there are high-priority commands pending
     */
    fun hasHighPriorityCommands(): Boolean {
        return commandQueue.any { it.priority == CommandPriority.HIGH }
    }
}
