package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.component.*
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.events.SessionPaused
import java.util.Optional
import java.util.UUID

class PauseCommand : AbstractPlayerCommand("pause", "Pause the arena wave") {
    private val sessionIdArg: OptionalArg<UUID?>
    private val pauseAllArg: FlagArg

    init {
//        this.setPermissionGroup(GameMode.Adventure)
        this.sessionIdArg = this.withOptionalArg<UUID?>("sessionId", "Sets the session id to stop", ArgTypes.UUID)
        this.pauseAllArg = this.withFlagArg("pauseAll", "Pause all arenas")
    }

    override fun execute(
        context: CommandContext,
        store: Store<EntityStore?>,
        ref: Ref<EntityStore?>,
        playerRef: PlayerRef,
        world: World
    ) {

        val event = SessionPaused().apply {
            this.sessionId = sessionIdArg.get(context)
            this.pauseAll = pauseAllArg.get(context)
        }

        HytaleServer.get().eventBus.dispatchFor(SessionPaused::class.java).dispatch(event)
    }
}

