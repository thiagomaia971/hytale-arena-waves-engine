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

    companion object {
        val ENEMY_ARRAY_CODEC = ArrayCodec<EnemyDefinition>(
            EnemyDefinition.CODEC,
            { size -> arrayOfNulls<EnemyDefinition>(size) }
        )

        val CODEC: BuilderCodec<WaveDefinition?> = BuilderCodec.builder<WaveDefinition?>(
            WaveDefinition::class.java,
            Supplier { WaveDefinition() })
            // 1. Field: interval
            .append(
                KeyedCodec("Interval", com.hypixel.hytale.codec.Codec.INTEGER),
                { config, value, _ -> config!!.interval = value!! },
                { config, _ -> config!!.interval }
            ).add()
            // 2. Field: enemies (The Array)
            .append(
                KeyedCodec("Enemies", ENEMY_ARRAY_CODEC),
                { config, value, _ -> config!!.enemies = value.toList() },
                { config, _ -> config!!.enemies.toList() as Array<out EnemyDefinition?>? }
            ).add()
            .build()
    }
}