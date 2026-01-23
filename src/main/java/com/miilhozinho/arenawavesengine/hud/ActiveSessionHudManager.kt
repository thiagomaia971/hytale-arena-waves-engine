package com.miilhozinho.arenawavesengine.hud

import au.ellie.hyui.builders.HudBuilder
import au.ellie.hyui.builders.HyUIHud
import au.ellie.hyui.builders.LabelBuilder
import au.ellie.hyui.builders.ProgressBarBuilder
import com.buuz135.mhud.MultipleHUD
import com.google.gson.Gson
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ActiveSessionHudManager(val repository: ArenaWavesEngineRepository) {
    var hudBuilder: HudBuilder
    var hudsByPlayerId: ConcurrentHashMap<String, ActiveSessionHudData> = ConcurrentHashMap()

    init {
        val html = """
<style>
    .active-session-hud {
        //layout-mode: Right;
        anchor-width: 400;
        anchor-height: 400;
        opacity: 0.2;
        anchor-right: 50;
        anchor-top: 50;
        //padding-right: 15;
        //flex: 1;
    }
    
    .header {
        background-image: url("Common/ContainerHeader.png");
        anchor-height: 38;
        anchor-top: 0;
    }
    
    .content {
        layout-mode: Top;
        anchor-top: 38;
        //anchor-height: 200;
        background-image: url("Common/ContainerPatch.png");
        background-border: 36;
        border: 30 30 30;
    }
</style>


    
<div class="active-session-hud">
    <div class="header">
            teste
    </div>
    <div class="content">
        content
    </div>
<!-- 
    <div class="footer">
    
    </div>
    -->
</div>
"""
        val html2 = """
                    <style>
                        /* Estilos Gerais */
                        .active-session-hud {
                            anchor-width: 500;
                            anchor-height: 400;
                            anchor-right: 25;
                            anchor-top: 40;
                        }
                        
                        .active-session-hud-container {
                            layout-mode: Top;
                        }
                        
                        .container {
                            anchor-height: 400;
                        }
                        
                        .container-contents {
                        }
                        
                        .row-display {
                            background-image: url("Common/ContainerHeader.png");
                            text-align: center;
                            anchor-height: 45;
                            vertical-align: center;
                        }
                        
                        .row-display-wave-progress {
                            background-image: url("Common/OptionBackgroundPatch.png");
                            anchor-height: 100;
                            vertical-align: center;
                        }
                        
                        .wave-progress-label {
                            text-align: center;
                        }
                        
                        .wave-progress-bar {
                            text-align: center;
                            anchor-bottom: 15;
                        }
                        
                        .wave-progress-values {
                            text-align: center;
                            anchor-top: 55;
                            font-size: 18
                        }
                        
                        .wave-progress-values-label {
                            font-size: 18
                        }
                    
                        .row-display-label {
                            layout-mode: Right;
                            vertical-align: center;
                            anchor-top: 10;
                            anchor-right: 15;
                            text-transform: uppercase; 
                            font-size: 22;
                            font-weight: bold;
                            color: #7FD9F8;
                        }
                    
                        .row-display-center-label {
                            text-transform: uppercase; 
                            font-size: 22;
                            font-weight: bold;
                            color: #7FD9F8;
                        }
                    
                        .row-display-value {
                            anchor-top: 10;
                            text-transform: uppercase; 
                            font-size: 22;
                            font-weight: bold;
                            color: #96a9be;
                        }
                        
                        
                        .state-display {
                            text-align: center;
                            anchor-top: 0;
                            anchor-bottom: 10;
                        }
                        
                        .state-display-style {
                            background-color: #FFAD42(0.4);
                            anchor-width: 300;
                            anchor-height: 25;
                            text-align: center;
                            vertical-align: center;
                        }
                    
                        .state-label {
                            anchor-top: 5;
                            color: #FFAD42;
                            font-size: 12;
                            font-weight: bold;
                            text-align: center;
                        }
                        
                        .enemy-list {
                            //background-image: url("Common/ContainerPatch.png");
                            anchor-height: 150;
                        }
                    
                        .enemy-box {
                            background-color: #0D131C;
                            layout: leftcenterwrap; /* LayoutMode para alinhar ícone e texto */
                            //border: 1px solid #3F4E63;
                        }
                    
                        .enemy-list-header {
                            color: #5D7A94;
                            font-size: 10;
                            text-transform: uppercase;
                        }
                        
                        .mob-row {
                            layout: full; /* Garante que o nome fique na esquerda e o X1 na direita */
                            margin-bottom: 2;
                        }
                    
                        .mob-text {
                            color: #D1E8FF;
                            font-size: 13;
                        }
                    
                        .mob-count {
                            color: #FFFFFF;
                            font-weight: bold;
                        }
                    
                        .footer {
                            color: #A0E8FF;
                            font-size: 12;
                        }
                    </style>
                    
                    <div class="active-session-hud">
                        <div class="active-session-hud-container">
                            <div class="container" data-hyui-title="Arena Waves Engine">
                                
                                <div class="container-contents">
                                    <div class="row-display">
                                        <p id="wave-counter-label" class="row-display-label">Wave</p><p id="row-display-value" class="row-display-value"></p>
                                    </div>
                                    <div class="state-display">
                                        <div class="state-display-style">
                                            <p id="wave-state" class="state-label">STATE: </p<p class="state-value">WAITING CLEAR</p>
                                        </div>
                                    </div>
                                    
                        <!-- 
                        
                                    <progress id="wave-progress-bar" 
                                              value="0.75" 
                                              data-hyui-direction="start"
                                              data-hyui-alignment="horizontal" />
                                    
                        -->
                                    <div class="row-display-wave-progress">
                                        <div class="wave-progress-label">
                                            <p class="row-display-center-label">Wave Progress</p>
                                        </div>
                                        <div class="wave-progress-bar">
                                            <progress id="wave-progress" value="0.7" style="anchor-width: 450; anchor-height: 20" 
                                                data-hyui-effect-width="450" 
                                                data-hyui-effect-height="20" 
                                                data-hyui-effect-offset="0">
                                            </progress>
                                        </div>
                                        <div class="wave-progress-values">
                                            <img src="Undead.png" width="25" height="30" style="anchor-top: 9;" />
                                            <p id="enemies-remaining" class="row-display-label wave-progress-values-label">Enemies Remaining: </p><p id="wave-enemies-remain" class="row-display-value wave-progress-values-label">4 / 10</p>
                                        </div>
                                    </div>
                                        
                                    <div id="enemy-list" class="enemy-list">
                                        <div class="mob-row"> 
                                            <p class="mob-text">Skeleton</p>
                                            <p style="color: #FFFFFF; font-weight: bold; text-align: right;">x3</p>
                                        </div>
                        
                                        <div class="mob-row"> 
                                            <p class="mob-text">Skeleton Archer</p>
                                            <p style="color: #FFFFFF; font-weight: bold; text-align: right;">x1</p>
                                        </div>
                                    </div>
                        
                        <!-- 
                        
                                    <div style="layout: middle; margin-top: 20;">
                                        <p id="timer" class="footer-text" style="flex-weight: 1;">Time: 01:15</p>
                                        <p id="score" class="footer-text" style="text-align: right; flex-weight: 1;">Score: 0</p>
                                    </div>
                        -->
                                </div>
                            </div>
                        </div>
                    </div>
"""

        hudBuilder = HudBuilder.detachedHud()
//            .fromFile("Hud/ArenaWavesEngine/ActiveSessionHud.ui")
            .fromHtml(html2)
            .withRefreshRate(500)
            .onRefresh { hud ->
                val playerRef = hud.playerRef
                val playerId = playerRef.uuid
                val playerDataHud = hudsByPlayerId[playerId.toString()] ?: return@onRefresh
                if (playerDataHud.hasUpdate){
                    playerDataHud.hasUpdate = false
                    val session = repository.getPlayerSession(playerId.toString()) ?: return@onRefresh
                    updateUICommand(session, null, hud)
                }
            }
//            .editElement { command ->
//                try {
//                    command.set("#TitleLabel.Text", "title labe")
//
//                }catch (e: Exception) {
//                    LogUtil.warn(e.message!!)
//                }
//            }
    }

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
                    hudBuilder
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
//            val hud = hudData.value.hyUIHud
//            updateUICommand(session, null, hud)
//            val world = Universe.get().getWorld(hud.value.playerRef.worldUuid!!) ?: return
//            world.execute {
//                val command = updateUICommand(session)
//                hud.value.hyUIHud.update(false, command)
//            }
        }


    }

    private fun updateUICommand(session: ArenaSession, commandArg: UICommandBuilder? = null, hud: HyUIHud? = null): UICommandBuilder {
        val command = commandArg ?: UICommandBuilder()
        val mapDef = repository.get().arenaMaps.find { it.id == session.waveMapId } ?: return command
        val currentWaveDef = mapDef.waves[session.currentWave] ?: return command
        val currentWaveData = session.wavesData[session.currentWave] ?: return command

        val enemiesKilled = currentWaveData.enemiesKilled
        val totalEnemiesWave = currentWaveDef.enemies.sumOf { it.count }

        LogUtil.debug("Drawing ${Gson().toJson(session)}")

        if (hud == null){
            command.set("#TitleLabel.Text", mapDef.name)
            command.set("#EnemiesAlive.Text", "$enemiesKilled/")
            command.set("#EnemiesTotalWave.Text", totalEnemiesWave.toString())
        }else {
            hud.getById("row-display-value", LabelBuilder::class.java).ifPresent { label ->
                label.withText("${session.currentWave + 1} / ${mapDef.waves.count()}")
            }
            hud.getById("wave-enemies-remain", LabelBuilder::class.java).ifPresent { label ->
                label.withText("$enemiesKilled / $totalEnemiesWave")
            }
            hud.getById("wave-progress", ProgressBarBuilder::class.java).ifPresent { progress ->
                progress.withValue((enemiesKilled.toFloat()/totalEnemiesWave.toFloat()))
            }
        }
        return command
    }

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
    val hyUIHud: HyUIHud,
    var hasUpdate: Boolean
)