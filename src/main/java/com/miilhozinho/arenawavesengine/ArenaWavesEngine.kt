package com.miilhozinho.arenawavesengine

import au.ellie.hyui.commands.*
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.event.EventRegistration
import com.hypixel.hytale.event.IEvent
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandRegistration
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.command.ArenaWavesEngineCommand
import com.miilhozinho.arenawavesengine.components.EnemyComponent
import com.miilhozinho.arenawavesengine.components.EnemyDeathRegisteredComponent
import com.miilhozinho.arenawavesengine.components.codecs.EnemyComponentCodec
import com.miilhozinho.arenawavesengine.components.codecs.EnemyDeathRegisteredComponentCodec
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.config.EventLog
import com.miilhozinho.arenawavesengine.config.SessionsConfig
import com.miilhozinho.arenawavesengine.config.codecs.ArenaWavesEngineConfigCodec
import com.miilhozinho.arenawavesengine.config.codecs.EventLogCodec
import com.miilhozinho.arenawavesengine.config.codecs.SessionsConfigCodec
import com.miilhozinho.arenawavesengine.events.*
import com.miilhozinho.arenawavesengine.hud.ActiveSessionHudManager
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.repositories.EventLogRepository
import com.miilhozinho.arenawavesengine.service.LogType
import com.miilhozinho.arenawavesengine.service.PlayerMessageManager
import com.miilhozinho.arenawavesengine.service.WaveEngine
import com.miilhozinho.arenawavesengine.service.WaveScheduler
import com.miilhozinho.arenawavesengine.systems.DamageTrackingSystem
import com.miilhozinho.arenawavesengine.systems.DeathDetectionSystem
import com.miilhozinho.arenawavesengine.util.LogUtil


class ArenaWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {
    // Wave services
    var configState: Config<ArenaWavesEngineConfig>? = null
    var sessionsState: Config<SessionsConfig>? = null
    var eventLogState: Config<EventLog>? = null
    var config: ArenaWavesEngineConfig = ArenaWavesEngineConfig()

    lateinit var waveEngine: WaveEngine
    lateinit var waveScheduler: WaveScheduler
    lateinit var activeSessionHudManager: ActiveSessionHudManager

    var enemyComponentType: ComponentType<EntityStore?, EnemyComponent>? = null
    var enemyDeathRegisteredComponentType: ComponentType<EntityStore?, EnemyDeathRegisteredComponent>? = null


    var commandRegistrations: Array<CommandRegistration?> = emptyArray()
    var domainEventsRegistration: Array<EventRegistration<Void, IEvent<Void>>?> = arrayOf()


    init {
        try {
//            ConfigLoader(dataDirectory).createOrLoad(pluginName)
            configState = this.withConfig<ArenaWavesEngineConfig>(pluginName, ArenaWavesEngineConfigCodec.CODEC)
            sessionsState = this.withConfig<SessionsConfig>("Sessions", SessionsConfigCodec.CODEC)
            eventLogState = this.withConfig<EventLog>("EventLog", EventLogCodec.CODEC)
        } catch (e: Exception) {
            LogUtil.severe("Failed to load configuration: ${e.message}")
            LogUtil.severe("Please check your config.json file for validation errors")
            throw e
        }
    }

