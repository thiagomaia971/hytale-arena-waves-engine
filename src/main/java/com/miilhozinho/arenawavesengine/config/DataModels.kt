package com.miilhozinho.arenawavesengine.config

/**
 * Data Layer: Immutable data classes representing the JSON structure for Hytale Arena Configuration.
 * These classes ensure immutability and easy copying using Kotlin's data class features.
 */

data class ArenaConfig(
    val enabled: Boolean = true,
    val defaultWaveCount: Int = 5,
    val defaultMobsPerWave: Int = 10,
    val defaultSpawnIntervalSeconds: Int = 30,
    val defaultSpawnRadius: Double = 50.0,
    val maxConcurrentMobsPerSession: Int = 50,
    val maxConcurrentSessionsGlobal: Int = 3,
    val debugLoggingEnabled: Boolean = false,
    val arenaMapDefinitions: List<ArenaMapDefinition> = emptyList(),
    val arenaSessions: List<String> = emptyList()
) {
    init {
        require(defaultWaveCount > 0) { "defaultWaveCount must be positive" }
        require(defaultMobsPerWave > 0) { "defaultMobsPerWave must be positive" }
        require(defaultSpawnIntervalSeconds > 0) { "defaultSpawnIntervalSeconds must be positive" }
        require(defaultSpawnRadius > 0.0) { "defaultSpawnRadius must be positive" }
        require(maxConcurrentMobsPerSession > 0) { "maxConcurrentMobsPerSession must be positive" }
        require(maxConcurrentSessionsGlobal > 0) { "maxConcurrentSessionsGlobal must be positive" }
        // Validate nested structures
        arenaMapDefinitions.forEach { it.validate() }
    }

    /**
     * Validates business rules and constraints
     */
    fun validateBusinessRules(): ArenaConfig {
        // Business rule: max concurrent sessions should not exceed a reasonable limit
        require(maxConcurrentSessionsGlobal <= 10) { "maxConcurrentSessionsGlobal cannot exceed 10" }
        // Business rule: cleanup timeout should be reasonable
        // Business rule: ensure arena map definitions are valid
        require(arenaMapDefinitions.all { it.waves.isNotEmpty() }) { "All arena maps must have at least one wave" }
        return this
    }
}

