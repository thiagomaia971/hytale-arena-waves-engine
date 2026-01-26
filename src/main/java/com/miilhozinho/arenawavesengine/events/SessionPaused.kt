package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.event.IEvent

class SessionPaused : IEvent<Void> {
    var sessionId: String? = null
    var pauseAll: Boolean = false
    var despawn: Boolean = true
}

