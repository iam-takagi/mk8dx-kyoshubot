package me.notsmatch.kyoshubot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.utils.Manager
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class EndCommand : Command(){

    init {
        this.name = "end"
        this.help = "募集を終了します"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val boshu = Manager.getBoshu(guild.idLong, channel.idLong)
                ?: return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: このチャンネルでは募集が開始されていません。")
                }.build())

            if(Manager.boshuList.remove(boshu)){
                textChannel.editMessageById(boshu.messageId,  EmbedBuilder().apply {
                    setColor(Color.CYAN)
                    setAuthor(
                        "募集は締め切られました",
                        null,
                        null
                    )
                    val builder = StringBuilder("日時: " + SimpleDateFormat("yyyy/MM/dd").format(Date(boshu.date)) + "\n" + "!add <hour> <need> <title> を使用して挙手項目を追加してください。\n")
                    builder.append("==========================\n")
                    val it = boshu.koumokuList.iterator()
                    while (it.hasNext()) {
                        val koumoku = it.next()
                        val b =
                            StringBuilder("・${koumoku.hour}時 @${koumoku.need - koumoku.kyoshuUsers.size} ${koumoku.title}")
                        if (koumoku.kyoshuUsers.size >= 1) {
                            b.append("\n")
                            koumoku.kyoshuUsers.forEach { id ->
                                val member = guild.getMemberById(id)
                                if (member != null) {
                                    koumoku.kyoshuUsers.forEach { id -> b.append(member.asMention) }
                                }
                            }
                        }
                        builder.append(b.toString())
                        if (it.hasNext()) {
                            builder.append("\n")
                        }
                    }
                    setDescription(builder.toString())
                }.build()).queue()
            }
        }
    }
}