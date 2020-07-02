package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color

class NotifyCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "notify"
        this.help = "挙手しているユーザーにメンションをつけてメッセージを送信します"
        this.arguments = "<hour> <message>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("notify") == null || !settings.getCommandOption("notify")!!.visibility) {
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

            val args = StringUtils.split(args)

            if(args.size >= 2) {

                if(!NumberUtils.isInteger(args[0]) || args[0].toInt() > 36 || args[0].toInt() < 0){
                    return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("hourは0~36で指定する必要があります。")
                    }.build())
                }

                val hour = args[0].toInt()

                val koumoku = boshu.getKoumokuByHour(hour)
                    ?: return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("${hour}時の項目は存在しません")
                    }.build())

                if(koumoku.getKyoshuSize() <= 0){
                    return replyInDm(EmbedBuilder().apply {
                        setColor(Color.RED)
                        setAuthor(
                            "Error",
                            null,
                            null
                        )
                        setDescription("${hour}時に挙手しているユーザーがいません")
                    }.build())
                }

                if(settings.notifyChannelId == 0L) {
                    val channel = boshu.channelId.let { guild.getTextChannelById(it) } ?:return
                    channel.sendMessage("${hour}時: " + koumoku.getKyoshuUsersMention(guild) + "\n" + args[1]).queue()
                }else{
                    val channel = settings.notifyChannelId.let { guild.getTextChannelById(it) } ?:return
                    channel.sendMessage("${hour}時: " + koumoku.getKyoshuUsersMention(guild) + "\n" + args[1]).queue()
                }
            }
        }
    }
}