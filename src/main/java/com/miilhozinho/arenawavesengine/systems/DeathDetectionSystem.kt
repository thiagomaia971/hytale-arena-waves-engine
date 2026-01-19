package com.miilhozinho.arenawavesengine.systems

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.CommandBuffer
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes
import com.hypixel.hytale.server.core.universe.Universe
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.miilhozinho.arenawavesengine.events.EntityKilled
import com.miilhozinho.arenawavesengine.events.SessionStarted
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.*

class DeathDetectionSystem() : EntityTickingSystem<EntityStore>() {

    override fun tick(
        dt: Float,
        index: Int,
        archetypeChunk: ArchetypeChunk<EntityStore?>,
        store: Store<EntityStore?>,
        commandBuffer: CommandBuffer<EntityStore?>
    ) {
        val entityRef = archetypeChunk.getReferenceTo(index)
        if (entityRef != null && entityRef.isValid()) {
            val deathComponent = store.getComponent<DeathComponent?>(entityRef, DeathComponent.getComponentType())
            if (deathComponent != null) {
                val uuidComponent = store.getComponent<UUIDComponent?>(entityRef, UUIDComponent.getComponentType())
                if (uuidComponent != null) {
                    val entityUuid = uuidComponent.getUuid()
                    val event = EntityKilled().apply {
                        this.entityId = entityUuid
                    }
                    HytaleServer.get().eventBus.dispatchFor(EntityKilled::class.java).dispatch(event)


//                    if (!this.processedDeaths.containsKey(entityUuid)) {
//                        this.processedDeaths.put(entityUuid, true)
//                        var entityName = this.entityNames.remove(entityUuid) as String?
//                        if (entityName == null) {
//                            val npcEntity = store.getComponent<NPCEntity?>(entityRef, NPCEntity.getComponentType()!!)
//                            if (npcEntity != null) {
//                                entityName = npcEntity.getNPCTypeId()
//                            } else {
//                                entityName = "Unknown Entity"
//                            }
//                        }
//
//                        val displayName = entityName.replace("_", " ")
//                        val attackerUuid = this.lastAttackers.remove(entityUuid) as UUID?
//                        if (attackerUuid != null) {
//                            val playerRef = Universe.get().getPlayer(attackerUuid)
//                            if (playerRef != null) {
//                                val playerName = playerRef.getUsername()
//                                var maxHealth = 0.0
//                                var maxHealthInfo = "N/A"
//                                val statMapType = EntityStatsModule.get().getEntityStatMapComponentType()
//                                val statMap = store.getComponent<EntityStatMap?>(entityRef, statMapType)
//                                if (statMap != null) {
//                                    val healthIndex = DefaultEntityStatTypes.getHealth()
//                                    val healthStat = statMap.get(healthIndex)
//                                    if (healthStat != null) {
//                                        maxHealth = healthStat.getMax().toDouble()
//                                        maxHealthInfo = maxHealth.toString()
//                                    }
//                                }
//
//                                if (maxHealth > 0.0) {
//                                    val xp: Double = this.levelingService.calculateXPFromMaxHealth(
//                                        maxHealth,
//                                        this.config.get() as LevelingConfig?
//                                    )
//                                    (DeathDetectionSystem.LOGGER.atInfo() as HytaleLogger.Api).log(
//                                        ">>> CALCULATED XP: " + playerName + " should gain " + String.format(
//                                            "%.1f",
//                                            xp
//                                        ) + " XP from " + entityName + " (maxHealth: " + maxHealth + ")"
//                                    )
//                                    if (xp > 0.0) {
//                                        this.levelingService.addExperience(
//                                            playerRef,
//                                            xp,
//                                            this.config.get() as LevelingConfig?,
//                                            commandBuffer
//                                        )
//                                        (DeathDetectionSystem.LOGGER.atInfo() as HytaleLogger.Api).log(
//                                            ">>> XP AWARDED: " + playerName + " gained " + String.format(
//                                                "%.1f",
//                                                xp
//                                            ) + " XP from killing " + entityName
//                                        )
//                                    } else {
//                                        (DeathDetectionSystem.LOGGER.atWarning() as HytaleLogger.Api).log(">>> XP is 0 or negative, not awarding XP")
//                                    }
//                                } else {
//                                    (DeathDetectionSystem.LOGGER.atWarning() as HytaleLogger.Api).log(">>> Max health is 0 or not found for entity: " + entityName + ", cannot award XP")
//                                }
//
//                                val message = String.format(
//                                    "<color:yellow>You killed: </color><color:gold>%s</color>",
//                                    displayName
//                                )
//                                NotificationHelper.sendNotification(playerRef, message)
//                                (DeathDetectionSystem.LOGGER.atInfo() as HytaleLogger.Api).log(">>> PLAYER KILL: " + playerName + " killed " + entityName + " (UUID: " + entityUuid.toString() + ", Max Health: " + maxHealthInfo + ")")
//                            }
//                        }
//                    }
                }
            }
        }

    }

    override fun getQuery(): Query<EntityStore?>? {
        return DeathComponent.getComponentType()
    }
}