package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.event.IEvent

class DamageDealt : IEvent<Void> {
    lateinit var victimId: String
    lateinit var attackerId: String
    var damage: Float = 0.0f
}