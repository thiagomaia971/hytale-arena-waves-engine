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
import com.miilhozinho.arenawavesengine.components.EnemyComponent
import com.miilhozinho.arenawavesengine.events.SessionUpdated
import com.miilhozinho.arenawavesengine.repositories.ArenaSessionRepository
import com.miilhozinho.arenawavesengine.repositories.ArenaWavesEngineRepository

class DamageTrackingSystem(
    private val repository: ArenaWavesEngineRepository,
    private val sessionRepository: ArenaSessionRepository
) : DamageEventSystem() {

    override fun handle(
        index: Int, archetypeChunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        commandBuffer: CommandBuffer<EntityStore>,
        damage: Damage)
    {
        val targetRef = archetypeChunk.getReferenceTo(index)
        if (targetRef.isValid) {
            val enemyComponent = store.getComponent(targetRef, EnemyComponent.getComponentType()) ?: return
            if (enemyComponent.sessionId == null) return

            val session = sessionRepository.getSession(enemyComponent.sessionId!!) ?: return
            val currentWave = session.getOrCreateCurrentWave()

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
                .dispatch(SessionUpdated(sessionRepository.getSession(enemyComponent.sessionId!!)!!))
        }
    }

    override fun getQuery(): Query<EntityStore?>? {
        return Query.and(EnemyComponent.getComponentType())
    }
}
