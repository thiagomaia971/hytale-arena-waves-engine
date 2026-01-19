package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig.Companion.ARENA_MAP_DEF_ARRAY_CODEC
import java.util.function.Supplier

class ArenaMapDefinition {
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var waves: List<WaveDefinition> = emptyList()

    /**
     * Validates this arena map definition
     */
    fun validate(): ArenaMapDefinition {
//        require(id.isEmpty()) { "id must be not null or empty" }
        require(waves.all { it.enemies.isNotEmpty() }) { "All waves must have at least one enemy" }
        waves.forEach { wave ->
            wave.validate()
        }
        return this
    }

    companion object {
        val WAVE_DEF_ARRAY_CODEC = ArrayCodec<WaveDefinition>(
            WaveDefinition.CODEC,
            { size -> arrayOfNulls<WaveDefinition>(size) }
        )
        val CODEC: BuilderCodec<ArenaMapDefinition?> = BuilderCodec.builder<ArenaMapDefinition?>(
            ArenaMapDefinition::class.java,
            Supplier { ArenaMapDefinition() })
            // 1. ID
            .append(
                KeyedCodec("Id", Codec.STRING),
                { config, value, _ -> config!!.id = value!! },
                { config, _ -> config!!.id }
            ).add()
            // 2. Name
            .append(
                KeyedCodec("Name", Codec.STRING),
                { config, value, _ -> config!!.name = value!! },
                { config, _ -> config!!.name }
            ).add()
            // 3. Description
            .append(
                KeyedCodec("Description", Codec.STRING),
                { config, value, _ -> config!!.description = value!! },
                { config, _ -> config!!.description }
            ).add()
            // 4. Waves Array
            .append(
                KeyedCodec("Waves", WAVE_DEF_ARRAY_CODEC),
                { config, value, _ -> config!!.waves = value!!.filterNotNull().toList() },
                { config, _ -> config!!.waves.toTypedArray() }
            ).add()
            .build()
    }
}