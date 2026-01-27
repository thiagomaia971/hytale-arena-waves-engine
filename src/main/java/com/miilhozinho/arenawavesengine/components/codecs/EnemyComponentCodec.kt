package com.miilhozinho.arenawavesengine.components.codecs

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.components.EnemyComponent
import java.util.function.Supplier

object EnemyComponentCodec {
    val CODEC: BuilderCodec<EnemyComponent?> = BuilderCodec.builder<EnemyComponent?>(
        EnemyComponent::class.java,
        Supplier { EnemyComponent() })
        .append(
            KeyedCodec("EntityRoleName", Codec.STRING),
            { config, value, _ -> config!!.entityRoleName = value!! },
            { config, _ -> config!!.entityRoleName }).add()
        .append(
            KeyedCodec("EntityId", Codec.STRING),
            { config, value, _ -> config!!.entityId = value!! },
            { config, _ -> config!!.entityId }).add()
        .append(
            KeyedCodec("SessionId", Codec.STRING),
            { config, value, _ -> config!!.sessionId = value!! },
            { config, _ -> config!!.sessionId }).add()
        .append(
            KeyedCodec("World", Codec.STRING),
            { config, value, _ -> config!!.world = value!! },
            { config, _ -> config!!.world }).add()
        .build()
}

