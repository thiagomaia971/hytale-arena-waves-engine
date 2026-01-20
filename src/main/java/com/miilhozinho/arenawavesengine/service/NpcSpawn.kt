package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.common.util.RandomUtil
import com.hypixel.hytale.component.AddReason
import com.hypixel.hytale.component.Holder
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.function.consumer.TriConsumer
import com.hypixel.hytale.math.shape.Box
import com.hypixel.hytale.math.util.MathUtil
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.math.vector.Vector3f
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.asset.type.model.config.Model
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule
import com.hypixel.hytale.server.core.entity.Frozen
import com.hypixel.hytale.server.core.entity.UUIDComponent
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent
import com.hypixel.hytale.server.core.modules.entity.player.ApplyRandomSkinPersistedComponent
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.flock.FlockPlugin
import com.hypixel.hytale.server.flock.config.FlockAsset
import com.hypixel.hytale.server.npc.NPCPlugin
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo
import com.hypixel.hytale.server.npc.entities.NPCEntity
import com.hypixel.hytale.server.npc.role.RoleDebugFlags
import com.hypixel.hytale.server.spawning.ISpawnableWithModel
import com.hypixel.hytale.server.spawning.SpawnTestResult
import com.hypixel.hytale.server.spawning.SpawningContext
import com.miilhozinho.arenawavesengine.domain.SpawnReturn
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Level
import javax.annotation.Nonnull
import kotlin.math.floor

