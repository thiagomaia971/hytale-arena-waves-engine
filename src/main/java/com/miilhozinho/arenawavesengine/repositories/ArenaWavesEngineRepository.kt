package com.miilhozinho.arenawavesengine.repositories

import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.config.ArenaMapDefinition
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.repositories.base.Repository
import com.miilhozinho.arenawavesengine.util.LogUtil

class ArenaWavesEngineRepository(config: Config<ArenaWavesEngineConfig>) : Repository<ArenaWavesEngineConfig>(config) {
    fun addSession(session: ArenaSession) {
        currentConfig.sessions += session
        LogUtil.debug("[Repository] Session ${session.id} added to config.")
    }

    fun getSession(sessionId: String): ArenaSession? {
        return currentConfig.sessions.find { it.id == sessionId }
    }

    fun findArenaMapDefinition(waveMapId: String): ArenaMapDefinition? {
        return currentConfig.arenaMaps.find { it.id == waveMapId }
    }

}