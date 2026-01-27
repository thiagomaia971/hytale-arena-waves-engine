package com.miilhozinho.arenawavesengine.config.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.config.EventDataLog
import java.util.function.Supplier

object EventDataLogCodec {
    val CODEC: BuilderCodec<EventDataLog?> = BuilderCodec.builder<EventDataLog?>(
        EventDataLog::class.java,
        Supplier { EventDataLog() })
        .append(
            KeyedCodec("FiredAt", Codec.STRING),
            { config, value, _ -> config!!.firedAt = value!! },
            { config, _ -> config!!.firedAt }).add()
        .append(
            KeyedCodec("Event", Codec.STRING),
            { config, value, _ -> config!!.event = value!! },
            { config, _ -> config!!.event }).add()
        .append(
            KeyedCodec("OldState", Codec.STRING),
            { config, value, _ -> config!!.oldState = value!! },
            { config, _ -> config!!.oldState }).add()
        .append(
            KeyedCodec("NewState", Codec.STRING),
            { config, value, _ -> config!!.newState = value!! },
            { config, _ -> config!!.newState }).add()
        .append(
            KeyedCodec("OldSession", Codec.STRING),
            { config, value, _ -> config!!.oldSession = value!! },
            { config, _ -> config!!.oldSession }).add()
        .append(
            KeyedCodec("NewSession", Codec.STRING),
            { config, value, _ -> config!!.newSession = value!! },
            { config, _ -> config!!.newSession }).add()
        .build()
}