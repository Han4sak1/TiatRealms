package me.gei.tiatrealms

import me.gei.tiatrealms.internal.config.TrConfig
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.chat.colored

object TiatRealms : Plugin() {

    override fun onEnable() {
        info("&a  _____ _       _     ____            _               ".colored())
        info("&a |_   _(_) __ _| |_  |  _ \\ ___  __ _| |_ __ ___  ___ ".colored())
        info("&a   | | | |/ _` | __| | |_) / _ \\/ _` | | '_ ` _ \\/ __|".colored())
        info("&a   | | | | (_| | |_  |  _ <  __/ (_| | | | | | | \\__ \\".colored())
        info("&a   |_| |_|\\__,_|\\__| |_| \\_\\___|\\__,_|_|_| |_| |_|___/".colored())
        info("&a                                                      ".colored())

        TrConfig.load()
    }
}