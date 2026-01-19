package com.miilhozinho.arenawavesengine.command

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand
import java.util.concurrent.CompletableFuture

class ArenaWavesEngineCommand : AbstractAsyncCommand("arena-waves-engine", "Arena waves-engine", ) {
    init {
        this.addAliases("awe")
        this.addSubCommand(StartCommand())
        this.addSubCommand(PauseCommand())
    }

    override fun executeAsync(p0: CommandContext): CompletableFuture<Void?> {
        TODO("Not yet implemented")
    }

}
