package me.gei.tiatrealms.internal.managers

import com.github.ginirohikocha.sns.api.ChaSNSAPI
import me.gei.tiatrealms.api.events.VanishStatusSetEvent
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.registerBukkitListener

object PluginCompactManager {

    @Awake(LifeCycle.ENABLE)
    private fun registerCompacts() {
        if(Bukkit.getPluginManager().isPluginEnabled("ChaSNS"))
           registerChaSNSCompact()
    }

    /**
     * 猹の社交兼容
     *
     */
    private fun registerChaSNSCompact() {
        registerBukkitListener(VanishStatusSetEvent.Pre::class.java) {
            if(ChaSNSAPI.listFriends(it.player).contains(ChaSNSAPI.getUser(it.target)))
                it.isCancelled = true
        }
        info("&aChaSNS Hook成功!")
    }
}