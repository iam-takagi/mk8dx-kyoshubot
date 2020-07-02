package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.service.GuildSettingsService
import me.notsmatch.kyoshubot.util.DiscordUtils
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.lang.StringBuilder

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

            boshu.messageId = channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.CYAN)
                        setAuthor(
                            "募集が進行中です",
                            null,
                            null
                        )
                        val builder =
                            StringBuilder("${settings.getMentionString(guild)}\nタイトル: " + boshu.title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                        builder.append("==========================\n")
                        val it = boshu.koumokuList.iterator()
                        while (it.hasNext()) {
                            val k = it.next()
                            val b = StringBuilder("・${k.hour}時 ${k.kyoshuSizeText()} ${k.title}")
                            if (k.kyoshuUsers.size >= 1) {
                                b.append("\n")
                                k.kyoshuUsers.forEach { user ->
                                    val member = guild.getMemberById(user.id)
                                    if (member != null) {
                                        b.append(DiscordUtils.getName(member))
                                        if(user.temporary){
                                            b.append("(仮)")
                                        }
                                        b.append(" ")
                                    }
                                }
                            }
                            builder.append(b.toString())
                            if (it.hasNext()) {
                                builder.append("\n")
                            }
                        }
                        setDescription(builder.toString())
                    }.build()
            ).complete().idLong

            boshu.save()
        }
    }
}