package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class ResendCommand (val boshuService: BoshuService,  val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "resend"
        this.help = "募集メッセージを再送信します"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("resend") == null || !settings.getCommandOption("resend")!!.visibility) {
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

            channel.deleteMessageById(boshu.messageId).queue()

            boshu.messageId = channel.sendMessage(
                    boshu.toEmbed(guild, settings, false)
            ).complete().idLong

            boshu.save()
        }
    }
}