package me.notsmatch.kyoshubot.command

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.service.BoshuService
import me.notsmatch.kyoshubot.util.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.lang.StringBuilder

class DropCommand(val boshuService: BoshuService) : Command(){

    init {
        this.name = "d"
        this.help = "時間を指定して挙手を下ろします"
        this.arguments = "<hour>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            event.message.delete().complete()

            val boshu = boshuService.getBoshu(guild.idLong, channel.idLong)
                ?: return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: このチャンネルでは募集が開始されていません。")
                }.build())

            val args = args.split(" ")

            var hour: String = ""
            if (args.size >= 1) {
                hour = args[0]
            }

            if(!NumberUtils.isInteger(hour) || hour.toInt() > 36 || hour.toInt() < 0){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: hourは0~36で指定する必要があります。")
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
                setDescription(":x: ${hour}時の項目は存在しません")
            }.build())

            if(koumoku.kyoshuUsers.contains(author.idLong)){
               if(koumoku.kyoshuUsers.remove(author.idLong)) {

                   boshu.save()

                   textChannel.editMessageById(boshu.messageId,  EmbedBuilder().apply {
                       setColor(Color.CYAN)
                       setAuthor(
                           "募集が進行中です",
                           null,
                           null
                       )
                       val builder = StringBuilder("@everyone\nタイトル: " + boshu.title + "\n" + ".add <hour> <need> <title> を使用して挙手項目を追加してください。")
                       builder.append("==========================\n")
                       val it = boshu.koumokuList.iterator()
                       while (it.hasNext()) {
                           val k = it.next()
                           val b =
                               StringBuilder("・${k.hour}時 @${k.need - k.kyoshuUsers.size} ${k.title}")
                           if (k.kyoshuUsers.size >= 1) {
                               b.append("\n")
                               k.kyoshuUsers.forEach { id ->
                                   val member = guild.getMemberById(id)
                                   if (member != null) {
                                       b.append(member.asMention)
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

            }else{
                replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: あなたは${hour}時に挙手していません")
                }.build())
            }
        }
    }
}