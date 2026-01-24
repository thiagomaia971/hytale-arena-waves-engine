package com.miilhozinho.arenawavesengine.service

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.Universe
import com.miilhozinho.arenawavesengine.util.LogUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerMessageManager {
    companion object {
        private val playersRef: ConcurrentHashMap<String, Player> = ConcurrentHashMap()

        fun sendMessage(playerId: String, message: Message, logType: LogType = LogType.DEBUG) {
            var player = playersRef[playerId]
            if (player == null) {
                val playerRef = Universe.get().getPlayer(UUID.fromString(playerId))
                if (playerRef == null || playerRef.reference == null) {
                    LogUtil.warn("Could not find player with id $playerId")
                    return
                }

                player = playerRef.reference!!.store.getComponent(playerRef.reference!!, Player.getComponentType())
                if (player == null) {
                    LogUtil.warn("Could not find player with id $playerId")
                    return
                }
                playersRef[playerId] = player
            }

            when(logType) {
                LogType.DEBUG -> LogUtil.debug(message.toString())
                LogType.INFO -> LogUtil.info(message.toString())
                LogType.WARN -> LogUtil.warn(message.toString())
            }
            player.sendMessage(message)
        }
    }
}

enum class LogType {
    DEBUG,
    INFO,
    WARN,
}