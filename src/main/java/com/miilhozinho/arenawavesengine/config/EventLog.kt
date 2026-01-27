package com.miilhozinho.arenawavesengine.config

import java.util.concurrent.ConcurrentHashMap

class EventLog {
    var events: ConcurrentHashMap<String, List<EventDataLog>> = ConcurrentHashMap()
}

