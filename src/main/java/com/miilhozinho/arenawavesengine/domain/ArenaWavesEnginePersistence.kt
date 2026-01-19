//package com.miilhozinho.arenawavesengine.domain
//
//import com.google.gson.Gson
//import com.google.gson.GsonBuilder
//import com.hypixel.hytale.math.vector.Vector3d
//import com.miilhozinho.arenawavesengine.domain.definitions.ArenaMapDefinition
//import com.miilhozinho.arenawavesengine.util.LogUtil
//import java.io.File
//import java.io.FileReader
//import java.io.FileWriter
//import java.nio.file.Files
//import java.nio.file.Paths
//import java.util.UUID
//
///**
// * Handles persistence of ArenaWavesEngine data to/from JSON file.
// */
//object ArenaWavesEnginePersistence {
//
//    private val gson: Gson = GsonBuilder()
//        .registerTypeAdapter(Vector3d::class.java, Vector3dAdapter())
//        .registerTypeAdapter(UUID::class.java, UUIDAdapter())
//        .setPrettyPrinting()
//        .create()
//
//    private const val DATA_FILE_NAME = "ArenaWavesEngine.json"
//
//    private fun getDataFile(): File {
//        // Use the same directory structure as the existing config
//        // In Hytale, mods are in server/mods/PluginName/
//        val modDir = Paths.get("mods", "Miilhozinho_ArenaWavesEngine")
//        Files.createDirectories(modDir)
//        return modDir.resolve(DATA_FILE_NAME).toFile()
//    }
//
//    fun loadData(): ArenaWavesEngineData {
//        val dataFile = getDataFile()
//        return try {
//            if (dataFile.exists()) {
//                FileReader(dataFile).use { reader ->
//                    gson.fromJson(reader, ArenaWavesEngineData::class.java) ?: ArenaWavesEngineData.createDefault()
//                }.also {
//                    LogUtil.info("[ArenaWavesEngine] Data loaded from ${dataFile.absolutePath}")
//                }
//            } else {
//                LogUtil.info("[ArenaWavesEngine] Data file not found, creating default data")
//                ArenaWavesEngineData.createDefault().also { saveData(it) }
//            }
//        } catch (e: Exception) {
//            LogUtil.severe("[ArenaWavesEngine] Failed to load data: ${e.message}, using defaults")
//            ArenaWavesEngineData.createDefault()
//        }
//    }
//
//    fun saveData(data: ArenaWavesEngineData) {
//        val dataFile = getDataFile()
//        try {
//            FileWriter(dataFile).use { writer ->
//                gson.toJson(data, writer)
//            }
//            LogUtil.info("[ArenaWavesEngine] Data saved to ${dataFile.absolutePath}")
//        } catch (e: Exception) {
//            LogUtil.severe("[ArenaWavesEngine] Failed to save data: ${e.message}")
//        }
//    }
//
//    fun updateArenaMapDefinitions(arenaMapDefinitions: List<ArenaMapDefinition>) {
//        val currentData = loadData()
//        val updatedData = currentData.copy(arenaMapDefinitions = arenaMapDefinitions)
//        saveData(updatedData)
//    }
//
//    fun getArenaMapDefinition(mapId: String): ArenaMapDefinition? {
//        return loadData().arenaMapDefinitions.find { it.id == mapId }
//    }
//
//    fun updateArenaSessions(arenaSessions: List<ArenaSession>) {
//        val currentData = loadData()
//        val updatedData = currentData.copy(arenaSessions = arenaSessions)
//        saveData(updatedData)
//    }
//
//    fun addArenaSession(session: ArenaSession) {
//        val currentData = loadData()
//        val updatedSessions = currentData.arenaSessions + session
//        updateArenaSessions(updatedSessions)
//    }
//
//    fun updateArenaSession(updatedSession: ArenaSession) {
//        val currentData = loadData()
//        val updatedSessions = currentData.arenaSessions.map { existing ->
//            if (existing.id == updatedSession.id) updatedSession else existing
//        }
//        updateArenaSessions(updatedSessions)
//    }
//
//    fun removeArenaSession(sessionId: ArenaSessionId) {
//        val currentData = loadData()
//        val updatedSessions = currentData.arenaSessions.filter { it.id != sessionId }
//        updateArenaSessions(updatedSessions)
//    }
//
//    fun getArenaSession(sessionId: ArenaSessionId): ArenaSession? {
//        return loadData().arenaSessions.find { it.id == sessionId }
//    }
//
//    fun getAllArenaSessions(): List<ArenaSession> {
//        return loadData().arenaSessions
//    }
//
//    fun getArenaMapDefinitions(): List<ArenaMapDefinition> {
//        return loadData().arenaMapDefinitions
//    }
//}
