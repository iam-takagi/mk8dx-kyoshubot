package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import java.awt.Color

class StartCommand(val boshuService: BoshuService, val settingsService: GuildSettingsService) : Command(){

    init {
        this.name = "start"
        this.help = "募集を開始します"
        this.arguments = "<title>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val settings = settingsService.getGuildSettings(guild.idLong)

            if (settings.getCommandOption("start") == null || !settings.getCommandOption("start")!!.visibility) {
                event.message.delete().complete()
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
                    setDescription("タイトルを入力してください")
                }.build())
            }

            val title = args[0]

            if(title.length > 30){
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
            if (boshuService.addBoshu(guild.idLong, channel.idLong, title)) {
                val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)!!

                val settings = settingsService.getGuildSettings(guild.idLong)

                boshu.messageId = channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.CYAN)
                        setAuthor(
                            "募集を開始しました",
                            null,
                            null
                        )
                        setDescription("${settings.getMentionString(guild)}\nタイトル: " + title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                    }.build()
                ).complete().idLong
                boshu.save()
            } else {
                replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription("このチャンネルでは既に募集が開始されています。 募集を終了するには !end を使用してください。")
                }.build())
            }
        }
    }
}