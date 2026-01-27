package com.miilhozinho.arenawavesengine.config

class WaveEnemyData {
    var enemyType: String = ""
    var alives: Int = 0
    var killed: Int = 0


    fun getTotalEnemiesWave(): Int {
        return alives + killed
    }
}