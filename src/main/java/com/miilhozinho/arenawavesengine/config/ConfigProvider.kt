package com.miilhozinho.arenawavesengine.config

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

/**
 * IO Layer: Asynchronous Provider using Coroutines to handle disk operations.
 * Provides thread-safe, atomic configuration file operations with backup and recovery.
 */
class ConfigProvider(
    private val configPath: Path,
    private val backupPath: Path = configPath.resolveSibling("${configPath.fileName}.backup"),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val mutex = Mutex()
    private var cachedConfig: ArenaConfig? = null
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    /**
     * Loads configuration from disk with validation and caching
     */
    suspend fun loadConfig(): Result<ArenaConfig> = withContext(dispatcher) {
        mutex.withLock {
            try {
                // Check if file exists
                if (!configPath.exists() || !configPath.isRegularFile()) {
                    // Create default config if file doesn't exist
                    val defaultConfig = ArenaConfig()
                    saveConfig(defaultConfig).getOrThrow()
                    cachedConfig = defaultConfig
                    return@withLock Result.success(defaultConfig)
                }

                // Read and deserialize
                val jsonBytes = Files.readAllBytes(configPath)
                val config = ConfigCodecs.deserializeArenaConfig(jsonBytes).getOrThrow()

                // Cache the loaded config
                cachedConfig = config
                Result.success(config)

            } catch (e: Exception) {
                // Try to load from backup if main file is corrupted
                if (backupPath.exists() && backupPath.isRegularFile()) {
                    try {
                        val backupBytes = Files.readAllBytes(backupPath)
                        val backupConfig = ConfigCodecs.deserializeArenaConfig(backupBytes).getOrThrow()
                        cachedConfig = backupConfig
                        Result.success(backupConfig)
                    } catch (backupException: Exception) {
                        Result.failure(IOException("Both main config and backup are corrupted", e))
                    }
                } else {
                    Result.failure(IOException("Failed to load config", e))
                }
            }
        }
    }

    /**
     * Saves configuration to disk with atomic write and backup creation
     */
    suspend fun saveConfig(config: ArenaConfig): Result<Unit> = withContext(dispatcher) {
        mutex.withLock {
            try {
                // Validate config before saving
                ConfigCodecs.validateArenaConfig(config).getOrThrow()

                // Serialize to JSON
                val jsonBytes = ConfigCodecs.serializeArenaConfigToBytes(config).getOrThrow()

                // Create temporary file for atomic write
                val tempPath = configPath.resolveSibling("${configPath.fileName}.tmp")

                // Write to temporary file first
                Files.write(tempPath, jsonBytes)

                // Create backup of current config if it exists
                if (configPath.exists()) {
                    Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING)
                }

                // Atomically move temporary file to final location
                Files.move(tempPath, configPath, StandardCopyOption.REPLACE_EXISTING)

                // Update cache
                cachedConfig = config

                Result.success(Unit)

            } catch (e: Exception) {
                Result.failure(IOException("Failed to save config", e))
            }
        }
    }

    /**
     * Gets cached configuration if available, otherwise loads from disk
     */
    suspend fun getCachedConfig(): Result<ArenaConfig> = withContext(dispatcher) {
        mutex.withLock {
            cachedConfig?.let { Result.success(it) } ?: loadConfig()
        }
    }

    /**
     * Forces a reload of configuration from disk
     */
    suspend fun reloadConfig(): Result<ArenaConfig> = withContext(dispatcher) {
        mutex.withLock {
            cachedConfig = null
            loadConfig()
        }
    }

    /**
     * Updates configuration using a transform function with optimistic locking
     */
    suspend fun updateConfig(transform: (ArenaConfig) -> ArenaConfig): Result<ArenaConfig> = withContext(dispatcher) {
        mutex.withLock {
            try {
                val currentConfig = getCachedConfig().getOrThrow()
                val updatedConfig = transform(currentConfig)
                saveConfig(updatedConfig).getOrThrow()
                Result.success(updatedConfig)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Checks if configuration file exists
     */
    fun configExists(): Boolean = configPath.exists() && configPath.isRegularFile()

    /**
     * Gets configuration file path
     */
    fun getConfigPath(): Path = configPath

    /**
     * Gets backup file path
     */
    fun getBackupPath(): Path = backupPath

    /**
     * Cleans up resources and cancels background operations
     */
    fun shutdown() {
        scope.cancel()
    }

    companion object {
        /**
         * Creates a ConfigProvider with default paths relative to the mod directory
         */
        fun create(modDirectory: Path): ConfigProvider {
            val configPath = modDirectory.resolve("ArenaWavesEngine.json")
            return ConfigProvider(configPath)
        }

        /**
         * Creates a ConfigProvider for testing with custom paths
         */
        fun createForTesting(configPath: Path, backupPath: Path? = null): ConfigProvider {
            return ConfigProvider(
                configPath,
                backupPath ?: configPath.resolveSibling("${configPath.fileName}.backup")
            )
        }
    }
}

/**
 * Extension function for safe configuration operations with error handling
 */
suspend fun ConfigProvider.loadConfigOrDefault(defaultConfig: ArenaConfig = ArenaConfig()): ArenaConfig {
    return loadConfig().getOrElse { e ->
        println("Warning: Failed to load config, using defaults: ${e.message}")
        defaultConfig
    }
}

/**
 * Extension function for safe configuration saving with logging
 */
suspend fun ConfigProvider.saveConfigSafe(config: ArenaConfig): Boolean {
    return saveConfig(config).fold(
        onSuccess = { true },
        onFailure = { e ->
            println("Error: Failed to save config: ${e.message}")
            false
        }
    )
}
