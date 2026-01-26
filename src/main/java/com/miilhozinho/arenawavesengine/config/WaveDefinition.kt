package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import java.util.function.Supplier

class WaveDefinition {

    var interval: Int = 30
    var enemies: List<EnemyDefinition> = listOf()

    /**
     * Validates this wave definition
     */
    fun validate(): WaveDefinition {
        require(interval > 0) { "interval must be at least 1" }
        require(enemies.all { it.count > 0 }) { "All enemy spawns must have positive count" }
        enemies.forEach { enemy ->
            enemy.validate()
        }
        return this
    }
}
