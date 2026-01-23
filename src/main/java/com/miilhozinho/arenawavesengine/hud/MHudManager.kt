//package com.miilhozinho.arenawavesengine.hud
//
//import com.buuz135.mhud.MultipleCustomUIHud
//import com.hypixel.hytale.server.core.entity.entities.Player
//import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud
//import com.hypixel.hytale.server.core.universe.PlayerRef
//import com.miilhozinho.arenawavesengine.util.LogUtil
//import java.lang.reflect.Method
//import javax.annotation.Nonnull
//
//
//class MHudManager {
//    companion object {
//        var mhudChecked: Boolean = false
//        var mhudInstance: Any? = null
//        var mhudSetCustomHudMethod: Method? = null
//        var mhudHideCustomHudMethod: Method? = null
//
//        fun setCustomHud(
//            @Nonnull player: Player,
//            @Nonnull playerRef: PlayerRef,
//            @Nonnull hudIdentifier: String,
//            @Nonnull customHud: CustomUIHud
//        ) {
//            checkMhudPlugin()
//            if (mhudInstance != null && mhudSetCustomHudMethod != null) {
//                try {
//                    mhudSetCustomHudMethod!!.invoke(
//                        mhudInstance,
//                        player,
//                        playerRef,
//                        hudIdentifier,
//                        customHud
//                    )
//                    return
//                } catch (e: java.lang.Exception) {
//                    LogUtil.warn("Error using MHUD plugin API for " + hudIdentifier + ": " + e.message)
//                }
//            } else {
//                LogUtil.warn("MHUD plugin not available - cannot set HUD: " + hudIdentifier)
//            }
//        }
//
//        fun hideCustomHud(@Nonnull player: Player, @Nonnull playerRef: PlayerRef, @Nonnull hudIdentifier: String) {
//            checkMhudPlugin()
//            if (mhudInstance != null && mhudHideCustomHudMethod != null) {
//                try {
//                    val currentCustomHud = player.getHudManager().getCustomHud()
//                    if (currentCustomHud is MultipleCustomUIHud) {
//                        currentCustomHud.getCustomHuds().remove(hudIdentifier)
//                        player.getHudManager().setCustomHud(playerRef, currentCustomHud)
//                        currentCustomHud.show()
//                    }
//
////                    mhudHideCustomHudMethod!!.invoke(
////                        mhudInstance,
////                        player,
////                        playerRef,
////                        hudIdentifier
////                    )
//                    return
//                } catch (e: java.lang.Exception) {
//                    LogUtil.warn("Error using MHUD plugin hideCustomHud API: " + e.message)
//                }
//            } else {
//                LogUtil.warn("MHUD plugin not available - cannot hide HUD: " + hudIdentifier)
//            }
//        }
//
//        fun getCustomHud(@Nonnull player: Player, @Nonnull hudIdentifier: String): CustomUIHud? {
//            checkMhudPlugin()
//            if (mhudInstance == null) {
//                return null
//            } else {
//                try {
//                    val currentCustomHud = player.getHudManager().getCustomHud()
//                    if (currentCustomHud == null) {
//                        return null
//                    }
//
//                    val hudClass: Class<*> = currentCustomHud.javaClass
//                    val className = hudClass.getName()
//                    if (className == "com.buuz135.mhud.MultipleCustomUIHud") {
//                        val getCustomHudsMethod = hudClass.getMethod("getCustomHuds")
//                        val huds: HashMap<String?, CustomUIHud?> =
//                            getCustomHudsMethod.invoke(currentCustomHud) as HashMap<String?, CustomUIHud?>
//                        return huds.get(hudIdentifier)
//                    }
//                } catch (e: java.lang.Exception) {
//                    LogUtil.warn("Error getting HUD from MHUD wrapper: " + e.message)
//                }
//
//                return null
//            }
//        }
//
//        private fun checkMhudPlugin() {
//            if (!mhudChecked) {
//                mhudChecked = true
//
//                try {
//                    val mhudClass = Class.forName("com.buuz135.mhud.MultipleHUD")
//                    val getInstanceMethod = mhudClass.getMethod("getInstance")
//                    val instance = getInstanceMethod.invoke(null as Any?)
//                    if (instance != null) {
//                        mhudInstance = instance
//
//                        try {
//                            mhudSetCustomHudMethod = mhudClass.getMethod(
//                                "setCustomHud",
//                                Player::class.java,
//                                PlayerRef::class.java,
//                                String::class.java,
//                                CustomUIHud::class.java
//                            )
//                        } catch (var5: NoSuchMethodException) {
//                            LogUtil.warn("MHUD plugin found but setCustomHud method not accessible")
//                        }
//
//                        try {
//                            mhudHideCustomHudMethod = mhudClass.getMethod(
//                                "hideCustomHud",
//                                Player::class.java,
//                                PlayerRef::class.java,
//                                String::class.java
//                            )
//                        } catch (var4: NoSuchMethodException) {
//                        }
//
//                        LogUtil.info("MHUD plugin detected - using its API for HUD management")
//                    } else {
//                        LogUtil.warn("MHUD class found but getInstance() returned null")
//                    }
//                } catch (var6: ClassNotFoundException) {
//                    LogUtil.info("MHUD plugin not found - HUD management disabled")
//                } catch (e: Exception) {
//                    LogUtil.warn("Error checking for MHUD plugin: " + e.message)
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//
//}