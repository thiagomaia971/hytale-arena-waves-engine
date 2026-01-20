package com.miilhozinho.arenawavesengine.domain

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import java.util.function.Supplier

enum class WaveState {
    IDLE,
    RUNNING,
    SPAWNING,
    WAITING_CLEAR,
    WAITING_INTERVAL,
    COMPLETED,
    STOPPED,
    FAILED;

    companion object {
        val CODEC: BuilderCodec<WaveState?> = BuilderCodec.builder<WaveState?>(
            WaveState::class.java,
            Supplier { WaveState.IDLE })
            .build()
    }
}
