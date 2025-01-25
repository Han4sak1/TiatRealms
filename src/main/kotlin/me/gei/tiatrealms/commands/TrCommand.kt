package me.gei.tiatrealms.commands

import me.gei.tiatrealms.internal.config.TrConfig
import me.gei.tiatrealms.internal.managers.VanishManager.addIndependentTarget
import me.gei.tiatrealms.internal.managers.VanishManager.removeIndependentTarget
import me.gei.tiatrealms.internal.managers.VanishManager.asyncUpdatePlayersVanishStatus
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@CommandHeader("realms", ["tr"])
object TrCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val setVisibility = subCommand {
        player("player") {
            suggestPlayers()
            dynamic("status") {
                suggest { listOf("hide", "show", "default") }
                execute<CommandSender> { sender, context, _ ->
                    if (sender.isOp) {
                        val player = sender as Player
                        val target = context.player("player").castSafely<Player>()
                        val status = context["status"]

                        if (target == sender || target == null || listOf("hide", "show", "default").none { it == status })
                            return@execute

                        when(status) {
                            "hide" -> {
                                player.addIndependentTarget(target, false)
                            }
                            "show" -> {
                                player.addIndependentTarget(target, true)
                            }
                            "default" -> player.removeIndependentTarget(target)
                        }
                        player.asyncUpdatePlayersVanishStatus()
                        sender.sendMessage("&7[&a!&7] &a完成".colored())
                    }
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            TrConfig.reload()

            sender.sendMessage("&7[&a!&7] &a重载成功!".colored())
        }
    }
}