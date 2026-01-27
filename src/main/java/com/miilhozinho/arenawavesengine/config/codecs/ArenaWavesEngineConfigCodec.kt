package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.miilhozinho.arenawavesengine.config.ArenaMapDefinition
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object ArenaWavesEngineConfigCodec {
    private val ARENA_MAP_DEF_ARRAY_CODEC = ArrayCodec<ArenaMapDefinition>(
        ArenaMapDefinitionCodec.CODEC,
        { size -> arrayOfNulls<ArenaMapDefinition>(size) }
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
            { config: ArenaWavesEngineConfig?, value: Boolean?, extraInfo -> config!!.enabled = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.enabled }).add()
        .append<Int?>(
            KeyedCodec<Int?>("DefaultWaveCount", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.defaultWaveCount = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.defaultWaveCount }).add()
        .append<Int?>(
            KeyedCodec<Int?>("DefaultMobsPerWave", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.defaultMobsPerWave = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.defaultMobsPerWave }).add()
        .append<Int?>(
            KeyedCodec<Int?>("DefaultSpawnIntervalSeconds", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.defaultSpawnIntervalSeconds = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.defaultSpawnIntervalSeconds }).add()
        .append<Double?>(
            KeyedCodec<Double?>("DefaultSpawnRadius", Codec.DOUBLE),
            { config: ArenaWavesEngineConfig?, value: Double?, extraInfo -> config!!.defaultSpawnRadius = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.defaultSpawnRadius }).add()
        .append<Int?>(
            KeyedCodec<Int?>("MaxConcurrentMobsPerSession", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.maxConcurrentMobsPerSession = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.maxConcurrentMobsPerSession }).add()
        .append<Int?>(
            KeyedCodec<Int?>("MaxConcurrentSessionsGlobal", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.maxConcurrentSessionsGlobal = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.maxConcurrentSessionsGlobal }).add()
        .append<Int?>(
            KeyedCodec<Int?>("CleanupTimeoutSeconds", Codec.INTEGER),
            { config: ArenaWavesEngineConfig?, value: Int?, extraInfo -> config!!.cleanupTimeoutSeconds = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.cleanupTimeoutSeconds }).add()
        .append<Boolean?>(
            KeyedCodec<Boolean?>("DebugLoggingEnabled", Codec.BOOLEAN),
            { config: ArenaWavesEngineConfig?, value: Boolean?, extraInfo -> config!!.debugLoggingEnabled = value!! },
            { config: ArenaWavesEngineConfig?, extraInfo -> config!!.debugLoggingEnabled }).add()
        .append(
            KeyedCodec("ArenaMaps", ARENA_MAP_DEF_ARRAY_CODEC),
            { config, value, _ -> config!!.arenaMaps = value.toList() },
            { config, _ -> config!!.arenaMaps.toTypedArray() }
        ).add()
        .build()
}
