package me.gei.tiatrealms.internal.managers

import me.gei.tiatareas.api.DiscoverAPI
import me.gei.tiatareas.api.events.AreaDiscoverEvent
import me.gei.tiatareas.api.events.AreaEnterEvent
import me.gei.tiatareas.api.events.AreaLeaveEvent
import me.gei.tiatrealms.internal.config.TrConfig
import me.gei.tiatrealms.api.events.VanishStatusSetEvent
import me.gei.tiatrealms.api.events.UpdateVanishStatusEvent
import me.gei.tiatrealms.internal.pojo.enums.VanishMode.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.onlinePlayers
import java.util.concurrent.ConcurrentHashMap

object VanishManager {

    /** 例外玩家列表，在此列表的玩家将不受隐藏策略约束 玩家 -> (目标 -> 可见状态) **/
    private val independentPlayers: ConcurrentHashMap<Player, ConcurrentHashMap<Player, Boolean>> = ConcurrentHashMap()

    private var statusUpdatingTasks: ConcurrentHashMap<Player, PlatformExecutor.PlatformTask> = ConcurrentHashMap()

    /** 加入例外玩家并更新其状态 **/
    fun Player.addIndependentTarget( target: Player, status: Boolean) {
        if(!independentPlayers[this]!!.containsKey(target)) {
            independentPlayers[this]!![target] = status
            this.asyncUpdatePlayersVanishStatus()
        }
    }

    /** 移除例外玩家并更新其状态 **/
    fun Player.removeIndependentTarget( target: Player) {
        if(independentPlayers[this]!!.containsKey(target)) {
            independentPlayers[this]!!.remove(target)
            this.asyncUpdatePlayersVanishStatus()
        }
    }

    fun Player.hasIndependentTarget(target: Player): Boolean {
        return independentPlayers[this]!!.containsKey(target)
    }

    /** 异步更新玩家状态
     * 并不是特别好的实现（史）
     *
     * 规则：
     * 玩家在Config区域内时
     *  黑名单：
     *      遍历所有玩家，对于区域内所有非玩家的目标，玩家目标互相执行隐藏操作
     *      对于不在区域内的目标，玩家可见目标，目标不可见玩家
     *  白名单：
     *      与黑名单相反
     *  玩家不在Config区域内时
     *  A. 如果Config内不存在世界节点，特判mode为null时情况，玩家目标相互可见
     *  B. 如果Config内存在世界节点
     *   黑名单：
     *      （1）如果世界节点下的区域节点为空（全局模式），则取世界所有目标，
     *          玩家目标互相可见
     *      （2）如果世界存在区域节点，则遍历世界所有目标，玩家目标相互可见
     *   白名单:
     *      与黑名单相反
     *
     * !!!: 处于例外玩家列表的玩家，他们的目标映射单独处理，不受以上规则约束
     * !!!: 如需在此操作进行前后进行处理，使用UpdateVanishStateEvent.Pre/Post
     *
     **/

