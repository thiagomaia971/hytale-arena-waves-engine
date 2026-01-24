package com.miilhozinho.arenawavesengine.hud

import au.ellie.hyui.builders.HudBuilder
import au.ellie.hyui.builders.HyUIHud
import au.ellie.hyui.html.TemplateProcessor
import com.buuz135.mhud.MultipleHUD
import com.google.gson.Gson
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ActiveSessionHudManager(val repository: ArenaWavesEngineRepository) {
    var hudsByPlayerId: ConcurrentHashMap<String, ActiveSessionHudData> = ConcurrentHashMap()
    val iterableFor : ForIterable = ForIterable()

    fun initializePlayerHud(playerRef: PlayerRef, store: Store<EntityStore?>) {
        val player = store.getComponent(playerRef.reference!!, Player.getComponentType()) ?: return
        val playerId = playerRef.uuid.toString()
        removePlayerHud(playerId)

        val data = ActiveSessionHudData(
            player,
            playerRef,
            null,
            playerId,
            HudBuilder.detachedHud()
                .fromTemplate(ActiveSessionHud.html, TemplateProcessor())
                .show(playerRef, store)
        )
        data.hyUIHud.hide()
        hudsByPlayerId[playerId] = data
    }

    fun updateHud(session: ArenaSession) {
        val players = session.activePlayers
        val allHuds = hudsByPlayerId.filter { players.contains(it.key) }
        for (hudData in allHuds) {
            hudData.value.sessionId = session.id

            val build = HudBuilder.detachedHud()
                .fromTemplate(ActiveSessionHud.html, ActiveSessionHud.createTemplateProcessor(session, hudData.value.playerId))

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
}

data class ActiveSessionHudData(
    val player: Player,
    val playerRef: PlayerRef,
    var sessionId: String?,
    val playerId: String,
    val hyUIHud: HyUIHud
)