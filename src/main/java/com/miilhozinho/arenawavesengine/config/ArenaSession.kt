package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.EnumCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.hypixel.hytale.math.vector.Vector3d
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
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

    var wavesData: ConcurrentHashMap<Int, WaveCurrentData> = ConcurrentHashMap()
        private set

    var activeEntities: Array<String> = emptyArray()
    var activePlayers: Array<String> = emptyArray()
    val currentWaveSpawnProgress: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    // Scheduled task for periodic wave processing (not serialized)
    @Transient
    var scheduledTask: ScheduledFuture<Void>? = null

    fun isCompleted(): Boolean {
        return state == WaveState.COMPLETED
    }

    fun createWaveData(): WaveCurrentData {
        return wavesData.getOrPut(currentWave) {
            val wavesData = WaveCurrentData()
            val mapDef = ArenaWavesEngine.repository.getMapDefition(waveMapId) ?: return wavesData
            val enemiesToKill = mapDef.waves[currentWave].enemies
            val enemiesKilled: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
            for (enemy in enemiesToKill) {
                enemiesKilled[enemy.enemyType] = 0
            }

            wavesData.enemiesKilled = enemiesKilled
            return@getOrPut wavesData
        }
    }

    fun getDamageScore(playerId: String): Int {
        return wavesData.values.map { it.damage[playerId]?.toInt() ?: 0 }.sum()
    }

    fun validate(): ArenaSession {
        return this
    }
}
