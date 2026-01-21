package com.miilhozinho.arenawavesengine.repositories.base

import com.miilhozinho.arenawavesengine.config.ArenaSession

interface IRepository<T> {
    fun get(): T
    fun loadConfig(): T
    fun save()
}