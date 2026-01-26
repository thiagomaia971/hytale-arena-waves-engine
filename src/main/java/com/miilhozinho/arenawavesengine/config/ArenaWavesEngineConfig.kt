package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

/**
 * Legacy configuration wrapper that integrates the new three-layer architecture
 * while maintaining backward compatibility with the existing Hytale plugin system.
 *
 * This class serves as a bridge between the old mutable config approach and the new
 * immutable data model with proper validation and async IO operations.
 */
class ArenaWavesEngineConfig {
    // Core configuration fields (maintain existing interface)
    var enabled: Boolean = true
    var defaultWaveCount: Int = 5
    var defaultMobsPerWave: Int = 10
    var defaultSpawnIntervalSeconds: Int = 30
    var defaultSpawnRadius: Double = 50.0
    var maxConcurrentMobsPerSession: Int = 50
    var maxConcurrentSessionsGlobal: Int = 3
    var cleanupTimeoutSeconds: Int = 300
    var debugLoggingEnabled: Boolean = true

    // Domain data fields (maintain existing interface)
    var arenaMaps: List<ArenaMapDefinition> = listOf(
        ArenaMapDefinition().apply {
            id = "default_arena_map"
            name = "Default arena map"
            description = "Any description for default arena map"
            waves = listOf(
                WaveDefinition().apply {
                    interval = 10
                    enemies = listOf(
                        EnemyDefinition().apply { enemyType = "Skeleton"; count = 3; },
                        EnemyDefinition().apply { enemyType = "Skeleton_Archer"; count = 1; },
                    )
                },
                WaveDefinition().apply {
                    interval = 1
                    enemies = listOf(
                        EnemyDefinition().apply { enemyType = "Skeleton"; count = 2; },
                        EnemyDefinition().apply { enemyType = "Skeleton_Archer"; count = 2; },
                    )
                }
            )
        }
    )
    var sessions: List<ArenaSession> = listOf()

    /**
     * Legacy validation method (now delegates to new validation system)
     */
    fun validate(): ArenaWavesEngineConfig {// 1. Validate Global Counters
        require(maxConcurrentSessionsGlobal > 0) { "maxConcurrentSessionsGlobal must be at least 1" }
        require(maxConcurrentMobsPerSession > 0) { "maxConcurrentMobsPerSession must be at least 1" }

        // 2. Validate Default Values
        require(defaultWaveCount > 0) { "defaultWaveCount must be positive" }
        require(defaultSpawnIntervalSeconds >= 5) { "defaultSpawnIntervalSeconds is too low (min 5s)" }
        require(defaultSpawnRadius > 0.0) { "defaultSpawnRadius must be positive" }

        // 3. Deep Validation of nested Map Definitions
        arenaMaps.forEach { mapDef ->
            mapDef.validate() // Ensure each map, wave, and enemy is valid
        }
        return this
    }
}
