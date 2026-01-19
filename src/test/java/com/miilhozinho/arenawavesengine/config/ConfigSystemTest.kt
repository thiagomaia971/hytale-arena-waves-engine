//package com.miilhozinho.arenawavesengine.config
//
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.*
//import org.junit.jupiter.api.Assertions.*
//import java.nio.file.Files
//import java.nio.file.Path
//import kotlin.io.path.createTempDirectory
//import kotlin.io.path.deleteIfExists
//
///**
// * Comprehensive test suite for the new configuration system architecture
// */
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class ConfigSystemTest {
//
//    private lateinit var tempDir: Path
//    private lateinit var configPath: Path
//    private lateinit var backupPath: Path
//
//    @BeforeAll
//    fun setup() {
//        tempDir = createTempDirectory("arena-config-test")
//        configPath = tempDir.resolve("ArenaWavesEngine.json")
//        backupPath = configPath.resolveSibling("${configPath.fileName}.backup")
//    }
//
//    @AfterAll
//    fun cleanup() {
//        // Clean up all test files
//        configPath.deleteIfExists()
//        backupPath.deleteIfExists()
//        Files.deleteIfExists(tempDir)
//    }
//
//    @BeforeEach
//    fun resetFiles() {
//        configPath.deleteIfExists()
//        backupPath.deleteIfExists()
//    }
//
//    @Test
//    fun `test data models validation - valid config`() {
//        val validConfig = ArenaConfig(
//            enabled = true,
//            defaultWaveCount = 5,
//            defaultMobsPerWave = 10,
//            defaultSpawnIntervalSeconds = 30,
//            defaultSpawnRadius = 50.0,
//            maxConcurrentMobsPerSession = 50,
//            maxConcurrentSessionsGlobal = 3,
//            cleanupTimeoutSeconds = 300,
//            debugLoggingEnabled = false,
//            arenaMapDefinitions = listOf(
//                ArenaMapDefinition(
//                    id = "test-map",
//                    name = "Test Arena",
//                    description = "A test arena map",
//                    waves = listOf(
//                        WaveDefinition(
//                            interval = 30,
//                            enemies = listOf(
//                                EnemyDefinition(
//                                    enemyType = "Skeleton",
//                                    count = 5,
//                                    radius = 10.0,
//                                    flockSize = 2,
//                                    frozen = false,
//                                    scale = 1.0,
//                                    spawnOnGround = true
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//            arenaSessions = listOf("session-1", "session-2")
//        )
//
//        // Should not throw exception
//        assertDoesNotThrow { validConfig.validateBusinessRules() }
//        assertEquals(1, validConfig.arenaMapDefinitions.size)
//        assertEquals("test-map", validConfig.arenaMapDefinitions[0].id)
//    }
//
//    @Test
//    fun `test data models validation - invalid config throws exceptions`() {
//        // Test negative wave count
//        assertThrows<IllegalArgumentException> {
//            ArenaConfig(defaultWaveCount = -1)
//        }
//
//        // Test negative spawn radius
//        assertThrows<IllegalArgumentException> {
//            ArenaConfig(defaultSpawnRadius = -5.0)
//        }
//
//        // Test empty enemy type
//        assertThrows<IllegalArgumentException> {
//            EnemyDefinition(
//                enemyType = "",
//                count = 1,
//                radius = 1.0,
//                flockSize = 1,
//                frozen = false,
//                scale = 1.0,
//                spawnOnGround = false
//            )
//        }
//
//        // Test flock size exceeding count
//        assertThrows<IllegalArgumentException> {
//            EnemyDefinition(
//                enemyType = "Skeleton",
//                count = 2,
//                radius = 1.0,
//                flockSize = 5, // Exceeds count
//                frozen = false,
//                scale = 1.0,
//                spawnOnGround = false
//            )
//        }
//
//        // Test business rule validation
//        assertThrows<IllegalArgumentException> {
//            ArenaConfig(maxConcurrentSessionsGlobal = 15).validateBusinessRules() // Exceeds limit of 10
//        }
//    }
//
//    @Test
//    fun `test codec serialization and deserialization`() = runBlocking {
//        val originalConfig = ArenaConfig(
//            enabled = true,
//            defaultWaveCount = 3,
//            debugLoggingEnabled = true,
//            arenaMapDefinitions = listOf(
//                ArenaMapDefinition(
//                    id = "codec-test-map",
//                    name = "Codec Test Map",
//                    description = "Testing codec serialization",
//                    waves = listOf(
//                        WaveDefinition(
//                            interval = 45,
//                            enemies = listOf(
//                                EnemyDefinition(
//                                    enemyType = "Zombie",
//                                    count = 3,
//                                    radius = 15.0,
//                                    flockSize = 1,
//                                    frozen = true,
//                                    scale = 1.5,
//                                    spawnOnGround = false
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//            arenaSessions = listOf("codec-session-1")
//        )
//
//        // Test serialization
//        val serialized = ConfigCodecs.serializeArenaConfig(originalConfig)
//        assertTrue(serialized.isSuccess)
//
//        // Test deserialization
//        val deserialized = ConfigCodecs.deserializeArenaConfig(serialized.getOrThrow())
//        assertTrue(deserialized.isSuccess)
//
//        val resultConfig = deserialized.getOrThrow()
//
//        // Verify all fields match
//        assertEquals(originalConfig.enabled, resultConfig.enabled)
//        assertEquals(originalConfig.defaultWaveCount, resultConfig.defaultWaveCount)
//        assertEquals(originalConfig.debugLoggingEnabled, resultConfig.debugLoggingEnabled)
//        assertEquals(originalConfig.arenaMapDefinitions.size, resultConfig.arenaMapDefinitions.size)
//        assertEquals(originalConfig.arenaSessions, resultConfig.arenaSessions)
//
//        // Verify nested structures
//        val originalMap = originalConfig.arenaMapDefinitions[0]
//        val resultMap = resultConfig.arenaMapDefinitions[0]
//        assertEquals(originalMap.id, resultMap.id)
//        assertEquals(originalMap.name, resultMap.name)
//        assertEquals(originalMap.description, resultMap.description)
//
//        val originalWave = originalMap.waves[0]
//        val resultWave = resultMap.waves[0]
//        assertEquals(originalWave.interval, resultWave.interval)
//
//        val originalEnemy = originalWave.enemies[0]
//        val resultEnemy = resultWave.enemies[0]
//        assertEquals(originalEnemy.enemyType, resultEnemy.enemyType)
//        assertEquals(originalEnemy.count, resultEnemy.count)
//        assertEquals(originalEnemy.radius, resultEnemy.radius)
//        assertEquals(originalEnemy.frozen, resultEnemy.frozen)
//        assertEquals(originalEnemy.scale, resultEnemy.scale)
//    }
//
//    @Test
//    fun `test config provider - save and load`() = runBlocking {
//        val provider = ConfigProvider.createForTesting(configPath)
//        assertFalse(provider.configExists())
//
//        val testConfig = ArenaConfig(
//            enabled = false,
//            defaultWaveCount = 7,
//            debugLoggingEnabled = true,
//            arenaMapDefinitions = emptyList(),
//            arenaSessions = listOf("test-session")
//        )
//
//        // Save config
//        val saveResult = provider.saveConfig(testConfig)
//        assertTrue(saveResult.isSuccess)
//        assertTrue(provider.configExists())
//
//        // Load config
//        val loadResult = provider.loadConfig()
//        assertTrue(loadResult.isSuccess)
//
//        val loadedConfig = loadResult.getOrThrow()
//        assertEquals(testConfig.enabled, loadedConfig.enabled)
//        assertEquals(testConfig.defaultWaveCount, loadedConfig.defaultWaveCount)
//        assertEquals(testConfig.debugLoggingEnabled, loadedConfig.debugLoggingEnabled)
//        assertEquals(testConfig.arenaSessions, loadedConfig.arenaSessions)
//
//        provider.shutdown()
//    }
//
//    @Test
//    fun `test config provider - backup recovery`() = runBlocking {
//        val provider = ConfigProvider.createForTesting(configPath)
//
//        val originalConfig = ArenaConfig(
//            enabled = true,
//            defaultWaveCount = 10,
//            debugLoggingEnabled = false
//        )
//
//        // Save original config
//        provider.saveConfig(originalConfig).getOrThrow()
//
//        // Corrupt the main config file by writing invalid JSON
//        Files.write(configPath, "invalid json content".toByteArray())
//
//        // Try to load - should fail on main file and recover from backup
//        val loadResult = provider.loadConfig()
//        assertTrue(loadResult.isSuccess)
//
//        val recoveredConfig = loadResult.getOrThrow()
//        assertEquals(originalConfig.enabled, recoveredConfig.enabled)
//        assertEquals(originalConfig.defaultWaveCount, recoveredConfig.defaultWaveCount)
//
//        provider.shutdown()
//    }
//
//    @Test
//    fun `test config provider - atomic writes and caching`() = runBlocking {
//        val provider = ConfigProvider.createForTesting(configPath)
//
//        val config1 = ArenaConfig(defaultWaveCount = 5)
//        val config2 = ArenaConfig(defaultWaveCount = 10)
//
//        // Save first config
//        provider.saveConfig(config1).getOrThrow()
//
//        // Get cached config
//        val cachedResult = provider.getCachedConfig()
//        assertTrue(cachedResult.isSuccess)
//        assertEquals(5, cachedResult.getOrThrow().defaultWaveCount)
//
//        // Update config
//        val updateResult = provider.updateConfig { it.copy(defaultWaveCount = 10) }
//        assertTrue(updateResult.isSuccess)
//        assertEquals(10, updateResult.getOrThrow().defaultWaveCount)
//
//        // Verify on disk
//        val reloadedResult = provider.reloadConfig()
//        assertTrue(reloadedResult.isSuccess)
//        assertEquals(10, reloadedResult.getOrThrow().defaultWaveCount)
//
//        provider.shutdown()
//    }
//
//    @Test
//    fun `test extension functions for safe operations`() = runBlocking {
//        val provider = ConfigProvider.createForTesting(configPath)
//
//        val validConfig = ArenaConfig(defaultWaveCount = 8)
//        val invalidConfig = ArenaConfig(defaultWaveCount = -5) // Will fail validation
//
//        // Test safe save
//        assertTrue(provider.saveConfigSafe(validConfig))
//        assertFalse(provider.saveConfigSafe(invalidConfig))
//
//        // Test load with default fallback
//        val loadedConfig = provider.loadConfigOrDefault(ArenaConfig(defaultWaveCount = 99))
//        assertEquals(8, loadedConfig.defaultWaveCount)
//
//        provider.shutdown()
//    }
//
//    @Test
//    fun `test legacy config wrapper integration`() = runBlocking {
//        val legacyConfig = ArenaWavesEngineConfig()
//        legacyConfig.initializeWithProvider(tempDir)
//
//        // Verify initial state (should be defaults)
//        assertTrue(legacyConfig.enabled)
//        assertEquals(5, legacyConfig.defaultWaveCount)
//
//        // Modify and save
//        legacyConfig.saveAsync()
//
//        // Reload and verify
//        legacyConfig.reloadAsync()
//        assertTrue(legacyConfig.enabled)
//        assertEquals(5, legacyConfig.defaultWaveCount)
//    }
//
//    @Test
//    fun `test comprehensive validation scenarios`() {
//        // Test all validation rules
//        val testCases = listOf(
//            // Valid configurations
//            ArenaConfig(
//                defaultWaveCount = 1,
//                defaultMobsPerWave = 1,
//                defaultSpawnIntervalSeconds = 1,
//                defaultSpawnRadius = 0.1,
//                maxConcurrentMobsPerSession = 1,
//                maxConcurrentSessionsGlobal = 1,
//                cleanupTimeoutSeconds = 60,
//                arenaMapDefinitions = listOf(
//                    ArenaMapDefinition(
//                        id = "a",
//                        name = "b",
//                        description = "",
//                        waves = listOf(
//                            WaveDefinition(
//                                interval = 1,
//                                enemies = listOf(
//                                    EnemyDefinition(
//                                        enemyType = "x",
//                                        count = 1,
//                                        radius = 0.1,
//                                        flockSize = 1,
//                                        frozen = false,
//                                        scale = 0.1,
//                                        spawnOnGround = false
//                                    )
//                                )
//                            )
//                        )
//                    )
//                )
//            ),
//
//            // Edge case: maximum allowed sessions
//            ArenaConfig(maxConcurrentSessionsGlobal = 10)
//        )
//
//        testCases.forEach { config ->
//            assertDoesNotThrow { config.validateBusinessRules() }
//        }
//
//        // Test invalid configurations
//        val invalidCases = listOf(
//            // Too many concurrent sessions
//            ArenaConfig(maxConcurrentSessionsGlobal = 11),
//            // Cleanup timeout too short
//            ArenaConfig(cleanupTimeoutSeconds = 59),
//            // Empty arena maps list when validation requires at least one wave
//            ArenaConfig(arenaMapDefinitions = listOf(
//                ArenaMapDefinition("id", "name", "", emptyList())
//            ))
//        )
//
//        invalidCases.forEach { config ->
//            assertThrows<IllegalArgumentException> { config.validateBusinessRules() }
//        }
//    }
//
//    @Test
//    fun `test codec handles complex nested structures correctly`() = runBlocking {
//        // Create a complex config with multiple nested levels
//        val complexConfig = ArenaConfig(
//            arenaMapDefinitions = listOf(
//                ArenaMapDefinition(
//                    id = "complex-map",
//                    name = "Complex Test Map",
//                    description = "Testing complex nested structures",
//                    waves = listOf(
//                        WaveDefinition(
//                            interval = 30,
//                            enemies = listOf(
//                                EnemyDefinition("Skeleton", 5, 10.0, 2, false, 1.0, true),
//                                EnemyDefinition("Zombie", 3, 8.0, 1, true, 1.2, false),
//                                EnemyDefinition("Spider", 8, 12.0, 4, false, 0.8, true)
//                            )
//                        ),
//                        WaveDefinition(
//                            interval = 60,
//                            enemies = listOf(
//                                EnemyDefinition("Boss", 1, 5.0, 1, false, 2.0, false)
//                            )
//                        )
//                    )
//                ),
//                ArenaMapDefinition(
//                    id = "simple-map",
//                    name = "Simple Map",
//                    description = "Simple test map",
//                    waves = listOf(
//                        WaveDefinition(
//                            interval = 20,
//                            enemies = listOf(
//                                EnemyDefinition("Goblin", 10, 15.0, 5, false, 0.5, true)
//                            )
//                        )
//                    )
//                )
//            ),
//            arenaSessions = listOf("session-alpha", "session-beta", "session-gamma")
//        )
//
//        // Serialize and deserialize
//        val serialized = ConfigCodecs.serializeArenaConfig(complexConfig).getOrThrow()
//        val deserialized = ConfigCodecs.deserializeArenaConfig(serialized).getOrThrow()
//
//        // Verify structure is preserved
//        assertEquals(2, deserialized.arenaMapDefinitions.size)
//        assertEquals(3, deserialized.arenaSessions.size)
//
//        // Verify first map
//        val firstMap = deserialized.arenaMapDefinitions[0]
//        assertEquals("complex-map", firstMap.id)
//        assertEquals(2, firstMap.waves.size)
//
//        // Verify first wave has 3 enemies
//        val firstWave = firstMap.waves[0]
//        assertEquals(30, firstWave.interval)
//        assertEquals(3, firstWave.enemies.size)
//
//        // Verify specific enemy
//        val skeletonEnemy = firstWave.enemies[0]
//        assertEquals("Skeleton", skeletonEnemy.enemyType)
//        assertEquals(5, skeletonEnemy.count)
//        assertEquals(10.0, skeletonEnemy.radius)
//        assertEquals(2, skeletonEnemy.flockSize)
//        assertFalse(skeletonEnemy.frozen)
//        assertEquals(1.0, skeletonEnemy.scale)
//        assertTrue(skeletonEnemy.spawnOnGround)
//    }
//}
