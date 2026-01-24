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

    fun openHud(sessionId: String, playerRef: PlayerRef, store: Store<EntityStore?>) {

        try {
            val session = repository.getSession(sessionId)!!
            LogUtil.debug("Opening HUD for Session: ${Gson().toJson(session)}")

            val player = store.getComponent(playerRef.reference!!, Player.getComponentType()) ?: return
            val playerId = playerRef.uuid.toString()
            removePlayerHud(playerId)

            val world = Universe.get().getWorld(playerRef.worldUuid!!) ?: return
            world.execute {
                val data = ActiveSessionHudData(
                    player,
                    playerRef,
                    sessionId,
                    playerId,
                    HudBuilder.detachedHud()
                        .fromTemplate(ActiveSessionHud.html, ActiveSessionHud.defaultTemplate())
                        .show(playerRef, store),
                    false
                )

                hudsByPlayerId[playerId] = data

                data.hyUIHud.add()
            }
            world.execute {
                updateHud(session)
            }
        } catch (e: Exception) {
            LogUtil.warn(e.message!!)
        }
    }

    fun updateHud(session: ArenaSession) {
        val players = session.activePlayers
        val allHuds = hudsByPlayerId.filter { players.contains(it.key) }
        for (hudData in allHuds) {
            hudData.value.hasUpdate = true

            val html = defineNewHtml(session, hudData.value)
            val template = defineAllValues(session, hudData.value)

            val build = HudBuilder.detachedHud()
                .fromTemplate(html, template)

            hudData.value.hyUIHud.update(build)
        }


    }

    private fun defineNewHtml(session: ArenaSession, activeHudData: ActiveSessionHudData): String {
        val mapDef = repository.get().arenaMaps.find { it.id == session.waveMapId } ?: return ActiveSessionHud.html
        val currentWaveDef = mapDef.waves[session.currentWave] ?: return ActiveSessionHud.html
        val currentWaveData = session.wavesData[session.currentWave] ?: return ActiveSessionHud.html

        return iterableFor.addFor(
            ActiveSessionHud.html,
            "enemy-list",
            currentWaveDef.enemies.map {
                "{{@enemyCard:enemyName=${it.enemyType},countKilled=${it.count - (currentWaveData.enemiesKilled[it.enemyType] ?: 0)},countDef=${it.count}}}"
            }.toTypedArray()
        )
    }

    private fun defineAllValues(session: ArenaSession, activeHudData: ActiveSessionHudData): TemplateProcessor {
        val template = ActiveSessionHud.defaultTemplate()
        val mapDef = repository.get().arenaMaps.find { it.id == session.waveMapId } ?: return template
        val currentWaveDef = mapDef.waves[session.currentWave] ?: return template
        val currentWaveData = session.wavesData[session.currentWave] ?: return template

        val enemiesKilled = currentWaveData.enemiesKilled.values.sum()
        val totalEnemiesWave = currentWaveDef.enemies.sumOf { it.count }
        val progressBarValue = enemiesKilled.toFloat()/totalEnemiesWave.toFloat()
        LogUtil.debug("Drawing ${Gson().toJson(session)}")

        template.setVariable("mapName", mapDef.name)
        template.setVariable("waveCount", "${session.currentWave + 1} / ${mapDef.waves.count()}")
        template.setVariable("enemiesRemain", "${(totalEnemiesWave - enemiesKilled)} / $totalEnemiesWave")
        template.setVariable("waveProgressBar", progressBarValue)
        template.setVariable("waveProgressLabel", "${(progressBarValue * 100).toInt()}%")
        template.setVariable("playerScore", session.wavesData.values.map { it.damage.values.sum() }.sum().toInt())

        return template;
    }

