package com.miilhozinho.arenawavesengine.repositories.base

import com.google.gson.Gson
import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.lang.reflect.Type

open abstract class Repository<T>(
    private val type: Type,
    val fileConfig: Config<T>) : IRepository<T> {

    protected var markToSave: Boolean = false
    var currentConfig: T
    protected var oldState: T

    init {
        currentConfig = fileConfig.get() as T
        oldState = getAsNoTracking()
    }

    fun getAsNoTracking(): T {
        val value = fileConfig.get() as T
        val json = Gson().toJson(value)
        return Gson().fromJson(json, type)
    }

    override fun get(): T {
        return currentConfig
    }

    override fun loadConfig(): T {
        currentConfig = fileConfig.get() as T
        return currentConfig
    }

    override fun save(forceSave: Boolean): Boolean {
        if (markToSave || forceSave){
            markToSave = false
            fileConfig.apply {
                save()
                LogUtil.debug("[Repository] Config saved.")
            }
            oldState = getAsNoTracking()
            return true
        }
        return false
    }

    override fun markToSave() {
        markToSave = true
    }
}