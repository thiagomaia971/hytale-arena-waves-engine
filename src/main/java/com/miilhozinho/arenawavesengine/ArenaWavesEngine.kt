package com.miilhozinho.arenawavesengine

import au.ellie.hyui.commands.HyUIShowcaseCommand
import com.hypixel.hytale.event.EventRegistration
import com.hypixel.hytale.event.IEvent
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.command.system.CommandRegistration
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.events.*
import com.miilhozinho.arenawavesengine.hud.ActiveSessionHudManager
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.service.WaveScheduler
import com.miilhozinho.arenawavesengine.systems.DamageTrackingSystem
import com.miilhozinho.arenawavesengine.systems.DeathDetectionSystem
import com.miilhozinho.arenawavesengine.util.ConfigLoader
import com.miilhozinho.arenawavesengine.util.LogUtil


class ArenaWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {
    // Wave services
    var configState: Config<ArenaWavesEngineConfig>? = null
    var config: ArenaWavesEngineConfig = ArenaWavesEngineConfig()
    lateinit var arenaWavesEngineRepository: ArenaWavesEngineRepository

    lateinit var waveEngine: WaveEngine
    lateinit var waveScheduler: WaveScheduler
    lateinit var activeSessionHudManager: ActiveSessionHudManager


    var commandStartEventRegistration: CommandRegistration? = null
    var domainEventsRegistration: Array<EventRegistration<Void, IEvent<Void>>?> = arrayOf()


    init {
        try {
            ConfigLoader(dataDirectory).createOrLoad(pluginName)
            configState = this.withConfig<ArenaWavesEngineConfig>(pluginName, ArenaWavesEngineConfig.CODEC)
        } catch (e: Exception) {
            LogUtil.severe("Failed to load configuration: ${e.message}")
            LogUtil.severe("Please check your config.json file for validation errors")
            throw e
        }
    }

    override fun setup() {
        super.setup()
        arenaWavesEngineRepository = ArenaWavesEngineRepository(configState!!)
        config = arenaWavesEngineRepository.loadConfig()
        config.validate()
        isDebugLogs = config.debugLoggingEnabled

        if (config.debugLoggingEnabled == true) {
            LogUtil.info("Debug logging enabled")
        }

        // Initialize wave services
        waveEngine = WaveEngine(arenaWavesEngineRepository)
        waveScheduler = WaveScheduler(arenaWavesEngineRepository, waveEngine)
        activeSessionHudManager = ActiveSessionHudManager(arenaWavesEngineRepository)

        LogUtil.info("[ArenaWavesEngine] Wave services initialized")
        commandStartEventRegistration = commandRegistry.registerCommand(ArenaWavesEngineCommand(activeSessionHudManager))
        commandStartEventRegistration = commandRegistry.registerCommand(HyUIShowcaseCommand())
        this.eventRegistry.registerGlobal(PlayerReadyEvent::class.java, { e ->
            val player = e.player ?: return@registerGlobal
            val ref = player.reference
            if (ref == null || !ref.isValid) return@registerGlobal
            val store = ref.store
            val playerRef = store.getComponent(ref, PlayerRef.getComponentType())!!

            loadInitialHud(playerRef)
        })

        entityStoreRegistry.registerSystem(DeathDetectionSystem())
        entityStoreRegistry.registerSystem(DamageTrackingSystem(arenaWavesEngineRepository))
//
        val eventBus = HytaleServer.get().eventBus
        domainEventsRegistration += eventBus.registerGlobal(SessionStarted::class.java, { event -> waveScheduler.startSession(event) }) as EventRegistration<Void, IEvent<Void>>?
//        domainEventsRegistration += eventBus.registerGlobal(SessionStarted::class.java, { event -> activeSessionHudManager.openHud(event.sessionId, event.pla) })
        domainEventsRegistration += eventBus.registerGlobal(SessionUpdated::class.java, { event -> activeSessionHudManager.updateHud(event.session) }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventBus.registerGlobal(SessionPaused::class.java, { event -> waveScheduler.pauseSession(event) }) as EventRegistration<Void, IEvent<Void>>?
        domainEventsRegistration += eventBus.registerGlobal(HudHided::class.java, { event -> activeSessionHudManager.removeAllHuds() }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventBus.registerGlobal(EntityKilled::class.java, { event -> waveScheduler.onEntityDeath(event) }) as EventRegistration<Void, IEvent<Void>>?
        domainEventsRegistration += eventBus.registerGlobal(DamageDealt::class.java, { event -> waveScheduler.onDamageDealt(event) }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventRegistry.registerGlobal(StartWorldEvent::class.java, { event -> loadMapsOnStartupServer(event)}) as EventRegistration<Void, IEvent<Void>>?
    }


    override fun start() {
        LogUtil.info("Start")

        val playersOn = Universe.get().players
        for (playerRef in playersOn)
            loadInitialHud(playerRef)
    }

    fun loadMapsOnStartupServer(event: StartWorldEvent) {
        val allSesionsRunning = arenaWavesEngineRepository.getActiveSessions().filter { it.world == event.world.name }

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
        }
    }

    fun loadInitialHud(playerRef: PlayerRef) {
        try {
            val ref = playerRef.reference!!
            val store = ref.store
            val session = arenaWavesEngineRepository.getActiveSessions().find { it.owner == playerRef.uuid.toString() }

            if (session != null) {
                val world = Universe.get().getWorld(playerRef.worldUuid!!)!!
                world.execute {
                    activeSessionHudManager.openHud(session.id, playerRef, store)
                }
            }

        } catch (e: Exception){
            LogUtil.warn(e.localizedMessage)
        }
    }


    override fun shutdown() {
        activeSessionHudManager.removeAllHuds()

//        waveScheduler.shutdown()
        commandStartEventRegistration?.unregister()
        for (session in domainEventsRegistration)
            session?.unregister()

        LogUtil.info("[ArenaWavesEngine] Shutdown complete")
    }

    companion object {
        var pluginName = "ArenaWavesEngine"
        var isDebugLogs = false
    }
}
