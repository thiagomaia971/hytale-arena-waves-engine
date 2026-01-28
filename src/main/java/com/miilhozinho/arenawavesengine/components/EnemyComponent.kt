package com.miilhozinho.arenawavesengine.components

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

class EnemyComponent()
    : Component<EntityStore?> {
    var entityRoleName: String? = null
    var entityId: String? = null
    var sessionId: String? = null
    var world: String? = null

    override fun clone(): Component<EntityStore?>? {
        val component = EnemyComponent()
        component.entityRoleName = entityRoleName
        component.entityId = entityId
        component.sessionId = sessionId
        component.world = world
        return component as Component<EntityStore?>?
    }
}