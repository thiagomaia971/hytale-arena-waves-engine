package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

class WaveCurrentData {
    var startTime: Long = 0
    var clearTime: Long = 0
    var duration: Int = 0
    var enemiesKilled: Int = 0
    val damage: ConcurrentHashMap<String, Float> = ConcurrentHashMap()

    companion object {
        val DAMAGE_MAP_CODEC = MapCodec<Float, ConcurrentHashMap<String, Float>>(
            Codec.FLOAT,
            { ConcurrentHashMap<String, Float>() },
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
                KeyedCodec("EnemiesKilled", Codec.INTEGER),
                { config, value, _ -> config!!.enemiesKilled = value!! },
                { config, _ -> config!!.enemiesKilled }).add()
            .append(
                KeyedCodec("Damage", DAMAGE_MAP_CODEC as Codec<ConcurrentHashMap<String, Float>>),
                { config, value, _ -> config!!.damage.clear(); if (value != null) config.damage.putAll(value) },
                { config, _ -> config!!.damage }).add()
            .build()
    }
}