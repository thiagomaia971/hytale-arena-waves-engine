package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.config.WaveEnemyData
import java.util.function.Supplier

object WaveEnemyDataCodec {
    val CODEC: BuilderCodec<WaveEnemyData?> = BuilderCodec.builder<WaveEnemyData?>(
        WaveEnemyData::class.java,
        Supplier { WaveEnemyData() })
        .append(
            KeyedCodec("EntityType", Codec.STRING),
            { config, value, _ -> config!!.enemyType = value!! },
            { config, _ -> config!!.enemyType }).add()
        .append(
            KeyedCodec("Alives", Codec.INTEGER),
            { config, value, _ -> config!!.alives = value!! },
            { config, _ -> config!!.alives }).add()
        .append(
            KeyedCodec("Killed", Codec.INTEGER),
            { config, value, _ -> config!!.killed = value!! },
            { config, _ -> config!!.killed }).add()
        .build()
}