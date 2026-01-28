package com.miilhozinho.arenawavesengine.config

import com.google.gson.Gson
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import java.util.concurrent.ConcurrentHashMap

class WaveCurrentData {
    var startTime: Long = 0
    var clearTime: Long = 0
    var duration: Int = 0

    var enemies: ConcurrentHashMap<String, WaveEnemyData> = ConcurrentHashMap()
        private set
    val damage: ConcurrentHashMap<String, Float> = ConcurrentHashMap()

    fun addEnemyData(enemyType: String, data: WaveEnemyData): WaveEnemyData {
        return enemies.getOrPut(enemyType) {
            data
        }
    }

    fun increaseKilledEnemy(enemyType: String, entityId: String): WaveEnemyData? {
        val enemy = enemies[enemyType]
        enemy?.killed += 1
        enemy?.alives -= 1
        ArenaWavesEngine.sessionRepository.get().entityToSessionMap.remove(entityId)
        return enemy
    }

    fun anounymousEnemies(): Array<Any> {
        val gson = Gson()
        val json = gson.toJson(this.enemies.values.toList())
        return Gson().fromJson(json, Array<Any>::class.java)
    }

    fun isCleared(): Boolean {
        return enemies.values.all { it.alives == 0 }
    }
}
