package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.miilhozinho.arenawavesengine.config.ArenaMapDefinition
import com.miilhozinho.arenawavesengine.config.WaveDefinition
import java.util.function.Supplier

object ArenaMapDefinitionCodec {
    private val WAVE_DEF_ARRAY_CODEC = ArrayCodec<WaveDefinition>(
        WaveDefinitionCodec.CODEC,
        { size -> arrayOfNulls<WaveDefinition>(size) }
    )

    val CODEC: BuilderCodec<ArenaMapDefinition?> = BuilderCodec.builder<ArenaMapDefinition?>(
        ArenaMapDefinition::class.java,
        Supplier { ArenaMapDefinition() })
        .append(
            KeyedCodec("Id", Codec.STRING),
            { config, value, _ -> config!!.id = value!! },
            { config, _ -> config!!.id }).add()
        .append(
            KeyedCodec("Name", Codec.STRING),
            { config, value, _ -> config!!.name = value!! },
            { config, _ -> config!!.name }).add()
        .append(
            KeyedCodec("Description", Codec.STRING),
            { config, value, _ -> config!!.description = value!! },
            { config, _ -> config!!.description }).add()
        .append(
            KeyedCodec("CleanupTimeoutSeconds", Codec.INTEGER),
            { config, value, _ -> config!!.cleanupTimeoutSeconds = value!! },
            { config, _ -> config!!.cleanupTimeoutSeconds }).add()
        .append(
            KeyedCodec("Waves", WAVE_DEF_ARRAY_CODEC),
            { config, value, _ -> config!!.waves = value.toList() },
            { config, _ -> config!!.waves.toTypedArray() }).add()
        .build()
}
