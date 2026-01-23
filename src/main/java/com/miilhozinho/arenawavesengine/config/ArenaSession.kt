package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.math.vector.Vector3d
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

class ArenaSession {
    var id: String = UUID.randomUUID().toString()
    var owner: String = ""
    var state: WaveState = WaveState.IDLE
    var spawnPosition: Vector3d = Vector3d()
    var currentWave: Int = 0
    var waveMapId: String = ""
    var world: String = "default"
    var startTime: Long = System.currentTimeMillis()

    val wavesData: ConcurrentHashMap<Int, WaveCurrentData> = ConcurrentHashMap()

    var activeEntities: Array<String> = emptyArray()
    var activePlayers: Array<String> = emptyArray()
    val currentWaveSpawnProgress: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    fun validate(): ArenaSession {
        return this
    }

    companion object {
        val STATE_CODEC = EnumCodec<WaveState>(
            WaveState::class.java,
            EnumCodec.EnumStyle.CAMEL_CASE
        )

        val INT_VALUE_MAP_CODEC = MapCodec<Int, ConcurrentHashMap<String, Int>>(
            Codec.INTEGER,
            { ConcurrentHashMap<String, Int>() },
            false
        )

        val WAVES_DATA_MAP_CODEC = MapCodec<WaveCurrentData?, ConcurrentHashMap<String, WaveCurrentData?>>(
            WaveCurrentData.CODEC as Codec<WaveCurrentData?>,
            { ConcurrentHashMap<String, WaveCurrentData?>() },
            false
        )

        val CODEC: BuilderCodec<ArenaSession?> = BuilderCodec.builder<ArenaSession?>(
            ArenaSession::class.java,
            Supplier { ArenaSession() })
            .append(
                KeyedCodec("Id", Codec.STRING),
                { config, value, _ -> config!!.id = value!! },
                { config, _ -> config!!.id }).add()
            .append(
                KeyedCodec("Owner", Codec.STRING),
                { config, value, _ -> config!!.owner = value!! },
                { config, _ -> config!!.owner }).add()
            .append(
                KeyedCodec("State", STATE_CODEC),
                { config, value, _ -> config!!.state = value!! },
                { config, _ -> config!!.state }).add()
            .append(
                KeyedCodec("SpawnPosition", Vector3d.CODEC),
                { config, value, _ -> config!!.spawnPosition = value!! },
                { config, _ -> config!!.spawnPosition }).add()
            .append(
                KeyedCodec("CurrentWave", Codec.INTEGER),
                { config, value, _ -> config!!.currentWave = value!! },
                { config, _ -> config!!.currentWave }).add()
            .append(
                KeyedCodec("WaveMapId", Codec.STRING),
                { config, value, _ -> config!!.waveMapId = value!! },
                { config, _ -> config!!.waveMapId }).add()
            .append(
                KeyedCodec("World", Codec.STRING),
                { config, value, _ -> config!!.world = value!! },
                { config, _ -> config!!.world }).add()
            .append(
                KeyedCodec("StartTime", Codec.LONG),
                { config, value, _ -> config!!.startTime = value!! },
                { config, _ -> config!!.startTime }).add()
            .append(
                KeyedCodec("WavesData", WAVES_DATA_MAP_CODEC as Codec<ConcurrentHashMap<String, WaveCurrentData?>>),
                { config, value, _ ->
                    config!!.wavesData.clear()
                    value?.forEach { (k, v) -> if (v != null) config.wavesData[k.toInt()] = v }
                },
                { config, _ ->
                    val map = ConcurrentHashMap<String, WaveCurrentData?>()
                    config!!.wavesData.forEach { (k, v) -> map[k.toString()] = v }
                    map
                }).add()
            .append(
                KeyedCodec("ActiveEntities", Codec.STRING_ARRAY),
                { config, value, _ -> config!!.activeEntities = value },
                { config, _ -> config!!.activeEntities } ).add()
            .append(
                KeyedCodec("ActivePlayers", Codec.STRING_ARRAY),
                { config, value, _ -> config!!.activePlayers = value },
                { config, _ -> config!!.activePlayers } ).add()
            .append(
                KeyedCodec("CurrentWaveSpawnProgress", INT_VALUE_MAP_CODEC),
                { config, value, _ -> config!!.currentWaveSpawnProgress.clear(); if (value != null) config.currentWaveSpawnProgress.putAll(value) },
                { config, _ -> config!!.currentWaveSpawnProgress }).add()
            .build()
    }
}
