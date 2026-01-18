package com.miilhozinho.mobwavesengine

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.miilhozinho.mobwavesengine.util.LogUtil

class MobWavesEngine(init: JavaPluginInit) : JavaPlugin(init) {

    override fun setup() {
        super.setup()
        LogUtil.info("Setup")
    }

    override fun start() {
        LogUtil.info("Start")
    }

    override fun shutdown() {
        LogUtil.info("Shutdown")
    }

}
