package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.math.vector.Vector3d
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

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

    fun getOrCreateCurrentWave(): WaveCurrentData {
        return wavesData.getOrPut(currentWave) {
            val waveData = WaveCurrentData()
            val mapDef = ArenaWavesEngine.repository.getMapDefinition(waveMapId) ?: return waveData
            val enemiesToKill = mapDef.waves[currentWave].enemies

            for (enemy in enemiesToKill) {
                waveData.addEnemyData( enemy.enemyType, WaveEnemyData().apply {
                    this.enemyType = enemy.enemyType
                    this.alives = enemy.count
                })
            }

            return@getOrPut waveData
        }
    }

//    fun getOrCreateCurrentWave(): WaveCurrentData {
//        return wavesData.getOrPut(currentWave) {
//            val wavesData = WaveCurrentData()
//            val mapDef = ArenaWavesEngine.repository.get().getArenaDefinition(waveMapId) ?: return wavesData
//            val enemiesToKill = mapDef.waves[currentWave].enemies
//
//            for (enemy in enemiesToKill) {
//                wavesData.addEnemyData( enemy.enemyType, WaveEnemyData().apply {
//                    this.enemyType = enemy.enemyType
//                    this.alives = enemy.count
//                })
//            }
//
//            return@getOrPut wavesData
//        }
//    }

    fun getDamageScore(playerId: String): Int {
        return wavesData.values.map { it.damage[playerId]?.toInt() ?: 0 }.sum()
    }

    fun validate(): ArenaSession {
        return this
    }
}
