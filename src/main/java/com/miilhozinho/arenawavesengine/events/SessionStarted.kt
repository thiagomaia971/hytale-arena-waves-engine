package com.miilhozinho.arenawavesengine.events

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.event.IEvent
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.UUID

class SessionStarted : IEvent<Void> {
    lateinit var waveMapId: String
    lateinit var context: CommandContext
    lateinit var store: Store<EntityStore?>
    lateinit var ref: Ref<EntityStore?>
    lateinit var playerRef: PlayerRef
    lateinit var world: World
}

class SessionPaused : IEvent<Void> {
    lateinit var sessionId: UUID
}

class EntityKilled : IEvent<Void> {
    lateinit var entityId: UUID
}