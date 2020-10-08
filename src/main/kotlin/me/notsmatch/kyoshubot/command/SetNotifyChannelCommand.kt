package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import org.apache.commons.lang3.StringUtils
import java.awt.Color

class SetNotifyChannelCommand (val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command() {

    init {
        this.name = "setnotifychannel"
        this.help = "通知先チャンネルを設定します"
        this.arguments = "<channel_id>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("setnotifychannel") == null || !settings.getCommandOption("setnotifychannel")!!.visibility) {
                event.message.delete().complete()
            }

            val role = guild.getRolesByName("Kyoshu Admin", true).first()
            if (!member.hasPermission(Permission.ADMINISTRATOR) && role == null || !member.hasPermission(Permission.ADMINISTRATOR) &&  !member.roles.contains(role)) {
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("管理者権限　または　権限ロール(Kyoshu Admin)が必要です。")
                }.build())
            }

            val args = StringUtils.split(args)
            if(args.isEmpty()){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("``.setnotifychannel <channel_id>``")
                }.build())
            }

            if(!NumberUtils.isLong(args[0])){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("チャンネルIDの形式が違います")
                }.build())
            }

            val channelId = args[0].toLong()

            val channel = guild.getTextChannelById(channelId)?:return replyInDm(EmbedBuilder().apply {
                setColor(Color.RED)
                setAuthor(
                    "Error",
                    null,
                    null
                )
                setDescription("チャンネルが見当たりませんでした")
            }.build())

            settings.notifyChannelId = channelId
            settings.save()

            replyInDm(EmbedBuilder().apply {
                setColor(Color.RED)
                setAuthor(
                    "Error",
                    null,
                    null
                )
                setDescription("通知チャンネルを設定しました")
            }.build())
        }
    }
}