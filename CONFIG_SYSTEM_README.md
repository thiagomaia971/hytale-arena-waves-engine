# Hytale Arena Configuration System Implementation

This document describes the robust, thread-safe, and validated configuration system implemented for the Hytale Arena Mod using Kotlin and a three-layer architecture.

## 🏗 Architecture Overview

The system is implemented with three distinct layers:

### 1. Data Layer (`DataModels.kt`)
- **Purpose**: Immutable data classes representing the JSON structure
- **Key Features**:
  - Kotlin `data class` for immutability and easy copying
  - Comprehensive validation in `init` blocks
  - Business rule validation methods
  - Nested structure validation

**Core Data Classes:**
```kotlin
data class ArenaConfig(
    val enabled: Boolean = true,
    val defaultWaveCount: Int = 5,
    val defaultMobsPerWave: Int = 10,
    val defaultSpawnIntervalSeconds: Int = 30,
    val defaultSpawnRadius: Double = 50.0,
    val maxConcurrentMobsPerSession: Int = 50,
    val maxConcurrentSessionsGlobal: Int = 3,
    val cleanupTimeoutSeconds: Int = 300,
    val debugLoggingEnabled: Boolean = false,
    val arenaMapDefinitions: List<ArenaMapDefinition> = emptyList(),
    val arenaSessions: List<String> = emptyList()
)

data class ArenaMapDefinition(
    val id: String,
    val name: String,
    val description: String,
    val waves: List<WaveDefinition>
)

data class WaveDefinition(
    val interval: Int,
    val enemies: List<EnemySpawnDefinition>
)

data class EnemySpawnDefinition(
    val enemyType: String,
    val count: Int,
    val radius: Double,
    val flockSize: Int,
    val frozen: Boolean,
    val scale: Double,
    val spawnOnGround: Boolean
)
```

### 2. Logic Layer (`ConfigCodecs.kt`)
- **Purpose**: Serialization and business rule validation
- **Key Features**:
  - Thread-safe validation using `runCatching`
  - JSON serialization using Gson
  - Comprehensive error handling
  - Business rule enforcement

**Validation Rules:**
- All numeric values must be positive
- Flock size cannot exceed enemy count
- Maximum concurrent sessions limited to 10
- Cleanup timeout must be at least 60 seconds
- Arena maps must have at least one wave
- Waves must have at least one enemy

### 3. IO Layer (`ConfigProvider.kt`)
- **Purpose**: Asynchronous disk operations using Coroutines
- **Key Features**:
  - Thread-safe file operations with `Mutex`
  - Atomic writes with temporary files
  - Automatic backup creation and recovery
  - Configuration caching
  - Optimistic locking for updates

**Key Operations:**
- `loadConfig()`: Loads and validates configuration
- `saveConfig()`: Saves with validation and backup
- `getCachedConfig()`: Returns cached config or loads from disk
- `reloadConfig()`: Forces reload from disk
- `updateConfig()`: Atomic configuration updates

## 🔄 Integration with Legacy System

The new architecture maintains backward compatibility through `ArenaWavesEngineConfig` wrapper class:

- **Legacy Interface**: Maintains existing mutable fields and methods
- **New Architecture**: Internally uses the three-layer system
- **Data Conversion**: Bidirectional conversion between old and new data models
- **Async Operations**: Provides both sync (for compatibility) and async methods

## 🧪 Testing

Comprehensive test suite in `ConfigSystemTest.kt` covers:

- **Data Model Validation**: Tests all validation rules and edge cases
- **Codec Operations**: Serialization/deserialization with complex nested structures
- **Provider Operations**: File I/O, caching, backup recovery, atomic writes
- **Extension Functions**: Safe operations with error handling
- **Legacy Integration**: Wrapper functionality and data conversion

