package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.event.IEvent
import com.hypixel.hytale.math.shape.Box
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.UUID

class SessionStarted : IEvent<Void> {
    lateinit var waveMapId: String
    lateinit var context: CommandContext
    lateinit var store: Store<EntityStore?>
    lateinit var ref: Ref<EntityStore?>
    lateinit var playerPosition: Vector3d
    lateinit var playerHeadRotation: Vector3f
    lateinit var playerBoundingBox: Box
    lateinit var playerRef: PlayerRef
    lateinit var world: World

    var radius: Double = 8.0
    var flagsString: String? = null
    var speedArg: Double? = null
    var nonRandom: Boolean = false
    var posOffset: Vector3d? = null
    var headRotation: String? = null
    var bodyRotation: String? = null
    var randomRotationArg: Boolean = false
    var facingRotation: Boolean = false
    var flockSize: Int = 1
    var frozen: Boolean = false
    var randomModel: Boolean = false
    var scaleArg: Float = 1.0F
    var bypassScaleLimitsArg: Boolean = false
    var test: Boolean = false
    var spawnPosition: Vector3d = Vector3d()
    var spawnOnGround: Boolean = false
}

class SessionPaused : IEvent<Void> {
    var sessionId: UUID? = null
    var pauseAll: Boolean = false
}

class EntityKilled : IEvent<Void> {
    lateinit var entityId: UUID
}