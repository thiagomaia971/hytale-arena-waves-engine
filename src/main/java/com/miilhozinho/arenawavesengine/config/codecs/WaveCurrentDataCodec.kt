package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.WaveCurrentData
import com.miilhozinho.arenawavesengine.config.WaveEnemyData
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object WaveCurrentDataCodec {
    private val DAMAGE_MAP_CODEC = MapCodec<Float, ConcurrentHashMap<String, Float>>(
        Codec.FLOAT,
        { ConcurrentHashMap<String, Float>() },
        false
    )

    val ENEMIES_MAP_CODEC = MapCodec<WaveEnemyData?, ConcurrentHashMap<String, WaveEnemyData?>>(
        WaveEnemyDataCodec.CODEC as Codec<WaveEnemyData?>,
        { ConcurrentHashMap<String, WaveEnemyData?>() },
        false
    )

    val CODEC: BuilderCodec<WaveCurrentData?> = BuilderCodec.builder<WaveCurrentData?>(
        WaveCurrentData::class.java,
        Supplier { WaveCurrentData() })
        .append(
            KeyedCodec("StartTime", Codec.LONG),
            { config, value, _ -> config!!.startTime = value!! },
            { config, _ -> config!!.startTime }).add()
        .append(
            KeyedCodec("ClearTime", Codec.LONG),
            { config, value, _ -> config!!.clearTime = value!! },
            { config, _ -> config!!.clearTime }).add()
        .append(
            KeyedCodec("Duration", Codec.INTEGER),
            { config, value, _ -> config!!.duration = value!! },
            { config, _ -> config!!.duration }).add()
        .append(
            KeyedCodec("Enemies", ENEMIES_MAP_CODEC as Codec<ConcurrentHashMap<String, WaveEnemyData?>>),
            { config, value, _ ->
                config!!.enemies.clear()
                value?.forEach { (k, v) -> if (v != null) config.enemies[k] = v }
            },
            { config, _ ->
                val map = ConcurrentHashMap<String, WaveEnemyData?>()
                config!!.enemies.forEach { (k, v) -> map[k.toString()] = v }
                map
            }).add()
        .append(
            KeyedCodec("Damage", DAMAGE_MAP_CODEC as Codec<ConcurrentHashMap<String, Float>>),
            { config, value, _ -> config!!.damage.clear(); if (value != null) config.damage.putAll(value) },
            { config, _ -> config!!.damage }).add()
        .build()
}
