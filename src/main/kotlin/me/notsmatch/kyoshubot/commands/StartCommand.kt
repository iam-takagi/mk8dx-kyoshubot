package me.notsmatch.kyoshubot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import me.notsmatch.kyoshubot.utils.Boshu
import me.notsmatch.kyoshubot.utils.Manager
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.lang.Exception
import java.text.SimpleDateFormat

class StartCommand() : Command(){

    init {
        this.name = "start"
        this.help = "募集を開始します"
        this.arguments = "<yyyy/MM/dd>"
    }

    override fun execute(event: CommandEvent?) {
        event?.apply {

            val args = args.split(" ")
            val date = args[0]
            try {
                SimpleDateFormat("yyyy/MM/dd").parse(date)
            } catch (e: Exception) {
                return replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: 日時の形式が正しくありません。")
                }.build())
            }
            if (Manager.addBoshu(guild.idLong, channel.idLong, SimpleDateFormat("yyyy/MM/dd").parse(date).time)) {
                Manager.getBoshu(guild.idLong, channel.idLong)!!.messageId = channel.sendMessage(
                    EmbedBuilder().apply {
                        setColor(Color.CYAN)
                        setAuthor(
                            "募集を開始しました",
                            null,
                            null
                        )
                        setDescription("日時: " + date + "\n" + "!add <hour> <need> <title> を使用して挙手項目を追加してください。")
                    }.build()
                ).complete().idLong
            } else {
                replyInDm(EmbedBuilder().apply {
                    setColor(Color.RED)
                    setAuthor(
                        "Error",
                        null,
                        null
                    )
                    setDescription(":x: このチャンネルでは既に募集が進行しています。 募集を終了するには !end を使用してください。")
                }.build())
            }
        }
    }
}