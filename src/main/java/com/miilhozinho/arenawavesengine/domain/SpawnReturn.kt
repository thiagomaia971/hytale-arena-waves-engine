package com.miilhozinho.arenawavesengine.domain

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.hypixel.hytale.server.npc.entities.NPCEntity

data class SpawnReturn(var npc: NPCEntity, var npcRef: Ref<EntityStore?>) {
}