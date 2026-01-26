package com.miilhozinho.arenawavesengine.commandHandlers

import com.miilhozinho.arenawavesengine.events.DamageDealt
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.util.LogUtil

/**
 * Command to handle damage dealt events.
 * Delegates to WaveEngine for damage processing.
 */
class DamageDealtCommand(
    private val waveEngine: WaveEngine,
    private val event: DamageDealt
) : BaseWaveCommand<Unit>(priority = CommandPriority.LOW) {

    override fun execute(): CommandResult<Unit> {
        return try {
            waveEngine.onDamageDealt(event.victimId, event.attackerId, event.damage)
            LogUtil.debug("[DamageDealtCommand] Processed damage ${event.damage} from ${event.attackerId} to ${event.victimId}")
            CommandResult.Success(Unit)

        } catch (e: Exception) {
            LogUtil.severe("[DamageDealtCommand] Failed to process damage event: ${e.message}")
            CommandResult.Failure("Failed to process damage event: ${e.message}")
        }
    }
}
