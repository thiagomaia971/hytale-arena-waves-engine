package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec

import java.util.function.Supplier
import kotlin.compareTo

class ArenaMapDefinition {
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var cleanupTimeoutSeconds: Int = 300
    var waves: List<WaveDefinition> = emptyList()

    /**
     * Validates this arena map definition
     */
    fun validate(): ArenaMapDefinition {
//        require(id.isEmpty()) { "id must be not null or empty" }
        require(cleanupTimeoutSeconds > 0) { "cleanupTimeoutSeconds must be positive" }
        require(cleanupTimeoutSeconds >= 60) { "cleanupTimeoutSeconds must be at least 60 seconds" }
        require(waves.all { it.enemies.isNotEmpty() }) { "All waves must have at least one enemy" }
        waves.forEach { wave ->
            wave.validate()
        }
        return this
    }
}
