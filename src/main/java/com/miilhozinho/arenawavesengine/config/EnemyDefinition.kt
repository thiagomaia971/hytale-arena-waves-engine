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

    companion object {
        val CODEC: BuilderCodec<EnemyDefinition?> = BuilderCodec.builder<EnemyDefinition?>(
            EnemyDefinition::class.java,
            Supplier { EnemyDefinition() })
            // enemyType: String
            .append(
                KeyedCodec("EnemyType", Codec.STRING),
                { config, value, _ -> config!!.enemyType = value!! },
                { config, _ -> config!!.enemyType }
            ).add()
            // count: Int
            .append(
                KeyedCodec("Count", Codec.INTEGER),
                { config, value, _ -> config!!.count = value!! },
                { config, _ -> config!!.count }
            ).add()
            // radius: Double
            .append(
                KeyedCodec("Radius", Codec.DOUBLE),
                { config, value, _ -> config!!.radius = value!! },
                { config, _ -> config!!.radius }
            ).add()
            // flockSize: Int
            .append(
                KeyedCodec("FlockSize", Codec.INTEGER),
                { config, value, _ -> config!!.flockSize = value!! },
                { config, _ -> config!!.flockSize }
            ).add()
            // frozen: Boolean
            .append(
                KeyedCodec("Frozen", Codec.BOOLEAN),
                { config, value, _ -> config!!.frozen = value!! },
                { config, _ -> config!!.frozen }
            ).add()
            // scale: Double
            .append(
                KeyedCodec("Scale", Codec.DOUBLE),
                { config, value, _ -> config!!.scale = value!! },
                { config, _ -> config!!.scale }
            ).add()
            // spawnOnGround: Boolean
            .append(
                KeyedCodec("SpawnOnGround", Codec.BOOLEAN),
                { config, value, _ -> config!!.spawnOnGround = value!! },
                { config, _ -> config!!.spawnOnGround }
            ).add()
            .build()
    }
}