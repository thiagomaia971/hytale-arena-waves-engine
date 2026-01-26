package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.ExtraInfo
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.config.EnemyDefinition
import java.util.function.Supplier

object EnemyDefinitionCodec {
    val CODEC: BuilderCodec<EnemyDefinition?> = BuilderCodec.builder<EnemyDefinition?>(
        EnemyDefinition::class.java,
        Supplier { EnemyDefinition() })
        .append(
            KeyedCodec("EnemyType", Codec.STRING),
            { config, value, _ -> config!!.enemyType = value!! },
            { config, _ -> config!!.enemyType }
        ).add()
        .append(
            KeyedCodec("Count", Codec.INTEGER),
            { config, value, _ -> config!!.count = value!! },
            { config, _ -> config!!.count }
        ).add()
        .append(
            KeyedCodec("Radius", Codec.DOUBLE),
            { config, value, _ -> config!!.radius = value!! },
            { config, _ -> config!!.radius }
        ).add()
        .append(
            KeyedCodec("FlockSize", Codec.INTEGER),
            { config, value, _ -> config!!.flockSize = value!! },
            { config, _ -> config!!.flockSize }
        ).add()
        .append(
            KeyedCodec("Frozen", Codec.BOOLEAN),
            { config, value, _ -> config!!.frozen = value!! },
            { config, _ -> config!!.frozen }
        ).add()
        .append(
            KeyedCodec("Scale", Codec.DOUBLE),
            { config, value, _ -> config!!.scale = value!! },
            { config, _ -> config!!.scale }
        ).add()
        .append(
            KeyedCodec("SpawnOnGround", Codec.BOOLEAN),
            { config, value, _ -> config!!.spawnOnGround = value!! },
            { config, _ -> config!!.spawnOnGround }
        ).add()
        .build()
}
