package com.miilhozinho.arenawavesengine

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.util.LogUtil

class ArenaWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {

    override fun setup() {
        super.setup()
        LogUtil.info("Setup")
        commandRegistry.registerCommand(ArenaWavesEngineCommand())
    }

    override fun start() {
        LogUtil.info("Start")
    }

    override fun shutdown() {
        LogUtil.info("Shutdown")
    }

}
