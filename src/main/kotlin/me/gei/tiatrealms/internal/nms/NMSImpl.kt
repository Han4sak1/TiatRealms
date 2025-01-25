package me.gei.tiatrealms.internal.nms

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.Packet

typealias NMS12SoundCategory = net.minecraft.server.v1_12_R1.SoundCategory
typealias NMS20SoundCategory = net.minecraft.sounds.SoundCategory

typealias NMS12BlockPosition = net.minecraft.server.v1_12_R1.BlockPosition
typealias NMS20BlockPosition = net.minecraft.core.BlockPosition

class NMSImpl: NMS() {

    override fun isPlayerSoundPacket(namedSoundEffectPacket: Packet): Boolean {
        if(MinecraftVersion.isUniversal) {
            val soundCategory = namedSoundEffectPacket.read<NMS20SoundCategory>("source")!!

            return soundCategory == NMS20SoundCategory.PLAYERS
        }
        else {
            val soundCategory = namedSoundEffectPacket.read<NMS12SoundCategory>("b")!!

            return soundCategory == NMS12SoundCategory.PLAYERS
        }
    }

    override fun isBlockSoundPacket(namedSoundEffectPacket: Packet): Boolean {
        if(MinecraftVersion.isUniversal) {
            val soundCategory = namedSoundEffectPacket.read<NMS20SoundCategory>("source")!!

            return soundCategory == NMS20SoundCategory.BLOCKS
        }
        else {
            val soundCategory = namedSoundEffectPacket.read<NMS12SoundCategory>("b")!!

            return soundCategory == NMS12SoundCategory.BLOCKS
        }
    }

    override fun getSoundLocation(world: World, namedSoundEffectPacket: Packet): Location {
        if(MinecraftVersion.isUniversal) {
            val x = namedSoundEffectPacket.read<Int>("x")!!.toDouble() / 8
            val y = namedSoundEffectPacket.read<Int>("y")!!.toDouble() / 8
            val z = namedSoundEffectPacket.read<Int>("z")!!.toDouble() / 8

            return Location(world, x, y, z)
        }
        else {
            val x = namedSoundEffectPacket.read<Int>("c")!!.toDouble() / 8
            val y = namedSoundEffectPacket.read<Int>("d")!!.toDouble() / 8
            val z = namedSoundEffectPacket.read<Int>("e")!!.toDouble() / 8

            return Location(world, x, y, z)
        }
    }

    private val chestTypes = listOf(
        XMaterial.CHEST,
        XMaterial.ENDER_CHEST,
        XMaterial.TRAPPED_CHEST,
        XMaterial.SHULKER_BOX,
        XMaterial.BLUE_SHULKER_BOX,
        XMaterial.RED_SHULKER_BOX,
        XMaterial.CYAN_SHULKER_BOX,
        XMaterial.GRAY_SHULKER_BOX,
        XMaterial.BLACK_SHULKER_BOX,
        XMaterial.BROWN_SHULKER_BOX,
        XMaterial.LIGHT_BLUE_SHULKER_BOX,
        XMaterial.GREEN_SHULKER_BOX,
        XMaterial.LIME_SHULKER_BOX,
        XMaterial.LIGHT_GRAY_SHULKER_BOX,
        XMaterial.MAGENTA_SHULKER_BOX,
        XMaterial.ORANGE_SHULKER_BOX,
        XMaterial.PINK_SHULKER_BOX,
        XMaterial.PURPLE_SHULKER_BOX,
        XMaterial.WHITE_SHULKER_BOX,
        XMaterial.YELLOW_SHULKER_BOX
    )

    override fun getBlockOrNull(world: World, blockActionPacket: Packet): Block? {
        if(MinecraftVersion.isUniversal) {
            val blockPosition = blockActionPacket.read<NMS20BlockPosition>("pos")!!
            val bukkitLocation = Location(world, blockPosition.x.toDouble(), blockPosition.y.toDouble(), blockPosition.z.toDouble())

            //判断是否是箱子
            if(chestTypes.contains(XMaterial.matchXMaterial(bukkitLocation.block.type)))
                return bukkitLocation.block

            return null
        }
        else {
            val blockPosition = blockActionPacket.read<NMS12BlockPosition>("a")!!
            val bukkitLocation = Location(world, blockPosition.x.toDouble(), blockPosition.y.toDouble(), blockPosition.z.toDouble())

            //判断是否是箱子
            if(chestTypes.contains(XMaterial.matchXMaterial(bukkitLocation.block.type)))
                return bukkitLocation.block

            return null
        }
    }
}