package com.miilhozinho.arenawavesengine.domain

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec

enum class WaveState {
    IDLE,
    RUNNING,
    SPAWNING,
    WAITING_CLEAR,
    COMPLETED,
    STOPPED,
    FAILED;
}
