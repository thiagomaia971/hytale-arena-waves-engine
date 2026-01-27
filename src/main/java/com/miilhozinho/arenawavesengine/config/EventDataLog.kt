package com.miilhozinho.arenawavesengine.config

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventDataLog {
    var event: String? = null
    var oldState: String? = null
    var newState: String? = null
    var oldSession: String? = null
    var newSession: String? = null
    var firedAt: String? = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
}