package com.miilhozinho.arenawavesengine.util

import com.hypixel.hytale.logger.HytaleLogger
import java.util.logging.Level

object LogUtil {
    private val logger: HytaleLogger = HytaleLogger.getLogger().getSubLogger("MobWavesEngine")
    fun info(message: String) {
        logger
            .at(Level.INFO)
            .log(message)
    }

    fun severe(message: String) {
        logger
            .at(Level.SEVERE)
            .log(message)
    }

    fun warn(message: String) {
        logger
            .at(Level.WARNING)
            .log(message)
    }

    fun debug(message: String) {
        logger
            .at(Level.FINE)
            .log(message)
    }
}
