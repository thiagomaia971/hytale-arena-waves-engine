package com.miilhozinho.arenawavesengine.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.jvm.java

class ConfigLoader(
    private val dataDir: Path
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun createOrLoad(fileName: String): ArenaWavesEngineConfig {
        Files.createDirectories(dataDir)
        val file = dataDir.resolve("$fileName.json")
        if (file.exists()){
            LogUtil.info("Loaded default config at: $file")
            return load(file)
        }

        val defaults = ArenaWavesEngineConfig()
        Files.writeString(file, gson.toJson(defaults))
        LogUtil.info("Created default config at: $file")
        return defaults
    }

    private fun load(file: Path): ArenaWavesEngineConfig {
        return try {
            val raw = Files.readString(file)
            val loaded = gson.fromJson(raw, ArenaWavesEngineConfig::class.java) ?: ArenaWavesEngineConfig()
            loaded.validate()
        } catch (e: Exception) {
            LogUtil.warn("Failed to load $file, using defaults. error=${e.message}")
            ArenaWavesEngineConfig()
        }
    }
}
