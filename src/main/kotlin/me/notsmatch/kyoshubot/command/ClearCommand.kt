package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class ClearCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService)  : Command() {

    init {
        this.name = "clear"
        this.help = "挙手ユーザーをクリアします"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {
            val settings = settingsService.getGuildSettings(guild.idLong)
            if (settings.banned) return reply("This server has been banned.")
            if (settings.getCommandOption("clear") == null || !settings.getCommandOption("clear")!!.visibility) {
                event.message.delete().complete()
            }

            val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)
                ?: return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("このチャンネルでは募集が開始されていません。")
                }.build())

            boshu.koumokuList.forEach{
                it.kyoshuUsers.clear()
            }

            boshu.save()

            boshu.updateMessage(guild, settings, false)
        }
    }
}