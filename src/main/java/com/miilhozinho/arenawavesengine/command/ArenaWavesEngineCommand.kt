package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import com.miilhozinho.arenawavesengine.hud.ActiveSessionHudManager
import java.util.concurrent.CompletableFuture

class ArenaWavesEngineCommand(val activeSessionHudManager: ActiveSessionHudManager) : AbstractAsyncCommand("arena-waves-engine", "Arena waves-engine", ) {
    init {
        this.addAliases("awe")
        this.addSubCommand(StartCommand(activeSessionHudManager))
        this.addSubCommand(PauseCommand(activeSessionHudManager))
    }

    override fun executeAsync(p0: CommandContext): CompletableFuture<Void?> {
        TODO("Not yet implemented")
    }

}
