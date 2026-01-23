package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.event.IEvent
import com.miilhozinho.arenawavesengine.config.ArenaSession

class SessionPaused : IEvent<Void> {
    var sessionId: String? = null
    var pauseAll: Boolean = false
    var despawn: Boolean = true
}

class SessionUpdated(val session: ArenaSession): IEvent<Void>