    fun Player.asyncUpdatePlayersVanishStatus() {
        //call pre event
        val preUpdateEvent = UpdateVanishStatusEvent.Pre(this)
        preUpdateEvent.call()
        if(preUpdateEvent.isCancelled) return

        val player = this
        submitAsync {
            //托管任务
            statusUpdatingTasks[player] = this

            /** 处理例外成员 **/
            var independentTargets: Set<Player> = HashSet()
            if(!independentPlayers[player].isNullOrEmpty()) {
                //映射 目标 -> 状态
                val independentTargetsMap = independentPlayers[player]!!
                //例外玩家列表
                independentTargets = independentPlayers[player]!!.map { it.key }.toSet()

                independentTargetsMap.entries.forEach {
                    player.setTargetVisibility(it.key, it.value)
                }
            }

            /** 默认处理 **/
            //如果在任意区域内
            if(player.isStandingInAnyTrArea()) {
                val mode = TrConfig.vanishedAreas[player.world]!!.mode

                /** 排除例外成员 **/
                val areaTargets = player.getStandingAreaTargets().subtract(independentTargets)
                val otherTargets = onlinePlayers
                    .subtract(areaTargets)
                    .toMutableSet()
                    .also { it.remove(player) }
                    .subtract(independentTargets)

                when(mode) {
                    //黑名单
                    BLACKLIST -> {
                        areaTargets
                            .forEach {
                                player.setTargetVisibility(it, false)
                                it.setTargetVisibility(player, false)
                            }
                        otherTargets
                            .forEach {
                                player.setTargetVisibility(it, true)
                                it.setTargetVisibility(player, false)
                            }
                    }

                    WHITELIST -> {
                        areaTargets
                            .forEach {
                                player.setTargetVisibility(it, true)
                                it.setTargetVisibility(player, true)
                            }
                        otherTargets
                            .forEach {
                                player.setTargetVisibility(it, false)
                                it.setTargetVisibility(player, true)
                            }
                    }
                }
            }
            //如果不在
            else {
                val mode = TrConfig.vanishedAreas[player.world]?.mode

                /** 排除例外成员 **/
                val worldTargetsAll = player.world.players
                    .also { it.remove(player) }
                    .toSet()
                    .subtract(independentTargets)

                //特判mode是否为null
                if(mode == null) {
                    worldTargetsAll.forEach {
                        player.setTargetVisibility(it, true)
                        it.setTargetVisibility(player, true)
                    }
                }
                else {
                    when(mode) {
                        //黑名单
                        BLACKLIST -> {
                            worldTargetsAll.forEach {
                                player.setTargetVisibility(it, true)
                                it.setTargetVisibility(player, true)
                            }
                        }
                        WHITELIST -> {
                            worldTargetsAll.forEach {
                                player.setTargetVisibility(it, false)
                                it.setTargetVisibility(player, false)
                            }
                        }
                    }
                }
            }

            //结束托管
            statusUpdatingTasks.remove(player)
        }

        //call post event
        UpdateVanishStatusEvent.Post(this).call()
    }

    /** 设置玩家状态 **/
    private fun Player.setTargetVisibility(target: Player, isVisible: Boolean) {
        submit {
            //call pre event
            val preSetEvent = VanishStatusSetEvent.Pre(this@setTargetVisibility, target, isVisible)
            preSetEvent.call()
            if(preSetEvent.isCancelled) return@submit

            if(isVisible)
            {
                this@setTargetVisibility.showPlayer(BukkitPlugin.getInstance(), target)
            }

            else {
                this@setTargetVisibility.hidePlayer(BukkitPlugin.getInstance(), target)
            }
            //call post event
            VanishStatusSetEvent.Post(this@setTargetVisibility, target, isVisible).call()
        }
    }

    /** 玩家所站位置是否为任何配置区域列表内的区域 **/
    private fun Player.isStandingInAnyTrArea(): Boolean {
        DiscoverAPI.getAreas(this.location)
            .forEach { standingArea ->
                TrConfig.vanishedAreas[this.world]?.areas?.forEach { vanishArea ->
                    if(standingArea.key == vanishArea)
                        return true
                }
            }

        return false
    }

    /** 获取所站区域的目标 **/
    private fun Player.getStandingAreaTargets(): Set<Player> {
        val players: HashSet<Player> = HashSet()
        DiscoverAPI.getAreas(this.location).forEach { area ->
            players.addAll(area.getPlayersInArea())
        }

        players.remove(this)

        return players
    }

    /**
     * 注册玩家
     */
    @SubscribeEvent(EventPriority.HIGHEST)
    private fun onPlayerLogin(event: PlayerLoginEvent) {
        independentPlayers.computeIfAbsent(event.player) { ConcurrentHashMap() }
        event.player.asyncUpdatePlayersVanishStatus()
    }

    /**
     * 注销玩家
     */
    @SubscribeEvent
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        independentPlayers.remove(event.player)
        //注销更新任务
        statusUpdatingTasks[event.player]?.cancel()
        statusUpdatingTasks.remove(event.player)
    }

    /**
     * 其他事件监听
     *
     * 监听等级：
     * 切换世界 > 离开区域 > 进入/发现区域
     */
    @SubscribeEvent(EventPriority.HIGH)
    private fun onPlayerWorldChange(event: PlayerChangedWorldEvent) {
        event.player.asyncUpdatePlayersVanishStatus()
    }

    @SubscribeEvent
    private fun onAreaLeave(event: AreaLeaveEvent) {
        event.player.asyncUpdatePlayersVanishStatus()
    }

    @SubscribeEvent(EventPriority.LOWEST)
    private fun onAreaEnter(event: AreaEnterEvent) {
        event.player.asyncUpdatePlayersVanishStatus()
    }

    @SubscribeEvent(EventPriority.LOWEST)
    private fun onAreaDiscover(event: AreaDiscoverEvent) {
        event.player.asyncUpdatePlayersVanishStatus()
    }
}