package com.miilhozinho.arenawavesengine

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.service.WaveScheduler
import com.miilhozinho.arenawavesengine.util.ConfigLoader
import com.miilhozinho.arenawavesengine.util.LogUtil

class ArenaWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {
    // Wave services
    lateinit var waveEngine: WaveEngine
    lateinit var waveScheduler: WaveScheduler

    init {
        try {
            ConfigLoader(dataDirectory).createOrLoad(pluginName)
            configState = this.withConfig<ArenaWavesEngineConfig?>(pluginName, ArenaWavesEngineConfig.CODEC)

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
        config = configState?.get()!!
        config.validate()

        LogUtil.info("Configuration loaded successfully with new architecture")
        if (config.debugLoggingEnabled == true) {
            LogUtil.info("Debug logging enabled")
        }

        // Initialize wave services
        waveEngine = WaveEngine(this)
        waveScheduler = WaveScheduler(waveEngine)

        LogUtil.info("[ArenaWavesEngine] Wave services initialized")
        LogUtil.info("Setup")
        commandRegistry.registerCommand(ArenaWavesEngineCommand())
        HytaleServer.get().eventBus.registerGlobal(SessionStarted::class.java, { event -> waveScheduler.startSession(event) })
        HytaleServer.get().eventBus.registerGlobal(SessionPaused::class.java, { event -> waveScheduler.pauseSession(event) })
        // entityStoreRegistry.registerSystem() // TODO: Implement system registration
    }

    override fun start() {
        LogUtil.info("Start")
    }

    override fun shutdown() {
        // Clean shutdown of wave services
        if (::waveScheduler.isInitialized) {
            waveScheduler.shutdown()
        }

        LogUtil.info("[ArenaWavesEngine] Shutdown complete")
    }

    companion object {
        var configState: Config<ArenaWavesEngineConfig?>? = null
        var config: ArenaWavesEngineConfig = ArenaWavesEngineConfig()
        var pluginName = "ArenaWavesEngine"
    }
}
