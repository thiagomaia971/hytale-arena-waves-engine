package com.miilhozinho.arenawavesengine.components

import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.ArenaWavesEngine

class EnemyDeathRegisteredComponent(): Component<EntityStore?> {
    override fun clone(): Component<EntityStore?>? {
        return EnemyDeathRegisteredComponent()
    }

    companion object {
        fun getComponentType(): ComponentType<EntityStore?, EnemyDeathRegisteredComponent> {
            return ArenaWavesEngine.enemyDeathRegisteredComponentType
        }
    }
}