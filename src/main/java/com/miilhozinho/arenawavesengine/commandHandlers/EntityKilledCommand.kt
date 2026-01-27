package com.miilhozinho.arenawavesengine.commandHandlers

import com.miilhozinho.arenawavesengine.events.EntityKilled
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to handle entity death events.
 * Delegates to WaveEngine for entity death processing.
 */
class EntityKilledCommand(
    private val waveEngine: WaveEngine,
    private val event: EntityKilled
) : BaseWaveCommand<Unit>(priority = CommandPriority.NORMAL) {

    override fun execute(): CommandResult<Unit> {
        return try {
            waveEngine.onEntityDeath(event.sessionId, event.entityId, event.entityRoleName)
            LogUtil.debug("[EntityKilledCommand] Processed death of entity ${event.entityId} in session ${event.sessionId}")
            CommandResult.Success(Unit)

        } catch (e: Exception) {
            LogUtil.severe("[EntityKilledCommand] Failed to process entity death ${event.entityId}: ${e.message}")
            CommandResult.Failure("Failed to process entity death: ${e.message}")
        }
    }
}
