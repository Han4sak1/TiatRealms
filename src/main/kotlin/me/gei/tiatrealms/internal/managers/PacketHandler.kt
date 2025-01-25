package me.gei.tiatrealms.internal.managers

import me.gei.tiatrealms.internal.nms.NMS
import org.bukkit.entity.Player
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.PacketSendEvent


/**
 * 数据包接管/篡改
 */
object PacketHandler {
    @SubscribeEvent
    private fun onInterceptionPacketSend(event: PacketSendEvent) {
        if (event.isCancelled) return
        when(event.packet.name) {
            /** 拦截被隐藏玩家的NamedSoundEffect数据包 **/
            "PacketPlayOutNamedSoundEffect" -> {
                if(NMS.Instance.isPlayerSoundPacket(event.packet)) {
                    //判断该包是否是被隐藏玩家发出的（暂无更好的实现）
                    event.player.getNearbyEntities(16.0, 16.0, 16.0).forEach { target ->
                        if(target !is Player) return
                        //检查是否有被隐藏玩家，和声音是否在可听见范围内
                        if(!event.player.canSee(target)) {
                            event.isCancelled = true
                        }
                    }
                }
                //其他方块音效，如打开箱子等
                else if(NMS.Instance.isBlockSoundPacket(event.packet)) {
                    val soundLocation = NMS.Instance.getSoundLocation(event.player.world, event.packet)
                    //判断周围5格(最大交互距离内)是否有被隐藏玩家
                    val nearByEntities = event.player.world.getNearbyEntities(soundLocation, 5.0, 5.0, 5.0)

                    nearByEntities.forEach {
                        if(it !is Player) return@forEach
                        if(!event.player.canSee(it)) {
                            event.isCancelled = true
                            return
                        }
                    }
                }
            }

            /** 被隐藏玩家静默开箱 **/
            "PacketPlayOutBlockAction" -> {
                if(NMS.Instance.getBlockOrNull(event.player.world, event.packet) == null) return

                val blockLocation = NMS.Instance.getBlockOrNull(event.player.world, event.packet)!!.location

                //判断箱子周围5格(最大交互距离内)是否有被隐藏玩家
                val nearByEntities = event.player.world.getNearbyEntities(blockLocation, 5.0, 5.0, 5.0)

                nearByEntities.forEach {
                    if(it !is Player) return@forEach
                    if(!event.player.canSee(it)) {
                        event.isCancelled = true
                        return
                    }
                }
            }
        }
    }
}