class NpcSpawn {
    fun execute(
        context: CommandContext,
        store: Store<EntityStore?>,
        playerPosition: Vector3d,
        playerHeadRotation: Vector3f,
        playerBoundingBox: Box,
        world: World,
        npcBuilderInfo: BuilderInfo,
        count: Int = 1,
        radius: Double = 8.0,
        flagsString: String? = null,
        speedArg: Double? = null,
        nonRandom: Boolean = false,
        posOffset: Vector3d? = null,
        headRotation: String? = null,
        bodyRotation: String? = null,
        randomRotationArg: Boolean = false,
        facingRotation: Boolean = false,
        flockSize: Int = 1,
        frozen: Boolean = false,
        randomModel: Boolean = false,
        scaleArg: Float = 1.0F,
        bypassScaleLimitsArg: Boolean = false,
        test: Boolean = false,
        positionSet: Vector3d? = null,
        spawnOnGround: Boolean = false,
    ): SpawnReturn {

        val npcPlugin = NPCPlugin.get()
        val roleInfo = npcBuilderInfo
        val roleIndex = roleInfo.getIndex()
        val flags = if (flagsString != null) RoleDebugFlags.getFlags(
            flagsString.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) else RoleDebugFlags.getPreset("none")
        val velocity = Vector3d(Vector3d.ZERO)
        if (speedArg != null) {
            PhysicsMath.vectorFromAngles(playerHeadRotation.getYaw(), playerHeadRotation.getPitch(), velocity)
            velocity.setLength(speedArg)
        }

        val random = (if (nonRandom) Random(0L) else ThreadLocalRandom.current()) as Random
        val headRotation: Vector3f? = if (headRotation != null) this.parseVector3f(
            context,
            headRotation
        ) else null
        var randomRotation = false
        var rotation: Vector3f? = playerHeadRotation
        if (bodyRotation != null) {
            rotation = this.parseVector3f(context, bodyRotation)
        } else if (randomRotationArg) {
            randomRotation = true
        } else if (facingRotation) {
            playerHeadRotation.setY(playerHeadRotation.getY() - Math.PI.toFloat())
        }

        if (flockSize != 0) {
            npcPlugin.forceValidation(roleIndex)
            if (!npcPlugin.testAndValidateRole(roleInfo)) {
                throw GeneralCommandException(Message.translation("server.commands.npc.spawn.validation_failed"))
            } else {
                try {
                    for (i in 0..<count) {
                        val roleBuilder = npcPlugin.tryGetCachedValidRole(roleIndex)
                        requireNotNull(roleBuilder) { "Can't find a matching role builder" }

                        require(roleBuilder is ISpawnableWithModel) { "Role builder must support ISpawnableWithModel interface" }

                        val spawnable = roleBuilder as ISpawnableWithModel
                        require(roleBuilder.isSpawnable()) { "Abstract role templates cannot be spawned directly - a variant needs to be created!" }

                        val spawningContext = SpawningContext()
                        if (!spawningContext.setSpawnable(spawnable)) {
                            throw GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSetRolebuilder"))
                        }

                        var skinApplyingFunction: TriConsumer<NPCEntity?, Ref<EntityStore?>?, Store<EntityStore?>?>? =
                            null
                        var model: Model?
                        if (randomModel) {
                            val playerSkin = CosmeticsModule.get().generateRandomSkin(RandomUtil.getSecureRandom())
                            model = CosmeticsModule.get().createModel(playerSkin)
                            skinApplyingFunction =
                                TriConsumer { npcEntity: NPCEntity?, entityStoreRef: Ref<EntityStore?>?, entityStore: Store<EntityStore?>? ->
                                    entityStore!!.putComponent<PlayerSkinComponent?>(
                                        entityStoreRef!!,
                                        PlayerSkinComponent.getComponentType(),
                                        PlayerSkinComponent(playerSkin)
                                    )
                                    entityStore.putComponent<ApplyRandomSkinPersistedComponent?>(
                                        entityStoreRef,
                                        ApplyRandomSkinPersistedComponent.getComponentType(),
                                        ApplyRandomSkinPersistedComponent.INSTANCE
                                    )
                                }
                        } else {
                            model = spawningContext.getModel()
                        }

                        if (randomRotation) {
                            rotation = Vector3f(0.0f, (2.0 * random.nextDouble() * Math.PI).toFloat(), 0.0f)
                        }

                        val modelAsset: ModelAsset? =
                            checkNotNull(ModelAsset.getAssetMap().getAsset(model!!.getModelAssetId()))
                        var scale = scaleArg
                        if (!bypassScaleLimitsArg) {
                            scale = MathUtil.clamp(scaleArg, modelAsset!!.getMinScale(), modelAsset.getMaxScale())
                        }

                        model = Model.createScaledModel(modelAsset!!, scale)

                        val npcRef: Ref<EntityStore?>
                        val npc: NPCEntity
                        if (count == 1 && test) {
                            if (!spawningContext.set(world, playerPosition.x, playerPosition.y, playerPosition.z)) {
                                throw GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSpawnNotEnoughSpace"))
                            }

                            if (spawnable.canSpawn(spawningContext) != SpawnTestResult.TEST_OK) {
                                throw GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSpawnNotSuitable"))
                            }

                            val spawnPosition = spawningContext.newPosition()
                            if (posOffset != null) {
                                spawnPosition.add(posOffset)
                            }

                            val npcPair = npcPlugin.spawnEntity(
                                store,
                                roleIndex,
                                spawnPosition,
                                rotation,
                                model,
                                skinApplyingFunction
                            )
                            npcRef = (npcPair!!.first()) as Ref
                            npc = npcPair.second() as NPCEntity
                            if (flockSize > 1) {
                                FlockPlugin.trySpawnFlock(
                                    npcRef,
                                    npc,
                                    store,
                                    roleIndex,
                                    spawnPosition,
                                    rotation,
                                    flockSize,
                                    skinApplyingFunction
                                )
                            }
                        } else {
                            val position: Vector3d?
                            if (positionSet != null) {
                                position = positionSet
                                position.y -= model!!.getBoundingBox()!!.min.y
                            } else {
                                position = Vector3d(playerPosition)
                                position.y =
                                    floor(position.y + playerBoundingBox.min.y + 0.01) - model!!.getBoundingBox()!!.min.y
                            }

                            if (posOffset != null) {
                                position.add(posOffset)
                            }

                            val npcPair =
                                npcPlugin.spawnEntity(store, roleIndex, position, rotation, model, skinApplyingFunction)
                            npcRef = (npcPair!!.first()) as Ref
                            npc = npcPair.second() as NPCEntity
                            if (flockSize > 1) {
                                FlockPlugin.trySpawnFlock(
                                    npcRef,
                                    npc,
                                    store,
                                    roleIndex,
                                    position,
                                    rotation,
                                    flockSize,
                                    skinApplyingFunction
                                )
                            }
                        }

                        val npcTransformComponent: TransformComponent? = checkNotNull(
                            store.getComponent<TransformComponent?>(
                                npcRef,
                                TransformComponent.getComponentType()
                            ) as TransformComponent?
                        )

                        val npcHeadRotationComponent: HeadRotation? = checkNotNull(
                            store.getComponent<HeadRotation?>(
                                npcRef,
                                HeadRotation.getComponentType()
                            ) as HeadRotation?
                        )


                        val npcUuidComponent: UUIDComponent? = checkNotNull(
                            store.getComponent<UUIDComponent?>(
                                npcRef,
                                UUIDComponent.getComponentType()
                            ) as UUIDComponent?
                        )

                        if (headRotation != null) {
                            npcHeadRotationComponent!!.getRotation().assign(headRotation)
                            store.ensureComponent<Frozen?>(npcRef, Frozen.getComponentType())
                        }

                        val npcPosition = npcTransformComponent!!.getPosition()
                        var x = npcPosition.getX()
                        var y = npcPosition.getY()
                        var z = npcPosition.getZ()
                        if (count > 1) {
                            x += random.nextDouble() * 2.0 * radius - radius
                            z += random.nextDouble() * 2.0 * radius - radius
                            y += if (spawnOnGround) 0.1 else random.nextDouble() * 2.0 + 5.0
                        } else {
                            y += 0.1
                        }

                        npcPosition.assign(x, y, z)
                        npc.saveLeashInformation(npcPosition, npcTransformComponent.getRotation())
                        if (!velocity.equals(Vector3d.ZERO)) {
                            npc.getRole()!!.forceVelocity(velocity, null as VelocityConfig?, false)
                        }

                        if (frozen) {
                            store.ensureComponent<Frozen?>(npcRef, Frozen.getComponentType())
                        }

                        val debugFlags = npc.getRoleDebugFlags().clone()
                        debugFlags.addAll(flags)
                        if (!debugFlags.isEmpty()) {
                            val holder: Holder<EntityStore?> = store.removeEntity(npcRef, RemoveReason.UNLOAD)
                            npc.setRoleDebugFlags(debugFlags)
                            store.addEntity(holder, AddReason.LOAD)
                        }

                        NPCPlugin.get().getLogger().at(Level.INFO).log(
                            "%s created with id %s at position %s",
                            npc.getRoleName(),
                            npcUuidComponent!!.getUuid(),
                            Vector3d.formatShortString(npcPosition)
                        )
                        return SpawnReturn(npc, npcRef)
                    }
                } catch (e: IllegalStateException) {
                    NPCPlugin.get().getLogger().at(Level.WARNING)
                        .log("Spawn failed: " + (e as RuntimeException).message)
                    throw GeneralCommandException(
                        Message.translation("server.commands.npc.spawn.failed")
                            .param("reason", (e as RuntimeException).message!!)
                    )
                } catch (e: NullPointerException) {
                    NPCPlugin.get().getLogger().at(Level.WARNING)
                        .log("Spawn failed: " + (e as RuntimeException).message)
                    throw GeneralCommandException(
                        Message.translation("server.commands.npc.spawn.failed")
                            .param("reason", (e as RuntimeException).message!!)
                    )
                } catch (e: IllegalArgumentException) {
                    NPCPlugin.get().getLogger().at(Level.WARNING)
                        .log("Spawn failed: " + (e as RuntimeException).message)
                    throw GeneralCommandException(
                        Message.translation("server.commands.npc.spawn.failed")
                            .param("reason", (e as RuntimeException).message!!)
                    )
                }
            }
        }
        throw GeneralCommandException(Message.raw("Failed to spawn"))
    }


