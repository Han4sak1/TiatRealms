package me.gei.tiatrealms.api.events

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class VanishStatusSetEvent {
    class Pre(val player: Player, val target: Player, val isVisible: Boolean): BukkitProxyEvent()

    class Post(val player: Player, val target: Player, val isVisible: Boolean): BukkitProxyEvent()
}