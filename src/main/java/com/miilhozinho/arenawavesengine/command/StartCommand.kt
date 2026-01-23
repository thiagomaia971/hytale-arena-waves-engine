package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.component.*
import com.hypixel.hytale.math.shape.Box
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.hud.ActiveSessionHudManager
import java.util.UUID

class StartCommand(val activeSessionHudManager: ActiveSessionHudManager) : AbstractPlayerCommand("start", "Start a arena wave") {
    private val waveMapIdArg: RequiredArg<String>
    init {
        this.waveMapIdArg = this.withRequiredArg<String>("waveMapId", "Sets the wave map", ArgTypes.STRING)
    }
    override fun execute(
        context: CommandContext,
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World
    ) {


        val playerHeadRotation = getPlayerHeadRotation(store, ref)
        val playerPosition = getPlayerPosition(store, ref)
        val playerBoundingBox = getPlayerBoundingBox(store, ref)
        val spawnPosition = calculateTargetPosition(
            playerPosition,
            playerHeadRotation,
            Vector3d(0.0, 0.0, 5.0)
        )

        val sessionStartedEvent = SessionStarted().apply {
            this.sessionId = UUID.randomUUID().toString()
            this.waveMapId = waveMapIdArg.get(context);
            this.store = store;
            this.playerPosition = playerPosition
            this.playerHeadRotation = playerHeadRotation
            this.playerBoundingBox = playerBoundingBox
            this.world = world;
            this.playerId = playerRef.uuid.toString();

            this.spawnPosition = spawnPosition
        }

        HytaleServer.get().eventBus.dispatchFor(SessionStarted::class.java).dispatch(sessionStartedEvent)
        context.sendMessage(Message.raw("Session ${sessionStartedEvent.sessionId} started!"))
        activeSessionHudManager.openHud(sessionStartedEvent.sessionId, playerRef, store)
    }

    private fun getPlayerHeadRotation(
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>
    ): com.hypixel.hytale.math.vector.Vector3f {
        val headRotationComponent = store.getComponent<HeadRotation?>(ref, HeadRotation.getComponentType()) as HeadRotation?
        checkNotNull(headRotationComponent)
        return headRotationComponent.getRotation()
    }

    private fun getPlayerPosition(
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>
    ): com.hypixel.hytale.math.vector.Vector3d {
        val transformComponent =
            store.getComponent<TransformComponent?>(ref, TransformComponent.getComponentType()) as TransformComponent?
        checkNotNull(transformComponent)
        return transformComponent.getPosition()
    }

    private fun getPlayerBoundingBox(
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>
    ): Box {
        val boundingBoxComponent = store.getComponent<BoundingBox?>(ref, BoundingBox.getComponentType()) as BoundingBox?

        checkNotNull(boundingBoxComponent)

        return boundingBoxComponent.getBoundingBox()
    }

    private fun calculateTargetPosition(
        playerPosition: Vector3d,
        playerHeadRotation: Vector3f,
        posOffset: Vector3d
    ): Vector3d {
        val forward = Vector3d()
        val right = Vector3d()

        // 1. Get the direction the player is facing
        PhysicsMath.vectorFromAngles(playerHeadRotation.getYaw(), 0.0f, forward)

        // 2. Calculate the "Right" vector (Perpendicular to forward)
        // If forward is (x, 0, z), right is (z, 0, -x)
        right.x = forward.z
        right.y = 0.0
        right.z = forward.x

        // 3. Scale directions by your offset values
        // posOffset.z = Ahead/Back
        // posOffset.x = Right/Left
        // posOffset.y = Up/Down
        val worldOffsetX = (forward.x * posOffset.z) + (right.x * posOffset.x)
        val worldOffsetY = posOffset.y
        val worldOffsetZ = (forward.z * posOffset.z) + (right.z * posOffset.x)

        // 4. ADD the offset to the current position
        return Vector3d(
            playerPosition.x + worldOffsetX,
            playerPosition.y + worldOffsetY,
            playerPosition.z + worldOffsetZ
        )
    }
}