    override fun setup() {
        super.setup()
        repository = ArenaWavesEngineRepository(configState!!)
        sessionRepository = ArenaSessionRepository(sessionsState!!)
        eventRepository = EventLogRepository(eventLogState!!)

        config = repository.loadConfig()
        config.validate()
        isDebugLogs = config.debugLoggingEnabled
        repository.save(true)

        if (config.debugLoggingEnabled == true) {
            LogUtil.info("Debug logging enabled")
        }

        enemyComponentType = this.entityStoreRegistry.registerComponent(EnemyComponent::class.java, "EnemyComponent", EnemyComponentCodec.CODEC) as ComponentType<EntityStore?, EnemyComponent>
        enemyDeathRegisteredComponentType = this.entityStoreRegistry.registerComponent(EnemyDeathRegisteredComponent::class.java, "EnemyDeathRegisteredComponent",
            EnemyDeathRegisteredComponentCodec.CODEC) as ComponentType<EntityStore?, EnemyDeathRegisteredComponent>
        if (enemyComponentType == null || enemyDeathRegisteredComponentType == null) return

        // Initialize wave services
        waveEngine = WaveEngine(enemyComponentType!!, repository, sessionRepository)
        waveScheduler = WaveScheduler(repository, sessionRepository, waveEngine)
        activeSessionHudManager = ActiveSessionHudManager(repository, sessionRepository)

        LogUtil.info("[ArenaWavesEngine] Wave services initialized")

        commandRegistrations += commandRegistry.registerCommand(HyUITestGuiCommand())
        commandRegistrations += commandRegistry.registerCommand(HyUIAddHudCommand())
        commandRegistrations += commandRegistry.registerCommand(HyUIRemHudCommand())
        commandRegistrations += commandRegistry.registerCommand(HyUIUpdateHudCommand())
        commandRegistrations += commandRegistry.registerCommand(HyUIShowcaseCommand())
        commandRegistrations += commandRegistry.registerCommand(HyUITemplateRuntimeCommand())

        commandRegistrations += commandRegistry.registerCommand(ArenaWavesEngineCommand(activeSessionHudManager))
        commandRegistrations += commandRegistry.registerCommand(HyUIShowcaseCommand())

        this.eventRegistry.registerGlobal(PlayerReadyEvent::class.java, { e ->
            val player = e.player ?: return@registerGlobal
            val ref = player.reference
            if (ref == null || !ref.isValid) return@registerGlobal
            val store = ref.store
            val playerRef = store.getComponent(ref, PlayerRef.getComponentType())!!

            loadInitialHud(playerRef)
        })


        entityStoreRegistry.registerSystem(DeathDetectionSystem(enemyComponentType!!, enemyDeathRegisteredComponentType!!))
        entityStoreRegistry.registerSystem(DamageTrackingSystem(enemyComponentType!!, repository, sessionRepository))

        val eventBus = HytaleServer.get().eventBus
        domainEventsRegistration += eventBus.registerGlobal(SessionStarted::class.java, { event -> waveScheduler.handleSessionStarted(event) }) as EventRegistration<Void, IEvent<Void>>?
        domainEventsRegistration += eventBus.registerGlobal(SessionUpdated::class.java, { event -> activeSessionHudManager.updateHud(event.session) }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventBus.registerGlobal(SessionPaused::class.java, { event -> waveScheduler.handleSessionPaused(event) }) as EventRegistration<Void, IEvent<Void>>?
        domainEventsRegistration += eventBus.registerGlobal(HudHided::class.java, { event -> activeSessionHudManager.removeAllHuds() }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventBus.registerGlobal(EntityKilled::class.java, { event -> waveScheduler.handleEntityKilled(event) }) as EventRegistration<Void, IEvent<Void>>?
        domainEventsRegistration += eventBus.registerGlobal(DamageDealt::class.java, { event -> waveScheduler.handleDamageDealt(event) }) as EventRegistration<Void, IEvent<Void>>?

        domainEventsRegistration += eventRegistry.registerGlobal(StartWorldEvent::class.java, { event -> loadMapsOnStartupServer(event)}) as EventRegistration<Void, IEvent<Void>>?

    }


    override fun start() {
        LogUtil.info("Start")


        val playersOn = Universe.get().players
        for (playerRef in playersOn)
            loadInitialHud(playerRef)

    }

    fun loadMapsOnStartupServer(event: StartWorldEvent) {
        val allSessionsRunning = sessionRepository.getActiveSessions().filter { it.world == event.world.name }

        for (session in allSessionsRunning) {
            // For server startup, we need to restart the wave processing for existing sessions
            // Since the session is already persisted, we just need to restart the scheduled task
            val sessionStartedEvent = SessionStarted().apply {
                this.sessionId = session.id
                this.waveMapId = session.waveMapId
                this.playerId = session.owner
                this.store = event.world.entityStore.store
                this.world = event.world
                this.spawnPosition = session.spawnPosition
                // Fill in other required fields with defaults
                this.playerPosition = com.hypixel.hytale.math.vector.Vector3d(0.00, 0.00, 0.00)
                this.playerHeadRotation = com.hypixel.hytale.math.vector.Vector3f(0f, 0f, 0f)
                this.playerBoundingBox = com.hypixel.hytale.math.shape.Box()
            }

            // Queue the event to restart wave processing
            waveScheduler.handleSessionStarted(sessionStartedEvent)
        }
    }

    fun loadInitialHud(playerRef: PlayerRef) {
        try {
            val ref = playerRef.reference!!
            val store = ref.store
            val world = Universe.get().getWorld(playerRef.worldUuid!!)!!
            world.execute {
                try {
                    activeSessionHudManager.initializePlayerHud(playerRef, store)
                    val session = sessionRepository.getActiveSessions().find { it.owner == playerRef.uuid.toString() } ?: return@execute
                    activeSessionHudManager.updateHud(session)
                }catch (e: Exception){
                    PlayerMessageManager.sendMessage(playerRef.uuid.toString(), Message.raw(e.message!!), LogType.WARN)
                }
            }

        } catch (e: Exception){
            LogUtil.warn(e.localizedMessage)
        }
    }


    override fun shutdown() {
        enemyComponentType = null
        enemyDeathRegisteredComponentType = null
        activeSessionHudManager.removeAllHuds()

        waveScheduler.shutdown()
        for (command in commandRegistrations)
            command?.unregister()
        for (session in domainEventsRegistration)
            session?.unregister()

        LogUtil.info("[ArenaWavesEngine] Shutdown complete")
    }

    companion object {
        var pluginName = "ArenaWavesEngine"
        var isDebugLogs = false
        lateinit var repository: ArenaWavesEngineRepository
        lateinit var sessionRepository: ArenaSessionRepository
        lateinit var eventRepository: EventLogRepository
    }
}
