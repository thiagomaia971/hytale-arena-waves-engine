package com.miilhozinho.arenawavesengine.repositories.base

import com.hypixel.hytale.server.core.util.Config
import com.miilhozinho.arenawavesengine.util.LogUtil

open class Repository<T>(val fileConfig: Config<T>) : IRepository<T> {
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

    override fun save() {
        fileConfig.apply {
            save()
            LogUtil.debug("[Repository] Config saved.")
        }
    }
}