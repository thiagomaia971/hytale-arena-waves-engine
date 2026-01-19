package com.miilhozinho.arenawavesengine.config

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.codec.codecs.array.ArrayCodec
import com.hypixel.hytale.math.vector.Vector3d
import com.miilhozinho.arenawavesengine.domain.WaveState
import java.util.UUID
import java.util.function.Supplier

class ArenaSession {
    val id: UUID = UUID.randomUUID()
    val owner: UUID = UUID.randomUUID()
    val center: Vector3d = Vector3d()
    val state: WaveState = WaveState.IDLE
    val currentWave: Int = 0
    val aliveEntityIds: Set<UUID> = emptySet()
    val waveMapId: String = ""
    val startTime: Long = System.currentTimeMillis() // When session started

    /**
     * Validates this arena map definition
     */
    fun validate(): ArenaSession {
        return this
    }

    companion object {
        val CODEC: BuilderCodec<ArenaSession?> = BuilderCodec.builder<ArenaSession?>(
            ArenaSession::class.java,
            Supplier { ArenaSession() })
            .build()
    }

//    val isActive: Boolean
//        get() = state in setOf(WaveState.RUNNING, WaveState.SPAWNING, WaveState.WAITING_CLEAR)
//
//    val isCompleted: Boolean
//        get() = state == WaveState.COMPLETED
//
//    val hasFailed: Boolean
//        get() = state == WaveState.FAILED
//
//    fun withState(newState: WaveState): com.miilhozinho.arenawavesengine.domain.ArenaSession = copy(state = newState)
//
//    fun withCurrentWave(newWave: Int): com.miilhozinho.arenawavesengine.domain.ArenaSession = copy(currentWave = newWave)
//
//    fun withAliveEntities(newAliveIds: Set<UUID>): com.miilhozinho.arenawavesengine.domain.ArenaSession = copy(aliveEntityIds = newAliveIds)
//
//    fun addAliveEntity(entityId: UUID): com.miilhozinho.arenawavesengine.domain.ArenaSession = copy(aliveEntityIds = aliveEntityIds + entityId)
//
//    fun removeAliveEntity(entityId: UUID): com.miilhozinho.arenawavesengine.domain.ArenaSession = copy(aliveEntityIds = aliveEntityIds - entityId)
//
//    fun clearAliveEntities(): ArenaSession = copy(aliveEntityIds = emptySet())
}