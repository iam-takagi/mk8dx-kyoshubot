package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.lang.StringBuilder

class StartCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command(){

    init {
        this.name = "start"
        this.help = "募集を開始します"
        this.arguments = "<title>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("start") == null || !settings.getCommandOption("start")!!.visibility) {
                event.message.delete().complete()
            }

            if (boshuService.getBoshu(guild.idLong, channel.idLong) != null)return replyInDm(EmbedBuilder().apply {
                setColor(Color.RED)
                setAuthor(
                    "Error",
                    null,
                    null
                )
                setDescription("このチャンネルでは既に募集が進行中です")
            }.build())

            val args = StringUtils.split(args)

            if(args.isEmpty()){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("タイトルを入力してください")
                }.build())
            }

            val b = StringBuilder()

            val it = args.iterator()
            while (it.hasNext()) {
                val next = it.next()
                b.append(next)

                if (it.hasNext()) b.append(" ")
            }

            if(b.toString().length > 30){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("タイトルは30文字以下に設定してください")
                }.build())
            }

            if (boshuService.addBoshu(guild.idLong, channel.idLong, b.toString())) {
                val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)!!

                val settings = settingsService.getGuildSettings(guild.idLong)

                boshu.messageId = channel.sendMessage(boshu.toEmbed(guild, settings, false)).complete().idLong

                boshu.save()
            }
        }
    }
}