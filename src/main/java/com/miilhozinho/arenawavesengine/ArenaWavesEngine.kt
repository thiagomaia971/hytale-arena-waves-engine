package com.miilhozinho.arenawavesengine

import com.hypixel.hytale.protocol.Vector3d
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent
import com.hypixel.hytale.server.core.universe.world.events.WorldEvent
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.events.EntityKilled
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.events.SessionPaused
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.service.WaveScheduler
import com.miilhozinho.arenawavesengine.systems.DeathDetectionSystem
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
        entityStoreRegistry.registerSystem(DeathDetectionSystem())

        HytaleServer.get().eventBus.registerGlobal(SessionStarted::class.java, { event -> waveScheduler.startSession(event) })
        HytaleServer.get().eventBus.registerGlobal(SessionPaused::class.java, { event -> waveScheduler.pauseSession(event) })
        HytaleServer.get().eventBus.registerGlobal(EntityKilled::class.java, { event -> waveScheduler.onEntityDeath(event) })

        eventRegistry.registerGlobal(StartWorldEvent::class.java, { event -> loadMapsOnStartupServer(event)})

    //        HytaleServer.get().eventBus.register(com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType::class.java, { event -> System.out.println(event.toString()) })
    //        HytaleServer.get().eventBus.register(EntityEventType::class.java, {event -> System.out.println("Entity event received: $event") })
//        entityStoreRegistry.registerEntityEventType()

    }


    override fun start() {
        LogUtil.info("Start")
    }

    fun loadMapsOnStartupServer(event: StartWorldEvent) {
        val allSesionsRunning = config.sessions.filter { it.world == event.world.name }

        for (session in allSesionsRunning) {
            val sessionStartedEvent = SessionStarted().apply {
                this.waveMapId = session.waveMapId
                this.store = event.world.entityStore.store;
                this.playerPosition = com.hypixel.hytale.math.vector.Vector3d(0.00, 0.00, 0.00);
                this.playerHeadRotation = com.hypixel.hytale.math.vector.Vector3f(0f, 0f, 0f)
                this.playerBoundingBox = com.hypixel.hytale.math.shape.Box()
                this.world = event.world;

                this.spawnPosition = session.spawnPosition
            }
            waveScheduler.startTask(session.id, sessionStartedEvent)
//            HytaleServer.get().eventBus.dispatchFor(SessionStarted::class.java).dispatch(sessionStartedEvent)
        }
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
