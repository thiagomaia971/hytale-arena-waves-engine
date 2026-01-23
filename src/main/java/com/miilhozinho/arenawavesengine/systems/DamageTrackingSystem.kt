package com.miilhozinho.arenawavesengine.systems

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.modules.entity.damage.Damage
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.EntitySource
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.miilhozinho.arenawavesengine.events.SessionUpdated
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository

class DamageTrackingSystem(val repository: ArenaWavesEngineRepository) : DamageEventSystem() {

    override fun handle(
        index: Int, archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        damage: Damage)
    {
        val targetRef = archetypeChunk.getReferenceTo(index)
        if (targetRef != null && targetRef.isValid()) {
            val targetUuidComponent =
                store.getComponent<UUIDComponent?>(targetRef, UUIDComponent.getComponentType()) ?: return

            val targetUuid = targetUuidComponent.getUuid()
            val sessionId = repository.get().entityToSessionMap[targetUuid.toString()] ?: return
            val currentWave = repository.getCurrentWave(sessionId) ?: return

            val damageSource = damage.source
            if (damageSource !is EntitySource)
                return

            val attackerRef = damageSource.ref
            if (!attackerRef.isValid)
                return

            val attackerUuidComponent =
                store.getComponent<UUIDComponent?>(attackerRef, UUIDComponent.getComponentType()) ?: return

            val attackerUuid = attackerUuidComponent.uuid.toString()

            val oldDamage = currentWave.damage.getOrDefault(attackerUuid, 0.0f)
            currentWave.damage[attackerUuid] = oldDamage + damage.amount

            repository.save()
            HytaleServer.get().eventBus.dispatchFor(SessionUpdated::class.java)
                .dispatch(SessionUpdated(repository.getSession(sessionId)!!))
        }
    }

    override fun getQuery(): Query<EntityStore?>? {
        return Query.any()
    }
}
