package com.miilhozinho.arenawavesengine.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.config.ArenaWavesEngineConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.java

class ConfigLoader(
    private val dataDir: Path
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun createOrLoad(fileName: String): ArenaWavesEngineConfig {
        val path = dataDir.resolve("$fileName.json")

        // 1. Define the Config wrapper
        val hytaleConfig = Config<ArenaWavesEngineConfig>(
            dataDir,
            fileName,
            ArenaWavesEngineConfig.CODEC
        )

        // 2. Check if the file exists before loading
        val exists = Files.exists(path)

        // 3. Load the data
        hytaleConfig.load()

        // 4. If it didn't exist, save the defaults to disk now
        if (!exists) {
            hytaleConfig.save()
            LogUtil.info("Created new default configuration file at: $path")
        } else {
            LogUtil.info("Loaded existing configuration from: $path")
        }

        return hytaleConfig.get()
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
