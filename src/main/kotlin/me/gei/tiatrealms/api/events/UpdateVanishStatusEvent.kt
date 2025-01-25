package me.gei.tiatrealms.api.events

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * 更新状态
 */
class UpdateVanishStatusEvent {
    class Pre(val player: Player): BukkitProxyEvent()

    class Post(val player: Player): BukkitProxyEvent()
}