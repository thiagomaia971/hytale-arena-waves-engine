package com.miilhozinho.arenawavesengine.hud

import au.ellie.hyui.builders.*
import au.ellie.hyui.html.TemplateProcessor
import com.buuz135.mhud.MultipleHUD
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ActiveSessionHudManager(
    private val repository: ArenaWavesEngineRepository,
    private val sessionRepository: ArenaSessionRepository) {
    private val htmlFile = "Hud/ArenaWavesEngine/ActiveSessionHud.html"
    var hudsByPlayerId: ConcurrentHashMap<String, ActiveSessionHudData> = ConcurrentHashMap()

    fun initializePlayerHud(playerRef: PlayerRef, store: Store<EntityStore?>) {
        val player = store.getComponent(playerRef.reference!!, Player.getComponentType()) ?: return
        val playerId = playerRef.uuid.toString()
//        removePlayerHud(playerId)

        val data = ActiveSessionHudData(
            player,
            playerRef,
            null,
            playerId,
            HudBuilder.detachedHud()
                .fromTemplate(ActiveSessionHud.html, TemplateProcessor())
                .withRefreshRate(500)
                .onRefresh { hud ->
                    refreshHud(hud)
                }
                .show(playerRef, store)
        )
//        data.hyUIHud.hide()
        hudsByPlayerId[playerId] = data
    }


    fun updateHud(session: ArenaSession) {
        if (session.isCompleted()) {
            for (playerId in session.activePlayers)
                removePlayerHud(playerId)
            return
        }

        val players = session.activePlayers
        val allHuds = hudsByPlayerId.filter { players.contains(it.key) }
        for (hudData in allHuds) {
            hudData.value.sessionId = session.id

            val build = HudBuilder.detachedHud()
//                .loadHtml(htmlFile, ActiveSessionHud.createTemplateProcessor(session, hudData.value.playerId))
                .fromTemplate(ActiveSessionHud.html, ActiveSessionHud.createTemplateProcessor(session, hudData.value.playerId))
                .withRefreshRate(500)
                .onRefresh { hud ->
                    refreshHud(hud)
                }
            hudData.value.hyUIHud.update(build)
        }


    }

    fun removeAllHuds() {
        for (playerId in hudsByPlayerId.keys)
            removePlayerHud(playerId)
    }

    fun removePlayerHud(playerId: String) {
        val playerRef = Universe.get().getPlayer(UUID.fromString(playerId)) ?: return
        val world = Universe.get().getWorld(playerRef.worldUuid!!) ?: return
        LogUtil.debug("cancel Hud")
        val playerHudData = hudsByPlayerId[playerId] ?: return

        world.execute {
            try {
                val player = playerRef.reference?.store?.getComponent(playerRef.reference!!, Player.getComponentType()) ?: return@execute
                playerHudData.hyUIHud.remove()
                MultipleHUD.getInstance().hideCustomHud(player, playerRef, playerHudData.hyUIHud.name)

            } catch (e: Exception) {
                LogUtil.warn(e.message!!)
            }
        }
    }

    private fun refreshHud(hud: HyUIHud) {
        val hudData = hudsByPlayerId[hud.playerRef.uuid.toString()] ?: return
        val session = sessionRepository.getSession(hudData.sessionId!!) ?: return
        val arenaDef = repository.getMapDefinition(session.waveMapId) ?: return
        val waveDef = arenaDef.waves[session.currentWave] ?: return

        hud.getById("elapsedTime", LabelBuilder::class.java).ifPresent { elapsedTime ->
            val elapsedTimeText = session.getElapsedTime()
            elapsedTime.withText(elapsedTimeText)
        }
//        hud.getById("interval-left-progress", ProgressBarBuilder::class.java).ifPresent { intervalLeftProgress ->
//            val totalIntervalWait = waveDef.interval * 1000L
//            val intervalElapsed = session.getElapsedIntervalTime()
//
//            val percentage = (intervalElapsed.toFloat() / totalIntervalWait.toFloat())
//            intervalLeftProgress.withValue(percentage)
//        }
//        hud.getById("interval-left", LabelBuilder::class.java).ifPresent { intervalLeftLabel ->
//            val intervalElapsed = session.getElapsedIntervalLeftTime(arenaDef)/100
//            intervalLeftLabel.withText(intervalElapsed.toString())
//        }
    }
}

data class ActiveSessionHudData(
    val player: Player,
    val playerRef: PlayerRef,
    var sessionId: String?,
    val playerId: String,
    val hyUIHud: HyUIHud
)