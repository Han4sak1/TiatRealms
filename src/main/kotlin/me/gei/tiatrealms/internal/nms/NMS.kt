package me.gei.tiatrealms.internal.nms

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.Packet
import taboolib.module.nms.nmsProxy

abstract class NMS {
    abstract fun isPlayerSoundPacket(namedSoundEffectPacket: Packet): Boolean

    abstract fun  isBlockSoundPacket(namedSoundEffectPacket: Packet): Boolean

    abstract fun getSoundLocation(world: World, namedSoundEffectPacket: Packet): Location

    abstract fun getBlockOrNull(world: World, blockActionPacket: Packet): Block?

    companion object {
        val Instance by unsafeLazy {
            nmsProxy<NMS>()
        }
    }
}