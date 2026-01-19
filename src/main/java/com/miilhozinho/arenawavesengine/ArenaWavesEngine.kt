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
            ConfigLoader(dataDirectory).createOrLoad(PLUGIN_NAME)
            CONFIG = this.withConfig<ArenaWavesEngineConfig?>(PLUGIN_NAME, ArenaWavesEngineConfig.CODEC)

            // Initialize the new configuration architecture
//            CONFIG?.get()?.initializeWithProvider(dataDirectory)
        } catch (e: Exception) {
            LogUtil.severe("Failed to load configuration: ${e.message}")
            LogUtil.severe("Please check your config.json file for validation errors")
            throw e // Fail-fast: prevent plugin from loading with invalid config
        }
    }

    override fun setup() {
        super.setup()

        CONFIG?.get()?.validate()
        LogUtil.info("Configuration loaded successfully with new architecture")
        if (CONFIG?.get()?.debugLoggingEnabled == true) {
            LogUtil.info("Debug logging enabled")
        }

        LogUtil.info("Setup")
        commandRegistry.registerCommand(ArenaWavesEngineCommand())
        // entityStoreRegistry.registerSystem() // TODO: Implement system registration
    }

    override fun start() {
        LogUtil.info("Start")
    }

    override fun shutdown() {
        LogUtil.info("Shutdown")
    }

    companion object {
        var CONFIG: Config<ArenaWavesEngineConfig?>? = null
        var PLUGIN_NAME = "ArenaWavesEngine"
    }
}
