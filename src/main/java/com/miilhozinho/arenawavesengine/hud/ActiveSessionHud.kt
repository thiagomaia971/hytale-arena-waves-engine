//package com.miilhozinho.arenawavesengine.hud
//
//import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud
//import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
//import com.hypixel.hytale.server.core.universe.PlayerRef
//import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository
//import com.miilhozinho.arenawavesengine.util.LogUtil
//
//class ActiveSessionHud(val repository: ArenaWavesEngineRepository, val sessionId: String, playerRef: PlayerRef) : CustomUIHud(playerRef) {
//    private var isBuilt = false
//
//    override fun build(builder: UICommandBuilder) {
//        try {
//            builder.append("Huds/ArenaWavesEngine/ActiveSessionHud.ui")
//            setData(builder)
//            this.isBuilt = true
//        } catch (e: Exception) {
//            LogUtil.warn("Failed to build LevelProgressHud: "+e.message)
//            this.isBuilt = false
//        }
//    }
//
//    public fun update(clear: Boolean) {
//        if (!isBuilt) return
//        val builder = UICommandBuilder()
//        setData(builder)
//        update(clear, builder)
//    }
//
//
//    private fun setData(builder: UICommandBuilder)
//    {
//        val session = repository.getSession(sessionId)!!
//        val mapDefinition = repository.get().arenaMaps.find { it.id == session.waveMapId }!!
//
//        builder.set("#TitleLabel.Text", mapDefinition.name)
//    }
//
//}