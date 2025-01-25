package me.gei.tiatrealms.internal.config

import me.gei.tiatrealms.internal.pojo.enums.VanishMode
import me.gei.tiatrealms.internal.pojo.VanishArea
import org.bukkit.Bukkit
import org.bukkit.World
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

object TrConfig {

    @Config("config.yml")
    lateinit var conf: ConfigFile
        private set

    var vanishedAreas: HashMap<World, VanishArea> = HashMap()
        private set

    fun load() {
        conf.getConfigurationSection("VanishAreas")!!.getKeys(false).forEach {
            val world = Bukkit.getWorld(it)
            val mode = VanishMode.valueOf(conf.getString("VanishAreas.$it.mode")!!)
            val areas = conf.getStringList("VanishAreas.$it.areas")

            vanishedAreas[world!!] = VanishArea(mode, areas)
        }

        info("&a${vanishedAreas.size} 个世界配置加载完成!".colored())
    }

    fun reload() {
        vanishedAreas.clear()
        load()
    }
}