//    private fun updateHudInformations(session: ArenaSession, hud: HyUIHud) {
//        val mapDef = repository.get().arenaMaps.find { it.id == session.waveMapId } ?: return
//        val currentWaveDef = mapDef.waves[session.currentWave] ?: return
//        val currentWaveData = session.wavesData[session.currentWave] ?: return
//
//        val enemiesKilled = currentWaveData.enemiesKilled.values.sum()
//        val totalEnemiesWave = currentWaveDef.enemies.sumOf { it.count }
//
//        LogUtil.debug("Drawing ${Gson().toJson(session)}")
//
//        hud.getById("row-display-value", LabelBuilder::class.java).ifPresent { label ->
//            label.withText("${session.currentWave + 1} / ${mapDef.waves.count()}")
//        }
//        hud.getById("wave-enemies-remain", LabelBuilder::class.java).ifPresent { label ->
//            label.withText("$enemiesKilled / $totalEnemiesWave")
//        }
//        hud.getById("wave-progress", ProgressBarBuilder::class.java).ifPresent { progress ->
//            progress.withValue((enemiesKilled.toFloat()/totalEnemiesWave.toFloat()))
//        }
//    }

//    private fun enemyGroupDiv(groupBuilder: GroupBuilder): GroupBuilder {
//        return groupBuilder.addChild(GroupBuilder
//            .group()
//            .withId("mob-row")
//            .withStyle(HyUIStyle()
//                .setAlignment(HyUIStyle.Alignment.Start)
//            )
//            .withLayoutMode(LayoutModeSupported.LayoutMode.Left)
//            .withAnchor(HyUIAnchor().setLeft(20))
//            .addChild(LabelBuilder.label()
//                .withStyle(HyUIStyle()
//                    .setTextColor("#D1E8FF")
//                    .setFontSize(16f))
//                .withFlexWeight(1)
//                .withText("_Skeleton"))
//            .addChild(LabelBuilder.label()
//                .withStyle(HyUIStyle()
//                    .setTextColor("#FFFFFF")
//                    .setFontSize(16f)
//                    .setRenderBold(true)
//                    .setAlignment(HyUIStyle.Alignment.End))
//                .withAnchor(HyUIAnchor()
//                    .setRight(20))
//                .withFlexWeight(1)
//                .withText("_x3")))
//
//    }

    fun removeAllHuds() {
        for (playerId in hudsByPlayerId.keys)
            removePlayerHud(playerId)
    }

    fun removePlayerHud(playerId: String) {
        val playerRef = Universe.get().getPlayer(UUID.fromString(playerId)) ?: return
        val world = Universe.get().getWorld(playerRef.worldUuid!!) ?: return
        LogUtil.debug("cancel Hud")
        val playerHudData = hudsByPlayerId.remove(playerId) ?: return

        world.execute {
            try {
                val player = playerRef.reference?.store?.getComponent(playerRef.reference!!, Player.getComponentType()) ?: return@execute
                playerHudData.hyUIHud.remove()
                MultipleHUD.getInstance().hideCustomHud(player, playerRef, playerHudData.hyUIHud.name)

            } catch (e: Exception) {
                LogUtil.warn(e.message!!)
            }
        }

//        val world = Universe.get().getWorld(session.world)!!
//        world.execute {
//            val hud = activeSessionHuds.remove(session.owner)
//            hud?.removeUnsafe()
//
//        }
//        val entityRef = world.getEntityRef(UUID.fromString(session.owner))!!
//        val player = world.entityStore.store.getComponent(entityRef, Player.getComponentType())!!
//        val playerRef = world.entityStore.store.getComponent(entityRef, PlayerRef.getComponentType())!!

//        world.execute {
//            val entityRef = world.getEntityRef(UUID.fromString(session.owner))!!
//            val player = world.entityStore.store.getComponent(entityRef, Player.getComponentType())!!
//            val playerRef = world.entityStore.store.getComponent(entityRef, PlayerRef.getComponentType())!!
//
//            MHudManager.hideCustomHud(player, playerRef, "ActiveSessionHud")
//            val hud = activeSessionHuds.remove(playerRef.uuid.toString())
//        }
    }
}

data class ActiveSessionHudData(
    val player: Player,
    val playerRef: PlayerRef,
    val sessionId: String,
    val playerId: String,
    val hyUIHud: HyUIHud,
    var hasUpdate: Boolean
)