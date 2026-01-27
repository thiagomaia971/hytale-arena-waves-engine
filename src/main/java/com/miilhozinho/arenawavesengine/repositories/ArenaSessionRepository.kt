package com.miilhozinho.arenawavesengine.repositories

import com.google.gson.Gson
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.config.ArenaSession
import com.miilhozinho.arenawavesengine.config.EventDataLog
import com.miilhozinho.arenawavesengine.config.SessionsConfig
import com.miilhozinho.arenawavesengine.domain.WaveState
import com.miilhozinho.arenawavesengine.repositories.base.Repository
import com.miilhozinho.arenawavesengine.service.PlayerMessageManager
import kotlin.collections.toTypedArray

class ArenaSessionRepository(fileConfig: Config<SessionsConfig>) : Repository<SessionsConfig>(SessionsConfig::class.java, fileConfig) {

    fun getSession(sessionId: String): ArenaSession? {
        return get().sessions[sessionId]
    }

    fun saveSession(session: ArenaSession, eventName: String? = null): Boolean {
        if (!markToSave) return false
        val oldSession = oldState.sessions[session.id]
        val config = fileConfig.get() as SessionsConfig

        config.sessions.getOrPut(session.id) { session }
//        config.sessions[session.id] = session
        currentConfig?.sessions[session.id] = session

        save(true)

        val newSessionJson = Gson().toJson(session)
        ArenaWavesEngine.eventRepository.addLog(
            session.id,
            EventDataLog().apply {
                this.event = eventName
                this.oldState = oldSession?.state.toString()
                this.newState = session.state.toString()
                this.oldSession = Gson().toJson(oldSession)
                this.newSession = newSessionJson
            }
        )
        val world = Universe.get().getWorld(session.world) ?: return true
        world.execute {
            PlayerMessageManager.sendMessageDebug(
                session.owner,
                Message.raw("Session ${session.id} updated (${oldSession?.state} -> ${session.state})"))
        }
        return true
    }

    fun getActiveSessions(): Array<ArenaSession> {
        return get().sessions.values.filter {
            it.state != WaveState.COMPLETED &&
                    it.state != WaveState.STOPPED &&
                    it.state != WaveState.FAILED }.toTypedArray()
    }
}