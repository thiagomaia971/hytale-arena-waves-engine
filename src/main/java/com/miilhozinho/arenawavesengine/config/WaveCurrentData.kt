package com.miilhozinho.arenawavesengine.config

import java.util.concurrent.ConcurrentHashMap

class WaveCurrentData {
    var startTime: Long = 0
    var clearTime: Long = 0
    var duration: Int = 0
    var enemiesKilled: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
    val damage: ConcurrentHashMap<String, Float> = ConcurrentHashMap()
}
