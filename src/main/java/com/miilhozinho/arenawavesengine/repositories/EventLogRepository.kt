package com.miilhozinho.arenawavesengine.repositories

import com.google.gson.Gson
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.config.EventDataLog
import com.miilhozinho.arenawavesengine.config.EventLog
import com.miilhozinho.arenawavesengine.repositories.base.Repository

class EventLogRepository(fileConfig: Config<EventLog>) : Repository<EventLog>(EventLog::class.java, fileConfig) {
    fun addLog(sessionId: String, event: EventDataLog) {
        var eventFinded = get().events.getOrPut(sessionId) { listOf(event) }
        eventFinded += event
        currentConfig.events[sessionId] = eventFinded
        save(true)
    }
}