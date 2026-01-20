package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.math.vector.Vector3d
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.UUID
import java.util.function.Supplier

class ArenaSession {
    var id: UUID = UUID.randomUUID()
    var owner: UUID = UUID.randomUUID()
    var state: WaveState = WaveState.IDLE
    var spawnPosition: Vector3d = Vector3d()
    var currentWave: Int = 0
    var aliveEntityIds: Set<UUID> = emptySet()
    var waveMapId: String = ""
    var startTime: Long = System.currentTimeMillis()
    var waveClearTime: Long = 0L

    fun validate(): ArenaSession {
        return this
    }

    companion object {
        val STATE_CODEC = EnumCodec<WaveState>(
            WaveState::class.java,
            EnumCodec.EnumStyle.CAMEL_CASE
        )

        val CODEC: BuilderCodec<ArenaSession?> = BuilderCodec.builder<ArenaSession?>(
            ArenaSession::class.java,
            Supplier { ArenaSession() })
            .append(
                KeyedCodec("Id", Codec.UUID_STRING),
                { config, value, _ -> config!!.id = value!! },
                { config, _ -> config!!.id }).add()
            .append(
                KeyedCodec("Owner", Codec.UUID_STRING),
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
                KeyedCodec("WaveClearTime", Codec.LONG),
                { config, value, _ -> config!!.waveClearTime = value!! },
                { config, _ -> config!!.waveClearTime }).add()
            .build()
    }
}
