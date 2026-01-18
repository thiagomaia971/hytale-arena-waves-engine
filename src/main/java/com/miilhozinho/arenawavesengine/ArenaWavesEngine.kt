package com.miilhozinho.arenawavesengine

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.util.ConfigLoader
import com.miilhozinho.arenawavesengine.util.LogUtil

class ArenaWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {

    init {
        try {
            ConfigLoader(dataDirectory).createOrLoad("ArenaWavesEngine")
            CONFIG = this.withConfig<ArenaWavesEngineConfig?>("ArenaWavesEngine", ArenaWavesEngineConfig.CODEC)
            CONFIG?.get()?.validate()
            LogUtil.info("Configuration loaded successfully")
            if (CONFIG?.get()?.debugLoggingEnabled == true) {
                LogUtil.info("Debug logging enabled")
            }
        } catch (e: Exception) {
            LogUtil.severe("Failed to load configuration: ${e.message}")
            LogUtil.severe("Please check your config.json file for validation errors")
            throw e // Fail-fast: prevent plugin from loading with invalid config
        }
    }

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

    companion object {
        var CONFIG: Config<ArenaWavesEngineConfig?>? = null
    }
}
