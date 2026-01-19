package com.miilhozinho.arenawavesengine.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * Logic Layer: Simplified JSON serialization and business rule validation.
 * Uses Gson for serialization since the Hytale Codec API seems to have limitations
 * with complex nested structures. Provides thread-safe, validated operations.
 */
object ConfigCodecs {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Validates an ArenaConfig with business rules and structural integrity
     */
    fun validateArenaConfig(config: ArenaConfig): Result<ArenaConfig> = runCatching {
        // Structural validation (init blocks handle this)
        config.validateBusinessRules()
    }

    /**
     * Safely deserializes and validates an ArenaConfig from JSON string
     */
    fun deserializeArenaConfig(jsonString: String): Result<ArenaConfig> = runCatching {
        val config = gson.fromJson(jsonString, ArenaConfig::class.java)
            ?: throw IllegalStateException("Failed to parse JSON")
        validateArenaConfig(config).getOrThrow()
    }

    /**
     * Safely serializes an ArenaConfig to JSON string with validation
     */
    fun serializeArenaConfig(config: ArenaConfig): Result<String> = runCatching {
        validateArenaConfig(config).getOrThrow()
        gson.toJson(config)
    }

    /**
     * Deserializes from byte array (UTF-8 encoded JSON)
     */
    fun deserializeArenaConfig(jsonBytes: ByteArray): Result<ArenaConfig> = runCatching {
        val jsonString = String(jsonBytes, Charsets.UTF_8)
        deserializeArenaConfig(jsonString).getOrThrow()
    }

    /**
     * Serializes to byte array (UTF-8 encoded JSON)
     */
    fun serializeArenaConfigToBytes(config: ArenaConfig): Result<ByteArray> = runCatching {
        val jsonString = serializeArenaConfig(config).getOrThrow()
        jsonString.toByteArray(Charsets.UTF_8)
    }
}
