package com.miilhozinho.arenawavesengine.components

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.ArenaWavesEngine

class EnemyComponent()
    : Component<EntityStore?> {
    var entityId: String? = null
    var sessionId: String? = null
    var world: String? = null

    override fun clone(): Component<EntityStore?>? {
        val component = EnemyComponent()
        component.entityId = entityId
        component.sessionId = sessionId
        component.world = world
        return component as Component<EntityStore?>?
    }

    companion object {
        fun getComponentType(): ComponentType<EntityStore?, EnemyComponent> {
            return ArenaWavesEngine.enemyComponentType
        }
    }
}