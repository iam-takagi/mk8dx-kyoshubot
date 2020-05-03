package me.notsmatch.kyoshubot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.utils.Koumoku
import me.notsmatch.kyoshubot.utils.Manager
import me.notsmatch.kyoshubot.utils.NumberUtils
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

class AddCommand : Command(){

    init {
        this.name = "add"
        this.help = "項目を追加します"
        this.arguments = "<hour> <need> <title> "
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

            val args = args.split(" ")
            var title: String = ""
            var hour: String = ""
            var need: String = ""

            if(args.size >= 3) {
                hour = args[0]
                need = args[1]
                title = args[2]
            } else {
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: !add <title> <hour> <need>")
                }.build())
            }


            if(boshu.getKoumokuByHour(hour.toInt()) != null) {
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: ${hour}時は既に項目が存在します")
                }.build())
            }

            if(title.length > 30){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: titleは30文字以下にする必要があります。")
                }.build())
            }
            else if(!NumberUtils.isInteger(hour) || hour.toInt() > 24 || hour.toInt() < 0){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: hourは0~24で指定する必要があります。")
                }.build())
            }
            else if(!NumberUtils.isInteger(need) || need.toInt() > 12 || need.toInt() < 0){
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: needは0~12で指定する必要があります。")
                }.build())
            }


            if(Manager.getBoshu(guild.idLong, channel.idLong)!!.koumokuList.add(Koumoku(title, hour.toInt(), need.toInt(), mutableListOf()))){
                replyInDm(
                    EmbedBuilder().apply {
                        setColor(Color.CYAN)
                        setAuthor(
                            "挙手項目を追加しました",
                            null,
                            null
                        )
                        addField("時間", hour + "時", true)
                        addField("募集人数", need + "人", true)
                        addField("タイトル", title, true)
                    }.build()
                )

                textChannel.editMessageById(boshu.messageId,  EmbedBuilder().apply {
                    setColor(Color.CYAN)
                    setAuthor(
                        "募集が進行中です",
                        null,
                        null
                    )
                    val builder = StringBuilder("日時: " + SimpleDateFormat("yyyy/MM/dd").format(Date(boshu.date)) + "\n" + "!add <hour> <need> <title> を使用して挙手項目を追加してください。\n")
                    builder.append("==========================\n")
                    val it = boshu.koumokuList.iterator()
                    while (it.hasNext()){
                        val k = it.next()
                        val b = StringBuilder("・${k.hour}時 @${k.need-k.kyoshuUsers.size} ${k.title}")
                        if(k.kyoshuUsers.size >= 1) {
                            b.append("\n")
                            k.kyoshuUsers.forEach { id ->
                                val member = guild.getMemberById(id)
                                if(member != null) {
                                    b.append(member.asMention)
                                }
                            }
                        }
                        builder.append(b.toString())
                        if(it.hasNext()){
                            builder.append("\n")
                        }
                    }
                    setDescription(builder.toString())
                }.build()).queue()
            }
        }
    }
}