package com.miilhozinho.arenawavesengine.commandHandlers

/**
 * Base interface for all wave-related commands.
 * Commands encapsulate business logic and return typed results.
 */
interface WaveCommand<T> {
    /**
     * Execute the command and return a result
     */
    fun execute(): CommandResult<T>

    /**
     * Priority for command execution (higher = more important)
     */
    val priority: CommandPriority

    /**
     * Timestamp when command was created
     */
    val timestamp: Long
}

/**
 * Priority levels for command execution
 */
enum class CommandPriority {
    HIGH,
    NORMAL,
    LOW
}

/**
 * Result of command execution
 */
sealed class CommandResult<T> {
    data class Success<T>(val data: T) : CommandResult<T>()
    data class Failure<T>(val error: String) : CommandResult<T>()
}

/**
 * Base class for wave commands with default priority and timestamp
 */
abstract class BaseWaveCommand<T>(
    override val priority: CommandPriority = CommandPriority.NORMAL,
    override val timestamp: Long = System.currentTimeMillis()
) : WaveCommand<T>
