package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.component.*
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.events.SessionStarted

class StartCommand : AbstractPlayerCommand("start", "Start a arena wave") {

    override fun execute(
        context: CommandContext,
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World
    ) {
        val sessionStartedEvent = SessionStarted().apply {
            this.waveMapId = "default_arena_map";
            this.context = context;
            this.store = store;
            this.ref = ref;
            this.playerRef = playerRef;
            this.world = world;
        }
        HytaleServer.get().eventBus.dispatchFor(SessionStarted::class.java).dispatch(sessionStartedEvent)
    }

}