    private fun parseVector3d(@Nonnull context: CommandContext, @Nonnull str: String): Vector3d? {
        val parts: Array<String?> = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 3) {
            context.sendMessage(Message.raw("Invalid Vector3d format: must be three comma-separated doubles"))
            return null
        } else {
            try {
                return Vector3d(parts[0]!!.toDouble(), parts[1]!!.toDouble(), parts[2]!!.toDouble())
            } catch (e: NumberFormatException) {
                context.sendMessage(Message.raw("Invalid Vector3d format: " + e.message))
                return null
            }
        }
    }

    private fun parseVector3f(@Nonnull context: CommandContext, @Nonnull str: String): Vector3f? {
        val parts: Array<String?> = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 3) {
            context.sendMessage(Message.raw("Invalid Vector3f format: must be three comma-separated floats"))
            return null
        } else {
            try {
                return Vector3f(parts[0]!!.toFloat(), parts[1]!!.toFloat(), parts[2]!!.toFloat())
            } catch (e: NumberFormatException) {
                context.sendMessage(Message.raw("Invalid Vector3f format: " + e.message))
                return null
            }
        }
    }

    private fun parseFlockSize(@Nonnull context: CommandContext, @Nonnull str: String): Int? {
        try {
            val size = str.toInt()
            if (size <= 0) {
                context.sendMessage(Message.raw("Flock size must be greater than 0!"))
                return null
            } else {
                return size
            }
        } catch (var5: NumberFormatException) {
            val flockDefinition = FlockAsset.getAssetMap().getAsset(str)
            if (flockDefinition == null) {
                context.sendMessage(Message.raw("No such flock asset: " + str))
                return null
            } else {
                return flockDefinition.pickFlockSize()
            }
        }
    }
}