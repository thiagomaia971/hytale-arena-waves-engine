package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.SessionsConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object SessionsConfigCodec {
    val SESSIONS_MAP_CODEC = MapCodec<ArenaSession, ConcurrentHashMap<String, ArenaSession>>(
        ArenaSessionCodec.CODEC,
        { ConcurrentHashMap<String, ArenaSession>() },
        false
    )

    val CODEC: BuilderCodec<SessionsConfig?> = BuilderCodec.builder<SessionsConfig?>(
        SessionsConfig::class.java,
        Supplier { SessionsConfig() })
        .append(
            KeyedCodec("Sessions", SESSIONS_MAP_CODEC),
            { config, value, _ ->
                config!!.sessions.clear()
                config.sessions.putAll(value)
            },
            { config, _ ->
                // Convert the Map<String, List> to Map<String, Array> for the Codec
                val exportMap = ConcurrentHashMap<String, ArenaSession>()
                config!!.sessions.forEach { (key, list) ->
                    exportMap[key] = list
                }
                exportMap
            }
        ).add()
        .build()
}