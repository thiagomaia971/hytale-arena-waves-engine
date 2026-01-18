package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import java.util.function.Supplier

class ArenaWavesEngineConfig {
    var enabled: Boolean = true
        private set
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
    var debugLoggingEnabled: Boolean = false
        private set

    fun validate(): ArenaWavesEngineConfig {
        require(defaultWaveCount > 0) { "defaultWaveCount must be positive" }
        require(defaultMobsPerWave > 0) { "defaultMobsPerWave must be positive" }
        require(defaultSpawnIntervalSeconds > 0) { "defaultSpawnIntervalSeconds must be positive" }
        require(defaultSpawnRadius > 0.0) { "defaultSpawnRadius must be positive" }
        require(maxConcurrentMobsPerSession > 0) { "maxConcurrentMobsPerSession must be positive" }
        require(maxConcurrentSessionsGlobal > 0) { "maxConcurrentSessionsGlobal must be positive" }
        require(cleanupTimeoutSeconds > 0) { "cleanupTimeoutSeconds must be positive" }
        return this
    }

    companion object {
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
            .build()
    }
}
