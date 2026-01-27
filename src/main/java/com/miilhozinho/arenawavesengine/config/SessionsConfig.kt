package com.miilhozinho.arenawavesengine.config

import java.util.concurrent.ConcurrentHashMap

class SessionsConfig {
    var entityToSessionMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
        private set
    var sessions: ConcurrentHashMap<String, ArenaSession> = ConcurrentHashMap()
        private set
}