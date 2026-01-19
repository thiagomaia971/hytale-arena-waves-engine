package com.miilhozinho.arenawavesengine.domain

import com.google.gson.*
import com.hypixel.hytale.math.vector.Vector3d
import java.lang.reflect.Type

class Vector3dAdapter : JsonSerializer<Vector3d>, JsonDeserializer<Vector3d> {

    override fun serialize(src: Vector3d, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("x", src.x)
        obj.addProperty("y", src.y)
        obj.addProperty("z", src.z)
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Vector3d {
        val obj = json.asJsonObject
        val x = obj.get("x").asDouble
        val y = obj.get("y").asDouble
        val z = obj.get("z").asDouble
        return Vector3d(x, y, z)
    }
}
