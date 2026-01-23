package com.miilhozinho.arenawavesengine.repositories.base

import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.util.LogUtil

open class Repository<T>(val fileConfig: Config<T>) : IRepository<T> {

    private var markToSave: Boolean = false
    var currentConfig: T

    init {
        currentConfig = fileConfig.get() as T
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
            return true
        }
        return false
    }

    override fun markToSave() {
        markToSave = true
    }
}