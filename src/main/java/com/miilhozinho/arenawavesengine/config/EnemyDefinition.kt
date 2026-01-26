package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import java.util.function.Supplier

class EnemyDefinition {
    var enemyType: String = ""
    var count: Int = 1
    var radius: Double = 1.0
    var flockSize: Int = 1
    var frozen: Boolean = false
    var scale: Double = 1.0
    var spawnOnGround: Boolean = false

    /**
     * Validates this enemy spawn definition
     */
    fun validate(): EnemyDefinition {
        require(flockSize <= count) { "flockSize ($flockSize) cannot exceed count ($count)" }
        require(count > 0) { "Count must be greater than 0" }
        require(scale > 0) { "Scale must be positive" }
        return this
    }
}
