package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.component.*
import com.hypixel.hytale.math.shape.Box
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.events.SessionStarted
import java.util.UUID

class StartCommand : AbstractPlayerCommand("start", "Start a arena wave") {
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

        val sessionStartedEvent = SessionStarted().apply {
            this.waveMapId = waveMapIdArg.get(context);
            this.context = context;
            this.store = store;
            this.ref = ref;
            this.playerPosition = playerPosition
            this.playerHeadRotation = playerHeadRotation
            this.playerBoundingBox = playerBoundingBox
            this.playerRef = playerRef;
            this.world = world;

            this.posOffset = Vector3d(0.0, 0.00, 5.00)
        }
        HytaleServer.get().eventBus.dispatchFor(SessionStarted::class.java).dispatch(sessionStartedEvent)
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

}