**Test Coverage:**
- ✅ Valid configuration creation and validation
- ✅ Invalid configuration rejection with proper exceptions
- ✅ JSON serialization/deserialization of complex nested structures
- ✅ File save/load operations with validation
- ✅ Backup recovery from corrupted files
- ✅ Atomic writes and caching behavior
- ✅ Safe operation extension functions
- ✅ Legacy wrapper integration

## 🚀 Usage Examples

### Basic Configuration Loading
```kotlin
val provider = ConfigProvider.create(modDirectory)

// Load configuration
val config = provider.loadConfig().getOrElse { defaultConfig ->
    println("Using default config: ${it.message}")
    defaultConfig
}

// Update configuration atomically
provider.updateConfig { currentConfig ->
    currentConfig.copy(enabled = false)
}
```

### Validation and Error Handling
```kotlin
val config = ArenaConfig(
    defaultWaveCount = 5,
    arenaMapDefinitions = listOf(validArenaMap)
)

// Validate business rules
ConfigCodecs.validateArenaConfig(config).fold(
    onSuccess = { validatedConfig ->
        println("Config is valid!")
    },
    onFailure = { error ->
        println("Config validation failed: ${error.message}")
    }
)
```

### Legacy Compatibility
```kotlin
// Existing code continues to work
val legacyConfig = ArenaWavesEngineConfig()
legacyConfig.initializeWithProvider(modDirectory)

// Legacy validation still works
legacyConfig.validate()

// New async operations available
runBlocking {
    legacyConfig.saveAsync()
    legacyConfig.reloadAsync()
}
```

## 🛡️ Thread Safety

The system ensures thread safety through:

- **Mutex Protection**: All file operations protected by `Mutex`
- **Immutable Data**: Kotlin `data class` ensures immutability
- **Atomic Operations**: File writes use temporary files and atomic moves
- **Coroutine Safety**: All suspend functions are main-safe

## 📊 Performance Characteristics

- **Lazy Loading**: Configuration loaded only when needed
- **Caching**: In-memory cache reduces disk I/O
- **Validation**: Only validated configurations are saved/loaded
- **Async Operations**: Non-blocking I/O operations
- **Backup Recovery**: Automatic fallback to backup on corruption

## 🔧 Configuration File Format

The system uses JSON format for human-readable configuration:

```json
{
  "enabled": true,
  "defaultWaveCount": 5,
  "defaultMobsPerWave": 10,
  "defaultSpawnIntervalSeconds": 30,
  "defaultSpawnRadius": 50.0,
  "maxConcurrentMobsPerSession": 50,
  "maxConcurrentSessionsGlobal": 3,
  "cleanupTimeoutSeconds": 300,
  "debugLoggingEnabled": false,
  "arenaMapDefinitions": [
    {
      "id": "forest-arena",
      "name": "Forest Arena",
      "description": "A challenging forest battleground",
      "waves": [
        {
          "interval": 30,
          "enemies": [
            {
              "enemyType": "Skeleton",
              "count": 5,
              "radius": 10.0,
              "flockSize": 2,
              "frozen": false,
              "scale": 1.0,
              "spawnOnGround": true
            }
          ]
        }
      ]
    }
  ],
  "arenaSessions": []
}
```

## 🎯 Benefits

1. **Type Safety**: Compile-time validation of configuration structure
2. **Thread Safety**: Concurrent access protection with mutexes
3. **Data Integrity**: Validation at all layers prevents invalid configurations
4. **Fault Tolerance**: Automatic backup recovery and graceful error handling
5. **Performance**: Caching and async operations minimize blocking
6. **Maintainability**: Clear separation of concerns across three layers
7. **Backward Compatibility**: Existing code continues to work unchanged
8. **Testability**: Comprehensive test coverage ensures reliability

## 📝 Future Enhancements

- **Configuration UI**: In-game configuration interface
- **Hot Reloading**: Runtime configuration updates without restart
- **Configuration Templates**: Predefined arena configurations
- **Validation Rules DSL**: Custom validation rule definition
- **Metrics and Monitoring**: Configuration usage statistics
- **Migration Tools**: Automatic configuration format upgrades
