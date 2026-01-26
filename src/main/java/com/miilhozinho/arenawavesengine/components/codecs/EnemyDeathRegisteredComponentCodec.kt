package com.miilhozinho.arenawavesengine.components.codecs

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.components.EnemyDeathRegisteredComponent
import java.util.function.Supplier

object EnemyDeathRegisteredComponentCodec {
    val CODEC: BuilderCodec<EnemyDeathRegisteredComponent?> = BuilderCodec.builder<EnemyDeathRegisteredComponent?>(
        EnemyDeathRegisteredComponent::class.java,
        Supplier { EnemyDeathRegisteredComponent() })
        .build()
}