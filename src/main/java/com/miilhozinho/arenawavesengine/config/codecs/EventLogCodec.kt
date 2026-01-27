package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.codec.codecs.map.MapCodec
import com.miilhozinho.arenawavesengine.config.EventDataLog
import com.miilhozinho.arenawavesengine.config.EventLog
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object EventLogCodec {

    val EVENT_DATA_LIST_CODEC = ArrayCodec<EventDataLog>(
        EventDataLogCodec.CODEC,
        { size -> arrayOfNulls<EventDataLog>(size) }
    )

    val EVENTS_MAP_CODEC = MapCodec<Array<EventDataLog>, ConcurrentHashMap<String, Array<EventDataLog>>>(
        EVENT_DATA_LIST_CODEC,
        { ConcurrentHashMap<String, Array<EventDataLog>>() },
        false
    )

    val CODEC: BuilderCodec<EventLog?> = BuilderCodec.builder<EventLog?>(
        EventLog::class.java,
        Supplier { EventLog() })
        .append(
            KeyedCodec("Events", EVENTS_MAP_CODEC),
            { config, value, _ ->
                config!!.events.clear()
                value?.forEach { (key, array) ->
                    config.events[key] = array.toList() // Convert Array back to List for your domain model
                }
            },
            { config, _ ->
                // Convert the Map<String, List> to Map<String, Array> for the Codec
                val exportMap = ConcurrentHashMap<String, Array<EventDataLog>>()
                config!!.events.forEach { (key, list) ->
                    exportMap[key] = list.toTypedArray()
                }
                exportMap
            }
        ).add()
        .build()
}