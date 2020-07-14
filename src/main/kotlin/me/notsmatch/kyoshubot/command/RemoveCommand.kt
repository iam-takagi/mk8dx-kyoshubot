package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.lang.StringBuilder

class RemoveCommand(val boshuService: BoshuService,  val settingsService: GuildSettingsService) : Command(){

    init {
        this.name = "remove"
        this.help = "項目を削除します"
        this.arguments = "<hour>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)
            if(settings.banned)return reply("This server has been banned.")
            if (settings.getCommandOption("remove") == null || !settings.getCommandOption("remove")!!.visibility) {
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

            var hour: String = ""
            if (args.size >= 1) {
                hour = args[0]
            }

            if(!NumberUtils.isInteger(hour) || hour.toInt() > 24 || hour.toInt() < 0){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("hourは0~24で指定する必要があります。")
                }.build())
            }

            val koumoku = boshu.getKoumokuByHour(hour.toInt()) ?:
            return replyInDm(EmbedBuilder().apply {
                setColor(Color.RED)
                setAuthor(
                    "Error",
                    null,
                    null
                )
                setDescription("${hour}時の項目は存在しません")
            }.build())

            if(boshu.koumokuList.remove(koumoku)){

                boshu.save()

                boshu.updateMessage(guild, settings, false)
            }
        }
    }
}