package com.miilhozinho.arenawavesengine.systems

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.entity.Entity
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.modules.entity.EntityModule
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.flock.FlockMembershipSystems
import com.miilhozinho.arenawavesengine.ArenaWavesEngine
import com.miilhozinho.arenawavesengine.components.EnemyComponent
import com.miilhozinho.arenawavesengine.components.EnemyDeathRegisteredComponent
import com.miilhozinho.arenawavesengine.events.EntityKilled

class DeathDetectionSystem(
    private val enemyComponentType: ComponentType<EntityStore?, EnemyComponent>,
    private val enemyDeathRegisteredComponentType: ComponentType<EntityStore?, EnemyDeathRegisteredComponent>
) : EntityTickingSystem<EntityStore>() {

    override fun tick(
        dt: Float,
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore?>,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        val entityRef = archetypeChunk.getReferenceTo(index)
        if (entityRef.isValid) {
            val deathComponent = store.getComponent<DeathComponent?>(entityRef, DeathComponent.getComponentType())
            if (deathComponent != null) {
                val uuidComponent = store.getComponent<UUIDComponent?>(entityRef, UUIDComponent.getComponentType())
                if (uuidComponent != null) {
                    val entityUuid = uuidComponent.uuid
                    val enemyComponent = store.getComponent(entityRef, enemyComponentType)!!
                    if (enemyComponent.sessionId == null) return
                    val event = EntityKilled().apply {
                        this.entityRoleName = enemyComponent.entityRoleName!!
                        this.sessionId = enemyComponent.sessionId!!
                        this.entityId = entityUuid.toString()
                    }

                    HytaleServer.get().eventBus.dispatchFor(EntityKilled::class.java).dispatch(event)
                    val world = Universe.get().getWorld(enemyComponent.world)!!
                    world.execute {
                        entityRef.store.addComponent(entityRef, enemyDeathRegisteredComponentType, EnemyDeathRegisteredComponent())
                    }
                }
            }
        }
    }

    override fun getQuery(): Query<EntityStore?>? {
        return Query.and(
            DeathComponent.getComponentType(),
            enemyComponentType,
            Query.not(enemyDeathRegisteredComponentType))
    }
}