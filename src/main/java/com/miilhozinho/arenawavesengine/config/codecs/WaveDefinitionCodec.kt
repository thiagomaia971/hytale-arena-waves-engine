package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.miilhozinho.arenawavesengine.config.EnemyDefinition
import com.miilhozinho.arenawavesengine.config.WaveDefinition
import java.util.function.Supplier

object WaveDefinitionCodec {
    private val ENEMY_ARRAY_CODEC = ArrayCodec<EnemyDefinition>(
        EnemyDefinitionCodec.CODEC,
        { size -> arrayOfNulls<EnemyDefinition>(size) }
    )

    val CODEC: BuilderCodec<WaveDefinition?> = BuilderCodec.builder<WaveDefinition?>(
        WaveDefinition::class.java,
        Supplier { WaveDefinition() })
        .append(
            KeyedCodec("Interval", com.hypixel.hytale.codec.Codec.INTEGER),
            { config, value, _ -> config!!.interval = value!! },
            { config, _ -> config!!.interval }
        ).add()
        .append(
            KeyedCodec("Enemies", ENEMY_ARRAY_CODEC),
            { config, value, _ -> config!!.enemies = value.toList() },
            { config, _ -> config!!.enemies.toTypedArray() }
        ).add()
        .build()
}
