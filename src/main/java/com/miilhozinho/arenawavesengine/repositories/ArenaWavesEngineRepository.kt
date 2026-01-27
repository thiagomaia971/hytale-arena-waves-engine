package com.miilhozinho.arenawavesengine.repositories

import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.config.ArenaMapDefinition
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import com.miilhozinho.arenawavesengine.repositories.base.Repository

class ArenaWavesEngineRepository(config: Config<ArenaWavesEngineConfig>) : Repository<ArenaWavesEngineConfig>(ArenaWavesEngineConfig::class.java, config) {
    fun getMapDefinition(waveMapId: String): ArenaMapDefinition? {
        return currentConfig?.arenaMaps?.find { it.id == waveMapId }
    }
}