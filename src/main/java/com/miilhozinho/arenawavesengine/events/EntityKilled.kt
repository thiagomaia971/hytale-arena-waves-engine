package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.event.IEvent

class EntityKilled : IEvent<Void> {
    lateinit var entityRoleName: String
    lateinit var sessionId: String
    lateinit var entityId: String
}