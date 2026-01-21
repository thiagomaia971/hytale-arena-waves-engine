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
    var waveClearTime: Long = 0L

    var activeEntities: Array<String> = emptyArray()

    // Runtime entity tracking (not persisted)
    val currentWaveSpawnProgress: MutableMap<String, Int> = ConcurrentHashMap()

    fun validate(): ArenaSession {
        return this
    }

    companion object {
        val STATE_CODEC = EnumCodec<WaveState>(
            WaveState::class.java,
            EnumCodec.EnumStyle.CAMEL_CASE
        )

        val ACTIVIES_ENTITY_MAP_CODEC = MapCodec<String, ConcurrentHashMap<String, String>>(
            Codec.STRING,
            { ConcurrentHashMap<String, String>() },
            false // false = Modifiable so we can clear/putAll
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
                KeyedCodec("WaveClearTime", Codec.LONG),
                { config, value, _ -> config!!.waveClearTime = value!! },
                { config, _ -> config!!.waveClearTime }).add()
            .append(
                KeyedCodec("ActiveEntities", Codec.STRING_ARRAY),
                { config, value, _ -> config!!.activeEntities = value },
                { config, _ -> config!!.activeEntities } ).add()
            .build()
    }
}
