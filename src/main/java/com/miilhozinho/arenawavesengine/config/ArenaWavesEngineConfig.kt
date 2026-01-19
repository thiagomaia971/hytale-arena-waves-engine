package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
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
        private set
    var defaultMobsPerWave: Int = 10
        private set
    var defaultSpawnIntervalSeconds: Int = 30
        private set
    var defaultSpawnRadius: Double = 50.0
        private set
    var maxConcurrentMobsPerSession: Int = 50
        private set
    var maxConcurrentSessionsGlobal: Int = 3
        private set
    var cleanupTimeoutSeconds: Int = 300
        private set
    var debugLoggingEnabled: Boolean = true
        private set

    // Domain data fields (maintain existing interface)
    var arenaMaps: List<ArenaMapDefinition> = listOf(
        ArenaMapDefinition().apply {
            id = "default_arena_map"
            name = "Default arena map"
            description = "Any description for default arena map"
            waves = listOf(
                WaveDefinition().apply {
                    interval = 30
                    enemies = listOf(
                        EnemyDefinition().apply { enemyType = "Skeleton"; count = 3; },
                        EnemyDefinition().apply { enemyType = "Skeleton_Archer"; count = 1; },
                    )
                }
            )
        }
    )
        private set
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

    companion object {
        val ARENA_MAP_DEF_ARRAY_CODEC = ArrayCodec<ArenaMapDefinition>(
            ArenaMapDefinition.CODEC,
            { size -> arrayOfNulls<ArenaMapDefinition>(size) }
        )
        val ARENA_SESSION_DEF_ARRAY_CODEC = ArrayCodec<ArenaSession>(
            ArenaSession.CODEC,
            { size -> arrayOfNulls<ArenaSession>(size) }
        )
        /**
         * Legacy CODEC for backward compatibility with Hytale plugin system
         * This maintains the existing JSON structure while internally using the new architecture
         */
        val CODEC: BuilderCodec<ArenaWavesEngineConfig?> = BuilderCodec.builder<ArenaWavesEngineConfig?>(
            ArenaWavesEngineConfig::class.java,
            Supplier { ArenaWavesEngineConfig() })
            .append<Boolean?>(
                KeyedCodec<Boolean?>("Enabled", Codec.BOOLEAN),
                { config: ArenaWavesEngineConfig?, value: Boolean?, extraInfo: ExtraInfo? ->
                    config!!.enabled = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.enabled }).add()
            .append<Int?>(
                KeyedCodec<Int?>("DefaultWaveCount", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.defaultWaveCount = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.defaultWaveCount }).add()
            .append<Int?>(
                KeyedCodec<Int?>("DefaultMobsPerWave", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.defaultMobsPerWave = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.defaultMobsPerWave }).add()
            .append<Int?>(
                KeyedCodec<Int?>("DefaultSpawnIntervalSeconds", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.defaultSpawnIntervalSeconds = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.defaultSpawnIntervalSeconds }).add()
            .append<Double?>(
                KeyedCodec<Double?>("DefaultSpawnRadius", Codec.DOUBLE),
                { config: ArenaWavesEngineConfig?, value: Double?, extraInfo: ExtraInfo? ->
                    config!!.defaultSpawnRadius = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.defaultSpawnRadius }).add()
            .append<Int?>(
                KeyedCodec<Int?>("MaxConcurrentMobsPerSession", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.maxConcurrentMobsPerSession = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.maxConcurrentMobsPerSession }).add()
            .append<Int?>(
                KeyedCodec<Int?>("MaxConcurrentSessionsGlobal", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.maxConcurrentSessionsGlobal = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.maxConcurrentSessionsGlobal }).add()
            .append<Int?>(
                KeyedCodec<Int?>("CleanupTimeoutSeconds", Codec.INTEGER),
                { config: ArenaWavesEngineConfig?, value: Int?, extraInfo: ExtraInfo? ->
                    config!!.cleanupTimeoutSeconds = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.cleanupTimeoutSeconds }).add()
            .append<Boolean?>(
                KeyedCodec<Boolean?>("DebugLoggingEnabled", Codec.BOOLEAN),
                { config: ArenaWavesEngineConfig?, value: Boolean?, extraInfo: ExtraInfo? ->
                    config!!.debugLoggingEnabled = value!!
                },
                { config: ArenaWavesEngineConfig?, extraInfo: ExtraInfo? -> config!!.debugLoggingEnabled }).add()
            .append(
                KeyedCodec("ArenaMaps", ARENA_MAP_DEF_ARRAY_CODEC),
                { config, value, _ -> config!!.arenaMaps = value.toList() },
                { config, _ -> config!!.arenaMaps.toTypedArray() }
            ).add()
            .append(
                KeyedCodec("Sessions", ARENA_SESSION_DEF_ARRAY_CODEC),
                { config, value, _ -> config!!.sessions = value.toList() },
                { config, _ -> config!!.sessions.toTypedArray() }
            ).add()
            .build()
    }
}
